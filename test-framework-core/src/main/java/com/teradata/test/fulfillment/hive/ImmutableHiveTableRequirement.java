/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

import com.teradata.test.Requirement;

public class ImmutableHiveTableRequirement implements Requirement
{

    private final HiveTableDefinition tableDefinition;

    public ImmutableHiveTableRequirement(HiveTableDefinition tableDefinition)
    {
        this.tableDefinition = tableDefinition;
    }

    public HiveTableDefinition getTableDefinition()
    {
        return tableDefinition;
    }
}
