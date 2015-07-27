# Tempto - test framework

This project allows developers to write and execute tests for SQL databases running
on Hadoop. Individual test requirements such as data generation, HDFS file copy/storage
of generated data and schema creation are expressed declaratively and are automatically
fulfilled by the framework. Developers can write tests using Java (using a TestNG like
paradigm and AssertJ style assertion) or by providing query files with expected results.


## Prerequisites

You will need the following software to be installed on the machine running the
framework:

* Java >= 1.8
* Python >= 2.6 (if you use the custom launcher that comes with the framework)

Currently we only support HDFS as a datastore. That means that on your Hadoop
cluster you'll need the following:

* Running Hadoop cluster with [WebHDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/WebHDFS.html)
* We suggest that cluster support [XAttr metadata](https://hadoop.apache.org/docs/r2.6.0/hadoop-project-dist/hadoop-hdfs/ExtededAttributes.html). 
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

Note: the machine running the framework and tests does not have to
be the same as the machine or set of machines running your SQL on Hadoop database.
For example, the framework and tests can be running on a Jenkins slave and the
framework will remotely interact with your cluster.

**TODO:** we should include here information which jar to use as dependency, and where to put file with properties,
how to setup maven plugins...

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

## Example test run

The steps below will run the example tests that come with the framework. They act as a basic
smoketest to ensure that you've setup everything properly.

* Build the framework:


```Shell
$ cd tempto
$ ./gradlew clean build
BUILD SUCCESSFUL
   
$Total time: 2 mins 47.263 secs
```

* Set configuration properties in the following configuration file: `tempto/tempto-examples/src/main/resources/tempto-configuration.yaml`.
The most important settings you'll need to change are the WebHDFS host, the Hive and Presto JDBC URLs. For more details please
refer to the **Configuration** section below.

* Ensure that WebHDFS, Hive and Presto are running.

* Run tests using the provided test launcher:

```Shell
$ cd tempto
$ bin/tempto \
     --tests-classpath tempto-examples/build/libs/tempto-examples-all.jar \
     --tests-package=com.teradata.tempto.examples \
     --exclude-groups quarantine \
     --report-dir /tmp/test-reports
Loading TestNG run, this may take a sec.  Please don't flip tables (╯°□°）╯︵ ┻━┻
...
[2015-04-02 15:21:48] Completed 18 tests
[2015-04-02 15:21:48] 17 SUCCEEDED      /      1 FAILED      /      0 SKIPPED
[2015-04-02 15:21:48] For tests logs see: tempto_logs/2015-04-02_15-15-16
See /tmp/test-reports/index.html for detailed results.
```

* The framework will print on your console whether a test passed or failed. A more detailed report
is available at `tempto/tempto-core-build-reports/html/index.html`. Note that
one test (`com.teradata.tempto.examples.SimpleQueryTest.failingTest`) is made to fail on purpose.


## Configuration

The test execution environment is configured via a hierarchical YAML file. The YAML file
is loaded from the classpath and it must be named `tempto-configuration.yaml`. 
If `tempto-configuration-local.yaml` file is present on classpath it will be also loaded and will
overwrite settings defined in `tempto-configuration.yaml` file.

Configuration files locations can be overidden by using following java system properties:
 
 * `tempto.configuration` - for overriding global configuration file location
 
 * `tempto.configuration.local` - for overriding local configuration file location  

```
 java ... -Dtempto.configuration=classpath:my_configuration.yaml \
          -Dtempto.configuration.local=file:/tmp/my_local_configuration.yaml
```

If you start tests using helper `tempto` script you can use 
`--tempto-configuration` and `--tempto-configuration-local` options to override
configuration files. 

The file
contains the following configuration sections:

* **hdfs**

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
You will need to define a connection for every database that will need to be access during the test run.
For example, if you'd like the framework to create tables for you in Hive, you'll have to specify connection
parameters for Hive.
TODO: what if you've already created all your tables in hive, do you still need
to provide connection parameters?

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
    jdbc_jar: tempto-hive-jdbc/build/libs/hive-jdbc-fat.jar                   # (optional) jar to be used for obtaining database driver. Should be used in case when we cannot have it in global classpath due to class conflicts. (e.g. hive driver conflicts with presto driver)
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

Current limitation is that only one table manager for each type can be defined.

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
can be annotated with `@Requires`. If a class is annotated with `@Requires`, that is the same having every
test method in that class be annoted with `@Requires`. The parameter passed to `@Requires` must be a class
that extends the `RequirementsProvider` interface. This
interface has a single method, `getRequirements()` that returns an instance of a `Requirement` object. You may notice
that a better way of passing in requirements would be to supply `@Requires` with an instance but Java only allows
constant argument annotations.

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

Alternatively one can make Test class itself implement RequirementProvider. Then requirements
returned by the implemeneted `getRequirements` method will be applied to all test methods in class.
 
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
It is called immutable because the contract with the test developer is that they will not, within 
the logic of their test, alter the state of the table (drop it, re-create it under a different name, delete data). 
This is done so that requirements can be recycled between tests. If 10 tests require an immutable table, 
that table will only be created once and the framework assumes it will be available for all tests.

##### Table Definitions

ImmutableHiveTableRequirement is parametrized with `TableDefinition`.
Target database in which table is created depends on `TableDefinition` instance passed as ImmutableTableRequirement
parameter. Currently we have only one implementation: `HiveTableDefinition` allowing defining tables in Hive.
Using `ImmutableTableRequirement` with `HiveTableDefinition` requires that a connection named `hive` is 
defined in configuration Yaml.

`HiveTableDefinition` include name, schema and dataSource. 
`HiveTableDefinitionBuilder` can be use to create new definition. You need to provide table name,
create table DDL template (_\{0\}_ is substituted with HDFS file location) and `DataSource`.

Certain commonly used tables, such as those in the TPC-H benchmark, are defined as constants and can
be found in `com.teradata.tempto.fulfillment.table.hive.tpch.TpchTableDefinitions`.

TODO: we need to clarify to the user how they create tables.

For example this is how the nation table is built:

```Java
    public static final HiveTableDefinition NATION =
        HiveTableDefinition.builder()
                .setName("nation")
                .setCreateTableDDLTemplate("" +
                        "CREATE TABLE nation(" +
                        "   n_nationkey     INT," +
                        "   n_name          STRING," +
                        "   n_regionkey     INT," +
                        "   n_comment       STRING) " +
                        "ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' " +
                        "LOCATION '{0}'")
                .setDataSource(new TpchDataSource(TpchTable.NATION, 1.0))
                .build();
```

Best way to crate `ImmutableTableRequirement` is to use `TableRequirements.immutableTable` factory
method.

#### MutableTableRequirement

When this requirement is fulfilled it will crate a table in underlying database. 
But unlike ImmutableTableRequirement framework does not assume table will not be modified. 
Each test using  ImmutableTableRequirement will have a separate instance of table created in 
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
 * PREPARED - no table is crated actually - but MutableTableState entry is crated for table and unique name is generated
 * CRATED - table is crated but is not populated with data 
 * LOADED - table is crated and populated with data 

### Executing queries

Queries are executed via implementations of the `QueryExecutor` interface. Currently the only implementation
is `JdbcQueryExecutor`. Each database configured in the YAML file will have its own query executor with the
same name. To retrieve that executor and issue queries against that database you can use the
`ThreadLocalTestContextHolder.testContext().getDependency(...)` as shown below.

```Java
    // execute query against the default database
    QueryResult defaultQueryResult = QueryExecutor.query("SELECT * FROM nation");
    
    // Retrieve QueryExecutor for another, non-default, database
    QueryExecutor prestoQueryExecutor = ThreadLocalTestContextHolder.testContext().getDependency(QueryExecutor.class, "presto");
    QueryResult queryResultPresto = prestoQueryExecutor.query("SELECT * FROM nation");
```

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

## Convention based file tests

Query tests can be written by providing the framework with a sql query file and a file with
the expected result. These tests are called convention based because of the directory structure
assumed by the framework, namely the directory convention.

Moreover you can define datasets that can be queried in your tests. These dataset files contain
the data along with the corresponding DDL. For examples take a look at files in the
`tempto-examples/src/main/resources/sql-tests` directory. The directory tree looks
like the following:

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

* TABLE_NAME.ddl - DDL for data.
* TABLE_NAME.data - file containing raw data.
* TABLE_NAME.data-revision - file with data marker. If you change your data, you should also increase this
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
 * %NAME% pattern which will be replaced with table name.
 * %LOCATION% pattern which will replaced with HDFS path where data will be uploaded.

##### JDBC tables

Example:
```
-- type: jdbc
CREATE TABLE %NAME% (
  id INT,
  name VARCHAR(100)
)
```

Template must contain %NAME% pattern which will be replaced with table name.

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
 * types - column types (required). Supported column types are:
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

Currently only HIVE table manager makes use of that. It should contain any string, which must be updated when table contents is changed.

TODO: where should be user create the sql-tests directory? Right now it's resources under the examples dir, where should they put it?

### Tests

Test case files are stores in `sql-tests/testcases_ directory`. The directory right under the
`testcases_directory` is the logical equivalent of a TestNG test class. Each logical test is pair
of files:
 
* **TEST.sql** - query to be invoked where the first line of file can be a SQL comment specifying
query execution requirements:

```
-- database: hive; groups: example_smoketest,group2; tables: nation;
SELECT * FROM nation
```

This test contains queries that should be executed against the Hive database. Only results
of the last query will be checked agains result file. In addition, the test is part of two
separate TestNG groups: example_smoketest and group2.

In above example queries will be run against database hive (see database key in a first row).
Test require immutable table nation to be created and loaded before query execution (see tables key). 


* **TEST.result** - file with the expected result of the query. The first line can be a SQL comment
with query assertion requirements:

```
-- delimiter: |; ignoreOrder: false; types: INTEGER|VARCHAR|INTEGER|VARCHAR
0|ALGERIA|0| haggle. carefully final deposits detect slyly agai|
...
```

Above we set the _|_ character as the delimiter, we ignore the order of rows during comparison and
that we expect the columns to be of the specified types. You always need to provide types because
when checking the result, the framework will have to cast String to the given type. This is of
course terrible for performance, but you're trading that for convenience (for example a test writer
that cannot/does not want to write Java).

Both SQL and result files honor comments which begin with _---_ prefix.

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

It is possible (which is useful for testing presto for example), to use a table which is created in one database (e.g. hive, psql) while sending test query to other database (e.g. presto).
Take a look at the example below. Here query is issued via presto JDBC, while nation table could be created somewhere else. In order to determine 
where nation should be created (find appropriate requirements) below matching flow is used:
 - If database is specified explicitly as prefix for table name (e.g psql.nation) then requirement for table in that database will be generated. Note that database must have a table_manager with type matching table manager of a table or error will be thrown.
 - If no database is specified explicitly then if there is only one database with table manager of type equal to table type then this database will be picked up.
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

File convention based tests: TODO

### Shell tempto launcher

Tests can be run using the `bin/tempto` script. This is a wrapper around a command
line invocation of the TestNG JVM. For a verbose description of all the execution options
supported by the `bin/tempto` script run:

```Shell
$ ./bin/tempto --help
```

**Basic parameters**

For running tests you have to specify at the least the following arguments:

* **classpath** - classpath will be scanned to find tests to be run and it may be either
a set of jars or directories or a mix of both.
* **tests-package** - defines java package containing tests. For Java based tests only tests residing in
this package (or some child package of this) will be executed. Additionally all convention based
tests found in class path will be executed.

Example run command would look like this:

```Shell
$ ./bin/tempto --tests-classpath tempto-examples/build/libs/tempto-examples-all.jar \
                     --tests-package=com.teradata.tempto.examples
```

In above example we set classpath to contain two entries:

* tempto-examples/src/main/resources - this is directory entry
* tempto-examples/build-libs/tempto-examples.jar

And tests package is set to com.teradata.tempto.examples.

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
        <td>--classes</td>
        <td>List of fully qualified java classess to be executed. Applies to java based tests only.</td>
    </tr>
    <tr>
        <td>--exclude-groups</td>
        <td>List of test groups which should be excluded from execution.</td>
    </tr>
</table>


**Debugging**

If you want to run tests from tempto script under debuger use --debug parameter. When this parameter is
specified Tempto will suspend execution at beginning and wait for debugger on TCP port _5005_.

```Shell
$ bin/tempto \
     --tests-classpath tempto-examples/build/libs/tempto-examples-all.jar \
     --tests-package=com.teradata.tempto.examples \
     --exclude-groups quarantine \
     --report-dir /tmp/test-reports
     --debug
Loading TestNG run, this may take a sec.  Please don't flip tables (╯°□°）╯︵ ┻━┻
Listening for transport dt_socket at address: 5005
```

At this point you may use your IDE of choice to connect to tempto VM.

## Developers

For every available `Requirement` there is one possible `Fulfiller`. Currently that mapping is hard coded.
All requirements and their corresponding fulfillers are packed into `tempto-core-all.jar`. In the
future we envision separating requirements and their possible fulfillers into separate jars.

## Acknowledgements

A special thanks to the entire Hadapt team for inspiring the architecture of this framework.
