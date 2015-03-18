/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.examples;

import com.teradata.test.hadoop.hdfs.HdfsClient;
import com.teradata.test.hadoop.hdfs.WebHDFSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.apache.commons.io.IOUtils.toInputStream;
import static org.assertj.core.api.Assertions.assertThat;

public class HdfsSmokeTest
{

    private static final Logger logger = LoggerFactory.getLogger(HdfsSmokeTest.class);

    // TODO: put this configuration in file
    private static final String webHdfsDataNodeName = "slave1";
    private static final String webHdfsNameNodeName = "master";
    private static final int webHdfsDataNodePort = 50075;
    private static final int webHdfsNameNodePort = 50070;
    private static final int nameNodePort = 8020;

    @Test(groups = "example_smoketest")
    public void testHdfsSaveRead()
    {
        HdfsClient hdfsClient = new WebHDFSClient(webHdfsDataNodeName, webHdfsDataNodePort, webHdfsNameNodeName, webHdfsNameNodePort, nameNodePort);

        String directory = "/tmp/product-test";
        String path = "/tmp/product-test/test-file";
        String username = "hdfs";
        String content = "test file content\n123\n";

        logger.info("Creating directory ({}) on hdfs", directory);
        hdfsClient.createDirectory(directory, username);

        logger.info("Saving file ({}) to hdfs", path);
        hdfsClient.saveFile(path, username, toInputStream(content));

        String readContent = hdfsClient.loadFile(path, username);
        logger.info("Read file ({}) content: \"{}\"", path, readContent);

        assertThat(readContent).isEqualTo(content);
    }
}
