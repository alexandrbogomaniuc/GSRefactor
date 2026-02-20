#!/bin/bash

BACKUP_HOME=/home/users/auditor/backup
DEPLOY_DIR=/home/users/auditor/deploy

APPLICATION_ROOT=/www/html/gs/ROOT

RESTART_SCRIPT=/www/restart/gs
SERVER_NAME=gs

MAX_SHUTDOWN_WAIT_SECONDS=120

source "../common/restore-jetty.sh"
