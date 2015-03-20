/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

import com.teradata.test.hadoop.hdfs.HdfsClient;
import com.teradata.test.tpch.IterableTpchEntityInputStream;
import com.teradata.test.tpch.TpchTable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;

import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static org.slf4j.LoggerFactory.getLogger;

public class TpchHiveDataSource
        implements HiveDataSource
{

    private static final Logger logger = getLogger(TpchHiveDataSource.class);

    private final TpchTable table;
    private final double scaleFactor;

    public TpchHiveDataSource(TpchTable table, double scaleFactor)
    {
        this.table = table;
        this.scaleFactor = scaleFactor;
    }

    @Override
    public String ensureDataOnHdfs()
    {
        try {
            String filePath = tpchTableHdfsPath();

            HdfsClient hdfsClient = testContext().getDependency(HdfsClient.class);
            String hdfsUsername = testContext().getDependency(String.class, "hdfs.username");

            long currentFileLength = hdfsClient.getLength(filePath, hdfsUsername);
            if (currentFileLength > 0) {
                long storedLength = getLength(tableInputStream());
                if (currentFileLength == storedLength) {
                    logger.debug("File {} ({} bytes) already exists", filePath, currentFileLength);
                    return filePath;
                }
                else {
                    logger.debug("File {} ({} bytes) already exists, but have different size than expected ({} bytes)", filePath, currentFileLength, storedLength);
                }
            }

            logger.debug("Saving new file {} for TPCH table {}", filePath, table);
            hdfsClient.saveFile(filePath, hdfsUsername, tableInputStream());

            return filePath;
        }
        catch (IOException e) {
            throw new RuntimeException("Could not generate data for table: " + table, e);
        }
    }

    /**
     * @return {TESTS_PATH}/tpch/sf-{scaleFactor}/{tableName}
     */
    private String tpchTableHdfsPath()
    {
        String testsPath = testContext().getDependency(String.class, "tests.hdfs.path");
        return String.format("%s/tpch/sf-%.2f/%s", testsPath, scaleFactor, table.name()).replaceAll("\\.", "_");
    }

    private IterableTpchEntityInputStream tableInputStream()
    {
        @SuppressWarnings("unchecked")
        Iterable<? extends io.airlift.tpch.TpchEntity> tableDataGenerator = table.getTpchTableEntity().createGenerator(scaleFactor, 1, 1);
        return new IterableTpchEntityInputStream<>(tableDataGenerator);
    }

    private long getLength(InputStream inputStream)
            throws IOException
    {
        long sum = 0;
        while (inputStream.read() >= 0) {
            sum += 0;
        }
        return sum;
    }
}
