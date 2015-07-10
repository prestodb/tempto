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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.teradata.tempto.fulfillment.table.MutableTableRequirement;
import com.teradata.tempto.fulfillment.table.TableInstance;
import com.teradata.tempto.fulfillment.table.TableManager;
import com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDataSource;
import com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDefinition;
import com.teradata.tempto.internal.fulfillment.table.TableNameGenerator;
import com.teradata.tempto.internal.uuid.UUIDGenerator;
import com.teradata.tempto.query.QueryExecutionException;
import com.teradata.tempto.query.QueryExecutor;
import org.slf4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.copyOf;
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
        implements TableManager<JdbcTableDefinition>
{
    private static final int BATCH_SIZE = 10000;
    private static final Logger LOGGER = getLogger(JdbcTableManager.class);

    private final QueryExecutor queryExecutor;
    private final TableNameGenerator tableNameGenerator;
    @Inject
    public JdbcTableManager(
            QueryExecutor queryExecutor,
            TableNameGenerator tableNameGenerator)
    {
        this.queryExecutor = checkNotNull(queryExecutor, "queryExecutor is null");
        this.tableNameGenerator = checkNotNull(tableNameGenerator, "tableNameGenerator is null");
    }

    @Override
    public TableInstance createImmutable(JdbcTableDefinition tableDefinition)
    {
        String tableNameInDatabase = tableDefinition.getName();
        LOGGER.debug("creating immutable table {}", tableNameInDatabase);
        dropTableIgnoreError(tableNameInDatabase); // ultimately we do not want to drop table here - if data did not change
        String createTableDDL = tableDefinition.getCreateTableDDL(tableNameInDatabase);
        queryExecutor.executeQuery(createTableDDL);
        JdbcTableDataSource dataSource = tableDefinition.getDataSource();
        insertData(tableNameInDatabase, dataSource);
        return new JdbcTableInstance(tableNameInDatabase, tableNameInDatabase, tableDefinition);
    }

    private void dropTableIgnoreError(String tableNameInDatabase)
    {
        try {
            queryExecutor.executeQuery("DROP TABLE " + tableNameInDatabase);
        }
        catch (QueryExecutionException e) {
            // ignore
        }
    }

    private PreparedStatement buildPreparedStatementForInsert(String tableName, int columnsCount)
            throws SQLException
    {

        String questionMarks = range(0, columnsCount).mapToObj(i -> "?").collect(joining(","));
        return queryExecutor.getConnection().prepareStatement(
                format("INSERT INTO %s VALUES (%s)", tableName, questionMarks));
    }

    @Override
    public TableInstance createMutable(JdbcTableDefinition tableDefinition, MutableTableRequirement.State state)
    {
        String tableNameInDatabase = tableNameGenerator.generateUniqueTableNameInDatabase(tableDefinition);
        LOGGER.debug("creating mutable table {}, name in database: {}", tableDefinition.getName(), tableNameInDatabase);
        JdbcTableInstance tableInstance = new JdbcTableInstance(tableDefinition.getName(), tableNameInDatabase, tableDefinition);
        if (state == PREPARED) {
            return tableInstance;
        }

        String createTableDDL = tableDefinition.getCreateTableDDL(tableNameInDatabase);
        queryExecutor.executeQuery(createTableDDL);

        if (state == CREATED) {
            return tableInstance;
        }

        assert state == LOADED;

        JdbcTableDataSource dataSource = tableDefinition.getDataSource();
        insertData(tableNameInDatabase, dataSource);
        return tableInstance;
    }

    @Override
    public void dropAllTables()
    {
        // not implementing since we are thinking of changing the flow here
    }

    private void insertData(String tableNameInDatabase, JdbcTableDataSource dataSource)
    {
        Iterator<List<Object>> dataRows = dataSource.getDataRows();
        PreparedStatement preparedStatement = null;
        List<List<Object>> batchRows = new ArrayList<>();
        try {
            int rowsInserted = 0;
            while (dataRows.hasNext()) {
                List<Object> values = dataRows.next();
                if (preparedStatement == null) {
                    preparedStatement = buildPreparedStatementForInsert(tableNameInDatabase, values.size());
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
            executeBatchWithVerification(preparedStatement, rowsInserted - batchRows.size(), batchRows);
            batchRows.clear();
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
        int[] insertCounts = preparedStatement.executeBatch();
        for (int rowIndex = 0; rowIndex < insertCounts.length; ++rowIndex) {
            if (insertCounts[rowIndex] != 1) {
                throw new RuntimeException("could not insert values for row " + (baseBatchRowIndex + rowIndex) + "; values=" + batchRows.get(rowIndex));
            }
        }
    }
}
