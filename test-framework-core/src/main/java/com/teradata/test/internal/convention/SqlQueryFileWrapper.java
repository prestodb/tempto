/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention;

import com.google.common.base.Splitter;
import com.teradata.test.internal.convention.HeaderFileParser.ParsingResult;
import com.teradata.test.query.QueryType;

import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.teradata.test.query.QueryExecutor.DEFAULT_DB_NAME;

public class SqlQueryFileWrapper
{

    private static final String GROUPS_HEADER_PROPERTY = "groups";
    private static final String DATABASE_HEADER_PROPERTY = "database";
    private static final String QUERY_TYPE_HEADER_PROPERTY = "queryType";
    private static final Splitter GROUPS_HEADER_PROPERTY_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();


    private final ParsingResult sqlFileParsingResult;

    public SqlQueryFileWrapper(ParsingResult sqlFileParsingResult)
    {
        this.sqlFileParsingResult = sqlFileParsingResult;
    }

    public String getDatabaseName(){
        return sqlFileParsingResult.getProperty(DATABASE_HEADER_PROPERTY).orElse(DEFAULT_DB_NAME);
    }

    public Set<String> getTestGroups(){
        String groupsProperty = sqlFileParsingResult.getProperty(GROUPS_HEADER_PROPERTY).orElse("");
        return newHashSet(GROUPS_HEADER_PROPERTY_SPLITTER.split(groupsProperty));
    }

    public String getContent()
    {
        return sqlFileParsingResult.getContent();
    }

    public Optional<QueryType> getQueryType(){
        return sqlFileParsingResult.getProperty(QUERY_TYPE_HEADER_PROPERTY).map(QueryType::valueOf);
    }
}
