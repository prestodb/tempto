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

package com.teradata.tempto.internal.hadoop.s3;

import java.io.*;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import com.teradata.tempto.hadoop.FileSystemClient;

import org.slf4j.Logger;
import org.apache.commons.io.IOUtils;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.google.common.base.Splitter;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

public class S3Client
        implements FileSystemClient
{
    private static final Logger logger = getLogger(S3Client.class);

    private static final String PATH_SEPARATOR = "/";
    private static final Splitter PATH_SPLITTER = Splitter.on('/').omitEmptyStrings();
    private static final String EMPTY_STRING = "";

    private static final String CONF_S3_AUTH_KEY_ID = "s3.auth.keyid";
    private static final String CONF_S3_AUTH_SECRET = "s3.auth.secret";
    private static final String CONF_S3_BUCKET_NAME = "s3.bucketname";

    private final String bucketName;
    private final AmazonS3 client;

    @Inject
    public S3Client(
            @Named(CONF_S3_AUTH_KEY_ID) String keyId,
            @Named(CONF_S3_AUTH_SECRET) String secret,
            @Named(CONF_S3_BUCKET_NAME) String bucketName)
    {
        // Check null values and set object variables
        checkNotNull(keyId, "aws api access key id is null");
        checkNotNull(secret, "aws api secret access key is null");
        this.bucketName = checkNotNull(bucketName, "s3 bucket name is null");

        // Create S3 client
        client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(buildAWSCredentials(keyId, secret)))
                .build();
    }

    private BasicAWSCredentials buildAWSCredentials(String keyId, String secret)
    {
        try {
            return new BasicAWSCredentials(keyId, secret);
        } catch (Exception e) {
            throw new AmazonClientException("Cannot initialize credentials", e);
        }
    }

    @Override
    public void createDirectory(String path)
    {
        // Check to make sure directory does not already exist
        if (exist(ensureStartSlashNotPresent(ensureTrailingSlashPresent(path)))) {
            return;
        }

        List<String> parts = PATH_SPLITTER.splitToList(path);
        StringBuilder sb = new StringBuilder();

        // Loop through all folders in path and create folders if not already exists
        for (String objectName : parts) {
            sb.append(objectName);
            sb.append(PATH_SEPARATOR);
            if (!exist(sb.toString())) {
                createEmptyObject(sb.toString());
            }
        }
    }

    private String ensureTrailingSlashPresent(String path)
    {
        if (!path.endsWith(PATH_SEPARATOR)) {
            path = path + PATH_SEPARATOR;
        }
        return path;
    }

    private String ensureStartSlashNotPresent(String path)
    {
        if (path.startsWith(PATH_SEPARATOR)) {
            path = path.substring(1);
        }
        return path;
    }

    private void createEmptyObject(String path) {
        // Create meta-data for your folder and set content-length to 0 (which indicates a folder)
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);

        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                ensureTrailingSlashPresent(path), emptyContent, metadata);

        // Send request to S3 to create folder
        client.putObject(putObjectRequest);
    }

    @Override
    public void deleteDirectory(String path)
    {
        List<String> objects = getAllObjects(ensureStartSlashNotPresent(ensureTrailingSlashPresent(path)));

        // For all objects retrieved, delete by sorted order of furthest away from path first
        Ordering<String> ordering = Ordering
                .from(Comparator
                        .comparingInt(object -> -PATH_SPLITTER.splitToList(object).size()));
        ordering.sortedCopy(objects).forEach(o -> deleteObject(o));

        // Delete the folder itself
        deleteObject(ensureStartSlashNotPresent(ensureTrailingSlashPresent(path)));
    }

    @Override
    public void delete(String path)
    {
        deleteObject(ensureStartSlashNotPresent(path));
    }

    private List<String> getAllObjects(String path)
    {
        // Get list of all objects underneath this path
        ListObjectsV2Result listing = client.listObjectsV2(new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(ensureTrailingSlashPresent(path)));

        // Return all objects (filtering out the path itself)
        return listing.getObjectSummaries().stream()
                .map(S3ObjectSummary::getKey)
                .filter(key -> !key.equals(ensureTrailingSlashPresent(path)))
                .collect(Collectors.toList());
    }

    private void deleteObject(String path)
    {
        client.deleteObject(bucketName, path);
    }

    @Override
    public void saveFile(String path, InputStream input, long byteLength)
    {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(byteLength);

        client.putObject(new PutObjectRequest(bucketName, ensureStartSlashNotPresent(path), input, metadata));

        logger.debug("Saved file {} to S3", path);
    }

    @Override
    public void saveFile(String path, RepeatableContentProducer repeatableContentProducer)
    {
        try {
            InputStream inputStream = repeatableContentProducer.getInputStream();
            byte[] contentBytes = IOUtils.toByteArray(inputStream);
            saveFile(path, repeatableContentProducer.getInputStream(), contentBytes.length);
        } catch (IOException e) {
            throw new RuntimeException("Could not save file " + path + " in S3", e);
        }
    }

    @Override
    public void loadFile(String path, OutputStream outputStream)
    {
        if (exist(path)) {
            try {
                InputStream inputStream = client.getObject(new GetObjectRequest(bucketName, ensureStartSlashNotPresent(path)))
                        .getObjectContent();
                IOUtils.copy(inputStream, outputStream);
            } catch (IOException e) {
                throw new RuntimeException("Could not read file " + path + " in S3", e);
            }
        }
    }

    @Override
    public boolean exist(String path)
    {
        return client.doesObjectExist(bucketName, path);
    }

    // Not needed for S3
    @Override
    public long getLength(String path) { return 0; }

    // Not needed for S3
    @Override
    public String getOwner(String path) { return EMPTY_STRING; }

    // Not supported in S3
    @Override
    public void setXAttr(String path, String key, String value) {}

    // Not supported in S3
    @Override
    public void removeXAttr(String path, String key) {}

    // Not supported in S3
    @Override
    public Optional<String> getXAttr(String path, String key) { return Optional.empty(); }
}
