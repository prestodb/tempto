/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive

import com.teradata.test.query.QueryExecutor
import spock.lang.Specification

import static com.google.common.collect.Iterables.getOnlyElement
import static com.teradata.test.fulfillment.hive.HiveType.INT
import static com.teradata.test.fulfillment.hive.HiveType.STRING

class HiveTablesFulfillerTest
        extends Specification
{
  QueryExecutor queryExecutor = Mock()
  HiveTablesFulfiller fulfiller = new HiveTablesFulfiller(queryExecutor)

  def "test fulfill/cleanup"()
  {
    when:
    def nationDataSource = Mock(HiveDataSource)
    def nationDefinition = HiveTableDefinition.builder()
            .setName('nation')
            .setDataSource(nationDataSource)
            .addColumn('n_nationid', INT)
            .addColumn('n_name', STRING)
            .build()

    def requirement = new ImmutableHiveTableRequirement(nationDefinition)
    nationDataSource.ensureDataOnHdfs() >> '/some/table/in/hdfs'
    def states = fulfiller.fulfill([requirement] as Set)
    
    assert states.size() == 1
    def state = getOnlyElement(states)
    assert state.class == HiveTablesState
    def hiveTablesState = (HiveTablesState) state
    def nationTableInstance = hiveTablesState.getTableInstance('nation')
    assert nationTableInstance.name == 'nation'
    assert nationTableInstance.nameInDatabase == 'nation'

    then:
    1 * queryExecutor.executeQuery('DROP TABLE IF EXISTS nation')
    then:
    1 * queryExecutor.executeQuery('CREATE TABLE nation(n_nationid INT,n_name STRING) LOCATION \'/some/table/in/hdfs\'')

    when:
    fulfiller.cleanup()

    then:
    1 * queryExecutor.executeQuery('DROP TABLE IF EXISTS nation')

    then:
    0 * _
  }
}
