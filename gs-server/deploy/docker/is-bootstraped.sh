#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";

COMPOSE_CONFIGS_FILE=${SCRIPT_DIR}/configs/docker-compose.yml;
if [ ! -f ${COMPOSE_CONFIGS_FILE} ]; then
    exit 1;
fi

services_count="$(docker-compose --project-name "GP3" --file ${COMPOSE_CONFIGS_FILE} config --services | wc -l)";
active_services_count="$(docker-compose --project-name "GP3" --file ${COMPOSE_CONFIGS_FILE} config --services | \
	docker-compose --project-name "GP3" --file ${COMPOSE_CONFIGS_FILE} ps -q | wc -l)";

if (( active_services_count < services_count )); then
	exit 1;
else 
	exit 0;
fi