/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.table;

import com.teradata.test.fulfillment.NamedObjectsState;

import java.util.Map;

import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;

public class ImmutableTablesState
        extends NamedObjectsState<TableInstance>
{

    public static ImmutableTablesState immutableTablesState()
    {
        return testContext().getDependency(ImmutableTablesState.class);
    }

    public ImmutableTablesState(Map<String, TableInstance> immutableTableInstances)
    {
        super(immutableTableInstances, "immutable table");
    }
}
