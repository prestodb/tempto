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

package com.teradata.tempto.internal.query;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.teradata.tempto.configuration.Configuration;
import com.teradata.tempto.initialization.AutoModuleProvider;
import com.teradata.tempto.initialization.SuiteModuleProvider;
import com.teradata.tempto.query.JdbcConnectionsPool;
import com.teradata.tempto.query.JdbcConnectivityParamsState;
import com.teradata.tempto.query.JdbcQueryExecutor;
import com.teradata.tempto.query.QueryExecutor;
import com.teradata.tempto.query.QueryExecutorDispatcher;

import javax.inject.Inject;

import java.util.Map;
import java.util.Set;

import static com.google.inject.multibindings.MapBinder.newMapBinder;
import static com.google.inject.name.Names.named;

@AutoModuleProvider
public class QueryExecutorModuleProvider
        implements SuiteModuleProvider
{
    public Module getModule(Configuration configuration)
    {
        JdbcConnectionsPool jdbcConnectionsPool = new JdbcConnectionsPool();
        JdbcConnectionsConfiguration jdbcConnectionsConfiguration = new JdbcConnectionsConfiguration(configuration);

        return new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind(JdbcConnectionsPool.class).toInstance(jdbcConnectionsPool);
                Set<String> definedJdcbConnectionNames = jdbcConnectionsConfiguration.getDefinedJdcbConnectionNames();
                for (String connectionName : definedJdcbConnectionNames) {
                    if (!connectionName.equals("cassandra")) {
                        bindDatabaseConnectionBeans(connectionName);
                    }
                }
            }

            private void bindDatabaseConnectionBeans(String connectionName)
            {
                JdbcConnectivityParamsState connectivityState = jdbcConnectionsConfiguration.getConnectionConfiguration(connectionName);
                Key<JdbcConnectivityParamsState> connectivityStateKey = Key.get(JdbcConnectivityParamsState.class, named(connectionName));
                bind(connectivityStateKey).toInstance(connectivityState);

                Key<QueryExecutor> queryExecutorKey = Key.get(QueryExecutor.class, named(connectionName));
                PrivateModule privateModule = new PrivateModule()
                {
                    @Override
                    protected void configure()
                    {
                        bind(JdbcConnectivityParamsState.class).to(connectivityStateKey);
                        bind(queryExecutorKey).to(JdbcQueryExecutor.class).in(Singleton.class);
                        expose(queryExecutorKey);
                    }
                };
                install(privateModule);
                newMapBinder(binder(), String.class, QueryExecutor.class).addBinding(connectionName).to(queryExecutorKey);
            }

            @Inject
            @Provides
            public QueryExecutorDispatcher defaultQueryExecutorDispatcher(Map<String, QueryExecutor> queryExecutors)
            {
                return connectionName -> queryExecutors.get(connectionName);
            }
        };
    }
}
