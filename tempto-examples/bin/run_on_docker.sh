#!/bin/bash -ex

# http://stackoverflow.com/questions/3572030/bash-script-absolute-path-with-osx
function absolutepath() {
    [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}

function retry() {
  END=$(($(date +%s) + 600))

  while (( $(date +%s) < $END )); do
    set +e
    "$@"
    EXIT_CODE=$?
    set -e

    if [[ ${EXIT_CODE} == 0 ]]; then
      break
    fi
    sleep 5
  done

  return ${EXIT_CODE}
}

function hadoop_master_container(){
  ${DOCKER_COMPOSE} ps -q hadoop-master
}

function check_hive() {
  # TODO use docker-compose
  docker exec $(hadoop_master_container) hive -e 'show tables'
}

function check_presto() {
  ${DOCKER_COMPOSE} run presto-cli \
    java -jar presto-cli.jar --server presto-master:8080 --execute 'SHOW CATALOGS' | \
    grep -i hive
}

function run_product_tests() {
  ${DOCKER_COMPOSE} run runner \
    java -jar /workspace/build/libs/tempto-examples-all.jar \
    --config-local /workspace/docker/tempto-configuration-docker-local.yaml \
    $*
}

# docker-compose down is not good enough because it's ignores services created with "run" command
function stop_container() {
  SERVICE_NAME=$1
  CONTAINER_IDS=$(${DOCKER_COMPOSE} ps -q ${SERVICE_NAME})
  for CONTAINER_ID in $CONTAINER_IDS; do
    echo "Stopping and removing ${SERVICE_NAME} with id ${CONTAINER_ID}"
    docker stop ${CONTAINER_ID}
    docker rm ${CONTAINER_ID}
  done
}

function cleanup() {
  # stop application runner containers started with "run"
  stop_container presto-cli
  stop_container runner

  # stop containers started with "up"
  ${DOCKER_COMPOSE} down

  # wait for docker logs termination
  wait
}

function termination_handler(){
  set +e
  cleanup
  exit 130
}

SCRIPT_DIR=$(dirname $(absolutepath "$0"))
DOCKER_COMPOSE="docker-compose -f ${SCRIPT_DIR}/../docker/docker-compose.yml"

# check docker and docker compose installation
docker-compose version
docker version

trap termination_handler INT TERM
${DOCKER_COMPOSE} stop || true

${DOCKER_COMPOSE} pull

${DOCKER_COMPOSE} build
${DOCKER_COMPOSE} up -d 
${DOCKER_COMPOSE} logs --no-color presto-master hive-master psql1 psql2 ssh &

retry check_hive
retry check_presto

# run product tests
set +e
run_product_tests "$*" &
PRODUCT_TESTS_PROCESS_ID=$!
EXIT_CODE=$?
wait $PRODUCT_TESTS_PROCESS_ID
set-x

cleanup

exit ${EXIT_CODE}
