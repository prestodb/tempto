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
  HiveDataSourceWriter dataSourceWriter = Mock()
  HiveTablesFulfiller fulfiller = new HiveTablesFulfiller(queryExecutor, dataSourceWriter)

  def "test fulfill/cleanup"()
  {
    when:
    def nationDataSource = Mock(DataSource)
    nationDataSource.getPath() >> '/some/table/in/hdfs'
    def nationDefinition = HiveTableDefinition.builder()
            .setName('nation')
            .setDataSource(nationDataSource)
            .setCreateTableDDLTemplate('CREATE TABLE nation(' +
            'n_nationid INT,' +
            'n_name STRING) ' +
            'ROW FORMAT DELIMITED FIELDS TERMINATED BY \'|\' ' +
            'LOCATION \'{0}\'')
            .build()

    def requirement = new ImmutableHiveTableRequirement(nationDefinition)
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
    1 * dataSourceWriter.ensureDataOnHdfs(_)
    1 * queryExecutor.executeQuery('CREATE TABLE nation(n_nationid INT,n_name STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY \'|\' LOCATION \'/some/table/in/hdfs\'')

    when:
    fulfiller.cleanup()

    then:
    1 * queryExecutor.executeQuery('DROP TABLE IF EXISTS nation')
    0 * _
  }
}
