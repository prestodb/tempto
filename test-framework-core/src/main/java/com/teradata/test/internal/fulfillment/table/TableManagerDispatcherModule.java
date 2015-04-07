/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.fulfillment.table;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.teradata.test.fulfillment.table.TableManager;
import com.teradata.test.fulfillment.table.TableManagerDispatcher;
import com.teradata.test.internal.fulfillment.hive.HiveTableManager;

import static com.google.inject.name.Names.named;

// TODO: provide proper dispatching here
public class TableManagerDispatcherModule
        extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind(Key.get(TableManager.class, named("hive"))).to(HiveTableManager.class);
    }

    @Inject
    @Provides
    public TableManagerDispatcher defaultTableManagerDispatcher(@Named("hive") TableManager tableManager)
    {
        return tableDefinition -> tableManager;
    }
}