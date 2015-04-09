/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.fulfillment.hive

import com.teradata.test.fulfillment.hive.DataSource
import com.teradata.test.fulfillment.hive.HiveTableDefinition
import com.teradata.test.hadoop.hdfs.HdfsClient
import com.teradata.test.query.QueryExecutor
import spock.lang.Specification

class HiveTableManagerTest
        extends Specification
{
  QueryExecutor queryExecutor = Mock()
  HiveDataSourceWriter dataSourceWriter = Mock()
  HdfsClient hdfsClient = Mock()

  def 'should create hive immutable table'()
  {
    setup:
    HiveTableManager tableManager = new HiveTableManager(queryExecutor, dataSourceWriter, hdfsClient, "password")

    when:
    def nationDefinition = getNationHiveTableDefinition()
    def nationTableInstance = tableManager.createImmutable(nationDefinition)

    assert nationTableInstance.name == 'nation'
    assert nationTableInstance.nameInDatabase == 'nation'

    then:
    1 * queryExecutor.executeQuery('DROP TABLE IF EXISTS nation')
    1 * dataSourceWriter.ensureDataOnHdfs(_, Optional.empty())
    1 * queryExecutor.executeQuery('CREATE TABLE nation(n_nationid INT,n_name STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY \'|\' LOCATION \'/some/table/in/hdfs\'')

    when:
    tableManager.drop(nationTableInstance)

    then:
    1 * queryExecutor.executeQuery('DROP TABLE IF EXISTS nation')
    0 * _
  }

  def getNationHiveTableDefinition()
  {
    def nationDataSource = Mock(DataSource)
    nationDataSource.getPath() >> '/some/table/in/hdfs'
    return HiveTableDefinition.builder()
            .setName('nation')
            .setDataSource(nationDataSource)
            .setCreateTableDDLTemplate('CREATE TABLE %NAME%(' +
            'n_nationid INT,' +
            'n_name STRING) ' +
            'ROW FORMAT DELIMITED FIELDS TERMINATED BY \'|\' ' +
            'LOCATION \'%LOCATION%\'')
            .build()
  }
}
