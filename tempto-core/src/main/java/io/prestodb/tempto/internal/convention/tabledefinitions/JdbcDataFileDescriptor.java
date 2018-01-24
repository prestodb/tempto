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
package io.prestodb.tempto.internal.convention.tabledefinitions;

import com.google.common.base.Splitter;
import io.prestodb.tempto.internal.convention.AnnotatedFileParser;
import io.prestodb.tempto.internal.convention.AnnotatedFileParser.SectionParsingResult;
import io.prestodb.tempto.internal.convention.SqlDescriptor;
import io.prestodb.tempto.internal.query.QueryRowMapper;

import java.nio.file.Path;
import java.sql.JDBCType;
import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class JdbcDataFileDescriptor
        extends SqlDescriptor
{
    private static final String DEFAULT_COLUMN_DELIMITER = "|";
    private static final String DEFAULT_TRIM_VALUES = "false";
    private static final Splitter TYPES_SPLITTER = Splitter.on('|');

    private final List<JDBCType> columnTypes;

    public static JdbcDataFileDescriptor sqlResultDescriptorFor(Path file)
    {
        return new JdbcDataFileDescriptor(getOnlyElement(new AnnotatedFileParser().parseFile(file)));
    }

    public JdbcDataFileDescriptor(SectionParsingResult sqlSectionParsingResult)
    {
        super(sqlSectionParsingResult);
        this.columnTypes = parseColumnTypes(sqlSectionParsingResult);
    }

    private List<JDBCType> parseColumnTypes(SectionParsingResult sqlFileSectionParsingResult)
    {
        String typesProperty = sqlFileSectionParsingResult.getProperty("types")
                .orElseThrow(() -> new IllegalArgumentException("missing 'types' property"));
        return stream(TYPES_SPLITTER.split(typesProperty).spliterator(), false)
                .map(JDBCType::valueOf)
                .collect(toList());
    }

    public List<List<Object>> getRows()
    {
        List<List<Object>> values = newArrayList();
        String delimiter = getColumnDelimiter();

        QueryRowMapper rowMapper = new QueryRowMapper(columnTypes);
        Splitter valuesSplitter = Splitter.on(delimiter);
        if (isTrimValues()) {
            valuesSplitter = valuesSplitter.trimResults();
        }

        for (String line : sqlSectionParsingResult.getContentLines()) {
            List<String> rowValues = parseLine(line, delimiter, valuesSplitter);
            values.add(rowMapper.mapToRow(rowValues).getValues());
        }

        return values;
    }

    private List<String> parseLine(String line, String delimiter, Splitter valuesSplitter)
    {
        List<String> rowValues = valuesSplitter.splitToList(line);
        if (line.trim().endsWith(delimiter)) {
            rowValues = rowValues.subList(0, rowValues.size() - 1);
        }
        return rowValues;
    }

    private boolean isTrimValues()
    {
        return Boolean.valueOf(getPropertyValue("trimValues").orElse(DEFAULT_TRIM_VALUES));
    }

    private String getColumnDelimiter()
    {
        return getPropertyValue("delimiter").orElse(DEFAULT_COLUMN_DELIMITER);
    }
}
