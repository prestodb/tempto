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

import io.prestodb.tempto.query.QueryExecutor;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.joining;

class BatchLoader
        implements Loader
{
    private final PreparedStatement preparedStatement;
    private final int columnsCount;

    public BatchLoader(QueryExecutor queryExecutor, String tableName, int columnsCount)
            throws SQLException
    {
        String questionMarks = IntStream.range(0, columnsCount)
                .mapToObj(i -> "?")
                .collect(joining(","));
        preparedStatement = queryExecutor.getConnection()
                .prepareStatement(String.format("INSERT INTO %s VALUES (%s)", tableName, questionMarks));
        this.columnsCount = columnsCount;
    }

    @Override
    public void load(List<List<Object>> batch)
            throws SQLException
    {
        if (batch.size() == 0) {
            return;
        }
        for (List<Object> row : batch) {
            checkArgument(row.size() == columnsCount, "Unexpected columns count: %d vs %d", row.size(), columnsCount);
            int pos = 1;
            for (Object value : row) {
                preparedStatement.setObject(pos, value);
                ++pos;
            }
            preparedStatement.addBatch();
        }
        int[] insertCounts = preparedStatement.executeBatch();
        for (int rowIndex = 0; rowIndex < insertCounts.length; ++rowIndex) {
            if (insertCounts[rowIndex] != 1) {
                throw new RuntimeException("could not insert values=" + batch.get(rowIndex));
            }
        }
    }

    @Override
    public void close()
            throws SQLException
    {
        if (preparedStatement != null) {
            preparedStatement.close();
        }
    }
}
