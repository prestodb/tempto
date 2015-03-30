/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

import com.teradata.test.fulfillment.table.TableDefinition;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.apache.commons.lang3.builder.HashCodeBuilder.reflectionHashCode;

public class HiveTableDefinition
        extends TableDefinition
{

    private final DataSource dataSource;
    private final String createTableDDLTemplate;

    private HiveTableDefinition(String name, String createTableDDLTemplate, DataSource dataSource)
    {
        super(name);
        this.dataSource = dataSource;
        this.createTableDDLTemplate = createTableDDLTemplate;
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

    public String getCreateTableDDLTemplate()
    {
        return createTableDDLTemplate;
    }

    public static HiveTableDefinition hiveTableDefinition(String name, String createTableDDLTemplate, DataSource dataSource)
    {
        return new HiveTableDefinition(name, createTableDDLTemplate, dataSource);
    }

    public static HiveTableDefinitionBuilder builder()
    {
        return new HiveTableDefinitionBuilder();
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

        public HiveTableDefinition build()
        {
            return new HiveTableDefinition(name, createTableDDLTemplate, dataSource);
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
