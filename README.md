# Tempto - test framework

This project allows developers to write and execute tests for SQL databases running
on Hadoop. Individual test requirements such as data generation, HDFS file copy/storage
of generated data and schema creation are expressed declaratively and are automatically
fulfilled by the framework. Developers can write tests using Java (using a TestNG like
paradigm and AssertJ style assertion) or by providing query files with expected results.


## Prerequisites

To use Tempto you need a Java 1.8 runtime.

Other dependencies will vary based on the set of features you are using.

### HDFS

For automatic provisioning HDFS based tables you need:
* Running Hadoop cluster with [WebHDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/WebHDFS.html)
* We suggest that cluster support [XAttr metadata](https://hadoop.apache.org/docs/r2.6.1/hadoop-project-dist/hadoop-hdfs/ExtendedAttributes.html).
Having that feature enabled improves test performance slightly.


## Basic concepts

* _Requirement_ - the set of resources a test needs in order to run, e.g. data stored HDFS, Hive tables, etc.
* _Test case_ - test of single functionality e.g. query.
* _Test group_ - logical grouping of test cases. For example one could define a join group, a group by group, 
                 a window function group etc. in order to test different SQL functionality.
* _Test context_ - object used to store context information specific to a test.
* _Java test_ - test written in Java, annotated with @Test, following the TestNG convention.
* _File based test_ - test written by specifying the query to run and the corresponding result using files.


## Setup

To use Tempto you need machine to execute test code and machine or machines to run tested application.

Note that the machine running the framework and tests does not have to
be the same as the machine or set of machines running your SQL on Hadoop database.
For example, typical configuration is:
* the framework and tests running on a Jenkins slave
* tested application cluster is running on separate machines and is accessed over network

## Logging

Tempto uses SLF4J for logging.

### Log file per test

If you are using log4j as your SLF4J backend we provide an appender which allows logging output of
each test and suite fulfillment process to separate files. To use that configure LOG4J appender as below:

```
log4j.rootLogger=INFO, TEST_FRAMEWORK_LOGGING_APPENDER
log4j.appender.TEST_FRAMEWORK_LOGGING_APPENDER=com.teradata.logging.TestFrameworkLoggingAppender
log4j.category.com.teradata.tempto=DEBUG
log4j.category.org.reflections=WARN
```

With this appender for each test suite run new logs directory is created within /tmp/tempto_logs. Name of directory
corresponds to time when Tempto is run (e.g. /tmp/tempto_logs/2015-04-22_15-23-09).
Log messages coming from different tests are logged to separate files.

Example contents of log directory:
```
com.facebook.presto.tests.hive.TestAllDatatypesFromHiveConnector.testSelectAllDatatypesOrc_2015-04-22_15-23-09
com.facebook.presto.tests.hive.TestAllDatatypesFromHiveConnector.testSelectAllDatatypesParquetFile_2015-04-22_15-23-09
com.facebook.presto.tests.hive.TestAllDatatypesFromHiveConnector.testSelectAllDatatypesRcfile_2015-04-22_15-23-09
com.facebook.presto.tests.hive.TestAllDatatypesFromHiveConnector.testSelectAllDatatypesTextFile_2015-04-22_15-23-09
com.facebook.presto.tests.hive.TestAllDatatypesFromHiveConnector.testSelectBinaryColumnTextFile_2015-04-22_15-23-09
com.facebook.presto.tests.hive.TestAllDatatypesFromHiveConnector.testSelectVarcharColumnForOrc_2015-04-22_15-23-09
SUITE_2015-04-22_15-23-09
```

If you want to override root location of logs you can use com.teradata.tempto.root.logs.dir
```
java -Dcom.teradata.tempto.root.logs.dir=/my/root/logs/dir ...
```

### logging test id

Tempto sets up 'test_id' entry in SLF4J logs context (MDC). It corresponds to name of test currently being run.
It can be used in logging patterns. If you are using log4j as a backend you can use it as below:
```
log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender
log4j.appender.CONSOLE.Target=System.out
log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout
log4j.appender.CONSOLE.layout.conversionPattern=%d{yyyy-MM-dd HH:mm:ss} %-5p %c{1}:%L [%X{test_id}] - %m%n
```

## Configuration

The test execution environment is configured via a hierarchical YAML file. The YAML file
is by default loaded from the classpath and must be named `tempto-configuration.yaml`.
If `tempto-configuration-local.yaml` file is present on classpath it will also be loaded and will
overwrite settings defined in `tempto-configuration.yaml` file.

Configuration files locations can be overidden by using following java system properties:
 
 * `tempto.configuration` - for overriding global configuration file location
 
 * `tempto.configuration.local` - for overriding local configuration file location  

```
 java ... -Dtempto.configuration=classpath:my_configuration.yaml \
          -Dtempto.configuration.local=file:/tmp/my_local_configuration.yaml
```

Configuration files can also be overridden by passing command line arguments to
tests runner based on `TemptoRunner` Java class. See below for details.

### Variable expansion

The configuration file can contain variable placeholders in form of `${variable_name}`.

Variables placeholders in templates are expanded during tests execution based on:
* System environment variables
* Other configuration keys in the file

Example configuration file with variables:
```YAML
common:
  master_host: localhost

hdfs:
  host: ${common.master_host}
```

### Configuration sections

#### **hdfs**

This section is used to configure how the framework accesses HDFS. During the fulfillment process,
the framework accesses HDFS through the WebHDFS REST API. In your Java tests you may also
access HDFS through the `HdfsClient` interface. Below is an example hdfs configuration section:

```YAML
hdfs:                     # HDFS related settings
  username: hdfs          # username to use for accessing HDFS
  webhdfs:
    host: master          # hostname exposing HDFS REST interface
    port: 50070           # port of HDFS REST interface
```

* **databases**

Currently we support only JDBC based database connections. Multiple such connections may be defined in this
section of the configuration. By default, tests and queries are executed using the connection named "default".
You can change "default" to point to whichever JDBC connection you want to query against (see example below).
You will need to define a connection for every database that will need to be accessed during the test run.
For example, if you'd like the framework to create tables for you in Hive, you'll have to specify connection
parameters for Hive.

Remark: Every database connection defined in the configuration will be initialized during framework startup,
        even if no tests using that type of connection are scheduled to be executed.
        This will be improved in the future.

```YAML
databases:           # database connections
  default:           # default connection to query against
    alias: presto    # points to connection defined below that you'd like to use as the default
 
  hive:              # connection named hive
    jdbc_driver_class: org.apache.hive.jdbc.HiveDriver                                # fully qualified JDBC driver classname
    jdbc_url: jdbc:hive2://master:10000                                               # database url
    jdbc_user: hdfs                                                                   # database user
    jdbc_password: na                                                                 # database password
    jdbc_pooling: false                                                               # (optional) should connection pooling be used (it does not work for Hive due to driver issues)
    jdbc_jar: tempto-hive-jdbc/build/libs/hive-jdbc-fat.jar                           # (optional) Path to jar containing database driver. Required if jar is not present in global classpath.
    table_manager_type: hive
 
  presto:           # connection named presto
    jdbc_driver_class: com.facebook.presto.jdbc.PrestoDriver
    jdbc_url: jdbc:presto://localhost:8080/hive/default
    jdbc_user: hdfs
    jdbc_password: na

  psql:           # posgresql
    jdbc_driver_class: org.postgresql.Driver
    jdbc_url: jdbc:postgresql://localhost:5432/postgres
    jdbc_user: blah
    jdbc_password: blah
    jdbc_pooling: true
    table_manager_type: jdbc

```

If we want framework to provision tables we need to specify table_manager_type for database connection.
Currently we support two table manager types:
 * hive: manages tables in HIVE. Is applicable to HDFS backed hive database connection.
 * jdbc: manages tables in standard SQL JDBC based database. Tables are populated using "INSERT INTO " statements.

* **tests**

This section is used to configure various properties used during test execution.

```YAML
tests:
  hdfs:
    path: /tempto  # where to store test data on HDFS
```

## Java based tests

### Example

See com.teradata.tempto.examples.SimpleQueryTest in tempto-examples module.

### Requirements

Tests may declare requirements that are fulfilled by the framework during suite/test initialization.

#### Explicit RequirementsProvider

You can specify Requirements for your test through the `@Requires` annotation. Test methods and whole classes
can be annotated with `@Requires`. If a class is annotated with `@Requires`, behavior is the same as when
each test method in that class is annotated with `@Requires`. The parameter passed to `@Requires` must be a class
that extends the `RequirementsProvider` interface.
This interface has a single method, `getRequirements()` that returns an instance of a `Requirement` object.

Remark: *It seems that a better way of passing in requirements would be to supply
         `@Requires` with an instance but Java only allows constant argument annotations.*

Here's an example implementation of the `RequirementProvider` interface:

```Java
private final class SimpleTestRequirements
        implements RequirementsProvider
{

    @Override
    public Requirement getRequirements()
    {
        // ensure TPCH nation table is available
        return new ImmutableHiveTableRequirement(NATION);
    }
}
```

In this case, `SimpleTestRequirements` encapsulated the single requirement of an immutable Hive table called nation.

The implementation of `RequirementProvider` is then passed as an argument to the `@Requires` annotation:

```Java
    @Test(groups = "query")
    @Requires(SimpleTestRequirements.class)
    public void selectAllFromNation()
    {
        assertThat(query("select * from nation")).hasRowsCount(25);
    }
```

If multiple `@Requires` annotations are stacked on top of one another on the same method or class, then
the requirements they return are combined.

#### Test class being RequirementsProvider

Alternatively one can make Test class itself implement `RequirementProvider`. Then requirements
returned by the implemented `getRequirements` method will be applied to all test methods in class.
 
```Java
private final class MyTestClass
        implements RequirementsProvider
{

    @Override
    public Requirement getRequirements()
    {
        // ensure TPCH nation table is available
        return new ImmutableHiveTableRequirement(NATION);
    }
    
    @Test
    public void someTestMethod() {
        assertThat(query("select * from nation")).hasRowsCount(25);
    }
}
```

### Requirement Types

This section lists the supported `Requirement` implementations that you can return
from `RequirementProvider#getRequirement()`.

#### ImmutableTableRequirement

When this requirement is fulfilled, it will create a table in the underlying database. 
It is called immutable table because the contract with the test developer is that it will not be altered by the test code.
For immutable tables test code is not allowed to
 * drop it
 * re-create it under a different name
 * delete/insert data.

The immutable tables can be reused across tests. If 10 tests require an immutable table,
that table will only be created once and the framework assumes it will be available for all tests.

Best way to create `ImmutableTableRequirement` is to use `TableRequirements.immutableTable` factory
method.

##### Table Definitions

`ImmutableHiveTableRequirement` is parametrized with `TableDefinition`.
Target database in which table is created depends on `TableDefinition` instance passed as `ImmutableTableRequirement`
parameter.

Currently we support:
 * `HiveTableDefinition` - allowing defining tables in Hive. Requires a database configuration entry with `hive` table manager defined.
 * `JDBCTableDefinition` - allowing defining table in JDBC databases. Requires a database configuration entry with `jdbc` table manager defined.

Remark: If multiple table managers of the same type (e.g. hive) are defined in the configuration, you have to provide
        database name explicitly in `ImmutableHiveTableRequirement`.

###### HiveTableDefinition

`HiveTableDefinition` include name, schema and dataSource.
`HiveTableDefinitionBuilder` can be use to create new table definition. You need to provide:
 * DDL template containing following template placeholders:
   * `%NAME%` - table name in hive
   * `%LOCATION%` - data location on HDFS
 * Optional specification of table partitions.
 * `HiveDataSource` responsible for providing table data which is saved to HDFS

Certain commonly used tables, such as those in the TPC-H benchmark, are defined as constants and can
be found in `com.teradata.tempto.fulfillment.table.hive.tpch.TpchTableDefinitions`.

For example this is how the nation table is built:

```Java
    public static final HiveTableDefinition NATION =
            HiveTableDefinition.builder()
                    .setName("nation")
                    .setCreateTableDDLTemplate("" +
                            "CREATE EXTERNAL TABLE %NAME%(" +
                            "   n_nationkey     INT," +
                            "   n_name          STRING," +
                            "   n_regionkey     INT," +
                            "   n_comment       STRING) " +
                            "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                            "LOCATION '%LOCATION%'")
                    .setDataSource(new TpchDataSource(TpchTable.NATION, 1.0))
                    .build();
```

###### JdbcTableDefinition

`JdbcTableDefinition` include name, schema and dataSource. You need to provide:
 * DDL template containing following template placeholders:
   * `%NAME%` - actual table name in database
 * `JdbcTableDataSource` responsible for providing list of rows which are inserted into created table.

Example:
```Java
    static {
        JdbcTableDataSource dataSource = () -> ImmutableList.<List<Object>>of(
                ImmutableList.of(1, "x"),
                ImmutableList.of(2, "y")
        ).iterator();
        TEST_TABLE_DEFINITION = jdbcTableDefinition("test_table", "CREATE TABLE %NAME% (a int, b varchar(100))", dataSource);
    }
```

#### MutableTableRequirement

When this requirement is fulfilled it will create a table in underlying database.
But unlike ImmutableTableRequirement framework does not assume table will not be modified. 
Each test using ImmutableTableRequirement will have a separate instance of table created in
database with unique name.

To access name of table in database from test code `MutableTablesState` must be used.
See following example.

```Java
    private static class MutableTableRequirements implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements()
        {
            mutableTable(NATION, "table", LOADED)
        }
    }

    @Test(groups = "query")
    @Requires(MutableTableRequirements.class)
    public void testWithMutableTable()
    {
        MutableTablesState mutableTablesState = testContext().getDependency(MutableTablesState.class);
        TableInstance tableInstance = mutableTablesState.get("table");
        assertThat(query("select * from " + tableInstance.getNameInDatabase())).hasAnyRows();
    }
```

One can request that mutable table is in one of three states:
 * `PREPARED` - no table is created actually - but `MutableTableState` entry is created for table and unique name is generated
 * `CREATED` - table is created but is not populated with data
 * `LOADED` - table is created and populated with data

#### Advanced requirement concepts

##### allOf and compose

There are cases when you want to run the same test with different requirements sets.
E.g. same query for multiple tables with same schema but stored in different file formats on HDFS.

For sake of such cases Tempto provides mechanism of generating requirements sets using `Requirements.allOf` and `Requirements.compose` methods.

See following example:
```
    private static class MultiSetRequirements implements RequirementsProvider
    {
        @Override
        public Requirement getRequirements()
        {
            allOf(compose(mutableTable(NATION_TEXT, "nation", LOADED),
                          mutableTable(REGION_TEXT, "region", LOADED)),
                  compose(mutableTable(NATION_ORC, "nation", LOADED),
                          mutableTable(REGION_ORC, "region", LOADED)),
                  compose(mutableTable(NATION_PARQUET, "nation", LOADED),
                          mutableTable(REGION_PARQUET, "region", LOADED))
            )
        }
    }

    @Test(groups = "query")
    @Requires(MultiSetRequirements.class)
    public void testJoinNationAndRegion()
    {
        MutableTablesState mutableTablesState = testContext().getDependency(MutableTablesState.class);
        TableInstance nation = mutableTablesState.get("nation");
        TableInstance region = mutableTablesState.get("region");
        assertThat(query("select * from "
                         + nation.getNameInDatabase() + ","
                         + region.getNameInDatabase "
                         + "where nation.region_key = region.key"))
                   .hasAnyRows();
    }
```

When tests are executed three instances of testJoinNationAndRegion test will be run.
One for each of the requirements passed to `allOf` method. Note that there are three requirements
passed to `allOf` method but each one is composite one and internally consists of two `mutableTable` requirements.
Composition is performed using `compose` methods. The following requirements sets will be used:
 * both `nation` and `region` tables stored as `TEXT`
 * both `nation` and `region` tables stored as `ORC`
 * both `nation` and `region` tables stored as `PARQUET`

### Executing queries

Queries are executed via implementations of the `QueryExecutor` interface. Currently the only implementation
is `JdbcQueryExecutor`. Each database configured in the YAML file will have
its own query executor with name the same as name of the database.
To retrieve that executor and issue queries against that database you can use the
`ThreadLocalTestContextHolder.testContext().getDependency(...)` as shown below.

```Java
    // execute query against the default database
    QueryResult defaultQueryResult = QueryExecutor.query("SELECT * FROM nation");
    
    // Retrieve QueryExecutor for another, non-default, database
    QueryExecutor prestoQueryExecutor = ThreadLocalTestContextHolder.testContext().getDependency(QueryExecutor.class, "presto");
    QueryResult queryResultPresto = prestoQueryExecutor.query("SELECT * FROM nation");
```

Alternatively test writer can inject named QueryExecutor to the test. See below.

To use default QueryExecutor one can use helper static method `QueryExecutor.query` (see examples).

### Query assertions

The `QueryAssert` class allows you to perform AssertJ style assetions on `QueryResult` objects. For more information
on the available types of assertions, check the methods of `QueryAssert`.

Example assertions:

```Java
      @Requires(TpchRequirements.class)
      @Test
      public void testContainsExactlyInOrder()
      {
          assertThat(query("SELECT n.nationkey, n.name, r.name FROM nation n " +
                  "INNER JOIN region r ON n.regionkey = r.regionkey " +
                  "WHERE name like 'A%' AND n.created > ? ORDER BY n.name", LocalDate.parse("2015-01-01")))
                  .hasColumns(INTEGER, VARCHAR, VARCHAR)
                  .containsOnly(
                          row(1, "ALGERIA", "AFRICA"),
                          row(7, "ARGENTINA", "SOUTH AMERICA"));
      }
```

### Injecting dependencies into tests

As an alternative to using `ThreadLocalTestContextHolder.testContext()` explicitly
dependencies can be injected to test by framework. Field injection and argument injection
through `setUp()` method is supported.

See example:
```
public class InjectionTest
        extends ProductTest
{
    @Inject
    MutableTablesState mutableTablesState;

    @BeforeTestWithContext
    @Inject
    public void setUp(
            ImmutableTablesState immutableTablesState,
            @Named("hdfs.username") String hdfsUsername
    )
    {
        immutableTablesState.get(...)
    }
...
```

In above example three objects are injected by the framework:
 * `MutableTablesState` is injected through field injection
 * `ImmutableTablesState` through parameter injection in `setUp` method
 * configuration value for key `hdfs.username` through parameter injection in `setUp` method

## Convention based SQL query tests

SQL query tests can be written in simpler form without using any Java code.

It is done by providing the framework with a sql query file and a file with
the expected result. These tests are called convention based because of the directory structure
assumed by the framework, namely the directory convention.

Moreover you can define Hive and JDBC datasets that can be queried in your tests.
These datasets files contain the data along with the corresponding DDL.
For examples take a look at files in the `tempto-examples/src/main/resources/sql-tests`
directory. The directory tree looks like the following:

```Shell
~/repos/tempto/tempto-examples/src/main/resources$ tree .
.
├── sql-tests
│   ├── datasets
│   │   ├── sample_table.data
│   │   ├── sample_table.data-revision
│   │   └── sample_table.ddl
│   └── testcases
│       ├── generated
│       │   └── nation.generator
│       ├── nation
│       │   ├── after
│       │   ├── allRows.result
│       │   ├── allRows.sql
│       │   └── before
│       ├── sample_table
│       │   ├── allRows.result
│       │   └── allRows.sql
│       └── sample_table_insert
│           └── insert.sql
├── suites.json
└── tempto-configuration.yaml
```

### Data sets

Data sets are stored in `sql-tests/datasets directory`. To create an example table, you will need to create three files:

* `TABLE_NAME.ddl` - DDL for data.
* `TABLE_NAME.data` - file containing raw data.
* `TABLE_NAME.data-revision` - file with data marker. If you change your data, you should also increase this
revision marker, so the new table data is automatically reloaded.

#### TABLE_NAME.ddl
Contains template for SQL for creating table. 
Header specifies type of table manager which should be used for this table definition. Can be `jdbc` or `hive`.

##### HIVE tables

Example:
```
-- type: hive
CREATE TABLE %NAME% (
  id INT,
  name STRING
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
LOCATION '%LOCATION%'
```

Template must contain:
 * `%NAME%` pattern which will be replaced with table name.
 * `%LOCATION%` pattern which will replaced with HDFS path where data will be uploaded.

##### JDBC tables

Example:
```
-- type: jdbc
CREATE TABLE %NAME% (
  id INT,
  name VARCHAR(100)
)
```

Template must contain `%NAME%` pattern which will be replaced with table name.

#### TABLE_NAME.data

Contains table data.

##### HIVE tables

For HIVE table manager content is not analyzed. Data file is just uploaded to HDFS.
 
##### JDBC tables

Example:
```
-- delimiter: |; trimValues: false; types: INTEGER|VARCHAR
3|A|
2|B|
1|C|
```
Header parameters are:
 * delimiter - data columns delimiter (default: |)
 * trimValues - remove leading and trailing whitespace from data values (default: false)
 * types - column types (optional). If skipped strict column type checking will not be performed. Supported column types are:
   - CHAR, VARCHAR, LONGVARCHAR, LONGNVARCHAR - character string
   - BOOLEAN - true/false
   - BIT - 0/1
   - TINYINT, SMALLINT, INTEGER, BIGINT - integer value
   - REAL, FLOAT, DOUBLE - floating point value
   - DECIMAL, NUMERIC - decimal point value
   - DATE - date; format: `yyyy-[m]m-[d]d`
   - TIME, TIME_WITH_TIMEZONE - time; format: `hh:mm:ss`
   - TIMESTAMP, TIMESTAMP_WITH_TIMEZONE - timestamp; format: `yyyy-M-d H:m:s.SSS`

#### TABLE_NAME.data-revision

Currently only HIVE table manager makes use of that. It should contain any string, which must be updated when
table contents is changed. It is used for determining if resending table data to HDFS cluster is required or not.

### Tests

Test case files are stored in `sql-tests/testcases_ directory`. The directory right under the
`testcases_directory` is the logical equivalent of a TestNG test class. Each logical test is pair
of files:
 
* **TEST.sql** - query to be invoked where the first line of file can be a SQL comment specifying
query execution requirements:

```
-- database: hive; groups: example_smoketest,group2; tables: nation;
SELECT * FROM nation
```

This test contains queries that should be executed against the Hive database.
The test is part of two separate TestNG groups: example_smoketest and group2.

In above example queries will be run against database hive (see database key in a first row).
Test require immutable table nation to be created and loaded before query execution (see tables key). 

It is possible to have more than one query in `*.sql` file.
To do that separate queries using semicolon and put in separate lines.

E.g
```
-- database: hive; tables: sample_hive_table; mutable_tables: sample_hive_table|created|sample_table_created, sample_hive_table|prepared|sample_table_prepared; groups: insert
INSERT INTO TABLE ${mutableTables.hive.sample_table_created} SELECT * from sample_hive_table;
SELECT * from ${mutableTables.hive.sample_table_created}
```

If multiple queries are defined in the `*.sql` file results
of the last query will be checked against result file.

* **TEST.result** - file with the expected result of the query. The first line can be a SQL comment
with query assertion requirements:

```
-- delimiter: |; ignoreOrder: false; types: INTEGER|VARCHAR|INTEGER|VARCHAR
0|ALGERIA|0| haggle. carefully final deposits detect slyly agai|
...
```

Above we set the `|` character as the delimiter, we ignore the order of rows during comparison and
that we expect the columns to be of the specified types.

Both SQL and result files honor comments which begin with `---` prefix.

It is possible to define both queries and results in single **TEST.sql** file.
Such file is divided into sections. Each section is separated by _--!_ prefix.
First section contains global properties. Next sections contain queries and
results separately. Each section can override global properties.
Additionally, each section can have a name. An example of such file would be:

```
-- database: hive; groups: example_smoketest,group2
-- delimiter: |; ignoreOrder: false; types: INTEGER|VARCHAR|INTEGER|VARCHAR
--! name: query_1
SELECT * FROM nation WHERE id=0
--!
0|ALGERIA|0| haggle. carefully final deposits detect slyly agai|
--! name: query_2
-- groups: additional_group
SELECT * FROM nation WHERE id=1
1|USA|1| foo bar|
--!
```

You are also able to add custom _before_ and _after_ scripts for your test. Those are executed
before and after each test case.
TODO more info on scripts, what they should be named, what they can contain.

#### Using tables across databases.

It is possible (which is useful for testing Presto for example) to use a table which is created in one database (e.g. hive, psql)
while sending test query to other database (e.g. Presto).
Take a look at the example below. Here query is issued via Presto JDBC, while nation table could be created somewhere else. In order to determine
where nation should be created (find appropriate requirements) below matching flow is used:
 - If database is specified explicitly as prefix for table name (e.g psql.nation) then requirement for
   table in that database will be generated. Note that database must have a table_manager with type
   matching table manager of a table or error will be thrown.
 - If no database is specified explicitly then if there is only one database with table manager
   of type equal to table type then this database will be picked up.
 - As fallback database to which test query would be send is used. Database type manager type vs table type checking is done.

```
-- database: presto; tables: nation;
SELECT * FROM nation
```

Here you have an example with an immutable table requirement from database psql.

```
-- database: presto; tables: psql.nation;
SELECT * FROM nation
```

### Generated tests

TODO (not used right now)

## Tests running

### Running tests from your IDE

Java based tests can be simply run as TestNG tests.

There is no such possibility for convention based tests right now.

### Using tempto runner

Tests can be run using the tempto runner. This is a java library which enables user to create an
executable jar which make it easy for the user to run the tests.
To see a verbose description of all the execution options run:

```Shell
$ java -jar tempto-examples/build/libs/tempto-examples-all.jar --help
```

To run example tests command would look like this:

```Shell
$ java -jar tempto-examples/build/libs/tempto-examples-all.jar
```

**Tests selection**

By default all tests found in classpath are executed but user may limit that.

<table>
    <tr>
        <td>--groups</td>
        <td>List of groups to be executed.</td>
    </tr>
    <tr>
        <td>--tests</td>
        <td>List of tests to be executed. For java based tests test name is just fully qualified method name
            e.g. com.teradata.tempto.examples.SimpleQueryTest.selectCountFromNation. For sql convention based tests name
            looks like: sql_tests.testcases.sample_table.allRows. Tests which name ends with one of patterns specified
            in --tests parameter will be executed.
        </td>
    </tr>
    <tr>
        <td>--exclude-groups</td>
        <td>List of test groups which should be excluded from execution.</td>
    </tr>
</table>


## Acknowledgements

A special thanks to the entire Hadapt team for inspiring the architecture of this framework.


