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

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static com.teradata.tempto.internal.query.JdbcUtils.dataSource;

public class JdbcConnectionsPool
{
    private final Map<JdbcConnectivityParamsState, DataSource> dataSources = newHashMap();

    public Connection connectionFor(JdbcConnectivityParamsState jdbcParamsState)
            throws SQLException
    {
        if (!dataSources.containsKey(jdbcParamsState)) {
            dataSources.put(jdbcParamsState, dataSource(jdbcParamsState));
        }

        Connection connection = dataSources.get(jdbcParamsState).getConnection();
        if (jdbcParamsState.prepareStatement.isPresent()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(jdbcParamsState.prepareStatement.get());
            }
        }
        return connection;
    }
}
