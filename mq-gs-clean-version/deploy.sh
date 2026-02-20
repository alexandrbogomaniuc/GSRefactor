#!/bin/bash

BACKUP_DIR="/www/html/gs_backup/ROOT_bitbucket_backup";
ROOT_DIR="/www/html/gs/ROOT";
ROOT_WAR_DIR="/www/html/gs_backup";

LOG_FILE="/deployment_log.txt"

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
    sudo /etc/init.d/jetty.gs stop
    sudo rm -r ${ROOT_DIR}
    sudo cp -r ${BACKUP_DIR} ${ROOT_DIR}
    sudo /etc/init.d/jetty.gs start
    # Log backup success or failure
    if ! (ps aux | grep jetty.gs | grep -q -v grep) || ! (ps aux | grep tomcat | grep -q -v grep); then
        echo "Deployment failed. Performing backup..."
        log_message "Backup failed."
        exit 1
    else
        log_message "Backup completed successfully."
    fi
}

function remove_backup_tmp_dir(){
    if [ -d ${BACKUP_DIR}_tmp ]; then
        sudo rm -r ${BACKUP_DIR}_tmp
    fi
}

# Deploy
sudo /etc/init.d/jetty.gs stop
check_command_status

if [ -d ${BACKUP_DIR} ]; then
        remove_backup_tmp_dir
        sudo mv ${BACKUP_DIR} ${BACKUP_DIR}_tmp
        check_command_status
fi

sudo mv ${ROOT_DIR} ${BACKUP_DIR}
check_command_status

remove_backup_tmp_dir

sudo unzip ${ROOT_WAR_DIR}/ROOT.war -d ${ROOT_DIR}
check_command_status

sudo find . -type f -name '*' -exec chmod u+rw-x,g+rw-x,o+r-wx {} \;
check_command_status

sudo find . -type d -name '*' -exec chmod u+rwx,g+rwxs,o+rx-w {} \;
check_command_status

sudo /etc/init.d/jetty.gs start
check_command_status

# Verify if Jetty process is running and if any process is run by Tomcat
if ! (ps aux | grep jetty.gs | grep -q -v grep) || ! (ps aux | grep tomcat | grep -q -v grep); then
    echo "Deployment failed. Performing backup..."
    do_backup
    exit 1
fi

log_message "Deployment completed successfully."