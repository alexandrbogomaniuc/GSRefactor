#!/bin/bash

BASE_WEBAPP_DIR=${BASE_WEBAPP_DIR};

if [ -z ${BASE_WEBAPP_DIR} ]; then
   echo "Please, set BASE_WEBAPP_DIR";
   exit 1
fi


SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";
GAME_SERVER_DIR=${SCRIPT_DIR}/../../game-server;

${GAME_SERVER_DIR}/build/local/build-for-local-machine.sh
if [ $? -ne 0 ]; then
    exit 1;
fi

DOCKER_CONFIGS_DIR=${SCRIPT_DIR}/../docker;

${DOCKER_CONFIGS_DIR}/is-bootstraped.sh
if [ $? -eq 0 ]; then
    ${DOCKER_CONFIGS_DIR}/stop-gs.sh
fi


ROOT_DIR=${BASE_WEBAPP_DIR}/webapps/gs/ROOT;
if [ ! -d "${ROOT_DIR}" ]; then
    ${DOCKER_CONFIGS_DIR}/create-base-dir.sh
fi


WEB_INF_DIR=${ROOT_DIR}/WEB-INF;
if [ ! -d "${WEB_INF_DIR}" ]; then
    mkdir -p ${WEB_INF_DIR};
fi


find ${ROOT_DIR} -mindepth 1 -name WEB-INF -prune -o -exec rm -rf {} \; 2> /dev/null
find ${ROOT_DIR}/WEB-INF -mindepth 1 \( -name "rlib" -o -name "rlib-dev" \) -prune -o -exec rm -rf {} \; 2> /dev/null


if [ ! -d "${ROOT_DIR}/WEB-INF/rlib" ]; then
    sshpass -p 'faq3YJaWon5B' scp -P 222 -o StrictHostKeyChecking=no deployer@10.2.0.40:/home/users/deployer/rlib.zip /tmp;
    unzip -q /tmp/rlib.zip -d ${WEB_INF_DIR};
    rm /tmp/rlib.zip;
fi
if [ ! -d "${ROOT_DIR}/WEB-INF/rlib-dev" ]; then
    sshpass -p 'faq3YJaWon5B' scp -P 222 -o StrictHostKeyChecking=no deployer@10.2.0.40:/home/users/deployer/rlib-dev.zip /tmp;
    unzip -q /tmp/rlib-dev.zip -d ${WEB_INF_DIR};
    rm /tmp/rlib-dev.zip;
fi


ROOT_WAR=${GAME_SERVER_DIR}/web-gs/target/ROOT.war;
unzip -q ${ROOT_WAR} -d ${ROOT_DIR}


${DOCKER_CONFIGS_DIR}/is-bootstraped.sh
if [ $? -ne 0 ]; then
    DEFAULT_CONFIGS_DIR=${BASE_WEBAPP_DIR}/default-configs;
    if [ -z "$(ls -A ${DEFAULT_CONFIGS_DIR})" ]; then
        cp -a ${GAME_SERVER_DIR}/config/local-machine/. ${DEFAULT_CONFIGS_DIR}
    fi

    ${DOCKER_CONFIGS_DIR}/configure.sh
    ${DOCKER_CONFIGS_DIR}/bootstrap.sh
else
    ${DOCKER_CONFIGS_DIR}/startup.sh
fi
