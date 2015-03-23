/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query;

import javax.sql.DataSource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

class NonPoolingJdbcDataSource
        implements DataSource
{
    private final JdbcConnectivityParamsState jdbcParamsState;
    private final Driver driver;

    public NonPoolingJdbcDataSource(JdbcConnectivityParamsState jdbcParamsState, ClassLoader driverClassLoader)
    {
        this.jdbcParamsState = jdbcParamsState;
        this.driver = getDriver(jdbcParamsState, driverClassLoader);
    }

    private static Driver getDriver(JdbcConnectivityParamsState jdbcParamsState, ClassLoader driverClassLoader)

    {
        try {
            Class<?> driverClass = Class.forName(jdbcParamsState.driverClass, true, driverClassLoader);
            return (Driver) driverClass.newInstance();
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("could not create JDBC Driver for connection " + jdbcParamsState.getName(), e);
        }
    }

    @Override
    public Connection getConnection()
            throws SQLException
    {
        Properties properties = prepareConnectionProperties();
        return driver.connect(jdbcParamsState.url, properties);
    }

    private Properties prepareConnectionProperties()
    {
        Properties properties = new Properties();
        properties.put("user", jdbcParamsState.user);
        properties.put("password", jdbcParamsState.password);
        return properties;
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
}
