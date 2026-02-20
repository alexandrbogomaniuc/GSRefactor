#!/bin/bash

BUILD_DIR="./game-server/web-gs/target/ROOT";
DEPLOY_DIR="/www/html/gs/ROOT";
LOG_FILE="./deployment_locally.log"

# Function to log messages
function log_message() {
        # Create the log message
        local message="$(date +'%Y-%m-%d %H:%M:%S') - $1"

        # Output to both the console and the log file
        echo "$message" | sudo tee -a "$LOG_FILE"
}

# Function to check if the previous command succeeded
function check_command_status() {
    if [ $? -ne 0 ]; then
        log_message "Command failed."
        exit 1
    fi
}

log_message "Build project to ${BUILD_DIR}..."
#mvn clean install -f ./game-server/pom.xml -s ./game-server/build/build-settings.xml -Dcluster.properties=common.properties
mvn clean install -f ./game-server/pom.xml -s ./game-server/build/build-settings.xml -Dcluster.properties=common.properties -Dmaven.test.skip=true
check_command_status
log_message "Build project to ${BUILD_DIR} complete."


# Find and remove files, excluding the 'export' folder
log_message "Starting cleanup in ${DEPLOY_DIR}..."
sudo find ${DEPLOY_DIR} -mindepth 1 -path ${DEPLOY_DIR}/export -prune -o -exec rm -rf {} +
check_command_status
log_message "Cleanup complete. All files except for the 'export' directory have been removed."

# Copy all contents from BUILD_DIR to DEPLOY_DIR
log_message "Copying contents from ${BUILD_DIR} to ${DEPLOY_DIR}..."
sudo cp -r "${BUILD_DIR}/." "${DEPLOY_DIR}/"
check_command_status
log_message "Copying contents from ${BUILD_DIR} to ${DEPLOY_DIR} complete. All files have been copied."

log_message "Change access rights for files in ${DEPLOY_DIR}..."
sudo find ${DEPLOY_DIR} -type f -name '*' -exec chmod u+rw-x,g+rw-x,o+r-wx {} \;
check_command_status
log_message "Change access rights for files in ${DEPLOY_DIR} complete."

log_message "Change access rights for dirs in ${DEPLOY_DIR}..."
sudo find ${DEPLOY_DIR} -type d -name '*' -exec chmod u+rwx,g+rwxs,o+rx-w {} \;
log_message "Change access rights for dirs in ${DEPLOY_DIR} complete."
check_command_status




