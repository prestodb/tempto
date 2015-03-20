/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.hadoop.hdfs;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.InputStream;
import java.io.OutputStream;

import static java.nio.charset.Charset.defaultCharset;

/**
 * HDFS client.
 */
public interface HdfsClient
{

    void createDirectory(String path, String username);

    void saveFile(String path, String username, InputStream input);

    void loadFile(String path, String username, OutputStream outputStream);

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
