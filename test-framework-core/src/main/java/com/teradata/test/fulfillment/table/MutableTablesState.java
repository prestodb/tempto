/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.table;

import com.teradata.test.fulfillment.NamedObjectsState;

import java.util.Map;

import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;

public class MutableTablesState
        extends NamedObjectsState<TableInstance>
{

    public static MutableTablesState mutableTablesState()
    {
        return testContext().getDependency(MutableTablesState.class);
    }

    public MutableTablesState(Map<String, TableInstance> mutableTableInstances)
    {
        super(mutableTableInstances, "mutable table");
    }
}
