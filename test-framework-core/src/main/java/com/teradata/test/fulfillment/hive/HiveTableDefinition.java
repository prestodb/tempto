/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

import com.teradata.test.fulfillment.table.TableDefinition;

import static com.google.common.base.Preconditions.checkArgument;
import static com.teradata.test.fulfillment.hive.InlineDataSource.createSameRowDataSource;
import static com.teradata.test.fulfillment.hive.InlineDataSource.createStringDataSource;
import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class HiveTableDefinition
        extends TableDefinition
{
    private static final String NAME_TEMPLATE = "%NAME%";
    private static final String LOCATION_TEMPLATE = "%LOCATION%";

    private final DataSource dataSource;
    private final String createTableDDLTemplate;

    private HiveTableDefinition(String name, String createTableDDLTemplate, DataSource dataSource)
    {
        super(name);
        this.dataSource = dataSource;
        this.createTableDDLTemplate = createTableDDLTemplate;

        checkArgument(createTableDDLTemplate.contains(NAME_TEMPLATE), "Create table DDL must contain %NAME% placeholder");
        checkArgument(createTableDDLTemplate.contains(LOCATION_TEMPLATE), "Create table DDL must contain %LOCATION% placeholder");
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

    public String getCreateTableDDLTemplate(String location)
    {
        return getCreateTableDDLTemplate(getName(), location);
    }

    public String getCreateTableDDLTemplate(String name, String location)
    {
        return createTableDDLTemplate.replace(NAME_TEMPLATE, name).replace(LOCATION_TEMPLATE, location).replaceAll("\'", "''");
    }

    public static HiveTableDefinition hiveTableDefinition(String name, String createTableDDLTemplate, DataSource dataSource)
    {
        return new HiveTableDefinition(name, createTableDDLTemplate, dataSource);
    }

    public static HiveTableDefinitionBuilder builder()
    {
        return new HiveTableDefinitionBuilder();
    }

    public static LikeTableDefinitionBuilder like(HiveTableDefinition hiveTableDefinition)
    {

        LikeTableDefinitionBuilder likeTableDefinitionBuilder = new LikeTableDefinitionBuilder();
        likeTableDefinitionBuilder.setName(hiveTableDefinition.getName());
        likeTableDefinitionBuilder.setCreateTableDDLTemplate(hiveTableDefinition.createTableDDLTemplate);
        likeTableDefinitionBuilder.setDataSource(hiveTableDefinition.getDataSource());
        return likeTableDefinitionBuilder;
    }

    public static class HiveTableDefinitionBuilder
    {

        private String name;
        private String createTableDDLTemplate;
        private DataSource dataSource;

        private HiveTableDefinitionBuilder()
        {
        }

        public HiveTableDefinitionBuilder setName(String name)
        {
            this.name = name;
            return this;
        }

        public HiveTableDefinitionBuilder setCreateTableDDLTemplate(String createTableDDLTemplate)
        {
            this.createTableDDLTemplate = createTableDDLTemplate;
            return this;
        }

        public HiveTableDefinitionBuilder setDataSource(DataSource dataSource)
        {
            this.dataSource = dataSource;
            return this;
        }

        public HiveTableDefinitionBuilder setData(String revision, String data)
        {
            this.dataSource = createStringDataSource(name, revision, data);
            return this;
        }

        public HiveTableDefinitionBuilder setRows(String revision, int rowsCount, String rowData)
        {
            this.dataSource = createSameRowDataSource(name, revision, 1, rowsCount, rowData);
            return this;
        }

        public HiveTableDefinitionBuilder setRows(String revision, int splitCount, int rowsInEachSplit, String rowData)
        {
            this.dataSource = createSameRowDataSource(name, revision, splitCount, rowsInEachSplit, rowData);
            return this;
        }

        public HiveTableDefinition build()
        {
            return new HiveTableDefinition(name, createTableDDLTemplate, dataSource);
        }
    }

    public static class LikeTableDefinitionBuilder
            extends HiveTableDefinitionBuilder
    {
        public LikeTableDefinitionBuilder withName(String name)
        {
            setName(name);
            return this;
        }

        public LikeTableDefinitionBuilder withDataSource(DataSource dataSource)
        {
            setDataSource(dataSource);
            return this;
        }

        public LikeTableDefinitionBuilder withNoData(String revision)
        {
            setData(revision, "");
            return this;
        }

        public LikeTableDefinitionBuilder withData(String revision, String data)
        {
            setData(revision, data);
            return this;
        }

        public LikeTableDefinitionBuilder withRows(String revision, int rowsCount, String rowData)
        {
            setRows(revision, rowsCount, rowData);
            return this;
        }

        public LikeTableDefinitionBuilder withRows(String revision, int splitCount, int rowsInEachSplit, String rowData)
        {
            setRows(revision, splitCount, rowsInEachSplit, rowData);
            return this;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        return reflectionEquals(this, o);
    }

    @Override
    public int hashCode()
    {
        return reflectionHashCode(this);
    }
}
