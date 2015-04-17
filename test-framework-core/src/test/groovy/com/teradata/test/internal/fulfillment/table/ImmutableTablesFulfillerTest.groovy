/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
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
    1 * tableManagerDispatcher.getTableManagerFor(tableInstance) >>> tableManager
    1 * tableManager.drop(tableInstance)
    0 * _
  }

  def getTableDefinition(String tableName)
  {
    def tableDefinition = Mock(TableDefinition)
    tableDefinition.name >> tableName
    return tableDefinition
  }
}
