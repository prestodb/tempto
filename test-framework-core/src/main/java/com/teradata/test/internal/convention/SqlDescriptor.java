/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.convention;

import com.google.common.base.Splitter;
import com.teradata.test.internal.convention.AnnotatedFileParser.SectionParsingResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Base class for {@link com.teradata.test.internal.convention.SqlQueryDescriptor}
 * and {@link com.teradata.test.convention.SqlResultDescriptor}.
 */
public class SqlDescriptor
{
    private static final Splitter HEADER_PROPERTY_SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

    protected final SectionParsingResult sqlSectionParsingResult;
    private final Map<String, String> baseProperties;

    protected SqlDescriptor(SectionParsingResult sqlSectionParsingResult, Map<String, String> baseProperties)
    {
        this.sqlSectionParsingResult = sqlSectionParsingResult;
        this.baseProperties = baseProperties;
    }

    public Optional<String> getName()
    {
        return sqlSectionParsingResult.getSectionName();
    }

    public String getContent()
    {
        return sqlSectionParsingResult.getContent();
    }

    public String getOriginalContent()
    {
        return sqlSectionParsingResult.getOriginalContent();
    }

    protected List<String> getPropertyValues(String property)
    {
        List<String> propertyValues = newArrayList();

        if (sqlSectionParsingResult.getProperty(property).isPresent()) {
            propertyValues.addAll(newArrayList(HEADER_PROPERTY_SPLITTER.split(sqlSectionParsingResult.getProperty(property).get())));
        }

        if (baseProperties.containsKey(property)) {
            propertyValues.addAll(newArrayList(HEADER_PROPERTY_SPLITTER.split(baseProperties.get(property))));
        }

        return propertyValues;
    }

    protected Set<String> getPropertyValuesSet(String property)
    {
        return newHashSet(getPropertyValues(property));
    }

    protected Optional<String> getPropertyValue(String property)
    {
        if (sqlSectionParsingResult.getProperty(property).isPresent()) {
            return sqlSectionParsingResult.getProperty(property);
        }
        else {
            return Optional.ofNullable(baseProperties.get(property));
        }
    }
}
