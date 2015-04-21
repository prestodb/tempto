/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.fulfillment.table;

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;

import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;

/**
 * Returns an appropriate {@link TableManager} based on table type.
 */
public interface TableManagerDispatcher
{
    TableManager getTableManagerFor(TableDefinition tableDefinition);

    default TableManager getTableManagerFor(TableInstance tableInstance)
    {
        return getTableManagerFor(tableInstance.tableDefinition());
    }

    public static TableManagerDispatcher getTableManagerDispatcher()
    {
        return testContext().getDependency(TableManagerDispatcher.class);
    }

    public static MapBinder<Class<? extends TableDefinition>, TableManager> tableManagerMapBinderFor(Binder binder)
    {
        return (MapBinder<Class<? extends TableDefinition>, TableManager>) newMapBinder(binder, TableDefinition.class.getClass(), TableManager.class);
    }
}
