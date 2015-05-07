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
  UUIDGenerator uuidGenerator = Mock()

  def 'should create hive immutable table'()
  {
    setup:
    HiveTableManager tableManager = new HiveTableManager(queryExecutor, dataSourceWriter, uuidGenerator, "/tests-path", hdfsClient, "password")
    def expectedTableLocation = '/tests-path/some/table/in/hdfs'
    def expectedTableName = 'nation'
    def expectedTableNameInDatabase = "nation"

    when:
    def nationDefinition = getNationHiveTableDefinition()
    def nationTableInstance = tableManager.createImmutable(nationDefinition)


    then:
    nationTableInstance.name == expectedTableName
    nationTableInstance.nameInDatabase == expectedTableNameInDatabase

    1 * queryExecutor.executeQuery("DROP TABLE IF EXISTS ${expectedTableNameInDatabase}")
    1 * dataSourceWriter.ensureDataOnHdfs(expectedTableLocation, _)
    1 * queryExecutor.executeQuery(expandDDLTemplate(NATION_DDL_TEMPLATE, expectedTableNameInDatabase, expectedTableLocation))

    when:
    tableManager.drop(nationTableInstance)

    then:
    1 * queryExecutor.executeQuery("DROP TABLE IF EXISTS ${expectedTableNameInDatabase}")
    0 * _
  }

  def 'should create hive mutable table loaded'()
  {
    setup:
    HiveTableManager tableManager = new HiveTableManager(queryExecutor, dataSourceWriter, uuidGenerator, "/tests-path", hdfsClient, "password")

    uuidGenerator.randomUUID() >> "randomSuffix"
    def expectedTableLocation = "/tests-path/mutable_tables/nation_randomSuffix"
    def expectedTableName = "nation"
    def expectedTableNameInDatabase = "nation_randomSuffix"

    when:
    def tableDefinition = getNationHiveTableDefinition()
    def tableInstance = tableManager.createMutable(tableDefinition, LOADED)

    then:
    tableInstance.nameInDatabase == expectedTableNameInDatabase
    tableInstance.name == expectedTableName
    1 * dataSourceWriter.ensureDataOnHdfs(expectedTableLocation, _)
    1 * queryExecutor.executeQuery(expandDDLTemplate(NATION_DDL_TEMPLATE, expectedTableNameInDatabase, expectedTableLocation))

    when:
    tableManager.drop(tableInstance)

    then:
    1 * queryExecutor.executeQuery("DROP TABLE IF EXISTS ${expectedTableNameInDatabase}")
    1 * hdfsClient.delete(expectedTableLocation, _)
    0 * _
  }

  def 'should create hive mutable table created'()
  {
    setup:
    HiveTableManager tableManager = new HiveTableManager(queryExecutor, dataSourceWriter, uuidGenerator, "/tests-path", hdfsClient, "password")

    uuidGenerator.randomUUID() >> "randomSuffix"
    def expectedTableLocation = "/tests-path/mutable_tables/nation_randomSuffix"
    def expectedTableName = "nation"
    def expectedTableNameInDatabase = "nation_randomSuffix"

    when:
    def tableDefinition = getNationHiveTableDefinition()
    def tableInstance = tableManager.createMutable(tableDefinition, CREATED)

    then:
    tableInstance.nameInDatabase == expectedTableNameInDatabase
    tableInstance.name == expectedTableName
    1 * queryExecutor.executeQuery(expandDDLTemplate(NATION_DDL_TEMPLATE, expectedTableNameInDatabase, expectedTableLocation))

    when:
    tableManager.drop(tableInstance)

    then:
    1 * queryExecutor.executeQuery("DROP TABLE IF EXISTS ${expectedTableNameInDatabase}")
    1 * hdfsClient.delete(expectedTableLocation, _)
    0 * _
  }

  private String expandDDLTemplate(String template, String tableName, String location)
  {
    return template.replace('%NAME%', tableName).replaceAll('%LOCATION%', location);
  }

  private static final String NATION_DDL_TEMPLATE = '''
    CREATE TABLE %NAME%(
            n_nationid INT,
            n_name STRING)
            ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
            LOCATION '%LOCATION%'
'''

  def getNationHiveTableDefinition()
  {
    def nationDataSource = Mock(DataSource)
    nationDataSource.getPathSuffix() >> 'some/table/in/hdfs'
    return HiveTableDefinition.builder()
            .setName('nation')
            .setDataSource(nationDataSource)
            .setCreateTableDDLTemplate(NATION_DDL_TEMPLATE)
            .build()
  }
}
