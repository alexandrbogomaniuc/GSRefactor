#!/bin/bash

BACKUP_DIR="/www/html/mp/ROOT_bitbucket_backup";
ROOT_DIR="/www/html/mp/ROOT";
MP_DIR="/www/html/mp";

LOG_FILE="/www/html/mp/deployment.log"

# Function to log messages
function log_message() {
    echo "$(date +'%Y-%m-%d %H:%M:%S') - $1" >> "$LOG_FILE"
}

# Function to check if the previous command succeeded
function check_command_status() {
    if [ $? -ne 0 ]; then
        echo "Command failed. Performing backup..."
        do_backup
        exit 1
    fi
}

# Function to perform the backup
function do_backup() {
    sudo /www/html/scripts/stop-mp.sh
    sudo rm -r ${ROOT_DIR}
    sudo cp -r ${BACKUP_DIR} ${ROOT_DIR}
    sudo /www/html/scripts/start-mp.sh
    # Log backup success or failure
    if ! (ps aux | grep netty | grep -v grep)  then
        echo "Deployment failed. Performing backup..."
        log_message "Backup failed."
        exit 1
    else
        log_message "Backup completed successfully."
    fi
}

function remove_backup_tmp_dir(){
    if [ -d ${BACKUP_DIR}_tmp ]; then
        echo removing backup dir...
        sudo rm -r ${BACKUP_DIR}_tmp
        echo done
    fi
}
echo deployment started
if (ps aux | grep netty | grep -v grep) then
    # Deploy
    echo stoping mp server
    sudo /www/html/scripts/stop-mp.sh
    check_command_status
    echo done
fi

if [ -d ${BACKUP_DIR} ]; then
        remove_backup_tmp_dir
        sudo mv ${BACKUP_DIR} ${BACKUP_DIR}_tmp
        check_command_status
fi

sudo mv ${ROOT_DIR} ${BACKUP_DIR}
check_command_status

remove_backup_tmp_dir
echo unziping deployment war
sudo unzip ${MP_DIR}/web-mp-casino.war -d ${ROOT_DIR}
check_command_status
echo done

cd /www/html/mp/ROOT || return $?
sudo find . -type f -name '*' -exec chmod u+rw-x,g+rw-x,o+r-wx {} \;
cd /www/html/mp || return $?

sudo find . -type d -name '*' -exec chmod u+rwx,g+rwxs,o+rx-w {} \;
check_command_status

echo starting mp server
sudo /www/html/scripts/start-mp.sh
check_command_status
echo done

sleep 3

# Verify if Jetty process is running and if any process is run by Tomcat
if ! (ps aux | grep netty | grep -v grep) then
    echo "Deployment failed. Performing backup..."
    do_backup
    exit 1
fi

log_message "Deployment completed successfully."