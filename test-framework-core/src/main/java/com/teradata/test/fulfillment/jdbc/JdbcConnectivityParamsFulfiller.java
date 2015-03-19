/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.fulfillment.jdbc;

import com.google.inject.Inject;
import com.teradata.test.Requirement;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.configuration.KeyUtils;
import com.teradata.test.context.State;
import com.teradata.test.fulfillment.RequirementFulfiller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;

import static com.teradata.test.fulfillment.jdbc.JdbcUtils.registerDriver;

/**
 * Produces {@link JdbcConnectivityParamsState} from {@link }.
 */
public class JdbcConnectivityParamsFulfiller
        implements RequirementFulfiller
{
    private static final String JDBC_DRIVER_CLASS = "jdbc_driver_class";
    private static final String JDBC_URL_KEY = "jdbc_url";
    private static final String JDBC_USER_KEY = "jdbc_user";
    private static final String JDBC_PASSWORD_KeY = "jdbc_password";
    private static final String DATABASES_CONFIGURATION_SECTION = "databases";

    private final Configuration configuration;

    @Inject
    public JdbcConnectivityParamsFulfiller(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public Set<State> fulfill(Set<Requirement> requirements)
    {

        Set<String> connectionNames = getDefinedJdcbConnectionNames();
        return connectionNames.stream()
                .map(this::parseConnectionConfiguration)
                .collect(Collectors.toSet());
    }

    private State parseConnectionConfiguration(String connectionName) {

        Configuration connectionConfiguration = getDatabaseConnectionSubConfiguration(connectionName);
        JdbcConnectivityParamsState jdbcConnectivityParamsState = new JdbcConnectivityParamsState(
                connectionName,
                connectionConfiguration.getStringMandatory(JDBC_DRIVER_CLASS),
                connectionConfiguration.getStringMandatory(JDBC_URL_KEY),
                connectionConfiguration.getStringMandatory(JDBC_USER_KEY),
                connectionConfiguration.getStringMandatory(JDBC_PASSWORD_KeY));

        checkConnection(jdbcConnectivityParamsState);
        return jdbcConnectivityParamsState;
    }

    private Configuration getDatabaseConnectionSubConfiguration(String connectionName)
    {
        return configuration.getSubconfiguration(KeyUtils.joinKey(DATABASES_CONFIGURATION_SECTION, connectionName));
    }

    private Set<String> getDefinedJdcbConnectionNames()
    {
        return configuration.getSubconfiguration(DATABASES_CONFIGURATION_SECTION).listKeyPrefixes(1);
    }

    private void checkConnection(JdbcConnectivityParamsState jdbcConnectivityParamsState)
    {
        registerDriver(jdbcConnectivityParamsState);
        try (Connection connection = JdbcUtils.connection(jdbcConnectivityParamsState)) {
            connection.isValid(1000);
        }
        catch (SQLException e) {
            throw new RuntimeException("JDBC server (url: "
                    + configuration.getStringMandatory(JDBC_URL_KEY)
                    + ", user: " + configuration.getStringMandatory(JDBC_USER_KEY) +
                    ") is not accessible", e);
        }
    }

    @Override
    public void cleanup()
    {
    }
}
