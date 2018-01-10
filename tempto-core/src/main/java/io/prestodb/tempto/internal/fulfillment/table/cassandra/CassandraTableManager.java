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
package io.prestodb.tempto.internal.fulfillment.table.cassandra;

import com.google.inject.Inject;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.fulfillment.table.MutableTableRequirement;
import io.prestodb.tempto.fulfillment.table.TableDefinition;
import io.prestodb.tempto.fulfillment.table.TableHandle;
import io.prestodb.tempto.fulfillment.table.TableInstance;
import io.prestodb.tempto.fulfillment.table.TableManager;
import io.prestodb.tempto.fulfillment.table.jdbc.RelationalDataSource;
import io.prestodb.tempto.internal.fulfillment.table.TableName;
import io.prestodb.tempto.internal.fulfillment.table.TableNameGenerator;
import io.prestodb.tempto.internal.query.CassandraQueryExecutor;
import io.prestodb.tempto.util.Lazy;
import org.slf4j.Logger;

import javax.inject.Named;
import javax.inject.Singleton;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State.CREATED;
import static io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State.LOADED;
import static io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State.PREPARED;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

@TableManager.Descriptor(tableDefinitionClass = CassandraTableDefinition.class, type = "CASSANDRA")
@Singleton
public class CassandraTableManager
        implements TableManager<CassandraTableDefinition>
{
    private static final Logger LOGGER = getLogger(CassandraTableManager.class);

    private final TableNameGenerator tableNameGenerator;
    private final Lazy<CassandraQueryExecutor> queryExecutor;
    private final String databaseName;
    private final String defaultKeySpace;
    private final boolean skipCreateSchema;
    private final int insertBatchRowsCount;

    @Inject
    public CassandraTableManager(
            TableNameGenerator tableNameGenerator,
            @Named("databaseName") String databaseName,
            Configuration configuration)
    {
        this.tableNameGenerator = requireNonNull(tableNameGenerator, "tableNameGenerator is null");
        this.queryExecutor = new Lazy<>(() -> new CassandraQueryExecutor(configuration));
        this.databaseName = requireNonNull(databaseName, "databaseName is null");
        this.defaultKeySpace = configuration.getStringMandatory("databases." + databaseName + ".default_schema");
        this.skipCreateSchema = configuration.getBoolean("databases." + databaseName + ".skip_create_schema").orElse(false);
        this.insertBatchRowsCount = configuration.getInt("databases." + databaseName + ".insert_batch_rows_count").orElse(10);
    }

    @Override
    public TableInstance<CassandraTableDefinition> createImmutable(CassandraTableDefinition tableDefinition, TableHandle tableHandle)
    {
        TableName tableName = createImmutableTableName(tableHandle);

        if (!queryExecutor.get().tableExists(tableName.getSchema().get(), tableName.getSchemalessNameInDatabase())) {
            if (!skipCreateSchema) {
                queryExecutor.get().executeQuery(format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}", tableName.getSchema().get()));
            }
            executeQueryIgnoreTypeError(tableDefinition.getCreateTableDDL(tableName.getNameInDatabase()));
            RelationalDataSource dataSource = tableDefinition.getDataSource();

            // TODO: check if data is up-to-date

            insertData(tableName, dataSource);
        }

        return new CassandraTableInstance(tableName, tableDefinition);
    }

    private void insertData(TableName tableName, RelationalDataSource dataSource)
    {
        checkState(queryExecutor.get().tableExists(tableName.getSchema().get(), tableName.getSchemalessNameInDatabase()),
                "table %s.%s does not exist",
                tableName.getSchema().get(),
                tableName.getSchemalessNameInDatabase());

        List<String> columnNames = queryExecutor.get().getColumnNames(tableName.getSchema().get(), tableName.getSchemalessNameInDatabase());

        CassandraBatchLoader loader = new CassandraBatchLoader(queryExecutor.get().getSession(), tableName.getNameInDatabase(), columnNames, insertBatchRowsCount);
        loader.load(dataSource.getDataRows());
    }

    @Override
    public TableInstance<CassandraTableDefinition> createMutable(CassandraTableDefinition tableDefinition, MutableTableRequirement.State state, TableHandle tableHandle)
    {
        TableName tableName = createMutableTableName(tableHandle);
        if (!tableName.getSchema().get().equals(defaultKeySpace)) {
            LOGGER.warn("Creating mutable table outside configured key space. It won't be cleaned if test fails.");
        }

        if (!skipCreateSchema) {
            queryExecutor.get().executeQuery(format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}", tableName.getSchema().get()));
        }

        CassandraTableInstance tableInstance = new CassandraTableInstance(tableName, tableDefinition);
        if (state == PREPARED) {
            return tableInstance;
        }

        executeQueryIgnoreTypeError(tableDefinition.getCreateTableDDL(tableName.getNameInDatabase()));

        if (state == CREATED) {
            return tableInstance;
        }

        checkState(state == LOADED, "Unexpected state");

        RelationalDataSource dataSource = tableDefinition.getDataSource();
        insertData(tableName, dataSource);
        return tableInstance;
    }

    @Override
    public void dropTable(TableName tableName)
    {
        executeQueryIgnoreTypeError("DROP TABLE " + tableName.getNameInDatabase());
    }

    public void dropTable(String tableName)
    {
        executeQueryIgnoreTypeError("DROP TABLE " + tableName);
    }

    @Override
    public String getDatabaseName()
    {
        return databaseName;
    }

    @Override
    public Class<? extends TableDefinition> getTableDefinitionClass()
    {
        return CassandraTableDefinition.class;
    }

    @Override
    public void dropStaleMutableTables()
    {
        List<String> tableNames = queryExecutor.get().getTableNames(defaultKeySpace).stream()
                .filter(tableNameGenerator::isMutableTableName)
                .map(tableName -> defaultKeySpace + "." + tableName)
                .collect(toList());

        for (String tableName : tableNames) {
            dropTable(tableName);
        }
    }

    protected TableName createMutableTableName(TableHandle tableHandle)
    {
        String nameInDatabase = tableNameGenerator.generateMutableTableNameInDatabase(tableHandle.getName());
        return new TableName(
                tableHandle.getDatabase().orElse(getDatabaseName()),
                Optional.of(tableHandle.getSchema().orElse(defaultKeySpace)),
                tableHandle.getName(),
                nameInDatabase
        );
    }

    protected TableName createImmutableTableName(TableHandle tableHandle)
    {
        return new TableName(
                tableHandle.getDatabase().orElse(getDatabaseName()),
                Optional.of(tableHandle.getSchema().orElse(defaultKeySpace)),
                tableHandle.getName(),
                tableHandle.getName()
        );
    }

    private void executeQueryIgnoreTypeError(String sql)
    {
        try {
            queryExecutor.get().executeQuery(sql);
        }
        catch (CassandraQueryExecutor.TypeNotSupportedException e) {
            LOGGER.warn(format("Execution of query (%s) failed: %s", sql, e));
        }
    }

    public void close()
    {
        queryExecutor.lazyGet().ifPresent(CassandraQueryExecutor::close);
    }
}
