/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.assertions;

import com.teradata.test.query.QueryResult;
import com.teradata.test.query.QueryResultValueComparator;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import java.sql.JDBCType;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.teradata.test.query.QueryResult.fromSqlIndex;
import static com.teradata.test.query.QueryResult.toSqlIndex;

public class QueryAssert
        extends AbstractAssert<QueryAssert, QueryResult>
{

    private final List<Comparator<Object>> columnComparators;

    protected QueryAssert(QueryResult actual, List<Comparator<Object>> columnComparators)
    {
        super(actual, QueryAssert.class);
        this.columnComparators = columnComparators;
    }

    public static QueryAssert assertThat(QueryResult queryResult)
    {
        List<Comparator<Object>> comparators = queryResult.getColumnTypes().stream()
                .map(QueryResultValueComparator::comparatorForType)
                .collect(Collectors.toList());

        return new QueryAssert(queryResult, comparators);
    }

    public QueryAssert hasRowsCount(int resultCount)
    {
        if (actual.getRowsCount() != resultCount) {
            failWithMessage("Expected result count to be <%s>, but was <%s>", resultCount, actual.getRowsCount());
        }
        return this;
    }

    public QueryAssert hasColumnsCount(int columnCount)
    {
        if (actual.getColumnsCount() != columnCount) {
            failWithMessage("Expected column count to be <%s>, but was <%s>", columnCount, actual.getRowsCount());
        }
        return this;
    }

    public QueryAssert hasColumns(JDBCType... expectedTypes)
    {
        hasColumnsCount(expectedTypes.length);
        for (int i = 0; i < expectedTypes.length; i++) {
            JDBCType expectedType = expectedTypes[i];
            JDBCType actualType = actual.getColumnType(toSqlIndex(i));

            if (!actualType.equals(expectedType)) {
                failWithMessage("Expected <%s> column of type <%s>, but was <%s>", i, expectedType, actualType);
            }
        }
        return this;
    }

    public QueryAssert hasRows(Row... rows)
    {
        hasRowsCount(rows.length);
        List<List<Object>> missingRows = newArrayList();
        for (Row row : rows) {
            List<Object> expectedRow = row.getValues();

            if (!containsRow(expectedRow)) {
                missingRows.add(expectedRow);
            }
        }

        if (!missingRows.isEmpty()) {
            failWithMessage(buildHasRowsErrorMessage(missingRows));
        }

        return this;
    }

    public QueryAssert hasRowsInOrder(Row... rows)
    {
        hasRowsCount(rows.length);
        List<Integer> unequalRowsIndexes = newArrayList();
        for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
            List<Object> expectedRow = rows[rowIndex].getValues();
            List<Object> actualRow = actual.row(rowIndex);

            if (!rowsEqual(expectedRow, actualRow)) {
                unequalRowsIndexes.add(rowIndex);
            }
        }

        if (!unequalRowsIndexes.isEmpty()) {
            failWithMessage(buildHasRowsInOrderErrorMessage(unequalRowsIndexes, rows));
        }

        return this;
    }

    private String buildHasRowsErrorMessage(List<List<Object>> missingRows)
    {
        StringBuilder msg = new StringBuilder("Could not find rows:");
        appendRows(msg, missingRows);
        msg.append("\n\nactual rows:");
        appendRows(msg, actual.rows());
        return msg.toString();
    }

    private void appendRows(StringBuilder msg, List<List<Object>> rows)
    {
        rows.stream().forEach(row -> msg.append('\n').append(row));
    }

    private String buildHasRowsInOrderErrorMessage(List<Integer> unequalRowsIndexes, Row[] rows)
    {
        StringBuilder msg = new StringBuilder("Not equal rows:");
        for (int i = 0; i < unequalRowsIndexes.size(); ++i) {
            int unequalRowIndex = unequalRowsIndexes.get(i);
            msg.append('\n').append(unequalRowIndex)
                    .append(" - expected: ")
                    .append(rows[unequalRowIndex].getValues())
                    .append(", actual: ")
                    .append(actual.row(unequalRowIndex));
        }
        return msg.toString();
    }

    private boolean containsRow(List<Object> expectedRow)
    {
        for (int i = 0; i < actual.getRowsCount(); i++) {
            if (rowsEqual(actual.row(i), expectedRow)) {
                return true;
            }
        }
        return false;
    }

    private boolean rowsEqual(List<Object> expectedRow, List<Object> actualRow)
    {
        for (int i = 0; i < expectedRow.size(); ++i) {
            Object expectedValue = expectedRow.get(i);
            Object actualValue = actualRow.get(i);

            if (columnComparators.get(i).compare(actualValue, expectedValue) != 0) {
                return false;
            }
        }
        return true;
    }

    public <T> QueryAssert column(int columnIndex, JDBCType type, ColumnValuesAssert<T> columnValuesAssert)
    {
        if (fromSqlIndex(columnIndex) > actual.getColumnsCount()) {
            failWithMessage("Result contains only <%s> columns, extracting column <%s>",
                    actual.getColumnsCount(), columnIndex);
        }

        JDBCType actualColumnType = actual.getColumnType(columnIndex);
        if (!type.equals(actualColumnType)) {
            failWithMessage("Expected <%s> column, to be type: <%s>, but was: <%s>", columnIndex, type, actualColumnType);
        }

        List<T> columnValues = actual.column(columnIndex);

        columnValuesAssert.assertColumnValues(Assertions.assertThat(columnValues));

        return this;
    }

    public <T> QueryAssert column(String columnName, JDBCType type, ColumnValuesAssert<T> columnValuesAssert)
    {
        Optional<Integer> index = actual.tryFindColumnIndex(columnName);
        if (!index.isPresent()) {
            failWithMessage("No column with name: <%s>", columnName);
        }

        return column(index.get(), type, columnValuesAssert);
    }

    public static class Row
    {

        private final List<Object> values;

        private Row(Object... values)
        {
            this.values = newArrayList(values);
        }

        public List<Object> getValues()
        {
            return values;
        }

        public static Row row(Object... values)
        {
            return new Row(values);
        }
    }
}
