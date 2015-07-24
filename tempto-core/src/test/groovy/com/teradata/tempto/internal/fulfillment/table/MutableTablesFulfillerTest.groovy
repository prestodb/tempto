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

import com.teradata.tempto.fulfillment.table.*
import spock.lang.Specification

import static com.google.common.collect.Iterables.getOnlyElement
import static com.teradata.tempto.fulfillment.table.MutableTableRequirement.State.CREATED
import static com.teradata.tempto.fulfillment.table.MutableTableRequirement.State.LOADED

class MutableTablesFulfillerTest
        extends Specification
{
  TableManager tableManager = Mock(TableManager)
  TableManagerDispatcher tableManagerDispatcher = Mock(TableManagerDispatcher)

  void setup()
  {
    tableManagerDispatcher.getTableManagerFor(_ as TableDefinition) >> tableManager
  }

  def "test mutable table fulfill/cleanup"()
  {
    setup:
    def tableDefinition = getTableDefinition("nation")

    def mutableTableInstanceLoaded = new TableInstance("nation", "nation_mutable", tableDefinition)
    def mutableTableRequirementLoaded = new MutableTableRequirement(tableDefinition)

    tableManager.createMutable(tableDefinition, LOADED) >> mutableTableInstanceLoaded

    MutableTablesFulfiller fulfiller = new MutableTablesFulfiller(tableManagerDispatcher)

    when:
    def states = fulfiller.fulfill([mutableTableRequirementLoaded] as Set)

    assert states.size() == 1
    def state = (MutableTablesState) getOnlyElement(states)
    assert state.get('nation') != null
    assert state.get('nation') == mutableTableInstanceLoaded

    then:
    1 * tableManagerDispatcher.getTableManagerFor(tableDefinition) >> tableManager
    1 * tableManager.createMutable(tableDefinition, LOADED) >> mutableTableInstanceLoaded

    when:
    fulfiller.cleanup()

    then:
    0 * _
  }

  def "test mutable named and created table fulfill/cleanup"()
  {
    setup:
    def tableDefinition = getTableDefinition("nation")

    def tableInstanceName = "table_instance_name"
    def mutableTableInstanceNamedCreated = new TableInstance(tableInstanceName, "nation_mutable", tableDefinition)
    def mutableTableRequirementNamedCreated = MutableTableRequirement.builder(tableDefinition)
            .withName(tableInstanceName)
            .withState(CREATED)
            .build()

    tableManager.createMutable(tableDefinition, CREATED) >> mutableTableInstanceNamedCreated

    MutableTablesFulfiller fulfiller = new MutableTablesFulfiller(tableManagerDispatcher)

    when:
    def states = fulfiller.fulfill([mutableTableRequirementNamedCreated] as Set)

    assert states.size() == 1
    def state = (MutableTablesState) getOnlyElement(states)
    assert state.get(tableInstanceName) != null
    assert state.get(tableInstanceName) == mutableTableInstanceNamedCreated

    then:
    1 * tableManagerDispatcher.getTableManagerFor(tableDefinition) >> tableManager
    1 * tableManager.createMutable(tableDefinition, CREATED) >> mutableTableInstanceNamedCreated

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
