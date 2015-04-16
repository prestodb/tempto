/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.table;

import com.teradata.test.context.State;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;

public class TablesState
        implements State
{
    private final Map<String, TableInstance> immutableTableInstances;
    private final Map<String, TableInstance> mutableTableInstances;
    private final Map<String, TableInstance> tableInstances;

    public TablesState(Map<String, TableInstance> immutableTableInstances, Map<String, TableInstance> mutableTableInstances)
    {
        this.immutableTableInstances = immutableTableInstances;
        this.mutableTableInstances = mutableTableInstances;
        this.tableInstances = newHashMap();
        tableInstances.putAll(immutableTableInstances);
        tableInstances.putAll(mutableTableInstances);
    }

    public TableInstance getTableInstance(String name)
    {
        checkArgument(tableInstances.containsKey(name), "no table instance found for name %s", name);
        return tableInstances.get(name);
    }

    public TableInstance getImmutableTableInstance(String name)
    {
        checkArgument(immutableTableInstances.containsKey(name), "no immutable table instance found for name %s", name);
        return immutableTableInstances.get(name);
    }

    public TableInstance getMutableTableInstance(String name)
    {
        checkArgument(mutableTableInstances.containsKey(name), "no mutable table instance found for name %s", name);
        return mutableTableInstances.get(name);
    }
}
