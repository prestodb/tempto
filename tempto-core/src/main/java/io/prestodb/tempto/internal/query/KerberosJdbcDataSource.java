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

import com.google.common.base.Throwables;
import io.prestodb.tempto.kerberos.KerberosAuthentication;
import io.prestodb.tempto.query.JdbcConnectivityParamsState;

import javax.security.auth.Subject;
import javax.sql.DataSource;

import java.io.PrintWriter;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;

public class KerberosJdbcDataSource
        implements DataSource
{
    private final Driver driver;
    private final KerberosAuthentication kerberosAuthentication;
    private final String jdbcUrl;

    public KerberosJdbcDataSource(JdbcConnectivityParamsState jdbcParamsState, Driver driver)
    {
        this.driver = driver;
        this.jdbcUrl = jdbcParamsState.url;
        checkArgument(jdbcParamsState.kerberosPrincipal.isPresent(), "kerberosPrincipal must be specified");
        checkArgument(jdbcParamsState.kerberosKeytab.isPresent(), "kerberosKeytab must be specified");
        this.kerberosAuthentication = new KerberosAuthentication(
                jdbcParamsState.kerberosPrincipal.get(),
                jdbcParamsState.kerberosKeytab.get()
        );
    }

    @Override
    public Connection getConnection()
            throws SQLException
    {
        Subject authenticatedSubject = kerberosAuthentication.authenticate();
        try {
            return Subject.doAs(authenticatedSubject,
                    (PrivilegedExceptionAction<Connection>) () -> driver.connect(jdbcUrl, new Properties()));
        }
        catch (PrivilegedActionException e) {
            Throwables.propagateIfPossible(e.getCause(), SQLException.class);
            throw Throwables.propagate(e);
        }
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
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface)
            throws SQLException
    {
        return false;
    }
}
