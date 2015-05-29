/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.teradata.test.internal.fulfillment.hive

import com.teradata.test.fulfillment.hive.DataSource
import com.teradata.test.fulfillment.hive.HiveTableDefinition
import com.teradata.test.hadoop.hdfs.HdfsClient
import com.teradata.test.internal.hadoop.hdfs.HdfsDataSourceWriter
import com.teradata.test.internal.uuid.UUIDGenerator
import com.teradata.test.query.QueryExecutor
import com.teradata.test.query.QueryResult
import com.teradata.test.query.QueryType
import spock.lang.Specification

import java.sql.JDBCType

import static com.teradata.test.fulfillment.table.MutableTableRequirement.State.CREATED
import static com.teradata.test.fulfillment.table.MutableTableRequirement.State.LOADED

class HiveTableManagerTest
        extends Specification
{
  String ROOT_PATH = "/tests-path"
  String MUTABLE_TABLES_PATH = ROOT_PATH + HiveTableManager.MUTABLE_TABLES_DIR

  QueryExecutor queryExecutor = Mock()
  HdfsDataSourceWriter dataSourceWriter = Mock()
  HdfsClient hdfsClient = Mock()
  UUIDGenerator uuidGenerator = Mock()
  HiveTableManager tableManager

  void setup()
  {
    uuidGenerator.randomUUID() >> "randomSuffix"
    tableManager = new HiveTableManager(queryExecutor, dataSourceWriter, uuidGenerator, ROOT_PATH, hdfsClient, "password");
  }

  def 'should drop all tables'()
  {
    when:
    tableManager.dropAllTables();

    then:
    1 * queryExecutor.executeQuery("SHOW TABLES", QueryType.SELECT) >> QueryResult.forSingleValue(JDBCType.VARCHAR, "some_table")
    1 * queryExecutor.executeQuery("DROP TABLE IF EXISTS some_table")
    1 * hdfsClient.delete(MUTABLE_TABLES_PATH, _)
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

    1 * dataSourceWriter.ensureDataOnHdfs(expectedTableLocation, _)
    1 * queryExecutor.executeQuery(expandDDLTemplate(NATION_DDL_TEMPLATE, expectedTableNameInDatabase, expectedTableLocation))
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
  }

  def 'should create hive mutable table created not partitioned'()
  {
    setup:
    def expectedTableLocation = MUTABLE_TABLES_PATH + "nation_randomSuffix"
    def expectedTableName = "nation"
    def expectedTableNameInDatabase = "nation_randomSuffix"

    when:
    def tableDefinition = getNationHiveTableDefinition()
    def tableInstance = tableManager.createMutable(tableDefinition, CREATED)

    then:
    tableInstance.nameInDatabase == expectedTableNameInDatabase
    tableInstance.name == expectedTableName
    1 * queryExecutor.executeQuery(expandDDLTemplate(NATION_DDL_TEMPLATE, expectedTableNameInDatabase, expectedTableLocation))
  }

  def 'should create hive mutable table loaded partitioned'()
  {
    setup:
    def expectedTableName = "nation"
    def expectedTableNameInDatabase = "nation_randomSuffix"
    def expectedTableLocation = MUTABLE_TABLES_PATH + expectedTableNameInDatabase
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
  }

  def 'should create hive mutable table created partitioned'()
  {
    setup:
    def expectedTableName = "nation"
    def expectedTableNameInDatabase = "nation_randomSuffix"
    def expectedTableLocation = MUTABLE_TABLES_PATH + expectedTableNameInDatabase
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
