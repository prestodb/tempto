/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

import com.teradata.test.Requirement;

public class ImmutableHiveTableRequirement implements Requirement
{
    private final String tableName;
    private final HiveDataSource dataSource;

    public ImmutableHiveTableRequirement(String tableName, HiveDataSource dataSource)
    {
        this.tableName = tableName;
        this.dataSource = dataSource;
    }

    public String getTableName()
    {
        return tableName;
    }

    public HiveDataSource getDataSource()
    {
        return dataSource;
    }
}
