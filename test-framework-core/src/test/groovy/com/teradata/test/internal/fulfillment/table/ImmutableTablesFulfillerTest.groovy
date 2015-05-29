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

package com.teradata.test.internal.fulfillment.table

import com.teradata.test.fulfillment.table.*
import spock.lang.Specification

import static com.google.common.collect.Iterables.getOnlyElement

class ImmutableTablesFulfillerTest
        extends Specification
{
  TableManager tableManager = Mock(TableManager)
  TableManagerDispatcher tableManagerDispatcher = Mock(TableManagerDispatcher)

  void setup()
  {
    tableManagerDispatcher.getTableManagerFor(_ as TableDefinition) >> tableManager
  }

  def "test immutable table fulfill/cleanup"()
  {
    setup:
    def tableDefinition = getTableDefinition("nation")
    def tableInstance = new TableInstance("nation", "nation", tableDefinition)
    def requirement = new ImmutableTableRequirement(tableDefinition)

    tableManager.createImmutable(tableDefinition) >> tableInstance

    ImmutableTablesFulfiller fulfiller = new ImmutableTablesFulfiller(tableManagerDispatcher)

    when:
    def states = fulfiller.fulfill([requirement] as Set)

    assert states.size() == 1
    def state = (ImmutableTablesState) getOnlyElement(states)
    assert state.get('nation') == tableInstance

    then:
    1 * tableManagerDispatcher.getTableManagerFor(tableDefinition) >> tableManager
    1 * tableManager.createImmutable(tableDefinition) >> tableInstance

    when:
    fulfiller.cleanup()

    then:
    0 * _
  }

  def getTableDefinition(String tableName)
  {
    def tableDefinition = Mock(TableDefinition)
    tableDefinition.name >> tableName
    return tableDefinition
  }
}
