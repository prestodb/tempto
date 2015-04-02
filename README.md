# Product test framework

This project provides a framework allowing developers to easily write product tests
for Hadoop SQL databases. Requirements such as: data generation, HDFS file storage,
schema generation are expressed declaratively and are automatically fulfilled by
framework. Query results can be then easily verified.

Framework facilitates writing tests using Java (TestNG, AssertJ) or by providing
files with queries and expected results. It includes Java based TPCH generator, with
ability to generate dataset of arbitrary size.


## Prerequisites

* Java >= 1.8
* Running Hadoop cluster with [WebHDFS](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/WebHDFS.html)
and [XAttr metadata](https://hadoop.apache.org/docs/r2.6.0/hadoop-project-dist/hadoop-hdfs/ExtendedAttributes.html) enabled
* Python >= 2.6 (if custom launcher is used)


## Basic concepts

* _Requirement_ - everything what is needed to run test, e.g. data stored HDFS, Hive tables, etc.
* _Test case_ - test of single functionality e.g. query.
* _Test group_ - logical grouping of test cases.
* _Test context_ - stores dependencies and information for particular test case.


## Setup

**TODO:** we should include here information which jar to use as dependency, and where to put file with properties,
how to setup maven plugins...

## Running samples

* Build project:

```Shell
$ test-framework$ ./gradlew clean build
...
BUILD SUCCESSFUL

Total time: 2 mins 47.263 secs
```

* Set configuration properties in file: _/test-framework/test-framework-examples/src/main/resources/test-configuration.yaml_.
You will need to change WebHDFS host, and hive and presto JDBC urls - check details in **Configuration** section below.

* Make sure that WebHDFS, Hive and Presto are running.

* Run tests using launcher:

```
$ ./bin/product-test \
     --tests-classpath test-framework-examples/src/main/resources/:test-framework-examples/build/libs/test-framework-examples.jar \
     --tests-package=com.teradata.test.examples \
     --exclude-groups quarantine
Loading TestNG run, this may take a sec.  Please don't flip tables (╯°□°）╯︵ ┻━┻
...
[2015-04-02 15:21:48] Completed 18 tests
[2015-04-02 15:21:48] 17 SUCCEEDED      /      1 FAILED      /      0 SKIPPED
[2015-04-02 15:21:48] For tests logs see: testlogs/2015-04-02_15-15-16
See /home/sogorkis/repos/test-framework/test-framework-core/build/reports/html/index.html for detailed results.
```

* Check result report - [/home/sogorkis/repos/test-framework/test-framework-core/build/reports/html/index.html](file:///home/sogorkis/repos/test-framework/test-framework-core/build/reports/html/index.html).
One test (_com.teradata.test.examples.SimpleQueryTest.failingTest_) should fail.


## Configuration

Configuration is hierarchical YAML file, and defines various aspects of tests execution environment. Yaml file
is loaded from classpath and its default name is _test-configuration.yaml_. It contains following sections:

* hdfs

HDFS is accessed through WebHDFS REST API. It is used internally by fulfillment process, but Java based tests
also can make use of it through _HdfsClient_ interface.

```YAML
hdfs:                     # HDFS related definition
  username: hdfs          # username to use for accessing HDFS
  webhdfs:
    host: master          # hostname exposing HDFS REST interface
    port: 50070           # port of HDFS REST interface
```

* databases

Currently we support only JDBC based database connections. Multiple connections may be defined in test configuration.
By default queries are executed using connection named "default", but user may specify that he want to use different
one (see below). For some internal test-framework aspects to work it may be necessary that other database connections
are defined in configuration. E.g. Hive table fulfiller requres that connection named "hive" is defined.

```YAML
databases:           # Database connections
  default:           # default connection
    alias: presto    # it just points to connection named presto defined below
 
  hive:              # connection named hive
    jdbc_driver_class: org.apache.hive.jdbc.HiveDriver                                # jdbc driver classname
    jdbc_url: jdbc:hive2://master:10000                                               # database url
    jdbc_user: hdfs                                                                   # database user
    jdbc_password: na                                                                 # database password
    jdbc_pooling: false                                                               # (optional) should connection pooling be used (it does not work for hive due to driver issues)
    jdbc_jar: test-framework-hive-jdbc/build/libs/test-framework-hive-jdbc-all.jar    # (optional) jar to be used for obtaining database driver. Should be used in case
                                                                                      # when we cannot have it in global classpath due to class conflicts 
                                                                                      # (e.g. hive driver conflicts with presto driver)
 
  presto:           # connection named presto
    jdbc_driver_class: com.facebook.presto.jdbc.PrestoDriver                         
    jdbc_url: jdbc:presto://localhost:8080/hive/default
    jdbc_user: hdfs
    jdbc_password: na
```

* tests

Test execution details.

```YAML
tests:
  hdfs:
    path: /product-test  # where to store test data on HDFS
```

## Java based tests

### Example

TODO

### Requirements

Test may define declarative requirements which are then fulfilled by test framework on suite/test initialization.

> Currently all available requirements are exposed by test-framework-core.jar, and also list of fulfillers is hardcoded. 
> Future improvement is to have better separation of non-related requirements/fulfillers into separate jars.

Requirements are defined through _@Require_ annotation. A parameter passed to annotation is class extending
_RequirementProvider_ interface. Interface have just one method _getRequirements_. Note that it seem more natural
to parametrize annotation with instance of  _RequirementProvider_ instead of class but Java language does not allow this.


Example _RequirementProvider_:

```Java
private static class SimpleTestRequirements
        implements RequirementsProvider
{

    @Override
    public Requirement getRequirements()
    {
        return new ImmutableHiveTableRequirement(NATION); // ensure TPCH nation table is available
    }
}
```

It defines _RequirementProvider_ requesting single requirement. In this case it is immutable hive table (Tpch.NATION).


Defined RequirementProvider then is attached to testMethod:

```Java
    @Test(groups = "query")
    @Requires(SimpleTestRequirements.class)
    public void selectAllFromNation()
    {
        assertThat(query("select * from nation")).hasRowsCount(25);
    }
```

_@Require_ annotation may also be used at class level which means the same as if it was defined for each test
method in class. If multiple _@Require_ annotations are defined for method they are combined.

### Requirements Types

* ImmutableHiveTableRequirement

It requests that table is created within Hive. ImmutableHiveTableRequirement is parametrized with _TableDefinition_
which include name, schema and dataSource. Instances of TableDefinitions reused in multiple cases are commonly
defined as constants (e.g. _com.teradata.test.fulfillment.hive.tpch.TpchTableDefinitions_ have TableDefinitions
for TPCH schema). _HiveTableDefinitionBuilder_ can be use to create new definition. You need to provide table name,
create table DDL template (_\{0\}_ is substituted with HDFS file location) and _DataSource_.

Example:

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

Queries are executed by _QueryExecutor_. Each database from configuration has its own query executor with the same
name. You can get query executor, by using _ThreadLocalTestContextHolder.testContext().getDependency(...)_ method.

Example:

```Java
    QueryResult qrDefault = QueryExecutor.query("SELECT * FROM nation"); // execute query on default database
    QueryExecutor prestoQueryExecutor = ThreadLocalTestContextHolder.testContext().getDependency(QueryExecutor.class, "presto");
    QueryResult qrDefault = prestoQueryExecutor.query("SELECT * FROM nation"); // execute query on default database
```

### Query assertions

_QueryAssertions_ class allows performing AssertJ style assertions on _QueryResult_ object. For more details check _QueryAssertions_
methods and examples:

Example assertions:

```JAVA
      @Requires(TpchRequirements.class)
      @Test
      public void testContainsExactlyInOrder()
      {
          assertThat(query("SELECT n.nationkey, n.name, r.name FROM nation n " +
                  "INNER JOIN region r ON n.regionkey = r.regionkey " +
                  "WHERE name like 'A%' AND n.created > ? ORDER BY n.name", LocalDate.parse("2015-01-01")))
                  .hasColumns(INTEGER, VARCHAR, VARCHAR)
                  .hasRowsInOrder(
                          row(1, "ALGERIA", "AFRICA"),
                          row(7, "ARGENTINA", "SOUTH AMERICA"));
      }
```

## File convention based tests

Query tests can be written by providing sql query file and file with expected result. Moreover you
can define datasets, which will create tables with data. For more examples take a look on files in
directory _test-framework-examples/src/main/resources/sql-tests_:

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

Data sets are defined in _sql-tests/datasets_ directory. To create example table, you need to create three files:

* TABLE_NAME.data - file containing raw data.
* TABLE_NAME.data-revision - file containing marker of data. If you change your data, you should also change
revision marker, so table data is automatically reloaded.
* TABLE_NAME.ddl - DDL template for data.

### Tests

Test cases are defined in _sql-tests/testcases_ directory. Directory under testcases directory is logical
equivalent of TestNG test class. Then each test is pair of two files:
 
* TEST.sql - query to be invoked, first line of file can be sql comment containing query execution requirements:

```
-- database: hive; groups: example_smoketest,group2
SELECT * FROM nation
```

This example expressed that query should be executed using Hive _QueryExecutor_ and it is part of two TestNG groups.

* TEST.result - file containing expected result of a query. First line can be sql comment containing query assertion
requirements:

```
-- delimiter: |; ignoreOrder: false; types: INTEGER|VARCHAR|INTEGER|VARCHAR
0|ALGERIA|0| haggle. carefully final deposits detect slyly agai|
...
```

In this example we defined _|_ as delimiter, that we ignore order of rows and that we expect columns of following types.
You always need to provide types as file based tests needs to perform casting from String to particular type.

Moreover you are able to add custom _before_ and _after_ scripts which are launched before and after each test case. 

### Generated tests

TODO

## Tests running

### Running tests from IDE

Java based tests can be simply run as TestNG tests.

File convention based tests: TODO

### Shell product-test launcher

Tests are run using bin/product-test script. This is a wrapper around command line TestNG runner. For verbose
description of all execution parameters supported by product-test run:

```Shell
$ ./bin/product-test --help
```

**Basic parameters**

For running test you have to specify at least:

* **classpath** - classpath will be scanned to find tests to be run. Classpath may be either set of jars of
directories or mix of both.
* **tests package** - defines java package containing tests. For Java based tests only tests residing in
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
            looks like: sql_query_test.sample_table.allRows. Tests which name ends with one of patterns specified
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