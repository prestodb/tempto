# Tempto example tests

The steps below will run the example tests that come with the framework. They act as a basic
smoketest to ensure that you've setup everything properly.

##Setup 

 * You need a cluster with hadoop accessible by name hadoop-master and a presto cluster accessible by name presto-master. In case you want to use different host names (or service ports) you need to change ```src/main/resources/tempto-configuration.yaml``` accordingly.For more details please refer to the [Configuration section](../README.md).
 * Ensure that WebHDFS, Hive and Presto are running.

Also you need PSQL instances.
```
docker run --name tempto-examples-psql -p 15432:5432 -e POSTGRES_USER=blah -e POSTGRES_PASSWORD=blah  -d postgres
docker run --name tempto-examples-psql2 -p 15433:5432 -e POSTGRES_USER=blah -e POSTGRES_PASSWORD=blah  -d postgres

# OR IF YOU ALREADY HAVE ABOVE DOCKER INSTANCES

docker start tempto-examples-psql
docker start tempto-examples-psql2
```

 * configure ssh in ```src/main/resources/tempto-configuration-local.yaml```

You need a pem (private key) file to login to the cluster and some user with a password set (yarn/yarn by default).
Example content for this file is:
```
ssh:
  identity: ~/hfab/hfab/util/pkg_data/insecure_key.pem
  roles:
    host_by_password:
      user: test
      password: testtest
      host: master

    host_by_identity:
      host: master
```

## build and run

```
cd tempto-examples
gradle clean build
../bin/tempto --tests-classpath build/libs/tempto-examples-all.jar --report-dir /tmp/report --tests-package com.teradata.tempto.examples.*
```

The framework will print on your console whether a test passed or failed. A more detailed report
is available at `/tmp/report/index.html`. 
Note that one test (`com.teradata.tempto.examples.SimpleQueryTest.failingTest`) is made to fail on purpose.
