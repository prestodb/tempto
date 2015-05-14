/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.query;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.Singleton;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.initialization.AutoModuleProvider;
import com.teradata.test.initialization.SuiteModuleProvider;
import com.teradata.test.query.JdbcConnectionsPool;
import com.teradata.test.query.JdbcConnectivityParamsState;
import com.teradata.test.query.JdbcQueryExecutor;
import com.teradata.test.query.QueryExecutor;

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
