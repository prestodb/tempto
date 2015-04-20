/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.fulfillment.table

import com.teradata.test.fulfillment.table.*
import spock.lang.Specification

import static com.google.common.collect.Iterables.getOnlyElement
import static com.teradata.test.fulfillment.table.MutableTableRequirement.State.CREATED
import static com.teradata.test.fulfillment.table.MutableTableRequirement.State.LOADED

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
    1 * tableManagerDispatcher.getTableManagerFor(mutableTableInstanceLoaded) >>> tableManager
    1 * tableManager.drop(mutableTableInstanceLoaded)
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
    1 * tableManagerDispatcher.getTableManagerFor(mutableTableInstanceNamedCreated) >>> tableManager
    1 * tableManager.drop(mutableTableInstanceNamedCreated)
    0 * _
  }

  def getTableDefinition(String tableName)
  {
    def tableDefinition = Mock(TableDefinition)
    tableDefinition.name >> tableName
    return tableDefinition
  }
}
