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

import com.google.common.collect.ImmutableList;
import io.prestodb.tempto.query.QueryExecutor;

import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static io.prestodb.tempto.internal.fulfillment.table.jdbc.JdbcTableManager.partitionBy;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

class InsertLoader
        implements Loader
{
    private static final int ROWS_PER_INSERT = 100;

    private final QueryExecutor queryExecutor;
    private final String tableName;
    private final List<JDBCType> columnTypes;

    public InsertLoader(QueryExecutor queryExecutor, String tableName, List<JDBCType> columnTypes)
            throws SQLException
    {
        this.queryExecutor = requireNonNull(queryExecutor, "queryExecutor is null");
        this.tableName = requireNonNull(tableName, "tableName is null");
        this.columnTypes = ImmutableList.copyOf(requireNonNull(columnTypes, "columnTypes is null"));
    }

    @Override
    public void load(List<List<Object>> batch)
            throws SQLException
    {
        for (List<List<Object>> rowsToInsert : partitionBy(batch.iterator(), ROWS_PER_INSERT)) {
            if (rowsToInsert.isEmpty()) {
                continue;
            }
            queryExecutor.executeQuery(insertSql(rowsToInsert));
        }
    }

    private String insertSql(List<List<Object>> rows)
    {
        String values = rows.stream()
                .map(row -> {
                    checkArgument(
                            row.size() == columnTypes.size(),
                            "Row has different number of columns: %d vs %d",
                            row.size(),
                            columnTypes.size());
                    String rowValues = IntStream.range(0, row.size())
                            .mapToObj(i -> asStringValue(columnTypes.get(i), row.get(i)))
                            .collect(joining(","));

                    return "(" + rowValues + ")";
                })
                .collect(joining(","));

        return String.format("INSERT INTO %s VALUES %s", tableName, values);
    }

    private String asStringValue(JDBCType jdbcType, Object o)
    {
        if (o == null) {
            return "null";
        }
        switch (jdbcType) {
            case VARCHAR:
            case CHAR:
            case LONGNVARCHAR:
                return "'" + o.toString() + "'";
            case TINYINT:
            case SMALLINT:
            case INTEGER:
            case BIGINT:
            case FLOAT:
            case REAL:
            case DOUBLE:
            case DECIMAL:
                return o.toString();
            default:
                throw new IllegalArgumentException("Unsupported column type for INSERT: " + jdbcType);
        }
    }

    @Override
    public void close()
            throws SQLException
    {}
}
