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

package io.prestodb.tempto.assertions;

import com.google.common.base.Joiner;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.internal.convention.SqlResultDescriptor;
import io.prestodb.tempto.internal.query.QueryResultValueComparator;
import io.prestodb.tempto.internal.query.QueryRowMapper;
import io.prestodb.tempto.query.QueryExecutionException;
import io.prestodb.tempto.query.QueryExecutor;
import io.prestodb.tempto.query.QueryResult;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;

import java.sql.JDBCType;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static io.prestodb.tempto.assertions.QueryAssert.Row.row;
import static io.prestodb.tempto.internal.configuration.TestConfigurationFactory.testConfiguration;
import static io.prestodb.tempto.query.QueryResult.fromSqlIndex;
import static io.prestodb.tempto.query.QueryResult.toSqlIndex;
import static java.lang.String.format;
import static java.sql.JDBCType.INTEGER;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

public class QueryAssert
        extends AbstractAssert<QueryAssert, QueryResult>
{
    private static final Logger LOGGER = getLogger(QueryExecutor.class);

    private static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("#0.00000000000");

    private final List<Comparator<Object>> columnComparators;
    private final List<JDBCType> columnTypes;

    private QueryAssert(QueryResult actual)
    {
        super(actual, QueryAssert.class);
        this.columnComparators = getComparators(actual);
        this.columnTypes = actual.getColumnTypes();
    }

    public static QueryAssert assertThat(QueryResult queryResult)
    {
        return new QueryAssert(queryResult);
    }

    public static QueryExecutionAssert assertThat(QueryCallback queryCallback)
    {
        QueryExecutionException executionException = null;
        try {
            queryCallback.executeQuery();
        }
        catch (QueryExecutionException e) {
            executionException = e;
        }
        return new QueryExecutionAssert(ofNullable(executionException));
    }

    public QueryAssert matches(SqlResultDescriptor sqlResultDescriptor)
    {
        if (sqlResultDescriptor.getExpectedTypes().isPresent()) {
            hasColumns(sqlResultDescriptor.getExpectedTypes().get());
        }

        List<Row> rows = null;
        try {
            rows = sqlResultDescriptor.getRows(columnTypes);
        }
        catch (Exception e) {
            failWithMessage("Could not map expected file content to query column types; types=%s; content=<%s>; error=<%s>",
                    columnTypes, sqlResultDescriptor.getOriginalContent(), e.getMessage());
        }

        if (sqlResultDescriptor.isIgnoreOrder()) {
            contains(rows);
        }
        else {
            containsExactly(rows);
        }

        if (!sqlResultDescriptor.isIgnoreExcessRows()) {
            hasRowsCount(rows.size());
        }

        return this;
    }

    public QueryAssert hasRowsCount(int resultCount)
    {
        if (actual.getRowsCount() != resultCount) {
            failWithMessage("Expected row count to be <%s>, but was <%s>; rows=%s", resultCount, actual.getRowsCount(), actual.rows());
        }
        return this;
    }

    public QueryAssert hasNoRows()
    {
        return hasRowsCount(0);
    }

    public QueryAssert hasAnyRows()
    {
        if (actual.getRowsCount() == 0) {
            failWithMessage("Expected some rows to be returned from query");
        }
        return this;
    }

    public QueryAssert hasColumnsCount(int columnCount)
    {
        if (actual.getColumnsCount() != columnCount) {
            failWithMessage("Expected column count to be <%s>, but was <%s> - columns <%s>", columnCount, actual.getColumnsCount(), actual.getColumnTypes());
        }
        return this;
    }

    public QueryAssert hasColumns(List<JDBCType> expectedTypes)
    {
        hasColumnsCount(expectedTypes.size());
        for (int i = 0; i < expectedTypes.size(); i++) {
            JDBCType expectedType = expectedTypes.get(i);
            JDBCType actualType = actual.getColumnType(toSqlIndex(i));

            if (!actualType.equals(expectedType)) {
                failWithMessage("Expected <%s> column of type <%s>, but was <%s>, actual columns: %s", i, expectedType, actualType, actual.getColumnTypes());
            }
        }
        return this;
    }

    public QueryAssert hasColumns(JDBCType... expectedTypes)
    {
        return hasColumns(Arrays.asList(expectedTypes));
    }

    /**
     * Verifies that the actual result set contains all the given {@code rows}
     *
     * @param rows Rows to be matched
     * @return this
     */
    public QueryAssert contains(List<Row> rows)
    {
        List<List<Object>> missingRows = newArrayList();
        for (Row row : rows) {
            List<Object> expectedRow = row.getValues();

            if (!containsRow(expectedRow)) {
                missingRows.add(expectedRow);
            }
        }

        if (!missingRows.isEmpty()) {
            failWithMessage(buildContainsMessage(missingRows));
        }

        return this;
    }

    /**
     * @param rows Rows to be matched
     * @return this
     * @see #contains(java.util.List)
     */
    public QueryAssert contains(Row... rows)
    {
        return contains(Arrays.asList(rows));
    }

    /**
     * Verifies that the actual result set consist of only {@code rows} in any order
     *
     * @param rows Rows to be matched
     * @return this
     */
    public QueryAssert containsOnly(List<Row> rows)
    {
        hasRowsCount(rows.size());
        contains(rows);

        return this;
    }

    /**
     * @param rows Rows to be matched
     * @return this
     * @see #containsOnly(java.util.List)
     */
    public QueryAssert containsOnly(Row... rows)
    {
        return containsOnly(Arrays.asList(rows));
    }

    /**
     * Verifies that the actual result set equals to {@code rows}.
     * ResultSet in different order or with any extra rows perceived as not same
     *
     * @param rows Rows to be matched
     * @return this
     */
    public QueryAssert containsExactly(List<Row> rows)
    {
        hasRowsCount(rows.size());
        List<Integer> unequalRowsIndexes = newArrayList();
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            List<Object> expectedRow = rows.get(rowIndex).getValues();
            List<Object> actualRow = actual.row(rowIndex);

            if (!rowsEqual(expectedRow, actualRow)) {
                unequalRowsIndexes.add(rowIndex);
            }
        }

        if (!unequalRowsIndexes.isEmpty()) {
            failWithMessage(buildContainsExactlyErrorMessage(unequalRowsIndexes, rows));
        }

        return this;
    }

    /**
     * @param rows Rows to be matched
     * @return this
     * @see #containsExactly(java.util.List)
     */
    public QueryAssert containsExactly(Row... rows)
    {
        return containsExactly(Arrays.asList(rows));
    }

    /**
     * Verifies number of rows updated/inserted by last update query
     *
     * @param count Number of rows expected
     * @return this
     */
    public QueryAssert updatedRowsCountIsEqualTo(int count)
    {
        hasRowsCount(1);
        hasColumnsCount(1);
        hasColumns(INTEGER);
        containsExactly(row(count));
        return this;
    }

    private static List<Comparator<Object>> getComparators(QueryResult queryResult)
    {
        Configuration configuration = testConfiguration();
        return queryResult.getColumnTypes().stream()
                .map(it -> QueryResultValueComparator.comparatorForType(it, configuration))
                .collect(Collectors.toList());
    }

    private String buildContainsMessage(List<List<Object>> missingRows)
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

    private String buildContainsExactlyErrorMessage(List<Integer> unequalRowsIndexes, List<Row> rows)
    {
        StringBuilder msg = new StringBuilder("Not equal rows:");
        for (Integer unequalRowsIndex : unequalRowsIndexes) {
            int unequalRowIndex = unequalRowsIndex;
            msg.append('\n');
            msg.append(unequalRowIndex);
            msg.append(" - expected: ");
            msg.append(rows.get(unequalRowIndex));
            msg.append('\n');
            msg.append(unequalRowIndex);
            msg.append(" - actual:   ");
            msg.append(new Row(actual.row(unequalRowIndex)));
        }
        return msg.toString();
    }

    private boolean containsRow(List<Object> expectedRow)
    {
        for (int i = 0; i < actual.getRowsCount(); i++) {
            if (rowsEqual(expectedRow, actual.row(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean rowsEqual(List<Object> expectedRow, List<Object> actualRow)
    {
        if (expectedRow.size() != actualRow.size()) {
            return false;
        }
        for (int i = 0; i < expectedRow.size(); ++i) {
            List<Object> acceptableValues = expectedRow.get(i) instanceof AcceptableValues ?
                    ((AcceptableValues) expectedRow.get(i)).getValues()
                    : singletonList(expectedRow.get(i));
            Object actualValue = actualRow.get(i);

            if (!isAnyValueEqual(i, acceptableValues, actualValue)) {
                return false;
            }
        }
        return true;
    }

    private boolean isAnyValueEqual(int column, List<Object> expectedValues, Object actualValue)
    {
        for (Object expectedValue : expectedValues) {
            if (columnComparators.get(column).compare(actualValue, expectedValue) == 0) {
                return true;
            }
        }
        return false;
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

    public static AcceptableValues anyOf(Object... values)
    {
        return new AcceptableValues(Arrays.asList(values));
    }

    @FunctionalInterface
    public static interface QueryCallback
    {
        QueryResult executeQuery()
                throws QueryExecutionException;
    }

    public static class QueryExecutionAssert
    {
        private Optional<QueryExecutionException> executionExceptionOptional;

        public QueryExecutionAssert(Optional<QueryExecutionException> executionExceptionOptional)
        {
            this.executionExceptionOptional = executionExceptionOptional;
        }

        private String getFailureMessage()
        {
            QueryExecutionException executionException = executionExceptionOptional
                    .orElseThrow(() -> new AssertionError("Query did not fail as expected."));
            return nullToEmpty(executionException.getMessage());
        }

        public QueryExecutionAssert failsWithMessage(String expectedErrorMessage)
        {
            String exceptionMessage = getFailureMessage();
            LOGGER.debug("Query failed as expected, with message: {}", exceptionMessage);
            if (!exceptionMessage.contains(expectedErrorMessage)) {
                throw new AssertionError(format(
                        "Query failed with unexpected error message: '%s' \n Expected error message to contain '%s'",
                        exceptionMessage,
                        expectedErrorMessage));
            }

            return this;
        }

        public QueryExecutionAssert failsWithMessageMatching(String expectedErrorMessagePattern)
        {
            requireNonNull(expectedErrorMessagePattern, "expectedErrorMessagePattern is null");
            String exceptionMessage = getFailureMessage();
            LOGGER.debug("Query failed as expected, with message: {}", exceptionMessage);
            if (!exceptionMessage.matches(expectedErrorMessagePattern)) {
                throw new AssertionError(format(
                        "Query failed with unexpected error message: '%s' \n Expected error message to match '%s'",
                        exceptionMessage,
                        expectedErrorMessagePattern));
            }

            return this;
        }
    }

    public static class Row
    {
        private final List<Object> values;

        public Row(Object... values)
        {
            this(newArrayList(values));
        }

        public Row(List<Object> values)
        {
            this.values = requireNonNull(values, "values is null");
        }

        public List<Object> getValues()
        {
            return values;
        }

        public static Row row(Object... values)
        {
            return new Row(values);
        }

        @Override
        public String toString()
        {
            StringBuilder msg = new StringBuilder();
            for (Object value : values) {
                if (value instanceof Double || value instanceof Float) {
                    msg.append(DECIMAL_FORMAT.format(value));
                }
                else if (value == null) {
                    msg.append("null");
                }
                else {
                    msg.append(value.toString());
                }
                msg.append('|');
            }
            return msg.toString();
        }
    }

    public static class AcceptableValues
    {
        private final List<Object> values;

        public AcceptableValues(List<Object> values)
        {
            this.values = unmodifiableList(new ArrayList<>(requireNonNull(values, "values can not be null")));
        }

        public List<Object> getValues()
        {
            return values;
        }

        @Override
        public String toString()
        {
            String jointValues = Joiner.on(", ")
                    .useForNull(QueryRowMapper.NULL_STRING)
                    .join(values);
            return "anyOf(" + jointValues + ")";
        }
    }
}
