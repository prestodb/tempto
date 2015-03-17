/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.fulfillment.jdbc;

import com.google.inject.Inject;
import com.teradata.test.configuration.Configuration;
import com.teradata.test.Requirement;
import com.teradata.test.context.State;
import com.teradata.test.fulfillment.RequirementFulfiller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Sets.newHashSet;
import static com.teradata.test.fulfillment.jdbc.JdbcUtils.registerDriver;
import static java.util.Collections.emptySet;

/**
 * Produces {@link JdbcConnectivityState} from {@link }.
 */
public class JdbcConnectivityFulfiller
        implements RequirementFulfiller
{
    private static final String JDBC_DRIVER_CLASS = "jdbc_driver_class";
    private static final String JDBC_URL_KEY = "jdbc_url";
    private static final String JDBC_USER_KEY = "jdbc_user";
    private static final String JDBC_PASSWORD_KeY = "jdbc_password";

    private final Configuration configuration;

    @Inject
    public JdbcConnectivityFulfiller(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public Set<State> fulfill(Set<Requirement> requirements)
    {
        if (isEmpty(filter(requirements, JdbcConnectivityRequirement.class))) {
            return emptySet();
        }

        JdbcConnectivityState jdbcConnectivityState = new JdbcConnectivityState(
                configuration.getStringMandatory(JDBC_DRIVER_CLASS),
                configuration.getStringMandatory(JDBC_URL_KEY),
                configuration.getStringMandatory(JDBC_USER_KEY),
                configuration.getStringMandatory(JDBC_PASSWORD_KeY));

        checkConnection(jdbcConnectivityState);

        return newHashSet(jdbcConnectivityState);
    }

    private void checkConnection(JdbcConnectivityState jdbcConnectivityState)
    {
        registerDriver(jdbcConnectivityState);
        try (Connection connection = JdbcUtils.connection(jdbcConnectivityState)) {
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
