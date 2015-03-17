/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;

public final class JdbcUtils
{

    public static void registerDriver(JdbcConnectivityState jdbcState)
    {
        try {
            Class.forName(jdbcState.driverClass);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load jdbc driver class: " + jdbcState.driverClass, e);
        }
    }

    public static Connection connection(JdbcConnectivityState jdbcState)
            throws SQLException
    {
        return getConnection(
                jdbcState.url,
                jdbcState.user,
                jdbcState.password);
    }

    private JdbcUtils()
    {
    }
}
