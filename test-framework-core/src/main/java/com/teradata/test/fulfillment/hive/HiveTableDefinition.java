/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

import com.teradata.test.fulfillment.table.TableColumn;
import com.teradata.test.fulfillment.table.TableDefinition;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class HiveTableDefinition
        extends TableDefinition
{
    public HiveTableDefinition(String name, List<TableColumn> columns, DataSource dataSource)
    {
        super(name, columns);
        this.dataSource = dataSource;
    }

    private final DataSource dataSource;

    public DataSource getDataSource()
    {
        return dataSource;
    }

    public static HiveTablesFulfillerBuilder builder()
    {
        return new HiveTablesFulfillerBuilder();
    }

    public static class HiveTablesFulfillerBuilder
    {

        private String name;
        private DataSource dataSource;
        private List<TableColumn> columns = newArrayList();

        private HiveTablesFulfillerBuilder()
        {
        }

        public HiveTablesFulfillerBuilder setName(String name)
        {
            this.name = name;
            return this;
        }

        public HiveTablesFulfillerBuilder setDataSource(DataSource dataSource)
        {
            this.dataSource = dataSource;
            return this;
        }

        public HiveTablesFulfillerBuilder addColumn(String name, HiveType hiveType)
        {
            columns.add(new TableColumn(name, hiveType.name()));
            return this;
        }

        public HiveTableDefinition build()
        {
            return new HiveTableDefinition(name, columns, dataSource);
        }
    }
}
