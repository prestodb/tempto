#!/bin/bash -ex

function wait_for() {
  echo "waiting for $*"
  for i in $(seq 10); do
    if $* > /dev/null 2>&1; then
      return
    fi
    echo ...
    sleep 5
  done
  echo "$* - did not succeeded within a time limit"
  exit 1
}

function get_docker_host_ip() {
  if [ x$DOCKER_HOST == "x" ]; then
    echo localhost
  else 
    echo $DOCKER_HOST | sed 's@.*/\(.*\):.*@\1@'
  fi
}

cd $(dirname $(readlink -f $0))

presto-devenv hadoop start
presto-devenv presto start
wait_for presto-devenv status
docker start tempto-examples-psql
docker start tempto-examples-psql2

export IDENTITY=~/.vagrant.d/insecure_private_key
export DOCKER_MACHINE=$(get_docker_host_ip)
java -jar ../build/libs/tempto-examples-all.jar $*

