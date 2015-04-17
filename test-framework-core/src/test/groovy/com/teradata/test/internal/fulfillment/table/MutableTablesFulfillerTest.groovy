/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.fulfillment.table

import com.teradata.test.fulfillment.table.*
import spock.lang.Specification

import static com.google.common.collect.Iterables.getOnlyElement

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

    def mutableTableInstance = new TableInstance("nation", "nation_mutable", tableDefinition)
    def mutableTableRequirement = new MutableTableRequirement(tableDefinition)

    tableManager.createMutable(tableDefinition) >> mutableTableInstance

    MutableTablesFulfiller fulfiller = new MutableTablesFulfiller(tableManagerDispatcher)

    when:
    def states = fulfiller.fulfill([mutableTableRequirement] as Set)

    assert states.size() == 1
    def state = (MutableTablesState) getOnlyElement(states)
    assert state.get('nation') != null
    assert state.get('nation') == mutableTableInstance

    then:
    1 * tableManagerDispatcher.getTableManagerFor(tableDefinition) >> tableManager
    1 * tableManager.createMutable(tableDefinition) >> mutableTableInstance

    when:
    fulfiller.cleanup()

    then:
    1 * tableManagerDispatcher.getTableManagerFor(mutableTableInstance) >>> tableManager
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
