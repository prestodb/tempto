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
import static junit.framework.TestCase.fail

class TableRequirementFulfillerTest
        extends Specification
{
  private static final String DATABASE_NAME = "database_name"
  private static final String OTHER_DATABASE_NAME = "other_database_name"

  TableManager tableManager = Mock(TableManager)
  TableManager otherTableManager = Mock(TableManager)
  TableManagerDispatcher tableManagerDispatcher = Mock(TableManagerDispatcher)

  void setup()
  {
    tableManager.databaseName >> DATABASE_NAME
    otherTableManager.databaseName >> OTHER_DATABASE_NAME
    tableManagerDispatcher.getTableManagerFor(_ as TableDefinition, Optional.empty()) >> tableManager
    tableManagerDispatcher.getTableManagerFor(_ as TableDefinition, Optional.of(DATABASE_NAME)) >> tableManager
    tableManagerDispatcher.getTableManagerFor(_ as TableDefinition, Optional.of(OTHER_DATABASE_NAME)) >> otherTableManager
  }

  def "test mutable table fulfill/cleanup"()
  {
    setup:
    def tableDefinition = getTableDefinition("nation")

    def mutableTableInstanceLoaded = new TableInstance("nation", "nation_mutable", tableDefinition)
    def mutableTableRequirementLoaded = MutableTableRequirement.builder(tableDefinition).build()

    tableManager.createMutable(tableDefinition, LOADED, _) >> mutableTableInstanceLoaded

    MutableTablesFulfiller fulfiller = new MutableTablesFulfiller(tableManagerDispatcher)

    when:
    def states = fulfiller.fulfill([mutableTableRequirementLoaded] as Set)

    assert states.size() == 1
    def state = (MutableTablesState) getOnlyElement(states)
    assert state.get('nation') != null
    assert state.get('nation') == mutableTableInstanceLoaded
    assert state.get('nation', Optional.of(DATABASE_NAME)) == mutableTableInstanceLoaded

    then:
    1 * tableManager.createMutable(tableDefinition, LOADED, _) >> mutableTableInstanceLoaded

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

    tableManager.createMutable(tableDefinition, CREATED, _) >> mutableTableInstanceNamedCreated

    MutableTablesFulfiller fulfiller = new MutableTablesFulfiller(tableManagerDispatcher)

    when:
    def states = fulfiller.fulfill([mutableTableRequirementNamedCreated] as Set)

    assert states.size() == 1
    def state = (MutableTablesState) getOnlyElement(states)
    assert state.get(tableInstanceName) != null
    assert state.get(tableInstanceName) == mutableTableInstanceNamedCreated
    assert state.get(tableInstanceName, Optional.of(DATABASE_NAME)) == mutableTableInstanceNamedCreated

    then:
    1 * tableManager.createMutable(tableDefinition, CREATED, _) >> mutableTableInstanceNamedCreated

    when:
    fulfiller.cleanup()

    then:
    0 * _
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
    assert state.get('nation', Optional.of(DATABASE_NAME)) == tableInstance

    then:
    1 * tableManager.createImmutable(tableDefinition) >> tableInstance

    when:
    fulfiller.cleanup()

    then:
    0 * _
  }

  def "test same immutable tables on different databases"()
  {
    setup:
    def tableDefinition = getTableDefinition("nation")
    def tableInstance = new TableInstance("nation", "nation", tableDefinition)
    def requirement = new ImmutableTableRequirement(tableDefinition, Optional.of(DATABASE_NAME))
    def requirementOnOtherDatabase = new ImmutableTableRequirement(tableDefinition, Optional.of(OTHER_DATABASE_NAME))

    tableManager.createImmutable(tableDefinition) >> tableInstance
    otherTableManager.createImmutable(tableDefinition) >> tableInstance

    ImmutableTablesFulfiller fulfiller = new ImmutableTablesFulfiller(tableManagerDispatcher)

    when:
    def states = fulfiller.fulfill([requirement, requirementOnOtherDatabase] as Set)

    assert states.size() == 1
    def state = (ImmutableTablesState) getOnlyElement(states)
    assert state.get('nation', Optional.of(DATABASE_NAME)) == tableInstance
    assert state.get('nation', Optional.of(OTHER_DATABASE_NAME)) == tableInstance
    try {
      state.get('nation')
      fail('Expected exception')
    }
    catch (RuntimeException ex) {
      assert ex.message.contains('Please specify database.')
    }

    then:
    1 * tableManager.createImmutable(tableDefinition) >> tableInstance
    1 * otherTableManager.createImmutable(tableDefinition) >> tableInstance

    when:
    fulfiller.cleanup()

    then:
    0 * _
  }

  def "test same immutable tables on same databases with different database aliases are filtered"()
  {
    setup:
    def tableDefinition = getTableDefinition("nation")
    def tableInstance = new TableInstance("nation", "nation", tableDefinition)
    def requirement = new ImmutableTableRequirement(tableDefinition, Optional.of(DATABASE_NAME))
    def requirementOnDefault = new ImmutableTableRequirement(tableDefinition)

    tableManager.createImmutable(tableDefinition) >> tableInstance

    ImmutableTablesFulfiller fulfiller = new ImmutableTablesFulfiller(tableManagerDispatcher)

    when:
    def states = fulfiller.fulfill([requirement, requirementOnDefault] as Set)

    assert states.size() == 1
    def state = (ImmutableTablesState) getOnlyElement(states)
    assert state.get('nation', Optional.of(DATABASE_NAME)) == tableInstance
    assert state.get('nation') == tableInstance

    then:
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
