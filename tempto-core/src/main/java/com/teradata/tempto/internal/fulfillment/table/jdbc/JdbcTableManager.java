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

package com.teradata.tempto.internal.fulfillment.table.jdbc;

import com.google.inject.Inject;
import com.teradata.tempto.fulfillment.table.MutableTableRequirement.State;
import com.teradata.tempto.fulfillment.table.TableDefinition;
import com.teradata.tempto.fulfillment.table.TableHandle;
import com.teradata.tempto.fulfillment.table.TableInstance;
import com.teradata.tempto.fulfillment.table.TableManager;
import com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDataSource;
import com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDefinition;
import com.teradata.tempto.internal.fulfillment.table.AbstractTableManager;
import com.teradata.tempto.internal.fulfillment.table.TableName;
import com.teradata.tempto.internal.fulfillment.table.TableNameGenerator;
import com.teradata.tempto.query.QueryExecutor;
import org.slf4j.Logger;

import javax.inject.Named;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.teradata.tempto.fulfillment.table.MutableTableRequirement.State.CREATED;
import static com.teradata.tempto.fulfillment.table.MutableTableRequirement.State.LOADED;
import static com.teradata.tempto.fulfillment.table.MutableTableRequirement.State.PREPARED;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.slf4j.LoggerFactory.getLogger;

@TableManager.Descriptor(tableDefinitionClass = JdbcTableDefinition.class, type = "JDBC")
public class JdbcTableManager
        extends AbstractTableManager<JdbcTableDefinition>
{
    private static final int BATCH_SIZE = 10000;
    private static final Logger LOGGER = getLogger(JdbcTableManager.class);

    private final QueryExecutor queryExecutor;
    private final String databaseName;

    @Inject
    public JdbcTableManager(
            QueryExecutor queryExecutor,
            TableNameGenerator tableNameGenerator,
            @Named("databaseName") String databaseName)
    {
        super(queryExecutor, tableNameGenerator);
        this.databaseName = databaseName;
        this.queryExecutor = checkNotNull(queryExecutor, "queryExecutor is null");
    }

    @Override
    public TableInstance createImmutable(JdbcTableDefinition tableDefinition, TableHandle tableHandle)
    {
        TableName tableName = createImmutableTableName(tableHandle);
        LOGGER.debug("creating immutable table {}", tableName);
        dropTableIgnoreError(tableName); // ultimately we do not want to drop table here - if data did not change
        createTable(tableDefinition, tableName);
        JdbcTableDataSource dataSource = tableDefinition.getDataSource();
        insertData(tableName, dataSource);
        return new JdbcTableInstance(tableName, tableDefinition);
    }

    private void createTable(JdbcTableDefinition tableDefinition, TableName tableName)
    {
        tableName.getSchema().ifPresent(schema ->
                        queryExecutor.executeQuery("CREATE SCHEMA IF NOT EXISTS " + schema)
        );
        queryExecutor.executeQuery(tableDefinition.getCreateTableDDL(tableName.getNameInDatabase()));
    }

    private PreparedStatement buildPreparedStatementForInsert(String tableName, int columnsCount)
            throws SQLException
    {
        String questionMarks = range(0, columnsCount).mapToObj(i -> "?").collect(joining(","));
        return queryExecutor.getConnection().prepareStatement(
                format("INSERT INTO %s VALUES (%s)", tableName, questionMarks));
    }

    @Override
    public TableInstance createMutable(JdbcTableDefinition tableDefinition, State state, TableHandle tableHandle)
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

        JdbcTableDataSource dataSource = tableDefinition.getDataSource();
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
        return JdbcTableDefinition.class;
    }

    private void insertData(TableName tableName, JdbcTableDataSource dataSource)
    {
        Iterator<List<Object>> dataRows = dataSource.getDataRows();
        PreparedStatement preparedStatement = null;
        List<List<Object>> batchRows = new ArrayList<>();
        try {
            int rowsInserted = 0;
            while (dataRows.hasNext()) {
                List<Object> values = dataRows.next();
                if (preparedStatement == null) {
                    preparedStatement = buildPreparedStatementForInsert(tableName.getNameInDatabase(), values.size());
                }
                int pos = 1;
                for (Object value : values) {
                    preparedStatement.setObject(pos, value);
                    ++pos;
                }
                batchRows.add(unmodifiableList(newArrayList(values)));
                preparedStatement.addBatch();
                if (++rowsInserted % BATCH_SIZE == 0) {
                    executeBatchWithVerification(preparedStatement, rowsInserted - batchRows.size(), batchRows);
                    batchRows.clear();
                }
            }
            if (preparedStatement != null) {
                executeBatchWithVerification(preparedStatement, rowsInserted - batchRows.size(), batchRows);
                batchRows.clear();
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                }
                catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void executeBatchWithVerification(PreparedStatement preparedStatement, int baseBatchRowIndex, List<List<Object>> batchRows)
            throws SQLException
    {
        checkNotNull(preparedStatement);
        int[] insertCounts = preparedStatement.executeBatch();
        for (int rowIndex = 0; rowIndex < insertCounts.length; ++rowIndex) {
            if (insertCounts[rowIndex] != 1) {
                throw new RuntimeException("could not insert values for row " + (baseBatchRowIndex + rowIndex) + "; values=" + batchRows.get(rowIndex));
            }
        }
    }
}
