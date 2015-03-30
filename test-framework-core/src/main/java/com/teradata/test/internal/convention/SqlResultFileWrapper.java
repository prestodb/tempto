/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention;

import com.google.common.base.Splitter;
import com.teradata.test.assertions.QueryAssert.Row;
import com.teradata.test.internal.query.QueryRowMapper;

import java.sql.JDBCType;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;

public class SqlResultFileWrapper
{

    private static final String DEFAULT_DELIMITER = "|";

    private static final String DEFAULT_IGNORE_ORDER = "false";
    private static final Splitter TYPES_SPLITTER = Splitter.on('|');

    private final FileParser.ParsingResult sqlFileParsingResult;
    private List<JDBCType> types;

    public SqlResultFileWrapper(FileParser.ParsingResult sqlFileParsingResult)
    {
        this.sqlFileParsingResult = sqlFileParsingResult;
    }

    public List<Row> getRows()
    {
        List<Row> values = newArrayList();
        String delimiter = getDelimiter();

        QueryRowMapper rowMapper = new QueryRowMapper(getTypes());
        Splitter valuesSplitter = Splitter.on(delimiter);

        for (String line : sqlFileParsingResult.getContentLines()) {
            List<String> rowValues = parseLine(line, delimiter, valuesSplitter);
            values.add(rowMapper.mapToRow(rowValues));
        }

        return values;
    }

    public List<JDBCType> getTypes()
    {
        if (types == null) {
            Optional<String> typesProperty = sqlFileParsingResult.getProperty("types");
            checkState(typesProperty.isPresent(), "Could not find 'types' property in .result file");

            types = StreamSupport.stream(TYPES_SPLITTER.split(typesProperty.get()).spliterator(), false)
                    .map(JDBCType::valueOf)
                    .collect(Collectors.toList());
        }
        return types;
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
        return Boolean.valueOf(sqlFileParsingResult.getProperty("ignoreOrder").orElse(DEFAULT_IGNORE_ORDER));
    }

    private String getDelimiter()
    {
        return sqlFileParsingResult.getProperty("delimiter").orElse(DEFAULT_DELIMITER);
    }
}
