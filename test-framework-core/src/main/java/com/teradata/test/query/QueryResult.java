/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query;

import java.sql.JDBCType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.beust.jcommander.internal.Maps.newHashMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

/**
 * Result of a query.
 * <p/>
 * It stores all returned values, column names and their types as {@link java.sql.JDBCType}.
 */
public class QueryResult
{

    private final List<JDBCType> columnTypes;
    private final Map<String, Integer> columnNamesIndexes;
    private final List<List<?>> values;

    public QueryResult(List<JDBCType> columnTypes, List<String> names, List<List<?>> values)
    {
        this.columnTypes = columnTypes;
        this.values = values;
        this.columnNamesIndexes = newHashMap();
        for (int i = 0; i < names.size(); i++) {
            columnNamesIndexes.put(names.get(i), toSqlIndex(i));
        }
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

    @SuppressWarnings("unchecked")
    public List<Object> row(int rowIndex)
    {
        return (List) values.get(rowIndex);
    }

    @SuppressWarnings("unchecked")
    public List<List<Object>> rows()
    {
        return (List) values;
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
}
