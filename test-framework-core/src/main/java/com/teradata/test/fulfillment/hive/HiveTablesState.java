/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive;

import com.teradata.test.context.State;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class HiveTablesState
        implements State
{
    private final Map<String, HiveTableInstance> tableInstances;

    public HiveTablesState(Map<String, HiveTableInstance> tableInstances)
    {
        this.tableInstances = tableInstances;
    }

    public HiveTableInstance getTableInstance(String name)
    {
        checkArgument(tableInstances.containsKey(name), "no table instance found for name %s", name);
        return tableInstances.get(name);
    }
}
