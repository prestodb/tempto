/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention;

import com.google.common.base.Splitter;
import com.teradata.test.internal.convention.HeaderFileParser.ParsingResult;
import com.teradata.test.query.QueryType;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.teradata.test.query.QueryExecutor.DEFAULT_DB_NAME;

public class SqlQueryFileWrapper
{

    private static final String GROUPS_HEADER_PROPERTY = "groups";
    private static final String DATABASE_HEADER_PROPERTY = "database";
    private static final String TABLES_HEADER_PROPERTY = "tables";
    private static final String QUERY_TYPE_HEADER_PROPERTY = "queryType";
    private static final String REQUIRES_HEADER_PROPERTY = "requires";
    private static final Splitter HEADER_PROPERTY_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    private final ParsingResult sqlFileParsingResult;

    public static SqlQueryFileWrapper sqlQueryFileWrapperFor(File queryFile)
    {
        return new SqlQueryFileWrapper(new HeaderFileParser().parseFile(queryFile));
    }

    public SqlQueryFileWrapper(ParsingResult sqlFileParsingResult)
    {
        this.sqlFileParsingResult = sqlFileParsingResult;
    }

    public String getDatabaseName()
    {
        return sqlFileParsingResult.getProperty(DATABASE_HEADER_PROPERTY).orElse(DEFAULT_DB_NAME);
    }

    public Set<String> getTableDefinitionNames()
    {
        return getPropertyValues(TABLES_HEADER_PROPERTY);
    }

    public Set<String> getTestGroups()
    {
        return getPropertyValues(GROUPS_HEADER_PROPERTY);
    }

    public Set<String> getRequirementClassNames()
    {
        return getPropertyValues(REQUIRES_HEADER_PROPERTY);
    }

    private Set<String> getPropertyValues(String property)
    {
        String propertyValue = sqlFileParsingResult.getProperty(property).orElse("");
        return newHashSet(HEADER_PROPERTY_SPLITTER.split(propertyValue));
    }

    public String getContent()
    {
        return sqlFileParsingResult.getContent();
    }

    public Optional<QueryType> getQueryType()
    {
        return sqlFileParsingResult.getProperty(QUERY_TYPE_HEADER_PROPERTY).map(QueryType::valueOf);
    }
}
