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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.inject.name.Names.named;
import static com.teradata.tempto.fulfillment.table.TableManagerDispatcher.tableManagerMapBinderFor;
import static com.teradata.tempto.internal.ReflectionHelper.getAnnotatedSubTypesOf;
import static java.util.stream.Collectors.toMap;

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
                Configuration databasesSectionConfiguration = configuration.getSubconfiguration("databases");
                Set<String> databaseNames = databasesSectionConfiguration.listKeyPrefixes(1);

                Map<String, ? extends Class<? extends TableManager>> tableManagerClasses = getTableManagerClassesByType();

                for (String database : databaseNames) {
                    Configuration databaseConfiguration = databasesSectionConfiguration.getSubconfiguration(database);
                    Optional<String> tableMangerTypeOptional = databaseConfiguration.getString("table_manager_type");
                    if (!tableMangerTypeOptional.isPresent()) {
                        continue;
                    }
                    String tableManagerType = tableMangerTypeOptional.get();
                    checkArgument(tableManagerClasses.containsKey(tableManagerType),
                            "unknown table manager type %s for database %s; expecting one of %s",
                            tableManagerType, database, tableManagerClasses.keySet());

                    Class<? extends TableManager> tableManagerClass = tableManagerClasses.get(tableManagerType.toLowerCase());
                    Class<? extends TableDefinition> tableDefinitionClass = tableManagerClass
                            .getAnnotation(TableManager.Descriptor.class).tableDefinitionClass();

                    tableManagerMapBinderFor(binder()).addBinding(tableDefinitionClass).to(tableManagerClass);
                    bind(Key.get(TableManager.class, named(database))).to(tableManagerClass);
                }
            }

            private Map<String, ? extends Class<? extends TableManager>> getTableManagerClassesByType()
            {
                return getAnnotatedSubTypesOf(TableManager.class, TableManager.Descriptor.class).stream()
                        .collect(toMap(
                                tableManagerClass -> tableManagerClass.getAnnotation(TableManager.Descriptor.class).type().toLowerCase(),
                                tableManagerClass -> tableManagerClass));
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
