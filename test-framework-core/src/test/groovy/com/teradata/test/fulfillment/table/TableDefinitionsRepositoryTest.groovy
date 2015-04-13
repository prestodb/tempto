/*
 * Copyright 2013-2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.fulfillment.table;

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
