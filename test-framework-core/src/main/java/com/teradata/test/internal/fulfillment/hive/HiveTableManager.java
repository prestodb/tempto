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
package com.teradata.test.internal.fulfillment.hive;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.teradata.test.fulfillment.hive.DataSource;
import com.teradata.test.fulfillment.hive.HiveTableDefinition;
import com.teradata.test.fulfillment.table.MutableTableRequirement.State;
import com.teradata.test.fulfillment.table.TableDefinition;
import com.teradata.test.fulfillment.table.TableManager;
import com.teradata.test.fulfillment.table.TableManager.AutoTableManager;
import com.teradata.test.hadoop.hdfs.HdfsClient;
import com.teradata.test.internal.hadoop.hdfs.HdfsDataSourceWriter;
import com.teradata.test.internal.uuid.UUIDGenerator;
import com.teradata.test.query.QueryExecutor;
import com.teradata.test.query.QueryResult;
import com.teradata.test.query.QueryType;
import org.slf4j.Logger;

import javax.inject.Named;

import java.util.Optional;

import static com.teradata.test.fulfillment.table.MutableTableRequirement.State.LOADED;
import static com.teradata.test.fulfillment.table.MutableTableRequirement.State.PREPARED;
import static java.text.MessageFormat.format;
import static org.slf4j.LoggerFactory.getLogger;

@AutoTableManager(tableDefinitionClass = HiveTableDefinition.class, name = "hive")
public class HiveTableManager
        implements TableManager
{
    public static final String MUTABLE_TABLES_DIR = "/mutable_tables/";

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
        String tableDataPath = getMutableTableHdfsPath(tableNameInDatabase, Optional.empty());
        LOGGER.debug("creating mutable table {}, name in database: {}, data path: {}", tableDefinition.getName(), tableNameInDatabase, tableDataPath);

        if (state == PREPARED) {
            return new HiveTableInstance(tableDefinition.getName(), tableNameInDatabase, hiveTableDefinition, Optional.of(tableDataPath));
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
        else if (state == LOADED) {
            uploadTableData(tableDataPath, hiveTableDefinition.getDataSource());
        }

        return new HiveTableInstance(tableDefinition.getName(), tableNameInDatabase, hiveTableDefinition, Optional.of(tableDataPath));
    }

    @Override
    public void dropAllTables()
    {
        LOGGER.debug("searching for all hive tables");
        QueryResult tables = queryExecutor.executeQuery("SHOW TABLES", QueryType.SELECT);
        LOGGER.debug(format("dropping [%d] hive tables", tables.getRowsCount()));
        if (tables.getRowsCount() > 0) {
            tables.column(1).stream().forEach(tableName -> dropTableDDL((String) tableName));
        }
        hdfsClient.delete(getMutableTablesDir(), hdfsUsername);
    }

    private void uploadTableData(String tableDataPath, DataSource dataSource)
    {
        hdfsDataSourceWriter.ensureDataOnHdfs(tableDataPath, dataSource);
    }

    private String getImmutableTableHdfsPath(DataSource dataSource)
    {
        return testDataBasePath + "/" + dataSource.getPathSuffix();
    }

    private String getMutableTablesDir()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(testDataBasePath);
        sb.append(MUTABLE_TABLES_DIR);
        return sb.toString();
    }

    private String getMutableTableHdfsPath(String tableName, Optional<Integer> partitionId)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getMutableTablesDir());
        sb.append(tableName);
        if (partitionId.isPresent()) {
            sb.append("/partition_").append(partitionId.get());
        }
        return sb.toString();
    }

    private void dropTableDDL(String tableName)
    {
        queryExecutor.executeQuery(format("DROP TABLE IF EXISTS {0}", tableName));
    }
}
