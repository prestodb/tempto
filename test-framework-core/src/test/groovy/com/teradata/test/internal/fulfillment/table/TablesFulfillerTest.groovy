/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.fulfillment.table

import com.teradata.test.fulfillment.table.*
import spock.lang.Specification

import static com.google.common.collect.Iterables.getOnlyElement

class TablesFulfillerTest
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

    def immutableTableInstance = new TableInstance("nation", "nation_immutable", tableDefinition)
    def immutableTableRequirement = new ImmutableTableRequirement(tableDefinition)

    def mutableTableInstance = new TableInstance("nation", "nation_mutable", tableDefinition)
    def mutableTableRequirement = new MutableTableRequirement(tableDefinition)

    tableManager.createImmutable(tableDefinition) >> immutableTableInstance
    tableManager.createMutable(tableDefinition) >> mutableTableInstance

    TablesFulfiller fulfiller = new TablesFulfiller(tableManagerDispatcher)

    when:
    def states = fulfiller.fulfill([immutableTableRequirement, mutableTableRequirement] as Set)

    assert states.size() == 1
    def state = (TablesState) getOnlyElement(states)
    assert state.getTableInstance('nation') != null
    assert state.getImmutableTableInstance('nation') == immutableTableInstance
    assert state.getMutableTableInstance('nation') == mutableTableInstance

    then:
    2 * tableManagerDispatcher.getTableManagerFor(tableDefinition) >> tableManager
    1 * tableManager.createImmutable(tableDefinition) >> immutableTableInstance
    1 * tableManager.createMutable(tableDefinition) >> mutableTableInstance

    when:
    fulfiller.cleanup()

    then:
    1 * tableManagerDispatcher.getTableManagerFor(immutableTableInstance) >>> tableManager
    1 * tableManagerDispatcher.getTableManagerFor(mutableTableInstance) >>> tableManager
    1 * tableManager.drop(immutableTableInstance)
    1 * tableManager.drop(mutableTableInstance)
    0 * _
  }

  def getTableDefinition(String tableName)
  {
    def tableDefinition = Mock(TableDefinition)
    tableDefinition.name >> tableName
    return tableDefinition
  }
}
