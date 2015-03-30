/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.query;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static com.beust.jcommander.internal.Maps.newHashMap;
import static com.teradata.test.internal.query.JdbcUtils.dataSource;

public class JdbcConnectionsPool
{
    private final Map<JdbcConnectivityParamsState, DataSource> dataSources = newHashMap();

    public Connection connectionFor(JdbcConnectivityParamsState jdbcParamsState)
            throws SQLException
    {
        if (!dataSources.containsKey(jdbcParamsState)) {
            dataSources.put(jdbcParamsState, dataSource(jdbcParamsState));
        }

        return dataSources.get(jdbcParamsState).getConnection();
    }
}
