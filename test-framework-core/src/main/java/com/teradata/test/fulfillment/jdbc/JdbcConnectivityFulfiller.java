/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.fulfillment.jdbc;

import com.google.inject.Inject;
import com.teradata.test.Configuration;
import com.teradata.test.Requirement;
import com.teradata.test.fulfillment.RequirementFulfiller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static java.sql.DriverManager.getConnection;

/**
 * Produces {@link JdbcConnectivityState} from {@link }.
 */
public class JdbcConnectivityFulfiller
        implements RequirementFulfiller<JdbcConnectivityState>
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
    public List<JdbcConnectivityState> fulfill(List<Requirement> requirements)
    {
        if (isEmpty(filter(requirements, JdbcConnectivityRequirement.class))) {
            return newArrayList();
        }

        checkConnection();

        return newArrayList(new JdbcConnectivityState(
                configuration.getStringMandatory(JDBC_DRIVER_CLASS),
                configuration.getStringMandatory(JDBC_URL_KEY),
                configuration.getStringMandatory(JDBC_USER_KEY),
                configuration.getStringMandatory(JDBC_PASSWORD_KeY)));
    }

    private void checkConnection()
    {
        try {
            Class.forName(configuration.getStringMandatory(JDBC_DRIVER_CLASS));
            Connection connection = getConnection(
                    configuration.getStringMandatory(JDBC_URL_KEY),
                    configuration.getStringMandatory(JDBC_USER_KEY),
                    configuration.getStringMandatory(JDBC_PASSWORD_KeY));
            connection.close();
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load JDBC driver class " + configuration.getStringMandatory(JDBC_DRIVER_CLASS));
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
