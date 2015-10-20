# Tempto example tests

The steps below will run the example tests that come with the framework. They act as a basic
smoketest to ensure that you've setup everything properly.

##Setup 

### Hadoop cluster

 You need a cluster with hadoop accessible by name `hadoop-master` and a presto cluster accessible by name `presto-master`.
 You can alter host names and ports by editing local `src/main/resources/tempto-configuration-local.yaml` configuration file.
 For more details please refer to the [Configuration section](../README.md).

 Ensure that WebHDFS, Hive and Presto are running.

### Also you need PSQL instances.
```
docker run --name tempto-examples-psql -p 15432:5432 -e POSTGRES_USER=blah -e POSTGRES_PASSWORD=blah  -d postgres
docker run --name tempto-examples-psql2 -p 15433:5432 -e POSTGRES_USER=blah -e POSTGRES_PASSWORD=blah  -d postgres

# OR IF YOU ALREADY HAVE ABOVE DOCKER INSTANCES

docker start tempto-examples-psql
docker start tempto-examples-psql2
```

If your docker server is not `localhost` you have to specify it in your
`src/main/resources/tempto-configuration-local.yaml`
```
DOCKER_MACHINE: host_of_my_docker_server
```

### Ssh-able host

You need some host accessible over ssh.
 * using private key
 * using password

Configure ssh in ```src/main/resources/tempto-configuration-local.yaml```
```
ssh:
  identity: ~/my_key.pem
  roles:
    host_by_password:
      user: username
      password: userpassword
      host: ssh_test_host

    host_by_identity:
      host: ssh_test_host
```

## build and run

Being in root `tempto` project directory run:
```
gradle clean build
java -jar tempto-examples/build/libs/tempto-examples-all.jar --report-dir /tmp/report
```

Note that one test (`com.teradata.tempto.examples.SimpleQueryTest.failingTest`) is made to fail on purpose.

To get help on more running option use:
```
java -jar tempto-examples/build/libs/tempto-examples-all.jar --help
```

The framework will print on your console whether a test passed or failed. A more detailed report
is available at `/tmp/report/index.html`. 

