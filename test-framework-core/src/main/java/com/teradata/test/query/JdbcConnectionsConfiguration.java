/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query;

import com.teradata.test.configuration.Configuration;
import com.teradata.test.configuration.KeyUtils;

import java.util.Optional;
import java.util.Set;

public class JdbcConnectionsConfiguration
{

    private static final String DATABASES_CONFIGURATION_SECTION = "databases";
    private static final String JDBC_DRIVER_CLASS = "jdbc_driver_class";
    private static final String JDBC_URL_KEY = "jdbc_url";
    private static final String JDBC_USER_KEY = "jdbc_user";
    private static final String JDBC_PASSWORD_KEY = "jdbc_password";
    private static final String JDBC_POOLING_KEY = "jdbc_pooling";
    private static final String ALIAS_KEY = "alias";

    private final Configuration configuration;

    public JdbcConnectionsConfiguration(Configuration configuration) {this.configuration = configuration;}

    public Set<String> getDefinedJdcbConnectionNames()
    {
        return configuration.getSubconfiguration(DATABASES_CONFIGURATION_SECTION).listKeyPrefixes(1);
    }

    public JdbcConnectivityParamsState getConnectionConfiguration(String connectionName) {

        Configuration connectionConfiguration = getDatabaseConnectionSubConfiguration(connectionName);
        Optional<String> alias = connectionConfiguration.getString(ALIAS_KEY);
        if (alias.isPresent()) {
            connectionConfiguration = getDatabaseConnectionSubConfiguration(alias.get());
        }

        JdbcConnectivityParamsState jdbcConnectivityParamsState = new JdbcConnectivityParamsState(
                connectionName,
                connectionConfiguration.getStringMandatory(JDBC_DRIVER_CLASS),
                connectionConfiguration.getStringMandatory(JDBC_URL_KEY),
                connectionConfiguration.getStringMandatory(JDBC_USER_KEY),
                connectionConfiguration.getStringMandatory(JDBC_PASSWORD_KEY),
                connectionConfiguration.getBoolean(JDBC_POOLING_KEY).orElse(true));

        return jdbcConnectivityParamsState;
    }

    private Configuration getDatabaseConnectionSubConfiguration(String connectionName)
    {
        return configuration.getSubconfiguration(KeyUtils.joinKey(DATABASES_CONFIGURATION_SECTION, connectionName));
    }


}
