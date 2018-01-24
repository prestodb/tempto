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
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
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

    public static DataSource dataSource(JdbcConnectivityParamsState jdbcParamsState)
    {
        if (jdbcParamsState.kerberosPrincipal.isPresent()) {
            return createKerberosDataSource(jdbcParamsState);
        }
        else if (jdbcParamsState.pooling) {
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
        dataSource.setDriverClassLoader(getDriverClassLoader(jdbcParamsState));
        return dataSource;
    }

    private static DataSource createNonPoolingDataSource(JdbcConnectivityParamsState jdbcParamsState)
    {
        return new NonPoolingJdbcDataSource(jdbcParamsState, getDatabaseDriver(jdbcParamsState));
    }

    private static DataSource createKerberosDataSource(JdbcConnectivityParamsState jdbcParamsState)
    {
        return new KerberosJdbcDataSource(jdbcParamsState, getDatabaseDriver(jdbcParamsState));
    }

    private static Driver getDatabaseDriver(JdbcConnectivityParamsState jdbcParamsState)
    {
        try {
            Class<?> driverClass = Class.forName(jdbcParamsState.driverClass, true, getDriverClassLoader(jdbcParamsState));
            return (Driver) driverClass.newInstance();
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("could not create JDBC Driver for connection " + jdbcParamsState.getName(), e);
        }
    }

    private static ClassLoader getDriverClassLoader(JdbcConnectivityParamsState jdbcParamsState)
    {
        try {
            ClassLoader myClassLoader = JdbcUtils.class.getClassLoader();
            if (!jdbcParamsState.jar.isPresent()) {
                return myClassLoader;
            }
            URL jarURL = new File(jdbcParamsState.jar.get()).toURL();
            return URLClassLoader.newInstance(new URL[] {jarURL}, myClassLoader);
        }
        catch (MalformedURLException e) {
            throw new RuntimeException("could not create classloader for file" + jdbcParamsState.jar.get(), e);
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
