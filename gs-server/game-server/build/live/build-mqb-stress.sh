#!/bin/bash

BUILD_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/../";

PROPERTIES_FILE="live/mqb-stress.properties";
BRAND="abs";

source "${BUILD_DIR}/build-server.sh"
