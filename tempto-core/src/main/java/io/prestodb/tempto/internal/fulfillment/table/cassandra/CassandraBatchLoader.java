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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class CassandraBatchLoader
{
    private final CqlSession session;
    private final String insertQuery;
    private final int columnsCount;
    private final int batchRowsCount;

    public CassandraBatchLoader(CqlSession session, String tableName, List<String> columnNames, int batchRowsCount)
    {
        this.session = requireNonNull(session, "session is null");
        requireNonNull(tableName, "tableName is null");
        requireNonNull(columnNames, "columnNames is null");
        this.insertQuery = createInsertQuery(tableName, columnNames);
        this.columnsCount = columnNames.size();
        checkArgument(batchRowsCount > 0, "batchRowsCount must be greater then zero");
        this.batchRowsCount = batchRowsCount;
    }

    private static String createInsertQuery(String tableName, List<String> columnNames)
    {
        return format("INSERT INTO %s (%s) VALUES(%s)",
                tableName,
                columnNames.stream().collect(joining(",")),
                repeatPattern("?", ",", columnNames.size()));
    }

    private static String repeatPattern(String ch, String separator, int times)
    {
        String result = "";

        for (int i = 0; i < times - 1; ++i) {
            result += ch + separator;
        }

        return result + ch;
    }

    public void load(Iterator<List<Object>> rows)
    {
        PreparedStatement statement = session.prepare(insertQuery);

        BatchStatementBuilder batchBuilder = createBatchStatementBuilder();
        int currentBatchSize = 0;
        
        while (rows.hasNext()) {
            if (currentBatchSize >= batchRowsCount) {
                session.execute(batchBuilder.build());
                batchBuilder = createBatchStatementBuilder();
                currentBatchSize = 0;
            }
            List<Object> row = rows.next();
            checkState(row.size() == columnsCount, "values count in a row is expected to be %d, but found: %d", columnsCount, row.size());
            batchBuilder.addStatement(statement.bind(row.toArray()));
            currentBatchSize++;
        }

        if (currentBatchSize > 0) {
            session.execute(batchBuilder.build());
        }
    }

    private static BatchStatementBuilder createBatchStatementBuilder()
    {
        return BatchStatement.builder(DefaultBatchType.UNLOGGED);
    }
}
