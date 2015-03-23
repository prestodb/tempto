/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.configuration.KeyUtils;

import java.util.Set;

import static com.google.inject.name.Names.named;

public class QueryExecutorModule
        extends AbstractModule
{

    private static final String DATABASES_CONFIGURATION_SECTION = "databases";
    private static final String JDBC_DRIVER_CLASS = "jdbc_driver_class";
    private static final String JDBC_URL_KEY = "jdbc_url";
    private static final String JDBC_USER_KEY = "jdbc_user";
    private static final String JDBC_PASSWORD_KEY = "jdbc_password";
    private static final String JDBC_POOLING_KEY = "jdbc_pooling";

    private final Configuration configuration;
    private final JdbcConnectionsPool jdbcConnectionsPool = new JdbcConnectionsPool();

    public QueryExecutorModule(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    protected void configure()
    {
        Set<String> definedJdcbConnectionNames = getDefinedJdcbConnectionNames(configuration);
        for (String connectionName : definedJdcbConnectionNames) {
            bindDatabaseConnectionBeans(connectionName);
        }
    }

    private Set<String> getDefinedJdcbConnectionNames(Configuration configuration)
    {
        return configuration.getSubconfiguration(DATABASES_CONFIGURATION_SECTION).listKeyPrefixes(1);
    }

    private Configuration getDatabaseConnectionSubConfiguration(String connectionName)
    {
        return configuration.getSubconfiguration(KeyUtils.joinKey(DATABASES_CONFIGURATION_SECTION, connectionName));
    }

    private void bindDatabaseConnectionBeans(String connectionName)
    {
        JdbcConnectivityParamsState connectivityState = parseConnectionConfiguration(connectionName);
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

    private JdbcConnectivityParamsState parseConnectionConfiguration(String connectionName) {

        Configuration connectionConfiguration = getDatabaseConnectionSubConfiguration(connectionName);
        JdbcConnectivityParamsState jdbcConnectivityParamsState = new JdbcConnectivityParamsState(
                connectionName,
                connectionConfiguration.getStringMandatory(JDBC_DRIVER_CLASS),
                connectionConfiguration.getStringMandatory(JDBC_URL_KEY),
                connectionConfiguration.getStringMandatory(JDBC_USER_KEY),
                connectionConfiguration.getStringMandatory(JDBC_PASSWORD_KEY),
                connectionConfiguration.getBoolean(JDBC_POOLING_KEY).orElse(true));

        return jdbcConnectivityParamsState;
    }
}
