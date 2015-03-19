/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.query;

import com.teradata.test.fulfillment.jdbc.JdbcConnectivityParamsState;
import com.teradata.test.query.QueryResult.QueryResultBuilder;

import javax.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static com.teradata.test.fulfillment.jdbc.JdbcUtils.connection;
import static com.teradata.test.query.QueryResult.toSqlIndex;

public class JdbcQueryExecutor
        implements QueryExecutor
{

    private JdbcConnectivityParamsState jdbcParmasState;

    @Inject
    public JdbcQueryExecutor(JdbcConnectivityParamsState jdbcParmasState)
    {
        this.jdbcParmasState = jdbcParmasState;
    }

    @Override
    public QueryResult executeQuery(String sql, QueryParam[] params)
    {
        try (
                Connection connection = connection(jdbcParmasState);
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            setQueryParams(statement, params);

            ResultSet rs = statement.executeQuery();
            return new QueryResultBuilder(rs.getMetaData())
                    .addRows(rs)
                    .build();
        }
        catch (SQLException e) {
            throw new RuntimeException("Error while executing query: " + sql + ", params: " + Arrays.toString(params), e);
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
}
