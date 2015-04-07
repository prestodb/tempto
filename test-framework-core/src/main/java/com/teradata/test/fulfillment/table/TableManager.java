/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.fulfillment.table;

import com.teradata.test.context.TestContext;

import static com.teradata.test.context.ThreadLocalTestContextHolder.runWithTextContext;
import static com.teradata.test.context.ThreadLocalTestContextHolder.testContext;
import static com.teradata.test.fulfillment.table.TableManagerDispatcher.getTableManagerDispatcher;

public interface TableManager
{
    TableInstance createImmutable(TableDefinition tableDefinition);

    TableInstance createMutable(TableDefinition tableDefinition);

    void drop(TableInstance tableInstance);

    /**
     * Makes a {@link TableInstance} dropped when a given {@link TestContext} is closed.
     */
    default void dropOnTestContextClose(TestContext testContext, TableInstance tableInstance)
    {
        testContext.registerCloseCallback(context -> runWithTextContext(context, () -> drop(tableInstance)));
    }

    default void dropOnTestContextClose(TableInstance tableInstance)
    {
        dropOnTestContextClose(testContext(), tableInstance);
    }

    public static TableInstance createImmutableTable(TableDefinition tableDefinition)
    {
        return getTableManagerDispatcher().getTableManagerFor(tableDefinition).createImmutable(tableDefinition);
    }

    public static TableInstance createMutableTable(TableDefinition tableDefinition)
    {
        return getTableManagerDispatcher().getTableManagerFor(tableDefinition).createMutable(tableDefinition);
    }

    public static void dropTable(TableInstance tableInstance)
    {
        getTableManagerDispatcher().getTableManagerFor(tableInstance).drop(tableInstance);
    }

    public static void dropTableOnTestContextClose(TableInstance tableInstance)
    {
        getTableManagerDispatcher().getTableManagerFor(tableInstance).dropOnTestContextClose(tableInstance);
    }
}
