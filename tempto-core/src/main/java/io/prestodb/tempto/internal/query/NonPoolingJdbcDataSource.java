/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.prestodb.tempto.internal.query;

import io.prestodb.tempto.query.JdbcConnectivityParamsState;

import javax.sql.DataSource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * DataSource implementation which creates new connection every time getConnection method is called
 */
class NonPoolingJdbcDataSource
        implements DataSource
{
    private final JdbcConnectivityParamsState jdbcParamsState;
    private final Driver driver;

    public NonPoolingJdbcDataSource(JdbcConnectivityParamsState jdbcParamsState, Driver driver)
    {
        this.jdbcParamsState = jdbcParamsState;
        this.driver = driver;
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
