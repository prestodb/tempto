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

package io.prestodb.tempto.internal.fulfillment.table

import io.prestodb.tempto.fulfillment.table.TableManager
import io.prestodb.tempto.fulfillment.table.hive.HiveDataSource
import io.prestodb.tempto.fulfillment.table.hive.HiveTableDefinition
import io.prestodb.tempto.fulfillment.table.jdbc.RelationalDataSource
import io.prestodb.tempto.fulfillment.table.jdbc.RelationalTableDefinition
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static io.prestodb.tempto.fulfillment.table.TableHandle.tableHandle
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
        psqlTableManager1.tableDefinitionClass >> RelationalTableDefinition
        psqlTableManager1.databaseName >> 'psql1'

        psqlTableManager2 = Mock(TableManager)
        psqlTableManager2.tableDefinitionClass >> RelationalTableDefinition
        psqlTableManager2.databaseName >> 'psql2'

        tableManagers = [
                hive1: hiveTableManager1,
                psql1: psqlTableManager1,
                psql2: psqlTableManager2
        ]
        instance = new DefaultTableManagerDispatcher(tableManagers)
    }

    @Unroll
    def 'test getTableMangerFor #tableHandle'()
    {
        expect:
        instance.getTableManagerFor(definitionClass, tableHandle) == tableManagers[tableManager]

        where:
        definitionClass       | tableHandle                                           | tableManager
        hiveTableDefinition() | hiveTableDefinition().tableHandle                     | 'hive1'
        hiveTableDefinition() | hiveTableDefinition().tableHandle.inDatabase('hive1') | 'hive1'
        jdbcTableDefinition() | jdbcTableDefinition().tableHandle.inDatabase('psql1') | 'psql1'
        jdbcTableDefinition() | jdbcTableDefinition().tableHandle.inDatabase('psql2') | 'psql2'
    }

    def 'multiple databases for table definition class'()
    {
        when:
        instance.getTableManagerFor(jdbcTableDefinition())
        then:
        IllegalStateException e = thrown()
        e.message.contains(
                'Multiple databases found for table: TableHandle{name=name}, definition class \'class io.prestodb.tempto.fulfillment.table.jdbc.RelationalTableDefinition\'. Pick a database from [psql1, psql2]')
    }

    def 'no database found'()
    {
        when:
        instance.getTableManagerFor(jdbcTableDefinition(), jdbcTableDefinition().getTableHandle().inDatabase('unknown'))
        then:
        IllegalStateException e = thrown()
        e.message.contains('No table manager found for table: TableHandle{database=unknown, name=name}')
    }

    def 'wrong table definition'()
    {
        when:
        instance.getTableManagerFor(jdbcTableDefinition(), jdbcTableDefinition().tableHandle.inDatabase('hive1'))
        then:
        IllegalStateException e = thrown()
        e.message.contains('does not match requested table definition class')
    }

    private RelationalTableDefinition jdbcTableDefinition()
    {
        return new RelationalTableDefinition(tableHandle('name'), 'ddl %NAME% %LOCATION%', Mock(RelationalDataSource))
    }

    private HiveTableDefinition hiveTableDefinition()
    {
        return new HiveTableDefinition(tableHandle('name'), 'ddl %NAME% %LOCATION%', Optional.of(Mock(HiveDataSource)), empty(), empty())
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
