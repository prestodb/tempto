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

package io.prestodb.tempto.internal.convention;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import io.prestodb.tempto.assertions.QueryAssert.Row;
import io.prestodb.tempto.internal.convention.AnnotatedFileParser.SectionParsingResult;
import io.prestodb.tempto.internal.query.QueryRowMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.JDBCType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.io.Resources.getResource;
import static io.prestodb.tempto.assertions.QueryAssert.Row.row;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class SqlResultDescriptor
        extends SqlDescriptor
{
    private static final String DEFAULT_COLUMN_DELIMITER = "|";

    private static final String DEFAULT_IGNORE_ORDER = "false";
    private static final String DEFAULT_IGNORE_EXCESS = "false";
    private static final String DEFAULT_TRIM_VALUES = "false";
    private static final String JOIN_ALL_VALUES_TO_ONE = "false";
    private static final Splitter TYPES_SPLITTER = Splitter.on('|');

    private final Optional<List<JDBCType>> expectedTypes;

    public static SqlResultDescriptor sqlResultDescriptorForResource(String resourceName)
    {
        try {
            return sqlResultDescriptorFor(getResource(resourceName).openStream());
        }
        catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static SqlResultDescriptor sqlResultDescriptorFor(Path resultDescriptorFile)
    {
        return new SqlResultDescriptor(getOnlyElement(new AnnotatedFileParser().parseFile(resultDescriptorFile)));
    }

    public static SqlResultDescriptor sqlResultDescriptorFor(InputStream inputStream)
            throws IOException
    {
        return new SqlResultDescriptor(getOnlyElement(new AnnotatedFileParser().parseFile(inputStream)));
    }

    public SqlResultDescriptor(SectionParsingResult sqlSectionParsingResult)
    {
        this(sqlSectionParsingResult, newHashMap());
    }

    public SqlResultDescriptor(SectionParsingResult sqlSectionParsingResult, Map<String, String> baseProperties)
    {
        super(sqlSectionParsingResult, baseProperties);
        this.expectedTypes = parseExpectedTypes(sqlSectionParsingResult);
    }

    private Optional<List<JDBCType>> parseExpectedTypes(SectionParsingResult sqlFileSectionParsingResult)
    {
        Optional<String> typesProperty = sqlFileSectionParsingResult.getProperty("types");

        return typesProperty.map(
                value -> stream(TYPES_SPLITTER.split(value).spliterator(), false)
                        .map(JDBCType::valueOf)
                        .collect(toList()));
    }

    public List<Row> getRows(List<JDBCType> columnTypes)
    {
        List<Row> values = newArrayList();
        String delimiter = getColumnDelimiter();

        QueryRowMapper rowMapper = new QueryRowMapper(columnTypes);
        Splitter valuesSplitter = Splitter.on(delimiter);
        if (isTrimValues()) {
            valuesSplitter = valuesSplitter.trimResults();
        }

        for (String line : sqlSectionParsingResult.getContentLines()) {
            List<String> rowValues = parseLine(line, delimiter, valuesSplitter);
            values.add(rowMapper.mapToRow(rowValues));
        }

        if (isJoinAllRowsToOne()) {
            checkState(columnTypes.size() == 1, "Expected single column result when 'joinAllRowsToOne' property is set, columnTypes: %s", columnTypes);
            String joinedRows = values.stream()
                    .map(row -> String.valueOf(row.getValues().get(0)))
                    .collect(joining("\n"));
            return ImmutableList.of(row(joinedRows));
        }

        return values;
    }

    public Optional<List<JDBCType>> getExpectedTypes()
    {
        return expectedTypes;
    }

    private List<String> parseLine(String line, String delimiter, Splitter valuesSplitter)
    {
        List<String> rowValues = valuesSplitter.splitToList(line);
        if (line.trim().endsWith(delimiter)) {
            rowValues = rowValues.subList(0, rowValues.size() - 1);
        }
        return rowValues;
    }

    public boolean isIgnoreOrder()
    {
        return Boolean.valueOf(getPropertyValue("ignoreOrder").orElse(DEFAULT_IGNORE_ORDER));
    }

    public boolean isIgnoreExcessRows()
    {
        return Boolean.valueOf(getPropertyValue("ignoreExcessRows").orElse(DEFAULT_IGNORE_EXCESS));
    }

    public boolean isTrimValues()
    {
        return Boolean.valueOf(getPropertyValue("trimValues").orElse(DEFAULT_TRIM_VALUES));
    }

    public boolean isJoinAllRowsToOne()
    {
        return Boolean.valueOf(getPropertyValue("joinAllRowsToOne").orElse(JOIN_ALL_VALUES_TO_ONE));
    }

    private String getColumnDelimiter()
    {
        return getPropertyValue("delimiter").orElse(DEFAULT_COLUMN_DELIMITER);
    }
}
