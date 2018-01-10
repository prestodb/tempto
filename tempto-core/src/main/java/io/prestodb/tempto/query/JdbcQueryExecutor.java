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

package io.prestodb.tempto.query;

import com.google.common.base.Throwables;
import io.prestodb.tempto.context.TestContext;
import org.slf4j.Logger;

import javax.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static io.prestodb.tempto.query.QueryResult.forSingleIntegerValue;
import static io.prestodb.tempto.query.QueryResult.toSqlIndex;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

public class JdbcQueryExecutor
        implements QueryExecutor
{
    private static final Logger LOGGER = getLogger(JdbcQueryExecutor.class);

    private final String jdbcUrl;
    private final JdbcConnectivityParamsState jdbcParamsState;
    private final JdbcConnectionsPool jdbcConnectionsPool;

    private Connection connection = null;

    @Inject
    public JdbcQueryExecutor(JdbcConnectivityParamsState jdbcParamsState,
            JdbcConnectionsPool jdbcConnectionsPool,
            TestContext testContext)
    {
        this.jdbcParamsState = requireNonNull(jdbcParamsState, "jdbcParamsState is null");
        this.jdbcConnectionsPool = requireNonNull(jdbcConnectionsPool, "jdbcConnectionsPool is null");
        this.jdbcUrl = jdbcParamsState.url;
        testContext.registerCloseCallback(context -> this.close());
    }

    public void openConnection()
    {
        closeConnection();
        try {
            connection = jdbcConnectionsPool.connectionFor(jdbcParamsState);
        }
        catch (SQLException e) {
            throw Throwables.propagate(e);
        }
    }

    public void closeConnection()
    {
        if (connection != null) {
            try {
                connection.close();
            }
            catch (SQLException e) {
                LOGGER.debug("Exception happened during closing connection.", e);
            }
            connection = null;
        }
    }

    @Override
    public QueryResult executeQuery(String sql, QueryParam... params)
            throws QueryExecutionException
    {
        return execute(sql, params);
    }

    @Override
    public Connection getConnection()
    {
        if (connection == null) {
            openConnection();
        }
        return connection;
    }

    private QueryResult execute(String sql, QueryParam... params)
            throws QueryExecutionException
    {
        if (connection == null) {
            openConnection();
        }

        sql = removeTrailingSemicolon(sql);

        LOGGER.debug("executing on {} query {} with params {}", jdbcUrl, sql, params);

        try {
            if (params.length == 0) {
                return executeQueryNoParams(sql);
            }
            else {
                return executeQueryWithParams(sql, params);
            }
        }
        catch (SQLException e) {
            throw new QueryExecutionException(e);
        }
    }

    private QueryResult executeQueryNoParams(String sql)
            throws SQLException
    {
        try (Statement statement = getConnection().createStatement()) {
            if (statement.execute(sql)) {
                return QueryResult.forResultSet(statement.getResultSet());
            }
            else {
                return forSingleIntegerValue(statement.getUpdateCount());
            }
        }
    }

    // TODO - remove this method as soon as Presto supports prepared statements
    private QueryResult executeQueryWithParams(String sql, QueryParam[] params)
            throws SQLException
    {
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            setQueryParams(statement, params);

            if (statement.execute()) {
                return QueryResult.forResultSet(statement.getResultSet());
            }
            else {
                return forSingleIntegerValue(statement.getUpdateCount());
            }
        }
    }

    private static void setQueryParams(PreparedStatement statement, QueryParam[] params)
            throws SQLException
    {
        for (int i = 0; i < params.length; ++i) {
            QueryParam param = params[i];
            statement.setObject(toSqlIndex(i), param.value, param.type.getVendorTypeNumber());
        }
    }

    @Override
    public void close()
    {
        closeConnection();
    }

    private String removeTrailingSemicolon(String sql)
    {
        return sql.trim().replaceAll(";$", "");
    }
}
