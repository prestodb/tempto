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
package com.teradata.tempto.runner.fulfillment.table

import com.teradata.tempto.fulfillment.table.TableDefinition;
import spock.lang.Specification;

public class TableDefinitionsRepositoryTest
        extends Specification
{
  
  def 'should add/get table definition to repository'()
  {
    setup:
    def tableDefinition = Mock(TableDefinition)
    tableDefinition.name >> "table1"

    def repository = new TableDefinitionsRepository()

    when:
    repository.register(tableDefinition)

    then:
    repository.getForName("table1") == tableDefinition

    when:
    repository.getForName("table2")

    then:
    def e = thrown(IllegalStateException)
    e.message == 'no table definition for: table2'
  }
}
