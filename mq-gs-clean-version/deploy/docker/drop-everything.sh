#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";
COMPOSE_CONFIGS_DIR=${SCRIPT_DIR}/configs;
COMPOSE_CONFIGS_FILE=${COMPOSE_CONFIGS_DIR}/docker-compose.yml;

docker-compose --project-name "GP3" --file ${COMPOSE_CONFIGS_FILE} rm -v --force --stop
docker volume rm gp3_cassandra-data

rm -rf ${COMPOSE_CONFIGS_DIR};
rm -rf ${BASE_WEBAPP_DIR};