/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.convention;

import com.teradata.test.internal.convention.AnnotatedFileParser.SectionParsingResult;
import com.teradata.test.query.QueryType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.teradata.test.query.QueryExecutor.DEFAULT_DB_NAME;

public class SqlQueryDescriptor
        extends SqlDescriptor
{

    private static final String GROUPS_HEADER_PROPERTY = "groups";
    private static final String DATABASE_HEADER_PROPERTY = "database";
    private static final String TABLES_HEADER_PROPERTY = "tables";
    private static final String QUERY_TYPE_HEADER_PROPERTY = "queryType";
    private static final String REQUIRES_HEADER_PROPERTY = "requires";

    public SqlQueryDescriptor(SectionParsingResult sqlSectionParsingResult)
    {
        this(sqlSectionParsingResult, newHashMap());
    }

    public SqlQueryDescriptor(SectionParsingResult sqlSectionParsingResult, Map<String, String> baseProperties)
    {
        super(sqlSectionParsingResult, baseProperties);
    }

    public String getDatabaseName()
    {
        return getPropertyValue(DATABASE_HEADER_PROPERTY).orElse(DEFAULT_DB_NAME);
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

    public Optional<QueryType> getQueryType()
    {
        return getPropertyValue(QUERY_TYPE_HEADER_PROPERTY).map(QueryType::valueOf);
    }
}
