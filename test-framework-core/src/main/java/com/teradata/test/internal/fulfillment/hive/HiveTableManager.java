/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.fulfillment.hive;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.teradata.test.fulfillment.hive.DataSource;
import com.teradata.test.fulfillment.hive.HiveTableDefinition;
import com.teradata.test.fulfillment.table.MutableTableRequirement.State;
import com.teradata.test.fulfillment.table.TableDefinition;
import com.teradata.test.fulfillment.table.TableInstance;
import com.teradata.test.fulfillment.table.TableManager;
import com.teradata.test.fulfillment.table.TableManager.AutoTableManager;
import com.teradata.test.hadoop.hdfs.HdfsClient;
import com.teradata.test.internal.hadoop.hdfs.HdfsDataSourceWriter;
import com.teradata.test.internal.uuid.UUIDGenerator;
import com.teradata.test.query.QueryExecutor;
import org.slf4j.Logger;

import javax.inject.Named;

import java.util.Optional;

import static com.teradata.test.fulfillment.table.MutableTableRequirement.State.LOADED;
import static java.text.MessageFormat.format;
import static org.slf4j.LoggerFactory.getLogger;

@AutoTableManager(tableDefinitionClass = HiveTableDefinition.class, name = "hive")
public class HiveTableManager
        implements TableManager
{
    private static final Logger LOGGER = getLogger(HiveTableManager.class);

    private final QueryExecutor queryExecutor;
    private final HdfsDataSourceWriter hdfsDataSourceWriter;
    private final UUIDGenerator uuidGenerator;
    private final String testDataBasePath;
    private final HdfsClient hdfsClient;
    private final String hdfsUsername;

    @Inject
    public HiveTableManager(@Named("hive") QueryExecutor queryExecutor,
            HdfsDataSourceWriter hdfsDataSourceWriter,
            UUIDGenerator uuidGenerator,
            @Named("tests.hdfs.path") String testDataBasePath,
            HdfsClient hdfsClient,
            @Named("hdfs.username") String hdfsUsername)
    {
        this.queryExecutor = queryExecutor;
        this.hdfsDataSourceWriter = hdfsDataSourceWriter;
        this.uuidGenerator = uuidGenerator;
        this.testDataBasePath = testDataBasePath;
        this.hdfsClient = hdfsClient;
        this.hdfsUsername = hdfsUsername;
    }

    @Override
    public HiveTableInstance createImmutable(TableDefinition tableDefinition)
    {
        HiveTableDefinition hiveTableDefinition = (HiveTableDefinition) tableDefinition;
        Preconditions.checkState(!hiveTableDefinition.isPartitioned(), "Partitioning not supported for immutable tables");
        String tableNameInDatabase = tableDefinition.getName();
        LOGGER.debug("creating immutable table {}", tableNameInDatabase);

        queryExecutor.executeQuery(dropTableDDL(tableNameInDatabase));

        String tableDataPath = getImmutableTableHdfsPath(hiveTableDefinition.getDataSource());
        uploadTableData(tableDataPath, hiveTableDefinition.getDataSource());

        queryExecutor.executeQuery(hiveTableDefinition.getCreateTableDDL(tableNameInDatabase, tableDataPath));

        return new HiveTableInstance(tableNameInDatabase, tableNameInDatabase, hiveTableDefinition, Optional.<String>empty());
    }

    @Override
    public HiveTableInstance createMutable(TableDefinition tableDefinition, State state)
    {
        HiveTableDefinition hiveTableDefinition = (HiveTableDefinition) tableDefinition;

        String tableSuffix = uuidGenerator.randomUUID().replace("-", "");
        String tableNameInDatabase = tableDefinition.getName() + "_" + tableSuffix;
        LOGGER.debug("creating mutable table {}, name in database: {}", tableDefinition.getName(), tableNameInDatabase);

        String tableDataPath = getMutableTableHdfsPath(tableNameInDatabase, Optional.empty());
        if (state == LOADED && !hiveTableDefinition.isPartitioned()) {
            uploadTableData(tableDataPath, hiveTableDefinition.getDataSource());
        }
        queryExecutor.executeQuery(hiveTableDefinition.getCreateTableDDL(tableNameInDatabase, tableDataPath));

        if (hiveTableDefinition.isPartitioned()) {
            int partitionId = 0;
            for (HiveTableDefinition.PartitionDefinition partitionDefinition : hiveTableDefinition.getPartitionDefinitons()) {
                String partitionDataPath = getMutableTableHdfsPath(tableNameInDatabase, Optional.of(partitionId));
                if (state == LOADED) {
                    uploadTableData(partitionDataPath, partitionDefinition.getDataSource());
                }
                queryExecutor.executeQuery(partitionDefinition.getAddPartitionTableDDL(tableNameInDatabase, partitionDataPath));
                partitionId++;
            }
        }

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

    private void uploadTableData(String tableDataPath, DataSource dataSource)
    {
        hdfsDataSourceWriter.ensureDataOnHdfs(tableDataPath, dataSource);
    }

    private String getImmutableTableHdfsPath(DataSource dataSource)
    {
        return testDataBasePath + "/" + dataSource.getPathSuffix();
    }

    private String getMutableTableHdfsPath(String tableName, Optional<Integer> partitionId)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(testDataBasePath);
        sb.append("/mutable_tables/");
        sb.append(tableName);
        if (partitionId.isPresent()) {
            sb.append("/partition_").append(partitionId.get());
        }
        return sb.toString();
    }

    private String dropTableDDL(String tableName)
    {
        return format("DROP TABLE IF EXISTS {0}", tableName);
    }
}
