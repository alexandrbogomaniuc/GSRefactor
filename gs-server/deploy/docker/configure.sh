#!/bin/bash

if [[ $EUID -eq 0 ]]; then
   echo "You should not run script under root" 
   exit 1
fi

if [ -z ${BASE_WEBAPP_DIR} ]; then
   echo "Specify base directory for your webapp:";
   read BASE_WEBAPP_DIR;
fi

while [ -z ${LOBBY_DOMAIN} ]
do
   echo "Specify correct (not empty) lobby domain with static content:";
   read LOBBY_DOMAIN;
done

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";
ROW_CONFIGS_DIR=${SCRIPT_DIR}/row-configs;
DEST_CONFIGS_DIR=${SCRIPT_DIR}/configs;

rm -rf ${DEST_CONFIGS_DIR};

export LOBBY_DOMAIN;
export WEBAPP_DIR=${BASE_WEBAPP_DIR}/webapps/gs;
export LOG_DIR=${BASE_WEBAPP_DIR}/logs;
export DEFAULT_CONFIGS_DIR=${BASE_WEBAPP_DIR}/default-configs;
export STATIC_DIR=${BASE_WEBAPP_DIR}/static

#copy row configs
cp -rf ${ROW_CONFIGS_DIR} ${DEST_CONFIGS_DIR};

#inline variables in copied row configs
IFS=$'\n'; set -f
for file in $(find ${DEST_CONFIGS_DIR} -type f); do
    TEMP=$(mktemp);
    envsubst '${LOBBY_DOMAIN},${WEBAPP_DIR},${LOG_DIR},${DEFAULT_CONFIGS_DIR},${STATIC_DIR}' < ${file} > ${TEMP};
    mv ${TEMP} ${file};
done
unset IFS; set +f