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
        echo "Command failed. Performing rollback..."
        perform_rollback
        exit 1
    fi
}

# Function to perform rollback
function perform_rollback() {
    echo "Rollback started"
    sudo /etc/init.d/netty stop
    sudo rm -r ${ROOT_DIR}
    sudo cp -r ${BACKUP_DIR} ${ROOT_DIR}
    sudo /etc/init.d/netty start

    sleep 20  # Waits 20 seconds; adjust as needed

    if ! (ps aux | grep netty | grep -v grep) then
        log_message "Rollback failed."
        exit 1
    else
        log_message "Rollback completed successfully."
    fi
}

function remove_backup_tmp_dir(){
    if [ -d ${BACKUP_DIR}_tmp ]; then
        echo removing backup dir...
        sudo rm -r ${BACKUP_DIR}_tmp
        echo done
    fi
}

# Function to perform deployment
function deploy() {
    echo "Deployment started"
    # if (ps aux | grep netty | grep -v grep) then
    #     echo "Stopping mp server"
    #     sudo /etc/init.d/netty stop
    #     check_command_status
    #     echo "Done"
    # fi

    echo "Checking for Netty process..."
    NETTY_PID=$(ps aux | grep "NettyServer" | grep java | awk '{print $2}')

    if [ -n "$NETTY_PID" ]; then
        echo "Stopping Netty server..."
        sudo /etc/init.d/netty stop

        TIMEOUT=30
        INTERVAL=2
        ELAPSED=0

        while [ -n "$(ps -p $NETTY_PID -o pid=)" ]; do
            if [ $ELAPSED -ge $TIMEOUT ]; then
                echo "Netty did not stop within $TIMEOUT seconds. Force killing..."
                sudo kill -9 $NETTY_PID
                break
            fi
            sleep $INTERVAL
            ELAPSED=$((ELAPSED + INTERVAL))
        done

        echo "Netty server stopped successfully."
    else
        echo "Netty server is not running."
    fi

    if [ -d ${BACKUP_DIR} ]; then
        remove_backup_tmp_dir
        sudo mv ${BACKUP_DIR} ${BACKUP_DIR}_tmp
        check_command_status
    fi

    sudo mv ${ROOT_DIR} ${BACKUP_DIR}
    check_command_status

    remove_backup_tmp_dir
    echo "Unzipping deployment war"
    sudo unzip ${MP_DIR}/web-mp-casino.war -d ${ROOT_DIR}
    check_command_status
    echo "Done"

    cd /www/html/mp/ROOT || exit 1
    sudo find . -type f -name '*' -exec chmod u+rw-x,g+rw-x,o+r-wx {} \;
    cd /www/html/mp || exit 1

    sudo find . -type d -name '*' -exec chmod u+rwx,g+rwxs,o+rx-w {} \;
    check_command_status

    echo "Starting mp service..."
    nohup sudo /etc/init.d/netty start > /dev/null 2>&1 &

    check_command_status
    echo "Done"

    sleep 20  # Waits 20 seconds; adjust as needed

    if ! (ps aux | grep netty | grep -v grep) then
        echo "Deployment failed. Performing rollback..."
        perform_rollback
        exit 1
    fi

    log_message "Deployment completed successfully."
}

# Main script execution logic
case "$1" in
    deploy)
        deploy
        ;;
    rollback)
        perform_rollback
        ;;
    *)
        echo "Usage: $0 {deploy|rollback}"
        exit 1
        ;;
esac
