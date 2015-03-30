/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.query;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.query.JdbcConnectionsPool;
import com.teradata.test.query.JdbcConnectivityParamsState;
import com.teradata.test.query.JdbcQueryExecutor;
import com.teradata.test.query.QueryExecutor;

import java.util.Set;

import static com.google.inject.name.Names.named;

public class QueryExecutorModule
        extends AbstractModule
{

    private final JdbcConnectionsPool jdbcConnectionsPool = new JdbcConnectionsPool();
    private final JdbcConnectionsConfiguration jdbcConnectionsConfiguration;

    public QueryExecutorModule(Configuration configuration)
    {
        this.jdbcConnectionsConfiguration = new JdbcConnectionsConfiguration(configuration);
    }

    @Override
    protected void configure()
    {
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
                bind(JdbcConnectionsPool.class).toInstance(jdbcConnectionsPool);
                bind(queryExecutorKey).to(JdbcQueryExecutor.class);
                expose(queryExecutorKey);
            }
        };
        install(privateModule);
    }
}
