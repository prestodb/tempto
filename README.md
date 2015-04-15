# Product test framework

This project allows developers to write and execute tests for SQL databases running
on Hadoop. Individual test requirements such as data generation, HDFS file copy/storage
of generated data and schema creation are expressed declaratively and are automatically
fulfilled by the framework. Developers can write tests using Java (using a TestNG like
paradigm and AssertJ style assertion) or by providing query files with expected resuls.


## Prerequisites

You will need the following software to be installed on the machine running the
framework:

* Java >= 1.8
* Python >= 2.6 (if you use the custom launcher that comes with the framework)

Currently we only support HDFS as a datastore. That means that on your Hadoop
cluster you'll need the following:

* Running Hadoop cluster with [WebHDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/WebHDFS.html)
and [XAttr metadata](https://hadoop.apache.org/docs/r2.6.0/hadoop-project-dist/hadoop-hdfs/ExtendedAttributes.html) enabled


## Basic concepts

* _Requirement_ - the set of resources a test needs in order to run, e.g. data stored HDFS, Hive tables, etc.
* _Test case_ - test of single functionality e.g. query.
* _Test group_ - logical grouping of test cases. For example one could define a join group, a group by group, 
                 a window function group etc. in order to test different SQL functionality.
* _Test suite_ - a set of different groups. For example you could have an administration suite, a concurrency suite etc.
                 each of which would compose different groups that fit under that umbrella.
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

## Example test run

The steps below will run the example tests that come with the framework. They act as a basic
smoketest to ensure that you've setup everything properly.

* Build the framework:

```Shell
$ cd test-framework
$ ./gradlew clean build
...
...
...
BUILD SUCCESSFUL

Total time: 2 mins 47.263 secs
```

* Set configuration properties in the following configuration file: `test-framework/test-framework-examples/src/main/resources/test-configuration.yaml`.
The most important settings you'll need to change are the WebHDFS host, the Hive and Presto JDBC URLs. For more details please
refer to the **Configuration** section below.

* Ensure that WebHDFS, Hive and Presto are running.

* Run tests using the provided test launcher:

```Shell
$ cd test-framework
$ bin/product-test \
     --tests-classpath test-framework-core/build/libs/test-framework-core-all.jar:test-framework-examples/src/main/resources/:test-framework-examples/build/libs/test-framework-examples.jar \
     --tests-package=com.teradata.test.examples \
     --exclude-groups quarantine \
     --report-dir /tmp
Loading TestNG run, this may take a sec.  Please don't flip tables (╯°□°）╯︵ ┻━┻
...
[2015-04-02 15:21:48] Completed 18 tests
[2015-04-02 15:21:48] 17 SUCCEEDED      /      1 FAILED      /      0 SKIPPED
[2015-04-02 15:21:48] For tests logs see: testlogs/2015-04-02_15-15-16
See /home/sogorkis/repos/test-framework/test-framework-core/build/reports/html/index.html for detailed results.
```

* The framework will print on your console whether a test passed or failed. A more detailed report
is available at `test-framework/test-framework-core-build-reports/html/index.html`. Note that
one test (`com.teradata.test.examples.SimpleQueryTest.failingTest`) is made to fail on purpose.


## Configuration

The test execution environment is configured via a hierarchical YAML file. The YAML file
is loaded from the classpath and it must be called `test-configuration.yaml`. The file
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
    jdbc_jar: test-framework-hive-jdbc/build/libs/hive-jdbc-fat.jar                   # (optional) jar to be used for obtaining database driver. Should be used in case
                                                                                      # when we cannot have it in global classpath due to class conflicts 
                                                                                      # (e.g. hive driver conflicts with presto driver)
 
  presto:           # connection named presto
    jdbc_driver_class: com.facebook.presto.jdbc.PrestoDriver
    jdbc_url: jdbc:presto://localhost:8080/hive/default
    jdbc_user: hdfs
    jdbc_password: na
```

* **tests**

This section is used to configure various properties used during test execution.

```YAML
tests:
  hdfs:
    path: /product-test  # where to store test data on HDFS
```

## Java based tests

### Example

TODO

### Requirements

Tests may declare requirements that are fulfilled by the framework during suite/test initialization.

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

In this case, `SimpleTestRequirements` enapsulated the single requirement of an immutable Hive table called nation.

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

### Requirement Types

This section lists the supported `Requirement` implementations that you can return
from `RequirementProvider#getRequirement()`.

* **ImmutableHiveTableRequirement**

When this requirement is fulfilled, it will create a table in Hive. It is called immutable because
the contract with the test developer is that they will not, within the logic of their test, alter
the state of the table (drop it, re-create it under a different name, delete data). This is done so
that requirements can be recycled between tests. If 10 tests require an immutable table, that table
will only be created once and the framework assumes it will be available for all tests.

Certain commonly used tables, such as those in the TPC-H benchmark, are defined as constants and can
be found in `com.teradata.test.fulfillment.hive.tpch.TpchTableDefinitions`.

TODO: we need to clarify to the user how they create tables.
ImmutableHiveTableRequirement is parametrized with _TableDefinition_
which include name, schema and dataSource. _HiveTableDefinitionBuilder_ can be use to create new definition. You need to provide table name,
create table DDL template (_\{0\}_ is substituted with HDFS file location) and _DataSource_.

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

### Executing queries

Queries are executed via implementations of the `QueryExecutor` interface. Currently the only implementaion
is `JdbcQueryExecutor`. Each database configured in the YAML file will have its own query executor with the
same name. To retrieve that executor and issue queries against that database you can use the
`ThreadLocalTestContextHolder.testContext().getDependency(...)` as shown below.

```Java
    // execute query agains the default database
    QueryResult defaultQueryResult = QueryExecutor.query("SELECT * FROM nation");
    
    // Retrieve QueryExecutor for another, non-default, database
    QueryExecutor prestoQueryExecutor = ThreadLocalTestContextHolder.testContext().getDependency(QueryExecutor.class, "presto");
    QueryResult queryResultPresto = prestoQueryExecutor.query("SELECT * FROM nation");
```

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
`test-framework-examples/src/main/resources/sql-tests` directory. The directory tree looks
like the following:

```Shell
~/repos/test-framework/test-framework-examples/src/main/resources$ tree .
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
├── suites.json
└── test-configuration.yaml
```

### Data sets

Data sets are stored in `sql-tests/datasets_ directory`. To create an example table, you will need to create three files:

* TABLE_NAME.data - file containing raw data.
* TABLE_NAME.data-revision - file with data marker. If you change your data, you should also increase this
revision marker, so the new table data is automatically reloaded.
* TABLE_NAME.ddl - DDL for data.

TODO: provide examples.
TODO: where should be user create the sql-tests directory? Right now it's resources under the examples dir, where should they put it?

### Tests

Test case files are stores in `sql-tests/testcases_ directory`. The directory right under the
`testcases_directory` is the logical equivalent of a TestNG test class. Each logical test is pair
of files:
 
* **TEST.sql** - query to be invoked where the first line of file can be a SQL comment specifying
query execution requirements:

```
-- database: hive; groups: example_smoketest,group2
SELECT * FROM nation
```

This test contains a single query that should be executed against the Hive database. In addition,
the test is part of two separate TestNG groups: example_smoketest and group2.

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

You are also able to add custom _before_ and _after_ scripts for your test. Those are executed
before and after each test case.
TODO more info on scripts, what they should be named, what they can contain.

### Generated tests

TODO

## Tests running

### Running tests from your IDE

Java based tests can be simply run as TestNG tests.

File convention based tests: TODO

### Shell product-test launcher

Tests can be run using the `bin/product-test` script. This is a wrapper around a command
line invocation of the TestNG JVM. For a verbose description of all the execution options
supported by the `bin/product-test` script run:

```Shell
$ ./bin/product-test --help
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
$ ./bin/product-test --tests-classpath test-framework-examples/src/main/resources/:test-framework-examples/build/libs/test-framework-examples.jar --tests-package=com.teradata.test.examples
```

In above example we set classpath to contain two entries:

* test-framework-examples/src/main/resources - this is directory entry
* test-framework-examples/build-libs/test-framework-examples.jar

And tests package is set to com.teradata.test.examples.

**Tests configuration**

By default we are searching for .yaml configuration file in classpath passed in --test-classpath parameter.
If classpath contains test-configuration.yaml file then it will be used during tests execution. User can override
tests configuration with _--test-configuration_ parameter.

```Shell
./bin/product-test ... --test-configuration my-test-configuration.yaml
```

**Tests selection**

By default all tests found in classpath are executed but user may limit that.

<table>
    <tr>
        <td style="width:120px">--suites</td>
        <td>List of suites to be executed. Each suite is a list of groups suites.json.
            TODO link to section with description of suites.json
        </td>
    </tr>
    <tr>
        <td>--groups</td>
        <td>List of groups to be executed.</td>
    </tr>
    <tr>
        <td>--tests</td>
        <td>List of tests to be executed. For java based tests test name is just fully qualified method name
            e.g. com.teradata.test.examples.SimpleQueryTest.selectCountFromNation. For sql convention based tests name 
            looks like: sql_tests.testcases.sample_table.allRows. Tests which name ends with one of patterns specified
            in --tests parameter will be executed.
        </td>
    </tr>
    <tr>
        <td>--classess</td>
        <td>List of fully qualified java classess to be executed. Applies to java based tests only.</td>
    </tr>
    <tr>
        <td>--exclude-groups</td>
        <td>List of test groups which should be excluded from execution.</td>
    </tr>
</table>


**Debugging**

If you want to run tests from product-test script under debuger use --debug parameter. When this parameter is
specified product tests framework will suspend execution at beginning and wait for debugger on TCP port _5005_.

```Shell
./bin/product-test --tests-classpath test-framework-examples/src/main/resources/:test-framework-examples/build/libs/test-framework-examples.jar --debug
Loading TestNG run, this may take a sec.  Please don't flip tables (╯°□°）╯︵ ┻━┻
Listening for transport dt_socket at address: 5005
```

At this point you may use your IDE of choice to connect to test-framework VM.

## Developers

For every available `Requirement` there is one possible `Fulfiller`. Currently that mapping is hard coded.
All requirements and their corresponding fulfillers are packed into `test-framework-core-all.jar`. In the
future we envision separating requirements and their possible fulfillers into separate jars.
