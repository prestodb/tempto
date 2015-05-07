/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.fulfillment.hive

import com.teradata.test.fulfillment.hive.DataSource
import com.teradata.test.fulfillment.hive.HiveTableDefinition
import com.teradata.test.hadoop.hdfs.HdfsClient
import com.teradata.test.internal.hadoop.hdfs.HdfsDataSourceWriter
import com.teradata.test.internal.uuid.DefaultUUIDGenerator
import com.teradata.test.internal.uuid.UUIDGenerator
import com.teradata.test.query.QueryExecutor
import spock.lang.Specification

import static com.teradata.test.fulfillment.table.MutableTableRequirement.State.CREATED
import static com.teradata.test.fulfillment.table.MutableTableRequirement.State.LOADED

class HiveTableManagerTest
        extends Specification
{
  QueryExecutor queryExecutor = Mock()
  HdfsDataSourceWriter dataSourceWriter = Mock()
  HdfsClient hdfsClient = Mock()
  UUIDGenerator uuidGenerator = new DefaultUUIDGenerator()

  def 'should create hive immutable table'()
  {
    setup:
    HiveTableManager tableManager = new HiveTableManager(queryExecutor, dataSourceWriter, uuidGenerator, "/tests-path", hdfsClient, "password")

    when:
    def nationDefinition = getNationHiveTableDefinition()
    def nationTableInstance = tableManager.createImmutable(nationDefinition)

    assert nationTableInstance.name == 'nation'
    assert nationTableInstance.nameInDatabase == 'nation'

    then:
    1 * queryExecutor.executeQuery('DROP TABLE IF EXISTS nation')
    1 * dataSourceWriter.ensureDataOnHdfs('/tests-path/some/table/in/hdfs', _)
    1 * queryExecutor.executeQuery('CREATE TABLE nation(n_nationid INT,n_name STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY \'|\' LOCATION \'/tests-path/some/table/in/hdfs\'')

    when:
    tableManager.drop(nationTableInstance)

    then:
    1 * queryExecutor.executeQuery('DROP TABLE IF EXISTS nation')
    0 * _
  }

  def 'should create hive mutable table'()
  {
    setup:
    HiveTableManager tableManager = new HiveTableManager(queryExecutor, dataSourceWriter, uuidGenerator, "/tests-path", hdfsClient, "password")

    when:
    def nationDefinition = getNationHiveTableDefinition()
    def nationTableInstanceCreated = tableManager.createMutable(nationDefinition, CREATED)
    def nationTableInstanceLoaded = tableManager.createMutable(nationDefinition, LOADED)

    def nationNameInDatabaseCreated = nationTableInstanceCreated.nameInDatabase
    def nationNameInDatabaseLoaded = nationTableInstanceLoaded.nameInDatabase

    def nationHdfsLocationCreated = "/tests-path/mutable_tables/${nationTableInstanceCreated.nameInDatabase}"
    def nationHdfsLocationLoaded = "/tests-path/mutable_tables/${nationTableInstanceLoaded.nameInDatabase}"

    then:
    1 * dataSourceWriter.ensureDataOnHdfs(_, _) >> { String dataPath, _ ->
      dataPath ==~ /\/tests-path\/mutable_tables\/nation_\w+/
    }

    2 * queryExecutor.executeQuery(
            { it ==~ /CREATE TABLE nation_\w+\(n_nationid INT,n_name STRING\) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\|' LOCATION '\/tests-path\/mutable_tables\/nation_\w+'/ },
            _)

    when:
    tableManager.drop(nationTableInstanceCreated)
    tableManager.drop(nationTableInstanceLoaded)

    then:
    1 * queryExecutor.executeQuery("DROP TABLE IF EXISTS ${nationNameInDatabaseCreated}")
    1 * hdfsClient.delete(nationHdfsLocationCreated, _)

    1 * queryExecutor.executeQuery("DROP TABLE IF EXISTS ${nationNameInDatabaseLoaded}")
    1 * hdfsClient.delete(nationHdfsLocationLoaded, _)

    0 * _
  }

  def getNationHiveTableDefinition()
  {
    def nationDataSource = Mock(DataSource)
    nationDataSource.getPathSuffix() >> 'some/table/in/hdfs'
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
