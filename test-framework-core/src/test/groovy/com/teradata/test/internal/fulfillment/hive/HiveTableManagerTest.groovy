/*
 * Copyright 2015, Teradata, Inc. All rights reserved.
 */
package com.teradata.test.internal.fulfillment.hive

import com.teradata.test.fulfillment.hive.DataSource
import com.teradata.test.fulfillment.hive.HiveTableDefinition
import com.teradata.test.hadoop.hdfs.HdfsClient
import com.teradata.test.internal.hadoop.hdfs.HdfsDataSourceWriter
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
  HiveTableManager tableManager

  void setup()
  {
    tableManager = new HiveTableManager(queryExecutor, dataSourceWriter, uuidGenerator, "/tests-path", hdfsClient, "password")
    uuidGenerator.randomUUID() >> "randomSuffix"
  }

  def 'should create hive immutable table'()
  {
    setup:
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


  def 'should create hive mutable table loaded not partitioned'()
  {
    setup:
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

  def 'should create hive mutable table created not partitioned'()
  {
    setup:
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

  def 'should create hive mutable table loaded partitioned'()
  {
    setup:
    def expectedTableName = "nation"
    def expectedTableNameInDatabase = "nation_randomSuffix"
    def expectedTableLocation = "/tests-path/mutable_tables/${expectedTableNameInDatabase}"
    def expectedPartition0Location = "${expectedTableLocation}/partition_0"
    def expectedPartition1Location = "${expectedTableLocation}/partition_1"

    when:
    def tableDefinition = getPartitionedNationHiveTableDefinition()
    def tableInstance = tableManager.createMutable(tableDefinition, LOADED)

    then:
    tableInstance.nameInDatabase == expectedTableNameInDatabase
    tableInstance.name == expectedTableName
    1 * dataSourceWriter.ensureDataOnHdfs(expectedPartition0Location, _)
    1 * dataSourceWriter.ensureDataOnHdfs(expectedPartition1Location, _)
    1 * queryExecutor.executeQuery(expandDDLTemplate(PARTITIONED_NATION_DDL_TEMPLATE, expectedTableNameInDatabase))
    1 * queryExecutor.executeQuery("ALTER TABLE ${expectedTableNameInDatabase} ADD PARTITION (pc=0) LOCATION '$expectedPartition0Location'")
    1 * queryExecutor.executeQuery("ALTER TABLE ${expectedTableNameInDatabase} ADD PARTITION (pc=1) LOCATION '$expectedPartition1Location'")

    when:
    tableManager.drop(tableInstance)

    then:
    1 * queryExecutor.executeQuery("DROP TABLE IF EXISTS ${expectedTableNameInDatabase}")
    1 * hdfsClient.delete(expectedTableLocation, _)
    0 * _
  }

  def 'should create hive mutable table created partitioned'()
  {
    setup:
    def expectedTableName = "nation"
    def expectedTableNameInDatabase = "nation_randomSuffix"
    def expectedTableLocation = "/tests-path/mutable_tables/${expectedTableNameInDatabase}"
    def expectedPartition0Location = "${expectedTableLocation}/partition_0"
    def expectedPartition1Location = "${expectedTableLocation}/partition_1"

    when:
    def tableDefinition = getPartitionedNationHiveTableDefinition()
    def tableInstance = tableManager.createMutable(tableDefinition, CREATED)

    then:
    tableInstance.nameInDatabase == expectedTableNameInDatabase
    tableInstance.name == expectedTableName
    1 * queryExecutor.executeQuery(expandDDLTemplate(PARTITIONED_NATION_DDL_TEMPLATE, expectedTableNameInDatabase))
    1 * queryExecutor.executeQuery("ALTER TABLE ${expectedTableNameInDatabase} ADD PARTITION (pc=0) LOCATION '$expectedPartition0Location'")
    1 * queryExecutor.executeQuery("ALTER TABLE ${expectedTableNameInDatabase} ADD PARTITION (pc=1) LOCATION '$expectedPartition1Location'")

    when:
    tableManager.drop(tableInstance)

    then:
    1 * queryExecutor.executeQuery("DROP TABLE IF EXISTS ${expectedTableNameInDatabase}")
    1 * hdfsClient.delete(expectedTableLocation, _)
    0 * _
  }

  private String expandDDLTemplate(String template, String tableName, String location = "n/a")
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
    DataSource nationDataSource = mockDataSource('some/table/in/hdfs')
    return HiveTableDefinition.builder()
            .setName('nation')
            .setDataSource(nationDataSource)
            .setCreateTableDDLTemplate(NATION_DDL_TEMPLATE)
            .build()
  }

  private static final String PARTITIONED_NATION_DDL_TEMPLATE = '''
    CREATE TABLE %NAME%(
            n_nationid INT,
            n_name STRING)
            ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
'''

  def getPartitionedNationHiveTableDefinition()
  {
    return HiveTableDefinition.builder()
            .setName('nation')
            .setCreateTableDDLTemplate(PARTITIONED_NATION_DDL_TEMPLATE)
            .addPartition("pc=0", mockDataSource("not/important"))
            .addPartition("pc=1", mockDataSource("not/important"))
            .build()
  }

  private DataSource mockDataSource(String pathSuffix)
  {
    def dataSource = Mock(DataSource)
    dataSource.getPathSuffix() >> pathSuffix
    return dataSource
  }
}
