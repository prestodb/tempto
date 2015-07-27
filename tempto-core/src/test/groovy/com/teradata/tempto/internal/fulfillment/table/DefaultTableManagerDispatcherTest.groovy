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

package com.teradata.tempto.internal.fulfillment.table

import com.teradata.tempto.fulfillment.table.DatabaseSelectionContext
import com.teradata.tempto.fulfillment.table.TableManager
import com.teradata.tempto.fulfillment.table.hive.HiveDataSource
import com.teradata.tempto.fulfillment.table.hive.HiveTableDefinition
import com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDataSource
import com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDefinition
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static java.util.Optional.empty
import static junit.framework.TestCase.fail

class DefaultTableManagerDispatcherTest
        extends Specification
{

  @Shared
  TableManager hiveTableManager1;
  @Shared
  TableManager psqlTableManager1;
  @Shared
  TableManager psqlTableManager2;
  @Shared
  DefaultTableManagerDispatcher instance;
  @Shared
  Map tableManagers

  def setup()
  {
    hiveTableManager1 = Mock(TableManager)
    hiveTableManager1.tableDefinitionClass >> HiveTableDefinition
    hiveTableManager1.databaseName >> 'hive1'

    psqlTableManager1 = Mock(TableManager)
    psqlTableManager1.tableDefinitionClass >> JdbcTableDefinition
    psqlTableManager1.databaseName >> 'psql1'

    psqlTableManager2 = Mock(TableManager)
    psqlTableManager2.tableDefinitionClass >> JdbcTableDefinition
    psqlTableManager2.databaseName >> 'psql2'

    tableManagers = [
            hive1: hiveTableManager1,
            psql1: psqlTableManager1,
            psql2: psqlTableManager2
    ]
    instance = new DefaultTableManagerDispatcher(tableManagers)
  }

  @Unroll
  def 'test getTableMangerFor'()
  {
    expect:
    instance.getTableManagerFor(definitionClass, databaseSelectionContext) == tableManagers[tableManager]

    where:
    definitionClass       | databaseSelectionContext                                    | tableManager
    hiveTableDefinition() | DatabaseSelectionContext.forDatabaseName('hive1')               | 'hive1'
    hiveTableDefinition() | DatabaseSelectionContext.none()                             | 'hive1'
    jdbcTableDefinition() | DatabaseSelectionContext.forDatabaseName('psql1')               | 'psql1'
    jdbcTableDefinition() | DatabaseSelectionContext.forDatabaseName('psql2')               | 'psql2'
    jdbcTableDefinition() | new DatabaseSelectionContext(empty(), Optional.of('psql2')) | 'psql2'
  }

  private TableManager d()
  {
    hiveTableManager1
  }

  def 'multiple databases for table definition class'()
  {
    when:
    instance.getTableManagerFor(jdbcTableDefinition(), DatabaseSelectionContext.none())
    then:
    IllegalStateException e = thrown()
    e.message.contains('Multiple databases found for table definition class \'class com.teradata.tempto.fulfillment.table.jdbc.JdbcTableDefinition\'')
  }

  def 'no database found'()
  {
    when:
    instance.getTableManagerFor(jdbcTableDefinition(), DatabaseSelectionContext.forDatabaseName('unknown'))
    then:
    IllegalStateException e = thrown()
    e.message.contains('No table manager found for database name \'unknown\'.')
  }

  def 'wrong table definition'()
  {
    when:
    instance.getTableManagerFor(jdbcTableDefinition(), DatabaseSelectionContext.forDatabaseName('hive1'))
    then:
    IllegalStateException e = thrown()
    e.message.contains('does not match requested table definition class')
  }

  private JdbcTableDefinition jdbcTableDefinition()
  {
    return new JdbcTableDefinition('name', 'ddl %NAME% %LOCATION%', Mock(JdbcTableDataSource))
  }

  private HiveTableDefinition hiveTableDefinition()
  {
    return new HiveTableDefinition('name', 'ddl %NAME% %LOCATION%', Optional.of(Mock(HiveDataSource)), empty())
  }

  def failWith(String message, Closure closure)
  {
    try {
      closure()
      fail('expected exception to be thrown here')
    }
    catch (Exception e) {
      assert e.message.contains(message)
    }
  }
}
