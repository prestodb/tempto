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
import com.google.inject.Singleton;
import com.teradata.tempto.configuration.Configuration;
import com.teradata.tempto.initialization.AutoModuleProvider;
import com.teradata.tempto.initialization.SuiteModuleProvider;
import com.teradata.tempto.query.JdbcConnectionsPool;
import com.teradata.tempto.query.JdbcConnectivityParamsState;
import com.teradata.tempto.query.JdbcQueryExecutor;
import com.teradata.tempto.query.QueryExecutor;

import java.util.Set;

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
                    bindDatabaseConnectionBeans(connectionName);
                }
            }

            private void bindDatabaseConnectionBeans(String connectionName)
            {
                JdbcConnectivityParamsState connectivityState = jdbcConnectionsConfiguration.getConnectionConfiguration(connectionName);
                Key<JdbcConnectivityParamsState> connectivityStateKey = Key.get(JdbcConnectivityParamsState.class, named(connectionName));
                bind(connectivityStateKey).toInstance(connectivityState);

                PrivateModule privateModule = new PrivateModule()
                {
                    @Override
                    protected void configure()
                    {
                        Key<QueryExecutor> queryExecutorKey = Key.get(QueryExecutor.class, named(connectionName));
                        bind(JdbcConnectivityParamsState.class).to(connectivityStateKey);
                        bind(queryExecutorKey).to(JdbcQueryExecutor.class).in(Singleton.class);
                        expose(queryExecutorKey);
                    }
                };
                install(privateModule);
            }
        };
    }
}
