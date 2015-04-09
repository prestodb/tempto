/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.fulfillment.hive;

import com.google.inject.Inject;
import com.teradata.test.fulfillment.hive.DataSource;
import com.teradata.test.fulfillment.hive.HiveTableDefinition;
import com.teradata.test.fulfillment.table.TableDefinition;
import com.teradata.test.fulfillment.table.TableInstance;
import com.teradata.test.fulfillment.table.TableManager;
import com.teradata.test.hadoop.hdfs.HdfsClient;
import com.teradata.test.query.QueryExecutor;
import org.slf4j.Logger;

import javax.inject.Named;

import java.util.Optional;
import java.util.UUID;

import static java.text.MessageFormat.format;
import static org.slf4j.LoggerFactory.getLogger;

public class HiveTableManager
        implements TableManager
{
    private static final Logger LOGGER = getLogger(HiveTableManager.class);

    private final QueryExecutor queryExecutor;
    private final HiveDataSourceWriter hiveDataSourceWriter;
    private final String testDataBasePath;
    private final HdfsClient hdfsClient;
    private final String hdfsUsername;

    @Inject
    public HiveTableManager(@Named("hive") QueryExecutor queryExecutor, HiveDataSourceWriter hiveDataSourceWriter,
            @Named("tests.hdfs.path") String testDataBasePath,
            HdfsClient hdfsClient, @Named("hdfs.username") String hdfsUsername)
    {
        this.queryExecutor = queryExecutor;
        this.hiveDataSourceWriter = hiveDataSourceWriter;
        this.testDataBasePath = testDataBasePath;
        this.hdfsClient = hdfsClient;
        this.hdfsUsername = hdfsUsername;
    }

    @Override
    public HiveTableInstance createImmutable(TableDefinition tableDefinition)
    {
        HiveTableDefinition hiveTableDefinition = (HiveTableDefinition) tableDefinition;
        LOGGER.debug("creating immutable table {}", tableDefinition.getName());

        queryExecutor.executeQuery(dropTableDDL(tableDefinition.getName()));

        DataSource dataSource = hiveTableDefinition.getDataSource();
        String tableDataPath = getImmutableTableHdfsPath(dataSource);
        hiveDataSourceWriter.ensureDataOnHdfs(tableDataPath, dataSource);

        String tableNameInDatabase = tableDefinition.getName();
        queryExecutor.executeQuery(createTableDDL(hiveTableDefinition, tableNameInDatabase, tableDataPath));

        return new HiveTableInstance(tableNameInDatabase, tableNameInDatabase, hiveTableDefinition, Optional.<String>empty());
    }

    @Override
    public HiveTableInstance createMutable(TableDefinition tableDefinition)
    {
        HiveTableDefinition hiveTableDefinition = (HiveTableDefinition) tableDefinition;

        String tableSuffix = UUID.randomUUID().toString().replace("-", "");
        String tableNameInDatabase = tableDefinition.getName() + "_" + tableSuffix;
        LOGGER.debug("creating mutable table {}, name in database: {}", tableDefinition.getName(), tableNameInDatabase);

        DataSource dataSource = hiveTableDefinition.getDataSource();
        String tableDataPath = getMutableTableHdfsPath(tableNameInDatabase);
        hiveDataSourceWriter.ensureDataOnHdfs(tableDataPath, dataSource);

        queryExecutor.executeQuery(createTableDDL(hiveTableDefinition, tableNameInDatabase, tableDataPath));

        return new HiveTableInstance(tableDefinition.getName(), tableNameInDatabase, hiveTableDefinition, Optional.of(tableDataPath));
    }

    @Override
    public void drop(TableInstance tableInstance)
    {
        HiveTableInstance hiveTableInstance = (HiveTableInstance) tableInstance;

        queryExecutor.executeQuery(dropTableDDL(hiveTableInstance.getNameInDatabase()));
        if (hiveTableInstance.getMutableDataHdfsDataPath().isPresent()) {
            hdfsClient.delete(hiveTableInstance.getMutableDataHdfsDataPath().get(), hdfsUsername);
        }
    }

    private String getImmutableTableHdfsPath(DataSource dataSource)
    {
        return testDataBasePath + "/" + dataSource.getPathSuffix();
    }

    private String getMutableTableHdfsPath(String tableName)
    {
        return testDataBasePath + "/mutable_tables/" + tableName;
    }

    private String createTableDDL(HiveTableDefinition tableDefinition, String tableName, String tableDataPath)
    {
        String escapedFormat = tableDefinition.getCreateTableDDLTemplate(tableName, tableDataPath);
        return format(escapedFormat, tableDefinition.getDataSource().getPathSuffix());
    }

    private String dropTableDDL(String tableName)
    {
        return format("DROP TABLE IF EXISTS {0}", tableName);
    }
}
