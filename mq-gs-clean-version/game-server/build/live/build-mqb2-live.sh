#!/bin/bash

BUILD_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/../";

PROPERTIES_FILE="live/mqb2-live.properties";
BRAND="betsoft";

source "${BUILD_DIR}/build-server.sh"