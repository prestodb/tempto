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

package io.prestodb.tempto.query;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static java.sql.JDBCType.INTEGER;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Result of a query.
 * <p>
 * It stores all returned values, column names and their types as {@link java.sql.JDBCType}.
 */
public class QueryResult
{
    private final List<JDBCType> columnTypes;
    private final BiMap<String, Integer> columnNamesIndexes;
    private final List<List<Object>> values;
    private final Optional<ResultSet> jdbcResultSet;

    private QueryResult(List<JDBCType> columnTypes, BiMap<String, Integer> columnNamesIndexes, List<List<Object>> values, Optional<ResultSet> jdbcResultSet)
    {
        this.columnTypes = columnTypes;
        this.values = values;
        this.columnNamesIndexes = columnNamesIndexes;
        this.jdbcResultSet = jdbcResultSet;
    }

    public int getRowsCount()
    {
        return values.size();
    }

    public int getColumnsCount()
    {
        return columnTypes.size();
    }

    public List<JDBCType> getColumnTypes()
    {
        return columnTypes;
    }

    public JDBCType getColumnType(int columnIndex)
    {
        return columnTypes.get(fromSqlIndex(columnIndex));
    }

    public Optional<Integer> tryFindColumnIndex(String columnName)
    {
        return ofNullable(columnNamesIndexes.get(columnName));
    }

    public List<Object> row(int rowIndex)
    {
        return values.get(rowIndex);
    }

    public List<List<Object>> rows()
    {
        return values;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> column(int sqlColumnIndex)
    {
        int internalColumnIndex = fromSqlIndex(sqlColumnIndex);
        return (List) values.stream()
                .map(row -> row.get(internalColumnIndex))
                .collect(toList());
    }

    public QueryResult project(int... sqlColumnIndexes)
    {
        List<JDBCType> projectedColumnTypes = newArrayList();
        List<String> projectedColumnNames = newArrayList();
        for (int sqlColumnIndex : sqlColumnIndexes) {
            projectedColumnTypes.add(columnTypes.get(fromSqlIndex(sqlColumnIndex)));
            projectedColumnNames.add(columnNamesIndexes.inverse().get(sqlColumnIndex));
        }
        QueryResultBuilder queryResultBuilder = new QueryResultBuilder(projectedColumnTypes, projectedColumnNames);
        for (List<Object> valueList : values) {
            List<Object> projectedValueList = Lists.newArrayList();
            for (int sqlColumnIndex : sqlColumnIndexes) {
                projectedValueList.add(valueList.get(fromSqlIndex(sqlColumnIndex)));
            }
            queryResultBuilder.addRow(projectedValueList);
        }
        if (jdbcResultSet.isPresent()) {
            queryResultBuilder.setJdbcResultSet(jdbcResultSet.get());
        }
        return queryResultBuilder.build();
    }

    public Optional<ResultSet> getJdbcResultSet()
    {
        return jdbcResultSet;
    }

    /**
     * In SQL/JDBC column indexing starts form 1. This method returns SQL index for given Java index.
     *
     * @param index 0 based column index
     * @return index + 1
     */
    public static int toSqlIndex(int index)
    {
        return index + 1;
    }

    /**
     * In SQL/JDBC column indexing starts form 1. This method returns Java index for given SQL index.
     *
     * @param index 1 based column index
     * @return index - 1;
     */
    public static int fromSqlIndex(int index)
    {
        return index - 1;
    }

    public static QueryResultBuilder builder(ResultSetMetaData metaData)
            throws SQLException
    {
        return new QueryResultBuilder(metaData);
    }

    public static QueryResult forSingleIntegerValue(int value)
            throws SQLException
    {
        return new QueryResult(ImmutableList.of(INTEGER), HashBiMap.create(), ImmutableList.of(ImmutableList.of(value)), Optional.empty());
    }

    public static <T> QueryResult forSingleValue(JDBCType type, T value)
            throws SQLException
    {
        return new QueryResult(ImmutableList.of(type), HashBiMap.create(), ImmutableList.of(ImmutableList.of(value)), Optional.empty());
    }

    public static QueryResult empty()
    {
        return new QueryResult(ImmutableList.of(INTEGER), HashBiMap.create(), ImmutableList.of(), Optional.empty());
    }

    public static QueryResult forResultSet(ResultSet rs)
            throws SQLException
    {
        return QueryResult.builder(rs.getMetaData())
                .addRows(rs)
                .setJdbcResultSet(rs)
                .build();
    }

    public static class QueryResultBuilder
    {
        private final List<JDBCType> columnTypes = newArrayList();
        private final BiMap<String, Integer> columnNamesIndexes = HashBiMap.create();
        private final List<List<Object>> values = newArrayList();
        private Optional<ResultSet> jdbcResultSet = Optional.empty();

        QueryResultBuilder(ResultSetMetaData metaData)
                throws SQLException
        {
            for (int sqlColumnIndex = 1; sqlColumnIndex <= metaData.getColumnCount(); ++sqlColumnIndex) {
                columnTypes.add(JDBCType.valueOf(metaData.getColumnType(sqlColumnIndex)));
                columnNamesIndexes.put(metaData.getColumnName(sqlColumnIndex), sqlColumnIndex);
            }
        }

        public QueryResultBuilder(List<JDBCType> columnTypes, List<String> columnNames)
        {
            checkState(columnTypes.size() == columnNames.size(),
                    "inconsistent number of entries in columnTypes and columnNames lists %s != %s",
                    columnTypes.size(), columnNames.size());
            this.columnTypes.addAll(columnTypes);
            int sqlColumnIndex = 1;
            for (String columnName : columnNames) {
                columnNamesIndexes.put(columnName, sqlColumnIndex);
                sqlColumnIndex++;
            }
        }

        public QueryResultBuilder addRow(Object... rowValues)
        {
            return addRow(Arrays.asList(rowValues));
        }

        public QueryResultBuilder addRow(List<Object> rowValues)
        {
            Preconditions.checkState(rowValues.size() == columnTypes.size(), "expected %s objects", columnTypes.size());
            values.add(newArrayList(rowValues));
            return this;
        }

        public QueryResultBuilder addRows(ResultSet rs)
                throws SQLException
        {
            int columnCount = columnTypes.size();

            while (rs.next()) {
                List<Object> row = newArrayList();
                for (int sqlColumnIndex = 1; sqlColumnIndex <= columnCount; ++sqlColumnIndex) {
                    row.add(rs.getObject(sqlColumnIndex));
                }
                values.add(row);
            }
            return this;
        }

        public QueryResultBuilder setJdbcResultSet(ResultSet rs)
        {
            this.jdbcResultSet = Optional.of(rs);
            return this;
        }

        public QueryResult build()
        {
            return new QueryResult(columnTypes, columnNamesIndexes, values, jdbcResultSet);
        }
    }
}
