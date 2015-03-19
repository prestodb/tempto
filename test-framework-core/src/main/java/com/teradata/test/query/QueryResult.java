/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query;

import com.google.common.collect.ImmutableList;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.beust.jcommander.internal.Maps.newHashMap;
import static com.google.common.collect.Lists.newArrayList;
import static java.sql.JDBCType.INTEGER;
import static java.util.Collections.emptyMap;
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
    private final Map<String, Integer> columnNamesIndexes;
    private final List<List<Object>> values;

    private QueryResult(List<JDBCType> columnTypes, Map<String, Integer> columnNamesIndexes, List<List<Object>> values)
    {
        this.columnTypes = columnTypes;
        this.values = values;
        this.columnNamesIndexes = columnNamesIndexes;
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

    /**
     * In SQL/JDBC column indexing starts form 1. This method returns SQL index for given Java index.
     *
     * @return index + 1
     */
    public static int toSqlIndex(int index)
    {
        return index + 1;
    }

    /**
     * In SQL/JDBC column indexing starts form 1. This method returns Java index for given SQL index.
     *
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
        return new QueryResult(ImmutableList.of(INTEGER), emptyMap(), ImmutableList.of(ImmutableList.of(value)));
    }

    static class QueryResultBuilder
    {

        private final List<JDBCType> columnTypes = newArrayList();
        private final Map<String, Integer> columnNamesIndexes = newHashMap();
        private final List<List<Object>> values = newArrayList();

        private QueryResultBuilder(ResultSetMetaData metaData)
                throws SQLException
        {
            for (int sqlColumnIndex = 1; sqlColumnIndex <= metaData.getColumnCount(); ++sqlColumnIndex) {
                columnTypes.add(JDBCType.valueOf(metaData.getColumnType(sqlColumnIndex)));
                columnNamesIndexes.put(metaData.getColumnName(sqlColumnIndex), sqlColumnIndex);
            }
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

        public QueryResult build()
        {
            return new QueryResult(columnTypes, columnNamesIndexes, values);
        }
    }
}
