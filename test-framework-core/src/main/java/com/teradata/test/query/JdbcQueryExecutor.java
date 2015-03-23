/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static com.teradata.test.query.QueryResult.forSingleIntegerValue;
import static com.teradata.test.query.QueryResult.toSqlIndex;
import static org.slf4j.LoggerFactory.getLogger;

public class JdbcQueryExecutor
        implements QueryExecutor
{
    private static final Logger LOGGER = getLogger(JdbcQueryExecutor.class);

    private JdbcConnectivityParamsState jdbcParamsState;
    private JdbcConnectionsPool jdbcConnectionsPool;

    @Inject
    public JdbcQueryExecutor(JdbcConnectivityParamsState jdbcParamsState, JdbcConnectionsPool jdbcConnectionsPool)
    {
        this.jdbcParamsState = jdbcParamsState;
        this.jdbcConnectionsPool = jdbcConnectionsPool;
    }

    @Override
    public QueryResult executeQuery(String sql, QueryParam[] params)
    {
        LOGGER.debug("executing query {} with params {}", sql, params);
        try (
                Connection connection = jdbcConnectionsPool.connectionFor(jdbcParamsState);
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            setQueryParams(statement, params);

            if (isSelect(sql)) {
                ResultSet rs = statement.executeQuery();
                return QueryResult.builder(rs.getMetaData())
                        .addRows(rs)
                        .build();
            }
            else {
                return forSingleIntegerValue(statement.executeUpdate());
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Error while executing query: " + sql + ", params: " + Arrays.toString(params), e);
        }
    }

    boolean isSelect(String sql)
    {
        return sql.trim().toLowerCase().startsWith("select");
    }

    private static void setQueryParams(PreparedStatement statement, QueryParam[] params)
            throws SQLException
    {
        for (int i = 0; i < params.length; ++i) {
            QueryParam param = params[i];
            statement.setObject(toSqlIndex(i), param.value, param.type.getVendorTypeNumber());
        }
    }
}
