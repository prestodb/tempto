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

package com.teradata.tempto.query;

import com.google.common.base.Throwables;
import com.teradata.tempto.context.TestContext;
import org.slf4j.Logger;

import javax.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static com.teradata.tempto.query.QueryResult.forSingleIntegerValue;
import static com.teradata.tempto.query.QueryResult.toSqlIndex;
import static com.teradata.tempto.query.QueryType.SELECT;
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
            throws SQLException
    {
        this.jdbcParamsState = requireNonNull(jdbcParamsState, "jdbcParamsState is null");
        this.jdbcConnectionsPool = requireNonNull(jdbcConnectionsPool, "jdbcConnectionsPool is null");
        this.jdbcUrl = jdbcParamsState.url;
        testContext.registerCloseCallback(context -> this.close());
        openConnection();
    }

    public void openConnection()
    {
        closeConnection();
        try {
            connection = jdbcConnectionsPool.connectionFor(jdbcParamsState);
        }
        catch (SQLException e){
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
        return execute(sql, isSelect(sql), params);
    }

    @Override
    public QueryResult executeQuery(String sql, QueryType queryType, QueryParam... params)
            throws QueryExecutionException
    {
        return execute(sql, queryType == SELECT, params);
    }

    @Override
    public Connection getConnection()
    {
        return requireNonNull(connection, "connection is null");
    }

    private QueryResult execute(String sql, boolean isSelect, QueryParam... params)
            throws QueryExecutionException
    {
        LOGGER.debug("executing on {} query {} with params {}", jdbcUrl, sql, params);

        try {
            if (params.length == 0) {
                return executeQueryNoParams(sql, isSelect);
            }
            else {
                return executeQueryWithParams(sql, isSelect, params);
            }
        }
        catch (SQLException e) {
            throw new QueryExecutionException(e);
        }
    }

    private QueryResult executeQueryNoParams(String sql, boolean isSelect)
            throws SQLException
    {
        try (Statement statement = getConnection().createStatement()) {
            if (isSelect) {
                ResultSet rs = statement.executeQuery(sql);
                return QueryResult.forResultSet(rs);
            }
            else {
                return forSingleIntegerValue(statement.executeUpdate(sql));
            }
        }
    }

    // TODO - remove this method as soon as Presto supports prepared statements
    private QueryResult executeQueryWithParams(String sql, boolean isSelect, QueryParam[] params)
            throws SQLException
    {
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            setQueryParams(statement, params);

            if (isSelect) {
                ResultSet rs = statement.executeQuery();
                return QueryResult.forResultSet(rs);
            }
            else {
                return forSingleIntegerValue(statement.executeUpdate());
            }
        }
    }

    boolean isSelect(String sql)
    {
        sql = sql.trim().toLowerCase();
        return sql.startsWith("select") || sql.startsWith("show");
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
}
