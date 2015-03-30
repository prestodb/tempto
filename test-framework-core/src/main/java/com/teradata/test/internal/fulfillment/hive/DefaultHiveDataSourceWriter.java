/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.fulfillment.hive;

import com.teradata.test.fulfillment.hive.DataSource;
import com.teradata.test.hadoop.hdfs.HdfsClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

public class DefaultHiveDataSourceWriter
        implements HiveDataSourceWriter
{

    private static final Logger LOGGER = getLogger(DefaultHiveDataSourceWriter.class);

    /**
     * XAttr name stored on HDFS for each data source file.
     */
    private static final String REVISON_XATTR_NAME = "user.test-data-revision";

    private final HdfsClient hdfsClient;
    private final String hdfsUsername;

    @Inject
    public DefaultHiveDataSourceWriter(HdfsClient hdfsClient, @Named("hdfs.username") String hdfsUsername)
    {
        this.hdfsClient = hdfsClient;
        this.hdfsUsername = hdfsUsername;
    }

    @Override
    public void ensureDataOnHdfs(DataSource dataSource)
    {
        String dataSourcePath = dataSource.getPath();
        String filePath = dataSourcePath + "/data";

        Optional<String> storedRevisionMarker = hdfsClient.getXAttr(dataSourcePath, hdfsUsername, REVISON_XATTR_NAME);
        if (storedRevisionMarker.isPresent()) {
            if (storedRevisionMarker.get().equals(dataSource.revisionMarker())) {
                LOGGER.debug("File {} ({}) already exists, skipping generation of data", filePath, storedRevisionMarker.get());
                return;
            }
            else {
                LOGGER.info("File {} ({}) already exists, but has different revision marker than expected: {}, so data will be regenerated",
                        filePath, storedRevisionMarker.get(), dataSource.revisionMarker());
            }
        }

        LOGGER.info("Saving new file {} ({})", filePath, dataSource.revisionMarker());
        hdfsClient.createDirectory(dataSourcePath, hdfsUsername);
        hdfsClient.saveFile(filePath, hdfsUsername, dataSource.data());
        hdfsClient.setXAttr(dataSourcePath, hdfsUsername, REVISON_XATTR_NAME, dataSource.revisionMarker());
    }
}
