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
package io.prestodb.tempto.internal.fulfillment.table;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import io.prestodb.tempto.configuration.Configuration;
import io.prestodb.tempto.fulfillment.table.ReadOnlyTableManager;
import io.prestodb.tempto.fulfillment.table.TableManager;
import io.prestodb.tempto.fulfillment.table.TableManagerDispatcher;
import io.prestodb.tempto.initialization.AutoModuleProvider;
import io.prestodb.tempto.initialization.SuiteModuleProvider;
import io.prestodb.tempto.internal.query.JdbcConnectionsConfiguration;
import io.prestodb.tempto.query.QueryExecutor;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static com.google.inject.name.Names.named;
import static io.prestodb.tempto.internal.ReflectionHelper.getAnnotatedSubTypesOf;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

@AutoModuleProvider
public class TableManagerDispatcherModuleProvider
        implements SuiteModuleProvider
{
    public Module getModule(Configuration configuration)
    {
        return new TableManagerModule(configuration);
    }

    private static class TableManagerModule
            extends AbstractModule
    {
        private final Configuration configuration;

        public TableManagerModule(Configuration configuration)
        {
            this.configuration = configuration;
        }

        @Override
        protected void configure()
        {
            Configuration databasesSectionConfiguration = configuration.getSubconfiguration("databases");
            Set<String> databaseNames = databasesSectionConfiguration.listKeyPrefixes(1);
            Set<String> jdbcDatabaseNames = new JdbcConnectionsConfiguration(configuration).getDefinedJdcbConnectionNames();

            if (databaseNames.isEmpty()) {
                bind(new TypeLiteral<Map<String, TableManager>>() {}).toInstance(emptyMap());
                bind(new TypeLiteral<Map<String, QueryExecutor>>() {}).toInstance(emptyMap());
                return;
            }

            Map<String, ? extends Class<? extends TableManager>> tableManagerClasses = getTableManagerClassesByType();
            for (String database : databaseNames) {
                Configuration databaseConfiguration = databasesSectionConfiguration.getSubconfiguration(database);
                String tableManagerType = databaseConfiguration.getString("table_manager_type")
                        .orElse(ReadOnlyTableManager.TYPE.toLowerCase());
                checkArgument(tableManagerClasses.containsKey(tableManagerType),
                        "unknown table manager type %s for database %s; expecting one of %s",
                        tableManagerType, database, tableManagerClasses.keySet());

                Class<? extends TableManager> tableManagerClass = tableManagerClasses.get(tableManagerType.toLowerCase());

                Key<TableManager> tableManagerKey = Key.get(TableManager.class, named(database));
                PrivateModule tableManagerPrivateModule = new PrivateModule()
                {
                    @Override
                    protected void configure()
                    {
                        if (jdbcDatabaseNames.contains(database)) {
                            // we bind matching QueryExecutor to be visible by TableManager without @Named annotation
                            bind(QueryExecutor.class).to(Key.get(QueryExecutor.class, named(database)));
                        }
                        databaseConfiguration.listKeys().forEach(key ->
                                databaseConfiguration.getString(key).ifPresent(value ->
                                        bind(Key.get(String.class, named(key))).toInstance(value)));
                        bind(Key.get(String.class, named("databaseName"))).toInstance(database);
                        bind(tableManagerKey).to(tableManagerClass);
                        expose(tableManagerKey);
                    }
                };
                install(tableManagerPrivateModule);
                newMapBinder(binder(), String.class, TableManager.class).addBinding(database).to(tableManagerKey);
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
        public TableManagerDispatcher defaultTableManagerDispatcher(Map<String, TableManager> tableManagers)
        {
            return new DefaultTableManagerDispatcher(tableManagers);
        }
    }
}
