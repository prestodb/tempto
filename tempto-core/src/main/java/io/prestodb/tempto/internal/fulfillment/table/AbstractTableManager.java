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
package io.prestodb.tempto.internal.fulfillment.table;

import com.google.common.collect.ImmutableList;
import io.prestodb.tempto.fulfillment.table.TableDefinition;
import io.prestodb.tempto.fulfillment.table.TableHandle;
import io.prestodb.tempto.fulfillment.table.TableManager;
import io.prestodb.tempto.query.QueryExecutionException;
import io.prestodb.tempto.query.QueryExecutor;
import org.slf4j.Logger;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static io.prestodb.tempto.fulfillment.table.TableHandle.tableHandle;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractTableManager<T extends TableDefinition>
        implements TableManager<T>
{
    private static final Logger LOGGER = getLogger(AbstractTableManager.class);

    private final QueryExecutor queryExecutor;
    private final TableNameGenerator tableNameGenerator;
    private boolean staleMutableTablesDropped = false;

    public AbstractTableManager(QueryExecutor queryExecutor, TableNameGenerator tableNameGenerator)
    {
        this.queryExecutor = requireNonNull(queryExecutor, "queryExecutor is null");
        this.tableNameGenerator = requireNonNull(tableNameGenerator, "tableNameGenerator is null");
    }

    @Override
    public void dropStaleMutableTables()
    {
        if (!staleMutableTablesDropped) {
            getTableNames().stream()
                    .filter(tableNameGenerator::isMutableTableName)
                    .forEach(tableName -> dropTableIgnoreError(createMutableTableName(tableHandle(tableName))));
        }
        staleMutableTablesDropped = true;
    }

    private List<String> getTableNames()
    {
        try {
            ImmutableList.Builder<String> tableNames = ImmutableList.builder();
            DatabaseMetaData metaData = queryExecutor.getConnection().getMetaData();
            try (ResultSet tables = metaData.getTables(null, null, null, null)) {
                while (tables.next()) {
                    tableNames.add(tables.getString("TABLE_NAME"));
                }
            }
            return tableNames.build();
        }
        catch (SQLException e) {
            LOGGER.debug("Unable to list table names", e);
            return ImmutableList.of();
        }
    }

    protected void dropTableIgnoreError(TableName tableName)
    {
        try {
            dropTable(tableName);
        }
        catch (QueryExecutionException ignored) {
            LOGGER.debug("{} - unable to drop table: {}", getDatabaseName(), tableName);
        }
    }

    @Override
    public void dropTable(TableName tableName)
    {
        queryExecutor.executeQuery("DROP TABLE " + tableName.getNameInDatabase());
    }

    protected TableName createMutableTableName(TableHandle tableHandle)
    {
        String nameInDatabase = tableNameGenerator.generateMutableTableNameInDatabase(tableHandle.getName());
        return new TableName(
                tableHandle.getDatabase().orElse(getDatabaseName()),
                tableHandle.getSchema(),
                tableHandle.getName(),
                nameInDatabase
        );
    }

    protected TableName createImmutableTableName(TableHandle tableHandle)
    {
        return new TableName(
                tableHandle.getDatabase().orElse(getDatabaseName()),
                tableHandle.getSchema(),
                tableHandle.getName(),
                tableHandle.getName()
        );
    }

    @Override
    public void close()
    {
        queryExecutor.close();
    }
}
