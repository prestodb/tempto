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
import com.teradata.tempto.internal.fulfillment.table.TableNameGenerator
import com.teradata.tempto.internal.fulfillment.table.jdbc.JdbcTableManager
import com.teradata.tempto.query.QueryExecutor
import spock.lang.Specification

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
    tableManager = new JdbcTableManager(Mock(QueryExecutor), new TableNameGenerator(), "db_name")
  }

  def 'table without rows does not throw'()
  {
    when:
    tableManager.createImmutable(tableDefinition, TableHandle.tableHandle(tableName))

    then:
    noExceptionThrown()
  }
}
