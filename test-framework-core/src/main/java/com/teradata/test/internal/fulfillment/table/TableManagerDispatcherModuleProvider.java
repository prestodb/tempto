/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.fulfillment.table;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.fulfillment.table.TableDefinition;
import com.teradata.test.fulfillment.table.TableManager;
import com.teradata.test.fulfillment.table.TableManager.AutoTableManager;
import com.teradata.test.fulfillment.table.TableManagerDispatcher;
import com.teradata.test.initialization.AutoModuleProvider;
import com.teradata.test.initialization.SuiteModuleProvider;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static com.google.inject.name.Names.named;
import static com.teradata.test.fulfillment.table.TableManagerDispatcher.tableManagerMapBinderFor;
import static com.teradata.test.internal.ReflectionHelper.getAnnotatedSubTypesOf;

@AutoModuleProvider
public class TableManagerDispatcherModuleProvider
        implements SuiteModuleProvider
{
    public Module getModule(Configuration configuration)
    {
        return new AbstractModule()
        {
            @Override
            protected void configure()
            {
                getAnnotatedSubTypesOf(TableManager.class, AutoTableManager.class).stream().forEach(
                        tableManagerClass -> {
                            AutoTableManager annotation = tableManagerClass.getAnnotation(AutoTableManager.class);
                            tableManagerMapBinderFor(binder()).addBinding(annotation.tableDefinitionClass()).to(tableManagerClass);
                            bind(Key.get(TableManager.class, named(annotation.name()))).to(tableManagerClass);
                        }
                );
            }

            @Inject
            @Provides
            public TableManagerDispatcher defaultTableManagerDispatcher(Map<Class, TableManager> tableManagers)
            {
                return tableDefinition -> {
                    Class<? extends TableDefinition> clazz = tableDefinition.getClass();
                    checkState(tableManagers.containsKey(clazz), "Table manager for %s is not registered", clazz);
                    return tableManagers.get(clazz);
                };
            }
        };
    }
}