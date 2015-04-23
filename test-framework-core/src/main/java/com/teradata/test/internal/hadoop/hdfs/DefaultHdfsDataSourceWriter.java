/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.hadoop.hdfs;

import com.teradata.test.fulfillment.hive.DataSource;
import com.teradata.test.hadoop.hdfs.HdfsClient;
import com.teradata.test.hadoop.hdfs.HdfsClient.RepeatableContentProducer;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class DefaultHdfsDataSourceWriter
        implements HdfsDataSourceWriter
{

    private static final Logger LOGGER = getLogger(DefaultHdfsDataSourceWriter.class);

    /**
     * XAttr name stored on HDFS for each data source file.
     */
    private static final String REVISON_XATTR_NAME = "user.test-data-revision";

    private final HdfsClient hdfsClient;
    private final String hdfsUsername;

    @Inject
    public DefaultHdfsDataSourceWriter(HdfsClient hdfsClient, @Named("hdfs.username") String hdfsUsername)
    {
        this.hdfsClient = hdfsClient;
        this.hdfsUsername = hdfsUsername;
    }

    @Override
    public void ensureDataOnHdfs(String dataSourcePath, DataSource dataSource)
    {
        Optional<String> storedRevisionMarker = hdfsClient.getXAttr(dataSourcePath, hdfsUsername, REVISON_XATTR_NAME);
        if (storedRevisionMarker.isPresent()) {
            if (storedRevisionMarker.get().equals(dataSource.revisionMarker())) {
                LOGGER.debug("Directory {} ({}) already exists, skipping generation of data", dataSourcePath, storedRevisionMarker.get());
                return;
            }
            else {
                LOGGER.info("Directory {} ({}) already exists, but has different revision marker than expected: {}, so data will be regenerated",
                        dataSourcePath, storedRevisionMarker.get(), dataSource.revisionMarker());
            }
        }

        hdfsClient.delete(dataSourcePath, hdfsUsername);
        hdfsClient.createDirectory(dataSourcePath, hdfsUsername);
        storeTableFiles(dataSourcePath, dataSource);
        hdfsClient.setXAttr(dataSourcePath, hdfsUsername, REVISON_XATTR_NAME, dataSource.revisionMarker());
    }

    private void storeTableFiles(String dataSourcePath, DataSource dataSource)
    {
        int fileIndex = 0;
        for (RepeatableContentProducer fileContent : dataSource.data()) {
            String filePath = dataSourcePath + "/data_" + fileIndex;
            LOGGER.debug("Saving new file {} ({})", filePath, dataSource.revisionMarker());
            hdfsClient.saveFile(filePath, hdfsUsername, fileContent);
            fileIndex++;
        }
    }
}
