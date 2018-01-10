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

import spock.lang.Specification
import spock.lang.Unroll

import static TableHandle.tableHandle

class TableHandleTest
        extends Specification
{
    @Unroll
    def 'parse #tableHandleStr to #expectedTableHandle'()
    {
        expect:
        TableHandle.parse(tableHandleStr) == expectedTableHandle

        where:
        tableHandleStr    | expectedTableHandle
        'table'           | tableHandle('table')
        'schema.table'    | tableHandle('table').inSchema('schema')
        'db.schema.table' | tableHandle('table').inDatabase('db').inSchema('schema')
    }
}
