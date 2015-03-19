/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;

public final class JdbcUtils
{

    public static void registerDriver(JdbcConnectivityParamsState jdbcParamsState)
    {
        try {
            Class.forName(jdbcParamsState.driverClass);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load jdbc driver class: " + jdbcParamsState.driverClass, e);
        }
    }

    public static Connection connection(JdbcConnectivityParamsState jdbcParamsState)
            throws SQLException
    {
        return getConnection(
                jdbcParamsState.url,
                jdbcParamsState.user,
                jdbcParamsState.password);
    }

    private JdbcUtils()
    {
    }
}
