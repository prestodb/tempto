---------------------------------------------------------
-- Copyright 2015, Teradata Corp. All rights reserved. --
---------------------------------------------------------

SQL Result Generator
================

This tool generates expected result files for use with the product-test framework's convention based tests.

Building the tool
-----------------
cd ~/test-framework/expected-result-generator
../gradlew buildRunnableJar

Writing tests
-------------
Each test is written as a pair of files: test.sql, and test.result.

**test.sql**

This file contains one SQL statement, which must end with a semicolon (;).
Statements may span multiple lines.  Comment lines
may appear before or after the SQL statement.
Comments are identified by "--".

Generating the result file
--------------------------
The actual execution of the SQL query is performed by a Java program using 
JDBC, but the user interface is provided by a Python script (run_sql_test.py).

    cd bin
    # Generate results for one SQL file:
    ./generate_results.py --properties ../config/db.properties -s ~/presto-tdc/presto-product-tests/src/test/resources/sql-tests/testcases/my_test/test1.sql
    
    # Generate results for all SQL files in the directory:
    ./generate_results.py --properties ../config/db.properties -s ~/presto-tdc/presto-product-tests/src/test/resources/sql-tests/testcases/my_test/

**NOTE:** Existing test.result files will not be overwritten.  If you want to update the result for a test, you will need to manually delete the existing .result file.

The properties file is parsed by the java.util.Properties, and thus consists
of key/value pairs, separated by equals or colon, one per line.  Comment
lines start with '#' or '!'.  All of the acceptable keys are shown in the
example below:

    user=swarm
    password=swarm
    jdbc_url=jdbc:postgresql://cobbler.td.teradata.com:5432/swarm

You can use -s to specify a single SQL file or a directory containing one or more SQL files.
If a directory is specified, only *.sql files in that directory will be executed.

You can also use the command line option "--test-list-file" to indicate that the tests to be executed are listed in a file.
This file must be formatted as follows:
    # This is a comment line
    -- This is another comment line
    test1.sql
    test2.sql
    test_directory

You can configure the Java application's logging behavior by editing 
**bin/log4j.properties**.


