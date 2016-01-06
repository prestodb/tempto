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

package com.teradata.tempto.fulfillment.table

import com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDataSource
import com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDefinition
import com.teradata.tempto.internal.configuration.EmptyConfiguration
import com.teradata.tempto.internal.fulfillment.table.TableNameGenerator
import com.teradata.tempto.internal.fulfillment.table.jdbc.JdbcTableManager
import com.teradata.tempto.query.QueryExecutor
import spock.lang.Specification

import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.ResultSetMetaData

public class JdbcTableManagerTest
        extends Specification
{
  static JdbcTableDefinition tableDefinition
  static JdbcTableManager tableManager
  static String tableName

  def setup()
  {
    tableName = "name"
    tableDefinition = JdbcTableDefinition.jdbcTableDefinition(tableName, "CREATE TABLE %NAME%(col1 INT)",
            { Collections.<List<Object>>emptyList().iterator() } as JdbcTableDataSource)

    def mockExecutor = Mock(QueryExecutor)
    def mockConnection = Mock(Connection)
    def mockMetadata = Mock(DatabaseMetaData)
    mockExecutor.connection >> mockConnection
    mockConnection.getMetaData() >> mockMetadata

    def mockResultSet = Mock(ResultSet)
    mockMetadata.getTables(_, _, _, _) >> mockResultSet
    mockResultSet.getMetaData() >> Mock(ResultSetMetaData)
    tableManager = new JdbcTableManager(mockExecutor, new TableNameGenerator(), "db_name", EmptyConfiguration.emptyConfiguration())
  }

  def 'table without rows does not throw'()
  {
    when:
    tableManager.createImmutable(tableDefinition, TableHandle.tableHandle(tableName))

    then:
    noExceptionThrown()
  }
}
