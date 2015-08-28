# Tempto example tests

## Running

### Setup 

 * setup clusters

You need a cluster with hadoop accessible by name hadoop-master and a presto cluster accessible by name presto-master.

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

### build and run

```
cd tempto-examples
gradle clean buildFatJar :third-party-deps:build
../bin/tempto --tests-classpath build/libs/tempto-examples-all-${project.version}.jar:../third-party-deps/build/libs/hive-jdbc-all.jar:../third-party-deps/build/libs/presto-jdbc-all.jar --report-dir /tmp/report --tests-package com.teradata.tempto.example.*
```
