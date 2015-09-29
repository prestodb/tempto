/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teradata.tempto.internal.hadoop.hdfs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HostAndPort;
import com.teradata.tempto.hadoop.hdfs.HdfsClient;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.HostAndPort.fromParts;
import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;
import static org.apache.commons.io.IOUtils.copyLarge;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_TEMPORARY_REDIRECT;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * HDFS client based on WebHDFS REST API.
 */
public class WebHDFSClient
        implements HdfsClient
{

    private static final Logger logger = getLogger(WebHDFSClient.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {};

    private static final int NUMBER_OF_RETRIES = 3;

    private final HostAndPort nameNode;

    private final CloseableHttpClient httpClient;

    @Inject
    public WebHDFSClient(
            @Named("hdfs.webhdfs.host") String webHdfsNameNodeHost,
            @Named("hdfs.webhdfs.port") int webHdfsNameNodePort)
    {
        this.nameNode = fromParts(checkNotNull(webHdfsNameNodeHost), webHdfsNameNodePort);
        checkArgument(webHdfsNameNodePort > 0, "Invalid name node WebHDFS port number: %s", webHdfsNameNodePort);

        this.httpClient = HttpClientBuilder.create().setRetryHandler(new DefaultHttpRequestRetryHandler(NUMBER_OF_RETRIES, true)).build();

        checkNameNodeAccessibility();
    }

    private void checkNameNodeAccessibility()
    {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet httpGet = httpGetFileStatus("/", "root");
            httpGet.setConfig(RequestConfig.custom().setConnectTimeout(1).build());
            client.execute(httpGet);
        }
        catch (IOException e) {
            throw new RuntimeException("Namenode is not accessible", e);
        }
    }

    @Override
    public void createDirectory(String path, String username)
    {
        // TODO: reconsider permission=777
        HttpPut mkdirRequest = new HttpPut(buildUri(path, username, "MKDIRS", Pair.of("permission", "777")));
        try (CloseableHttpResponse response = httpClient.execute(mkdirRequest)) {
            if (response.getStatusLine().getStatusCode() != SC_OK) {
                throw invalidStatusException("MKDIRS", path, username, mkdirRequest, response);
            }
            logger.debug("Created directory {} - username: {}", path, username);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not create directory " + path + " in hdfs, user: " + username, e);
        }
    }

    @Override
    public void delete(String path, String username)
    {
        Pair[] params = {Pair.of("recursive", "true")};
        HttpDelete removeFileOrDirectoryRequest = new HttpDelete(buildUri(path, username, "DELETE", params));
        try (CloseableHttpResponse response = httpClient.execute(removeFileOrDirectoryRequest)) {
            if (response.getStatusLine().getStatusCode() != SC_OK) {
                throw invalidStatusException("DELETE", path, username, removeFileOrDirectoryRequest, response);
            }
            logger.debug("Removed file or directory {} - username: {}", path, username);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not remove file or directory " + path + " in hdfs, user: " + username, e);
        }
    }

    @Override
    public void saveFile(String path, String username, InputStream input)
    {
        try {
            saveFile(path, username, new BufferedHttpEntity(new InputStreamEntity(input)));
        }
        catch (IOException e) {
            throw new RuntimeException("Could not create buffered http entity", e);
        }
    }

    @Override
    public void saveFile(String path, String username, RepeatableContentProducer repeatableContentProducer)
    {
        saveFile(path, username, new EntityTemplate(toApacheContentProducer(repeatableContentProducer)));
    }

    private ContentProducer toApacheContentProducer(RepeatableContentProducer repeatableContentProducer)
    {
        return (OutputStream outputStream) -> {
            try (InputStream inputStream = repeatableContentProducer.getInputStream()) {
                copyLarge(inputStream, outputStream);
            }
        };
    }

    private void saveFile(String path, String username, HttpEntity entity)
    {
        Pair<String, String> params = Pair.of("overwrite", "true");
        String writeRedirectUri = executeAndGetRedirectUri(new HttpPut(buildUri(path, username, "CREATE", params)));
        HttpPut writeRequest = new HttpPut(writeRedirectUri);
        writeRequest.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(writeRequest)) {
            if (response.getStatusLine().getStatusCode() != SC_CREATED) {
                throw invalidStatusException("CREATE", path, username, writeRequest, response);
            }
            long length = waitForFileSavedAndReturnLength(path, username);
            logger.debug("Saved file {} - username: {}, size: {}", path, username, byteCountToDisplaySize(length));
        }
        catch (IOException e) {
            throw new RuntimeException("Could not save file " + path + " in hdfs, user: " + username, e);
        }
    }

    @Override
    public void loadFile(String path, String username, OutputStream outputStream)
    {
        HttpGet readRequest = new HttpGet(buildUri(path, username, "OPEN"));
        try (CloseableHttpResponse response = httpClient.execute(readRequest)) {
            if (response.getStatusLine().getStatusCode() != SC_OK) {
                throw invalidStatusException("OPEN", path, username, readRequest, response);
            }

            IOUtils.copy(response.getEntity().getContent(), outputStream);

            logger.debug("Loaded file {} - username: {}", path, username);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not read file " + path + " in hdfs, user: " + username, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public long getLength(String path, String username)
    {
        HttpGet readRequest = httpGetFileStatus(path, username);
        try (CloseableHttpResponse response = httpClient.execute(readRequest)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != SC_OK) {
                throw invalidStatusException("GETFILESTATUS", path, username, readRequest, response);
            }
            Map<String, Object> responseObject = deserializeJsonResponse(response);
            return ((Number) ((Map<String, Object>) responseObject.get("FileStatus")).get("length")).longValue();
        }
        catch (IOException e) {
            throw new RuntimeException("Could not get file status: " + path + " , user: " + username, e);
        }
    }

    @Override
    public boolean exist(String path, String username)
    {
        HttpGet readRequest = httpGetFileStatus(path, username);
        try (CloseableHttpResponse response = httpClient.execute(readRequest)) {
            return response.getStatusLine().getStatusCode() == SC_OK;
        }
        catch (IOException e) {
            throw new RuntimeException("Could not get file status: " + path + " , user: " + username, e);
        }
    }

    private HttpGet httpGetFileStatus(String path, String username) {return new HttpGet(buildUri(path, username, "GETFILESTATUS"));}

    @Override
    public void setXAttr(String path, String username, String key, String value)
    {
        Pair[] params = {Pair.of("xattr.name", key), Pair.of("xattr.value", value), Pair.of("flag", "CREATE")};
        HttpPut setXAttrRequest = new HttpPut(buildUri(path, username, "SETXATTR", params));
        try (CloseableHttpResponse response = httpClient.execute(setXAttrRequest)) {
            if (response.getStatusLine().getStatusCode() != SC_OK) {
                throw invalidStatusException("SETXATTR", path, username, setXAttrRequest, response);
            }
            logger.debug("Set xAttr {} = {} for {}, username: {}", key, value, path, username);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not set xAttr for path: " + path + " in hdfs, user: " + username, e);
        }
    }

    @Override
    public void removeXAttr(String path, String username, String key)
    {
        Pair[] params = {Pair.of("xattr.name", key)};
        HttpPut setXAttrRequest = new HttpPut(buildUri(path, username, "REMOVEXATTR", params));
        try (CloseableHttpResponse response = httpClient.execute(setXAttrRequest)) {
            if (response.getStatusLine().getStatusCode() != SC_OK) {
                throw invalidStatusException("SETXATTR", path, username, setXAttrRequest, response);
            }
            logger.debug("Remove xAttr {} for {}, username: {}", key, path, username);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not remove xAttr for path: " + path + " in hdfs, user: " + username, e);
        }
    }

    @Override
    public Optional<String> getXAttr(String path, String username, String key)
    {
        Pair[] params = {Pair.of("xattr.name", key)};
        HttpGet setXAttrRequest = new HttpGet(buildUri(path, username, "GETXATTRS", params));
        try (CloseableHttpResponse response = httpClient.execute(setXAttrRequest)) {
            if (response.getStatusLine().getStatusCode() == SC_NOT_FOUND) {
                return Optional.empty();
            }
            if (response.getStatusLine().getStatusCode() != SC_OK) {
                throw invalidStatusException("GETXATTRS", path, username, setXAttrRequest, response);
            }

            Map<String, Object> responseObject = deserializeJsonResponse(response);
            if (responseObject.get("XAttrs") == null) {
                return Optional.empty();
            }

            String xArgValue = StringUtils.strip(((Map<String, Object>) ((List<Object>) responseObject.get("XAttrs")).get(0)).get("value").toString(), "\"");
            return Optional.of(xArgValue);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not get xAttr for path: " + path + " in hdfs, user: " + username, e);
        }
    }

    private String executeAndGetRedirectUri(HttpUriRequest request)
    {
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() != SC_TEMPORARY_REDIRECT) {
                throw new RuntimeException("Expected redirect for request: " + request);
            }
            return response.getFirstHeader("Location").getValue();
        }
        catch (IOException e) {
            throw new RuntimeException("Could not execute request " + request, e);
        }
    }

    /**
     * There is some wired bug in WebHDFS, which happens for big files. Just after saving such file
     * it is not possible to immediately set xAttr. Calling GETFILESTATUS seems to introduce
     * some synchronization point, so it should be used just after saving file.
     */
    private long waitForFileSavedAndReturnLength(String path, String username)
    {
        return getLength(path, username);
    }

    private URI buildUri(String path, String username, String operation, Pair<String, String>... parameters)
    {
        try {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            URIBuilder uriBuilder = new URIBuilder()
                    .setScheme("http")
                    .setHost(nameNode.getHostText())
                    .setPort(nameNode.getPort())
                    .setPath("/webhdfs/v1" + checkNotNull(path))
                    .setParameter("op", checkNotNull(operation))
                    .setParameter("user.name", checkNotNull(username));

            for (Pair<String, String> parameter : parameters) {
                uriBuilder.setParameter(parameter.getKey(), parameter.getValue());
            }

            return uriBuilder.build();
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Could not create save file URI" +
                    ", nameNode: " + nameNode +
                    ", path: " + path +
                    ", username: " + username);
        }
    }

    private RuntimeException invalidStatusException(String operation, String path, String username, HttpRequest request, HttpResponse response)
            throws IOException
    {
        return new RuntimeException("Operation " + operation +
                " on file " + path + " failed, user: " + username +
                ", status: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase() +
                ", content: " + IOUtils.toString(response.getEntity().getContent()) +
                ", request: " + request.getRequestLine().getMethod() + " " + request.getRequestLine().getUri());
    }

    private Map<String, Object> deserializeJsonResponse(HttpResponse response)
            throws IOException
    {
        return MAPPER.readValue(IOUtils.toString(response.getEntity().getContent()), MAP_TYPE_REFERENCE);
    }
}
