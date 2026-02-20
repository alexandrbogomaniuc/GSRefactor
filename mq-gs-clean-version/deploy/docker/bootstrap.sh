#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";
COMPOSE_CONFIGS_FILE=${SCRIPT_DIR}/configs/docker-compose.yml;

docker-compose --project-name "GP3" --file ${COMPOSE_CONFIGS_FILE} up --build -d