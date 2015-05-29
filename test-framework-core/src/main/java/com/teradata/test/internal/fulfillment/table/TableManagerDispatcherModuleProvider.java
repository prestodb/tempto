/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.teradata.test.internal.fulfillment.table;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.fulfillment.table.TableDefinition;
import com.teradata.test.fulfillment.table.TableManager;
import com.teradata.test.fulfillment.table.TableManager.AutoTableManager;
import com.teradata.test.fulfillment.table.TableManagerDispatcher;
import com.teradata.test.initialization.AutoModuleProvider;
import com.teradata.test.initialization.SuiteModuleProvider;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
                return new TableManagerDispatcher() {
                    @Override
                    public TableManager getTableManagerFor(TableDefinition tableDefinition)
                    {
                        Class<? extends TableDefinition> clazz = tableDefinition.getClass();
                        checkState(tableManagers.containsKey(clazz), "Table manager for %s is not registered", clazz);
                        return tableManagers.get(clazz);
                    }

                    @Override
                    public Collection<TableManager> getAllTableManagers()
                    {
                        return tableManagers.values();
                    }
                };
            }
        };
    }
}