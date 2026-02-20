#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";
DOCKER_CONFIGS_DIR=${SCRIPT_DIR}/../docker;

${DOCKER_CONFIGS_DIR}/shutdown.sh