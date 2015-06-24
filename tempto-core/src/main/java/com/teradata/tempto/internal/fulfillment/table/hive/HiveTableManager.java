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
package com.teradata.tempto.internal.fulfillment.table.hive;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.teradata.tempto.fulfillment.table.MutableTableRequirement.State;
import com.teradata.tempto.fulfillment.table.TableDefinition;
import com.teradata.tempto.fulfillment.table.TableManager;
import com.teradata.tempto.fulfillment.table.hive.HiveDataSource;
import com.teradata.tempto.fulfillment.table.hive.HiveTableDefinition;
import com.teradata.tempto.hadoop.hdfs.HdfsClient;
import com.teradata.tempto.internal.fulfillment.table.TableNameGenerator;
import com.teradata.tempto.internal.hadoop.hdfs.HdfsDataSourceWriter;
import com.teradata.tempto.internal.uuid.UUIDGenerator;
import com.teradata.tempto.query.QueryExecutor;
import com.teradata.tempto.query.QueryResult;
import com.teradata.tempto.query.QueryType;
import org.slf4j.Logger;

import javax.inject.Named;
import javax.inject.Singleton;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.teradata.tempto.fulfillment.table.MutableTableRequirement.State.LOADED;
import static com.teradata.tempto.fulfillment.table.MutableTableRequirement.State.PREPARED;
import static java.text.MessageFormat.format;
import static org.slf4j.LoggerFactory.getLogger;

@TableManager.Descriptor(tableDefinitionClass = HiveTableDefinition.class, type = "HIVE")
@Singleton
public class HiveTableManager
        implements TableManager<HiveTableDefinition>
{
    public static final String MUTABLE_TABLES_DIR = "/mutable_tables/";

    private static final Logger LOGGER = getLogger(HiveTableManager.class);

    private final QueryExecutor queryExecutor;
    private final HdfsDataSourceWriter hdfsDataSourceWriter;
    private final TableNameGenerator tableNameGenerator;
    private final String testDataBasePath;
    private final HdfsClient hdfsClient;
    private final String hdfsUsername;

    @Inject
    public HiveTableManager(QueryExecutor queryExecutor,
            HdfsDataSourceWriter hdfsDataSourceWriter,
            TableNameGenerator tableNameGenerator,
            @Named("tests.hdfs.path") String testDataBasePath,
            HdfsClient hdfsClient,
            @Named("hdfs.username") String hdfsUsername)
    {
        this.queryExecutor = checkNotNull(queryExecutor, "queryExecutor is null");
        this.hdfsDataSourceWriter = checkNotNull(hdfsDataSourceWriter, "hdfsDataSourceWriter is null");
        this.tableNameGenerator = checkNotNull(tableNameGenerator, "tableNameGenerator is null");
        this.testDataBasePath = checkNotNull(testDataBasePath, "testDataBasePath is null");
        this.hdfsClient = checkNotNull(hdfsClient, "hdfsClientd is null");
        this.hdfsUsername = checkNotNull(hdfsUsername, "hdfsUsername is null");
    }

    @Override
    public HiveTableInstance createImmutable(HiveTableDefinition tableDefinition)
    {
        Preconditions.checkState(!tableDefinition.isPartitioned(), "Partitioning not supported for immutable tables");
        String tableNameInDatabase = tableDefinition.getName();
        LOGGER.debug("creating immutable table {}", tableNameInDatabase);

        String tableDataPath = getImmutableTableHdfsPath(tableDefinition.getDataSource());
        uploadTableData(tableDataPath, tableDefinition.getDataSource());

        queryExecutor.executeQuery(tableDefinition.getCreateTableDDL(tableNameInDatabase, tableDataPath));

        return new HiveTableInstance(tableNameInDatabase, tableNameInDatabase, tableDefinition, Optional.<String>empty());
    }

    @Override
    public HiveTableInstance createMutable(HiveTableDefinition tableDefinition, State state)
    {
        String tableNameInDatabase = tableNameGenerator.generateUniqueTableNameInDatabase(tableDefinition);
        String tableDataPath = getMutableTableHdfsPath(tableNameInDatabase, Optional.empty());
        LOGGER.debug("creating mutable table {}, name in database: {}, data path: {}", tableDefinition.getName(), tableNameInDatabase, tableDataPath);

        if (state == PREPARED) {
            return new HiveTableInstance(tableDefinition.getName(), tableNameInDatabase, tableDefinition, Optional.of(tableDataPath));
        }

        queryExecutor.executeQuery(tableDefinition.getCreateTableDDL(tableNameInDatabase, tableDataPath));

        if (tableDefinition.isPartitioned()) {
            int partitionId = 0;
            for (HiveTableDefinition.PartitionDefinition partitionDefinition : tableDefinition.getPartitionDefinitons()) {
                String partitionDataPath = getMutableTableHdfsPath(tableNameInDatabase, Optional.of(partitionId));
                if (state == LOADED) {
                    uploadTableData(partitionDataPath, partitionDefinition.getDataSource());
                }
                queryExecutor.executeQuery(partitionDefinition.getAddPartitionTableDDL(tableNameInDatabase, partitionDataPath));
                partitionId++;
            }
        }
        else if (state == LOADED) {
            uploadTableData(tableDataPath, tableDefinition.getDataSource());
        }

        return new HiveTableInstance(tableDefinition.getName(), tableNameInDatabase, tableDefinition, Optional.of(tableDataPath));
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

    private void uploadTableData(String tableDataPath, HiveDataSource dataSource)
    {
        hdfsDataSourceWriter.ensureDataOnHdfs(tableDataPath, dataSource);
    }

    private String getImmutableTableHdfsPath(HiveDataSource dataSource)
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
