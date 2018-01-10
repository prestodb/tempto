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
package io.prestodb.tempto.fulfillment.table;

import spock.lang.Specification

import static TableHandle.tableHandle;

class TableDefinitionsRepositoryTest
        extends Specification
{
    def 'should add/get table definition to repository'()
    {
        setup:
        def tpchCustomer = Mock(TableDefinition)
        tpchCustomer.tableHandle >> tableHandle("customer")
        def tpcdsCustomer = Mock(TableDefinition)
        tpcdsCustomer.tableHandle >> tableHandle("customer").inSchema('tpcds')
        def noSchemaSample = Mock(TableDefinition)
        noSchemaSample.tableHandle >> tableHandle("noSchemaSample")
        def sampleInSchema = Mock(TableDefinition)
        sampleInSchema.tableHandle >> tableHandle("sample").inSchema('schema')

        def repository = new TableDefinitionsRepository()

        when:
        repository.register(tpchCustomer)
        repository.register(tpcdsCustomer)
        repository.register(noSchemaSample)
        repository.register(sampleInSchema)

        then:
        repository.get(tableHandle("noSchemaSample")) == noSchemaSample
        repository.get(tableHandle("sample").inSchema('schema')) == sampleInSchema
        repository.get(tableHandle("customer")) == tpchCustomer
        repository.get(tableHandle("customer").inSchema('tpcds')) == tpcdsCustomer
        repository.get(tableHandle("noSchemaSample").inSchema('tpcds')) == noSchemaSample

        when:
        repository.get(tableHandle("sample").inSchema('tpcds'))

        then:
        def e = thrown(IllegalStateException)
        e.message == 'no table definition for: tpcds.sample'

        when:
        repository.get(tableHandle("sample"))

        then:
        def e2 = thrown(IllegalStateException)
        e2.message == 'no table definition for: sample'
    }
}
