/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.hadoop.hdfs;

import com.google.common.net.HostAndPort;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.HostAndPort.fromParts;

/**
 * HDFS client based on WebHDFS REST API.
 */
public class WebHDFSClient
        implements HdfsClient
{

    private static final Logger logger = LoggerFactory.getLogger(WebHDFSClient.class);

    private final HostAndPort dataNode;
    private final HostAndPort nameNode;
    private final int nameNodePort;

    private final CloseableHttpClient httpClient;

    public WebHDFSClient(String webHdfsDataNodeHost, int webHdfsDataNodePort, String webHdfsNameNodeHost, int webHdfsNameNodePort, int nameNodePort)
    {
        this.dataNode = fromParts(checkNotNull(webHdfsDataNodeHost), webHdfsDataNodePort);
        this.nameNode = fromParts(checkNotNull(webHdfsNameNodeHost), webHdfsNameNodePort);
        this.nameNodePort = nameNodePort;
        checkArgument(webHdfsDataNodePort > 0, "Invalid data node WebHDFS port number: %s", webHdfsDataNodePort);
        checkArgument(webHdfsNameNodePort > 0, "Invalid name node WebHDFS port number: %s", webHdfsNameNodePort);
        checkArgument(nameNodePort > 0, "Invalid name node port number: %s", nameNodePort);

        this.httpClient = HttpClients.custom()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
    }

    @PreDestroy
    public void closeHttpClient()
            throws IOException
    {
        httpClient.close();
    }

    @Override
    public void createDirectory(String path, String username)
    {
        HttpPut mkdirRequest = new HttpPut(buildUri(path, username, "MKDIRS"));
        try (CloseableHttpResponse response = httpClient.execute(mkdirRequest)) {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw invalidStatusException("MKDIRS", path, username, mkdirRequest, response);
            }
            logger.debug("Created directory {} - username: {}", path, username);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not save file " + path + " in hdfs, user: " + username, e);
        }
    }

    @Override
    public void saveFile(String path, String username, InputStream input)
    {
        HttpPut writeRequest = new HttpPut(buildUri(path, username, "CREATE"));
        writeRequest.setEntity(new InputStreamEntity(input));

        try (CloseableHttpResponse response = httpClient.execute(writeRequest)) {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                throw invalidStatusException("CREATE", path, username, writeRequest, response);
            }
            logger.debug("Save file {} - username: {}", path, username);
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
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw invalidStatusException("OPEN", path, username, readRequest, response);
            }

            IOUtils.copy(response.getEntity().getContent(), outputStream);

            logger.debug("Loaded file {} - username: {}", path, username);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not read file " + path + " in hdfs, user: " + username, e);
        }
    }

    private URI buildUri(String path, String username, String operation)
    {
        try {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            HostAndPort targetHost = getTargetHost(operation);
            return new URIBuilder()
                    .setScheme("http")
                    .setHost(targetHost.getHostText())
                    .setPort(targetHost.getPort())
                    .setPath("/webhdfs/v1" + checkNotNull(path))
                    .setParameter("op", checkNotNull(operation))
                    .setParameter("user.name", checkNotNull(username))
                    .setParameter("overwrite", "true")
                    .setParameter("namenoderpcaddress", nameNode.getHostText() + ":" + nameNodePort)
                    .build();
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Could not create save file URI" +
                    ", dataNode: " + dataNode +
                    ", nameNode: " + nameNode +
                    ", path: " + path +
                    ", username: " + username);
        }
    }

    private HostAndPort getTargetHost(String operation)
    {
        switch (operation) {
            case "MKDIRS":
                return nameNode;
            case "CREATE":
            case "OPEN":
                return dataNode;
            default:
                throw new RuntimeException("Unsupported operation: " + operation);
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
}
