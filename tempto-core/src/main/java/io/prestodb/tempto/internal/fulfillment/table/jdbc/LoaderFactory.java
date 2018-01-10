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

package io.prestodb.tempto.internal.fulfillment.table.jdbc;

import io.prestodb.tempto.query.QueryExecutor;
import org.slf4j.Logger;

import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

class LoaderFactory
{
    private static final Logger LOGGER = getLogger(LoaderFactory.class);

    Loader create(QueryExecutor queryExecutor, String tableName)
            throws SQLException
    {
        List<JDBCType> columnTypes = queryExecutor.executeQuery("SELECT * FROM " + tableName + " WHERE 1=2").getColumnTypes();

        try {
            return new BatchLoader(queryExecutor, tableName, columnTypes.size());
        }
        catch (SQLException sqlException) {
            LOGGER.warn("Unable to insert data with PreparedStatement", sqlException);
            return new InsertLoader(queryExecutor, tableName, columnTypes);
        }
    }
}
