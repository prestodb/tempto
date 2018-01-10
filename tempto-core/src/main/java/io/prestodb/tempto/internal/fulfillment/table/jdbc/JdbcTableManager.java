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

package io.prestodb.tempto.internal.fulfillment.table.jdbc;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State;
import io.prestodb.tempto.fulfillment.table.TableDefinition;
import io.prestodb.tempto.fulfillment.table.TableHandle;
import io.prestodb.tempto.fulfillment.table.TableInstance;
import io.prestodb.tempto.fulfillment.table.TableManager;
import io.prestodb.tempto.fulfillment.table.jdbc.RelationalDataSource;
import io.prestodb.tempto.fulfillment.table.jdbc.RelationalTableDefinition;
import io.prestodb.tempto.internal.fulfillment.table.AbstractTableManager;
import io.prestodb.tempto.internal.fulfillment.table.TableName;
import io.prestodb.tempto.internal.fulfillment.table.TableNameGenerator;
import io.prestodb.tempto.query.QueryExecutionException;
import io.prestodb.tempto.query.QueryExecutor;
import io.prestodb.tempto.query.QueryResult;
import org.slf4j.Logger;

import javax.inject.Named;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State.CREATED;
import static io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State.LOADED;
import static io.prestodb.tempto.fulfillment.table.MutableTableRequirement.State.PREPARED;
import static org.slf4j.LoggerFactory.getLogger;

@TableManager.Descriptor(tableDefinitionClass = RelationalTableDefinition.class, type = "JDBC")
public class JdbcTableManager
        extends AbstractTableManager<RelationalTableDefinition>
{
    private static final int BATCH_SIZE = 10000;
    private static final Logger LOGGER = getLogger(JdbcTableManager.class);

    private final QueryExecutor queryExecutor;
    private final String databaseName;
    private final Configuration configuration;

    @Inject
    public JdbcTableManager(
            QueryExecutor queryExecutor,
            TableNameGenerator tableNameGenerator,
            @Named("databaseName") String databaseName,
            Configuration configuration)
    {
        super(queryExecutor, tableNameGenerator);
        this.databaseName = databaseName;
        this.configuration = configuration;
        this.queryExecutor = checkNotNull(queryExecutor, "queryExecutor is null");
    }

    @Override
    public TableInstance createImmutable(RelationalTableDefinition tableDefinition, TableHandle tableHandle)
    {
        TableName tableName = createImmutableTableName(tableHandle);
        LOGGER.debug("creating immutable table {}", tableName);
        // TODO: drop and recreate table if the underlying data has changed

        if (!tableName.getSchema().isPresent()) {
            // If there's no schema specified, you need to drop and recreate the table because there
            // could be a table from another schema that has the same name
            dropTableIgnoreError(tableName);
            createAndInsertData(tableDefinition, tableName);
        }
        else if (!tableExists(tableName)) {
            createAndInsertData(tableDefinition, tableName);
        }
        else {
            LOGGER.info("Table {} already exists, skipping creation of immutable table", tableName.getNameInDatabase());
        }
        return new JdbcTableInstance(tableName, tableDefinition);
    }

    private void createAndInsertData(RelationalTableDefinition tableDefinition, TableName tableName)
    {
        createTable(tableDefinition, tableName);
        RelationalDataSource dataSource = tableDefinition.getDataSource();
        insertData(tableName, dataSource);
    }

    private boolean tableExists(TableName tableName)
    {
        Connection connection = queryExecutor.getConnection();
        try {
            DatabaseMetaData metadata = connection.getMetaData();
            String escape = metadata.getSearchStringEscape();
            try (ResultSet resultSet = metadata.getTables(
                    connection.getCatalog(),
                    escapeNamePattern(tableName.getSchema().orElse(null), escape),
                    escapeNamePattern(tableName.getName(), escape),
                    new String[] {"TABLE"})
            ) {
                return QueryResult.forResultSet(resultSet).getRowsCount() > 0;
            }
        }
        catch (SQLException e) {
            throw new QueryExecutionException(e);
        }
    }

    private static String escapeNamePattern(String name, String escape)
    {
        if ((name == null) || (escape == null)) {
            return name;
        }
        checkArgument(!escape.equals("_"), "Escape string must not be '_'");
        checkArgument(!escape.equals("%"), "Escape string must not be '%'");
        name = name.replace(escape, escape + escape);
        name = name.replace("_", escape + "_");
        name = name.replace("%", escape + "%");
        return name;
    }

    private void createTable(RelationalTableDefinition tableDefinition, TableName tableName)
    {
        boolean skipCreateSchema = configuration.getBoolean("databases." + databaseName + ".skip_create_schema").orElse(false);
        if (!skipCreateSchema) {
            tableName.getSchema().ifPresent(schema ->
                    queryExecutor.executeQuery("CREATE SCHEMA IF NOT EXISTS " + schema)
            );
        }

        queryExecutor.executeQuery(tableDefinition.getCreateTableDDL(tableName.getNameInDatabase()));
    }

    @Override
    public TableInstance createMutable(RelationalTableDefinition tableDefinition, State state, TableHandle tableHandle)
    {
        TableName tableName = createMutableTableName(tableHandle);
        LOGGER.debug("creating mutable table {}", tableName);
        JdbcTableInstance tableInstance = new JdbcTableInstance(tableName, tableDefinition);
        if (state == PREPARED) {
            return tableInstance;
        }

        createTable(tableDefinition, tableName);

        if (state == CREATED) {
            return tableInstance;
        }

        assert state == LOADED;

        RelationalDataSource dataSource = tableDefinition.getDataSource();
        insertData(tableName, dataSource);
        return tableInstance;
    }

    @Override
    public String getDatabaseName()
    {
        return databaseName;
    }

    @Override
    public Class<? extends TableDefinition> getTableDefinitionClass()
    {
        return RelationalTableDefinition.class;
    }

    private void insertData(TableName tableName, RelationalDataSource dataSource)
    {
        Iterator<List<Object>> dataRows = dataSource.getDataRows();
        if (!dataRows.hasNext()) {
            return;
        }
        try (Loader loader = new LoaderFactory().create(queryExecutor, tableName.getNameInDatabase())) {
            for (List<List<Object>> batch : partitionBy(dataRows, BATCH_SIZE)) {
                loader.load(batch);
            }
        }
        catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    public static Iterable<List<List<Object>>> partitionBy(Iterator<List<Object>> dataRows, int partitionSize)
    {
        return () -> new Iterator<List<List<Object>>>()
        {
            @Override
            public boolean hasNext()
            {
                return dataRows.hasNext();
            }

            @Override
            public List<List<Object>> next()
            {
                List<List<Object>> batch = new ArrayList<>();
                while (dataRows.hasNext() && batch.size() < partitionSize) {
                    batch.add(dataRows.next());
                }
                return batch;
            }
        };
    }
}
