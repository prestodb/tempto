/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */

package com.teradata.test.internal.fulfillment.table

import com.teradata.test.fulfillment.hive.DataSource
import com.teradata.test.fulfillment.hive.HiveTableDefinition
import com.teradata.test.fulfillment.hive.ImmutableHiveTableRequirement
import com.teradata.test.fulfillment.table.TableDefinition
import com.teradata.test.fulfillment.table.TableManager
import com.teradata.test.fulfillment.table.TableManagerDispatcher
import com.teradata.test.fulfillment.table.TablesState
import com.teradata.test.internal.fulfillment.hive.HiveDataSourceWriter
import com.teradata.test.internal.fulfillment.hive.HiveTableManager
import com.teradata.test.query.QueryExecutor
import spock.lang.Specification

import static com.google.common.collect.Iterables.getOnlyElement

class TablesFulfillerTest
        extends Specification
{
  QueryExecutor queryExecutor = Mock()
  HiveDataSourceWriter dataSourceWriter = Mock()

  def "test hive fulfill/cleanup"()
  {
    when:
    TablesFulfiller fulfiller = getTablesFulfillerFor(new HiveTableManager(queryExecutor, dataSourceWriter))
    def nationDataSource = Mock(DataSource)
    nationDataSource.getPath() >> '/some/table/in/hdfs'
    def nationDefinition = HiveTableDefinition.builder()
            .setName('nation')
            .setDataSource(nationDataSource)
            .setCreateTableDDLTemplate('CREATE TABLE %NAME%(' +
            'n_nationid INT,' +
            'n_name STRING) ' +
            'ROW FORMAT DELIMITED FIELDS TERMINATED BY \'|\' ' +
            'LOCATION \'%LOCATION%\'')
            .build()

    def requirement = new ImmutableHiveTableRequirement(nationDefinition)
    def states = fulfiller.fulfill([requirement] as Set)

    assert states.size() == 1
    def state = getOnlyElement(states)
    assert state.class == TablesState
    def hiveTablesState = (TablesState) state
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

  def getTablesFulfillerFor(HiveTableManager tableManager)
  {
    TableManagerDispatcher dispatcher = new TableManagerDispatcher() {
      @Override
      TableManager getTableManagerFor(TableDefinition tableDefinition)
      {
        return tableManager
      }
    }
    return new TablesFulfiller(dispatcher)
  }
}
