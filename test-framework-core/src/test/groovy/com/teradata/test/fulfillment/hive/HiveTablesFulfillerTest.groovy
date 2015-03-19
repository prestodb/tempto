/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.fulfillment.hive

import com.teradata.test.query.QueryExecutor
import spock.lang.Specification

import static com.google.common.collect.Iterables.getOnlyElement

class HiveTablesFulfillerTest
        extends Specification
{
  QueryExecutor queryExecutor = Mock()
  HiveTablesFulfiller fulfiller = new HiveTablesFulfiller(queryExecutor)

  def "test fulfill/cleanup"()
  {
    when:
    def nationDataSource = Mock(HiveDataSource)
    def requirement = new ImmutableHiveTableRequirement('nation', nationDataSource)
    nationDataSource.getHdfsPath() >> '/some/table/in/hdfs'
    def states = fulfiller.fulfill([requirement] as Set)
    
    assert states.size() == 1
    def state = getOnlyElement(states)
    assert state.class == HiveTablesState
    def hiveTablesState = (HiveTablesState) state
    def nationTableInstance = hiveTablesState.getTableInstance('nation')
    assert nationTableInstance.name == 'nation'
    assert nationTableInstance.nameInDatabase == 'nation'

    then:
    1 * queryExecutor.executeQuery('DROP IF EXISTS TABLE nation')
    then:
    1 * queryExecutor.executeQuery('CREATE TABLE nation LOCATION /some/table/in/hdfs')

    when:
    fulfiller.cleanup()

    then:
    1 * queryExecutor.executeQuery('DROP IF EXISTS TABLE nation')

    then:
    0 * _
  }
}
