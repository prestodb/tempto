/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.table;

import com.teradata.test.context.State;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class TablesState
        implements State
{
    private final Map<String, TableInstance> tableInstances;

    public TablesState(Map<String, TableInstance> tableInstances)
    {
        this.tableInstances = tableInstances;
    }

    public TableInstance getTableInstance(String name)
    {
        checkArgument(tableInstances.containsKey(name), "no table instance found for name %s", name);
        return tableInstances.get(name);
    }
}
