#!/bin/bash

RESTART_SCRIPT=${RESTART_SCRIPT}
BACKUP_HOME=${BACKUP_HOME}
APPLICATION_ROOT=${APPLICATION_ROOT}
MAX_SHUTDOWN_WAIT_SECONDS=${MAX_SHUTDOWN_WAIT_SECONDS}
DEPLOY_DIR=${DEPLOY_DIR}
SERVER_NAME=${SERVER_NAME}

if [ -z ${RESTART_SCRIPT} ] || [ -z ${BACKUP_HOME} ] || [ -z ${APPLICATION_ROOT} ] || \
   [ -z ${MAX_SHUTDOWN_WAIT_SECONDS} ] || [ -z ${DEPLOY_DIR} ] || [ -z ${SERVER_NAME} ]
then
    echo "Some of the configuration parameters missed. Please, check configuration file."
    exit 1
fi

BACKUP_LOCATION=`cat ${BACKUP_HOME}/latest-backup-${SERVER_NAME}.txt`
LOG=${DEPLOY_DIR}/logs/${SERVER_NAME}/restore-`date +%Y-%m-%d-%H-%M`.log

if [ ! -d ${BACKUP_LOCATION} ]
then
    echo "Backup not found..."
    exit 1
fi

mkdir -p "$(dirname "${LOG}")"
touch ${LOG}

PID=""
function update_pid () {
    PID=`ps -Af | grep [j]etty.${SERVER_NAME} | awk '{print $2}'`
}

update_pid
if [ -z "${PID}" ]
then
    echo "Server not started, skipping Shut Down..." | tee -a ${LOG}
else
    echo "Shutting down server with PID=${PID}..." | tee -a ${LOG}
    touch ${RESTART_SCRIPT}.s

    i=0
    while [ ${i} -le ${MAX_SHUTDOWN_WAIT_SECONDS} ] && [ -n "${PID}" ]
    do
        sleep 1
        echo -n .
        let i++
        update_pid
    done

    echo

    if [ -n "${PID}" ]
    then
        echo "Can't shutdown server. You should manually investigate the reason of this problem." | tee -a ${LOG}
        exit 1
    fi

    echo "Server successfully stopped in ${i} seconds" | tee -a ${LOG}
fi

echo "Restoring application from backup..." | tee -a ${LOG}
rsync -av --exclude-from 'exclude.txt' --delete ${BACKUP_LOCATION}/* ${APPLICATION_ROOT}/ >> ${LOG}

echo "Restarting application..." | tee -a ${LOG}
touch ${RESTART_SCRIPT}.r

echo "Waiting 20 seconds..." | tee -a ${LOG}
sleep 20

echo "Wipe of Tomcat cache..." | tee -a ${LOG}
touch ${RESTART_SCRIPT}.w
