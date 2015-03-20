/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.fulfillment.jdbc.JdbcConnectivityParamsState;
import com.teradata.test.fulfillment.jdbc.JdbcUtils;

import java.util.Set;

import static com.google.inject.name.Names.named;

public class QueryExecutorModule
        extends AbstractModule
{
    private final Configuration configuration;
    private final JdbcConnectionsPool jdbcConnectionsPool = new JdbcConnectionsPool();

    public QueryExecutorModule(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    protected void configure()
    {
        Set<String> definedJdcbConnectionNames = JdbcUtils.getDefinedJdcbConnectionNames(configuration);
        for (String connectionName : definedJdcbConnectionNames) {
            bindQueryExecutorFor(connectionName);
        }
    }

    private void bindQueryExecutorFor(String connectionName)
    {
        PrivateModule privateModule = new PrivateModule()
        {
            @Override
            protected void configure()
            {
                Key<QueryExecutor> queryExecutorKey = Key.get(QueryExecutor.class, named(connectionName));

                bind(JdbcConnectivityParamsState.class).to(Key.get(JdbcConnectivityParamsState.class, named(connectionName)));
                bind(JdbcConnectionsPool.class).toInstance(jdbcConnectionsPool);
                bind(queryExecutorKey).to(JdbcQueryExecutor.class);

                expose(queryExecutorKey);
            }
        };
        install(privateModule);
    }
}
