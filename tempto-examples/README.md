# Tempto example tests

The steps below will run the example tests that come with the framework. They act as a basic
smoketest to ensure that you've setup everything properly.

## Installing dependencies

*Running product tests requires at least 4GB free memory.*

#### On GNU/Linux
* [```docker >= 1.10```](https://docs.docker.com/installation/#installation)

    ```
    wget -qO- https://get.docker.com/ | sh
    ```

* [```docker-compose >= 1.60```](https://docs.docker.com/compose/install/)

    ```
    pip install docker-compose
    ```

#### On Mac OS X

* [```docker-toolbox >= 1.10```](https://www.docker.com/products/docker-toolbox)

On Mac OS X installing docker-toolbox gives access to a preconfigured shell environment
with ```docker``` and ```docker-compose``` available. The shortcut to this preconfigured
shell environment for docker may be found in Applications/Docker as "Docker Quickstart Terminal."
Note that all commands given in later parts of these instructions should be run from this
environment.

## Running tests

Running tempto example product requires a testing cluster which is provisioned by docker containers and managed by docker-compose.

Note that one test (`com.teradata.tempto.examples.SimpleQueryTest.failingTest`) is made to fail on purpose.

### By automation script

To run example product tests use the following command:

```
tempto-examples/bin/run_on_docker.sh
```

### Manual execution of product tests
#### Provisioning of testing cluster

To run example product tests first you need to setup a testing cluster

```
cd tempto-examples/docker
docker-compose up -d
# wait a while to make sure that testing clusters is up and ready
```

#### Product tests execution on Linux-based operating systems

You can run product tests with:

```
cd tempto-examples/docker
java -jar ../build/libs/tempto-examples-all.jar
```

To run product tests in debugging mode you can:

```
cd tempto-examples/docker
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 -jar ../build/libs/tempto-examples-all.jar
```

then you can attach IDE to start debug session.

After you finish with example product tests execution you can teardown the cluster:


#### Product tests execution on MAC

To run product tests on MAC please do:

```
cd tempto-examples/docker
docker-compose runner java -jar /workspace/build/libs/tempto-examples-all.jar --config tempto-configuration.yaml,/workspace/docker/tempto-configuration-docker-local.yaml
```

To run product tests in debugging mode you can:

```
cd tempto-examples/docker
docker-compose run -p 5005:5005 runner java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 -jar /workspace/build/libs/tempto-examples-all.jar --config tempto-configuration.yaml,/workspace/docker/tempto-configuration-docker-local.yaml
```

then you can attach IDE to start debug session.

#### Tearing down the testing cluster
```
cd tempto-examples/docker
docker-compose down
```


