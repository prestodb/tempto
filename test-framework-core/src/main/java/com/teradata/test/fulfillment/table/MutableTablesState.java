/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.table;

import com.teradata.test.fulfillment.NamedObjectsState;

import java.util.Map;
import java.util.Map.Entry;

import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static java.util.stream.Collectors.toMap;

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

    public Map<String, String> getNameInDatabaseMap()
    {
        return objects.entrySet()
                .stream()
                .collect(toMap(Entry::getKey, e -> e.getValue().getNameInDatabase()));
    }
}
