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
package com.teradata.tempto.internal.fulfillment.table;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.teradata.tempto.configuration.Configuration;
import com.teradata.tempto.fulfillment.table.TableDefinition;
import com.teradata.tempto.fulfillment.table.TableManager;
import com.teradata.tempto.fulfillment.table.TableManagerDispatcher;
import com.teradata.tempto.initialization.AutoModuleProvider;
import com.teradata.tempto.initialization.SuiteModuleProvider;
import com.teradata.tempto.internal.fulfillment.table.hive.HiveTableManager;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.inject.name.Names.named;
import static com.teradata.tempto.fulfillment.table.TableManagerDispatcher.tableManagerMapBinderFor;

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
                Set<String> databaseNames = configuration.getSubconfiguration("databases").listKeyPrefixes(1);
                Configuration managersSectionConfiguration = configuration.getSubconfiguration("table_managers");

                for (String tableManagerName : managersSectionConfiguration.listKeyPrefixes(1)) {
                    Configuration managerConfiguration = configuration.getSubconfiguration(tableManagerName);
                    String tableDefinitionClassName = managerConfiguration.getStringMandatory("table_definition_class");
                    String tableManagerClassName = getTableManagerClassName(
                            managerConfiguration.getString("manager_class"),
                            managerConfiguration.getString("manager_type"),
                            tableManagerName);
                    String databaseName = configuration.getString("database").orElse(tableManagerName);
                    checkArgument(databaseNames.contains(databaseName), "unknown database %s defined for table manager %s", databaseName, tableManagerName);

                    Class<? extends TableDefinition> tableDefinitionClass = getClassForName(tableDefinitionClassName, TableDefinition.class);
                    Class<? extends TableManager> tableManagerClass = getClassForName(tableManagerClassName, TableManager.class);

                    tableManagerMapBinderFor(binder()).addBinding(tableDefinitionClass).to(tableManagerClass);
                    bind(Key.get(TableManager.class, named(tableManagerName))).to(tableManagerClass);
                }
            }

            private <T> Class<? extends T> getClassForName(String className, Class<T> expectedParentClass)
            {
                Class<?> clazz;
                try {
                    clazz = Class.forName(className);
                    checkArgument(expectedParentClass.isAssignableFrom(clazz),
                            "%s does not inherit from %s", clazz, expectedParentClass);
                }
                catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("could not instantiate class " + className, e);
                }
                return (Class<? extends T>) clazz;
            }

            private String getTableManagerClassName(Optional<String> tableManagerClassOptional, Optional<String> tableManagerTypeOptional, String tableManagerName)
            {
                checkArgument(tableManagerClassOptional.isPresent() ^ tableManagerTypeOptional.isPresent(),
                        "exactly one of manager_class/manager_type must be defined for table manager %s", tableManagerName);
                return tableManagerClassOptional.orElse(tableManagerTypeOptional.map(type -> {
                    switch (type) {
                        case "HIVE":
                            return HiveTableManager.class.getCanonicalName();
                        default:
                            throw new IllegalArgumentException("unknown table manager type " + type + " for table manager " + tableManagerName);
                    }
                }).get());
            }

            @Inject
            @Provides
            public TableManagerDispatcher defaultTableManagerDispatcher(Map<Class, TableManager> tableManagers)
            {
                return new TableManagerDispatcher()
                {
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
