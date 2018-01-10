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
package io.prestodb.tempto.fulfillment.table

import io.prestodb.tempto.internal.fulfillment.table.TableName
import spock.lang.Specification
import spock.lang.Unroll

import static TableHandle.tableHandle

class TablesStateTest
        extends Specification
{
    static TableInstance db1_table1 = table('db1', 'table1')

    static TableInstance db1_table2 = table('db1', 'table2')
    static TableInstance db1_schema1_table2 = table('db1', 'schema1', 'table2')

    static TableInstance db1_table3 = table('db1', 'table3')
    static TableInstance db2_table3 = table('db2', 'table3')

    static TableInstance db1_schema2_table4 = table('db1', 'schema2', 'table4')
    static TableInstance db2_schema2_table4 = table('db2', 'schema2', 'table4')

    static TableInstance db1_table5 = table('db1', 'table5')
    static TableInstance db1_schema3_table5 = table('db1', 'schema3', 'table5')

    static TableInstance db1_schema4_table6 = table('db1', 'schema4', 'table6')
    static TableInstance db1_schema5_table6 = table('db1', 'schema5', 'table6')

    static TableInstance db1_schema6_table7 = table('db1', 'schema6', 'table7')

    static TablesState tablesState

    def setup()
    {
        tablesState = new TablesState(
                [db1_table1,
                 db1_table2, db1_schema1_table2,
                 db1_table3, db2_table3,
                 db1_schema2_table4, db2_schema2_table4,
                 db1_table5, db1_schema3_table5,
                 db1_schema4_table6, db1_schema5_table6,
                 db1_schema6_table7
                ],
                'table') {
        }
    }

    @Unroll
    def 'find by #tableHandle'()
    {
        expect:
        tablesState.get(tableHandle) == expectedTable

        where:
        tableHandle                                                 | expectedTable
        tableHandle('table1')                                       | db1_table1
        tableHandle('table2').inSchema('schema1')                   | db1_schema1_table2
        tableHandle('table3').inDatabase('db1')                     | db1_table3
        tableHandle('table4').inDatabase('db1').inSchema('schema2') | db1_schema2_table4
        tableHandle('table5').inDatabase('db1').inSchema('schema3') | db1_schema3_table5
        tableHandle('table5').withNoSchema()                        | db1_table5
        tableHandle('table6').inSchema('schema5')                   | db1_schema5_table6
        tableHandle('table7')                                       | db1_schema6_table7
    }

    def 'no table found'()
    {
        when:
        tablesState.get(tableHandle('unknown'))
        then:
        def ex = thrown(RuntimeException)
        ex.message.contains('No table instance found')
    }

    def 'multiple tables found found'()
    {
        when:
        tablesState.get(tableHandle('table3'))
        then:
        def ex = thrown(RuntimeException)
        ex.message.contains('Multiple table instances found')
    }

    static TableInstance table(String... names)
    {
        TableName name
        if (names.length == 2) {
            name = new TableName(names[0], Optional.empty(), names[1], names[1])
        }
        else if (names.length == 3) {
            name = new TableName(names[0], Optional.of(names[1]), names[2], names[2])
        }
        return new TableInstance(name, new TableDefinition(tableHandle("ignore")) {})
    }
}
