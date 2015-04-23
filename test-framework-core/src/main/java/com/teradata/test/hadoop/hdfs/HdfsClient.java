/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.hadoop.hdfs;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import static java.nio.charset.Charset.defaultCharset;

/**
 * HDFS client.
 */
public interface HdfsClient
{
    /**
     * Interface of an object that can open same input stream multiple times.
     */
    @FunctionalInterface
    interface RepeatableContentProducer
    {
        /**
         * @return a content {@link InputStream}. It will be automatically closed after use.
         */
        InputStream getInputStream()
                throws IOException;
    }

    void createDirectory(String path, String username);

    void delete(String path, String username);

    void saveFile(String path, String username, InputStream input);

    void saveFile(String path, String username, RepeatableContentProducer repeatableContentProducer);

    void loadFile(String path, String username, OutputStream outputStream);

    void setXAttr(String path, String username, String key, String value);

    Optional<String> getXAttr(String path, String username, String key);

    default String loadFile(String path, String username)
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        loadFile(path, username, output);
        return new String(output.toByteArray(), defaultCharset());
    }

    /**
     * @return length of a file stored in HDFS, -1 if file not exists
     */
    long getLength(String path, String username);
}
