#!/bin/bash

BUILD_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/../";

PROPERTIES_FILE="local/local-machine.properties";
BRAND="abs";

source "${BUILD_DIR}/build-server.sh"
