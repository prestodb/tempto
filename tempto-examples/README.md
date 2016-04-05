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

To run example product tests use the following command:

```
tempto-examples/bin/run_on_docker.sh
```

Note that one test (`com.teradata.tempto.examples.SimpleQueryTest.failingTest`) is made to fail on purpose.

