/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.table;

import com.teradata.test.fulfillment.NamedObjectsState;

import java.util.Map;

public class ImmutableTablesState
        extends NamedObjectsState<TableInstance>
{

    public ImmutableTablesState(Map<String, TableInstance> immutableTableInstances)
    {
        super(immutableTableInstances, "immutable table");
    }
}
