/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query;

import com.teradata.test.query.JdbcConnectivityParamsState;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

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

    public static DataSource dataSource(JdbcConnectivityParamsState jdbcParamsState)
    {
        if (jdbcParamsState.pooling) {
            return createPoolingDataSource(jdbcParamsState);
        }
        else {
            return createNonPoolingDataSource(jdbcParamsState);
        }
    }

    private static DataSource createPoolingDataSource(JdbcConnectivityParamsState jdbcParamsState)
    {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(jdbcParamsState.driverClass);
        dataSource.setUrl(jdbcParamsState.url);
        dataSource.setUsername(jdbcParamsState.user);
        dataSource.setPassword(jdbcParamsState.password);
        return dataSource;
    }

    private static DataSource createNonPoolingDataSource(JdbcConnectivityParamsState jdbcParamsState)
    {
        registerDriver(jdbcParamsState);
        return new DataSource() {
            @Override
            public Connection getConnection()
                    throws SQLException
            {
                return connection(jdbcParamsState);
            }

            @Override
            public Connection getConnection(String username, String password)
                    throws SQLException
            {
                throw new RuntimeException("not implemented");
            }

            @Override
            public PrintWriter getLogWriter()
                    throws SQLException
            {
                return null;
            }

            @Override
            public void setLogWriter(PrintWriter out)
                    throws SQLException
            {

            }

            @Override
            public void setLoginTimeout(int seconds)
                    throws SQLException
            {
            }

            @Override
            public int getLoginTimeout()
                    throws SQLException
            {
                return 0;
            }

            @Override
            public Logger getParentLogger()
                    throws SQLFeatureNotSupportedException
            {
                return null;
            }

            @Override
            public <T> T unwrap(Class<T> iface)
                    throws SQLException
            {
                throw new RuntimeException("not implemented");
            }

            @Override
            public boolean isWrapperFor(Class<?> iface)
                    throws SQLException
            {
                return false;
            }
        };
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
