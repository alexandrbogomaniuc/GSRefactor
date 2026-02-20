#!/bin/bash

SERVER_MODULE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/../";

CUSTOM_LOCAL_REPO="$1"
LOCAL_REPO_OPTION="";
if [ ! -z "${CUSTOM_LOCAL_REPO}" ]; then
    LOCAL_REPO_OPTION="-Dmaven.repo.local=${CUSTOM_LOCAL_REPO}"
fi

CUSTOM_PROFILE_OPTION="";
if [ ! -z "${CUSTOM_PROFILE}" ]; then
    CUSTOM_PROFILE_OPTION="-P=${CUSTOM_PROFILE}"
fi

mvn clean package -f "${SERVER_MODULE_DIR}/pom.xml" -s "${SERVER_MODULE_DIR}/build/build-settings.xml" \
    -Dcluster.properties="${PROPERTIES_FILE}" ${LOCAL_REPO_OPTION} ${CUSTOM_PROFILE_OPTION}

if [ $? -eq 0 ]; then
    if [[ ${PROPERTIES_FILE} != *"gp3-local"* ]]; then
        ${SERVER_MODULE_DIR}/build/check-on-brand-issues.sh ${BRAND}
    fi
else
    exit 1;
fi