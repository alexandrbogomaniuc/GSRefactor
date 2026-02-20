#!/usr/bin/env bash

SERVER_TYPE=$2
if [[ ! "$SERVER_TYPE" =~ ^(mp|gs)$ ]]; then
    echo "Syntax gsmp.sh start|stop mp|gs"
    exit 1
fi

function log() {
    echo "[Watchdog] [`date`] ${*}"
}

# Checks if process by given pid is running
function pid_running() {
    ps -p $1 2>&1 >/dev/null
    if [ $? -eq 0 ]; then
        return 1
    else
        return 0
    fi
}

#;
# wait_server_up()
# waits for target server (mp or gs) to fully boot and initialize
# @param check_url URL to check server. Success criteria is when given URL returns response code 200
# @param server_pid server PID
# @return void
#"
function wait_server_up()
{
    # Wait for given to become available (return HTTP 200)
    log "Waiting for ${1} to become available..."
    until curl -s -o /dev/null -w "%{http_code}" "${1}" | grep -q "200"; do
        pid_running $2
        if [[ $? -eq 0 ]]; then
            log "Server PID ${2} died while checking ${1}"
            exit 1
        fi

        sleep 1
    done
    log "${1} is available."
}

function fetch_server_id()
{
    if [ "$1" != "mp" ]; then
        log "Server ID retrieval for $1 is not needed. Ignoring."
        return
    fi

    log "Fetching server ID from ${2}..."
    SERVER_ID=$(curl -s "$2")

    # Check if SERVER_ID is numeric
    if ! [[ "$SERVER_ID" =~ ^[0-9]+$ ]]; then
      log "Error: Server ID is not numeric. Received: '${SERVER_ID}'. Server restart is needed."
      exit 1
    fi
    
    log "Server ID is valid number: ${SERVER_ID}"
    return $SERVER_ID
}

function notify_load_balancer() 
{
    attempt=1
    while [ $attempt -le 30 ]; do
        log "Attempt $attempt to register against load balancer ${1}..."

        if curl -s -f -o /dev/null "${1}"; then
            log "Successfully called ${1}."
            return
        else
            log "Request failed. Retrying in 2 seconds..."
            sleep 2
        fi
        ((attempt++))
    done

    log "All attempts to contact the load balancer failed."
    exit 1
}

# Tries to terminate process gracefully
# if graceful termination doesn't succeed, kills process
function terminate_process() {
    local pid=$1
    local timeout=30
    local elapsed=0

    if ! kill -0 "$pid" 2>/dev/null; then
        echo "Process $pid not running."
        return 1
    fi

    echo "Sending SIGTERM to process $pid..."
    kill -TERM "$pid"

    # Wait up to $timeout seconds
    while kill -0 "$pid" 2>/dev/null; do
        if (( elapsed >= timeout )); then
            echo "Process $pid did not terminate after $timeout seconds. Sending SIGKILL..."
            kill -KILL "$pid"
            break
        fi
        sleep 1
        ((elapsed++))
    done

    # Final check
    if kill -0 "$pid" 2>/dev/null; then
        echo "Failed to terminate process $pid."
        return 1
    else
        echo "Process $pid terminated."
        return 0
    fi
}

CHECK_URL=
SERVER_UP_URL=
SERVER_DOWN_URL=

if [[ "$SERVER_TYPE" == "gs" ]]; then
    CHECK_URL="http://127.0.0.1:8080/checkServerId.jsp"
    SERVER_UP_URL="http://${LB_VM_IP}/gs/dlb/add?ip=${SERVER_VM_IP}&port=8080"
    SERVER_DOWN_URL="http://${LB_VM_IP}/gs/dlb/remove?ip=${SERVER_VM_IP}&port=8080"
elif [[ "$SERVER_TYPE" == "mp" ]]; then
    CHECK_URL="http://127.0.0.1:8080/checkServerId.jsp"
    SERVER_UP_URL="http://${LB_VM_IP}/mp/dlb/add?ip=${SERVER_VM_IP}&port=8080&sid=%s"
    SERVER_DOWN_URL="http://${LB_VM_IP}/mp/dlb/remove?sid=%s"
fi

#CHECK_URL="http://127.0.0.1:8080/getid"
#SERVER_UP_URL="http://127.0.0.1:8080/up"
#SERVER_DOWN_URL="https://google.com"

WATCHDOG_PIDFILE=~/.$2.watchdog.pid
SERVER_PIDFILE=~/.$2.pid
SERVER_SIDFILE=~/.$2.sid

WATCHDOG_PID=
SERVER_PID=

SERVER_ID=

function notify_lb()
{
    sid=`cat $SERVER_SIDFILE`
    LB_URL=$(printf "$1" "$sid")
    notify_load_balancer $LB_URL
}

function stop_server()
{
    log Server shutdown initiated

    pid=`cat $SERVER_PIDFILE`

    log Going to terminate process with pid $pid
    terminate_process $pid

    # removing pidfiles
    rm -f $WATCHDOG_PIDFILE
    rm -f $SERVER_PIDFILE

    # removing traps
    trap - SIGINT SIGTERM EXIT


    notify_lb $SERVER_DOWN_URL

    rm -f $SERVER_SIDFILE

    # graceful shutdown
    exit 0
}

if [ "$1" = start ]; then

    if test -f "$WATCHDOG_PID"; then
        ps -p `cat $WATCHDOG_PID`
        if [ $? = 0 ]; then
            # process is already running
            exit
        fi
    fi

    WATCHDOG_PID=$$
    echo $$ > "$WATCHDOG_PIDFILE"
    log Watchdog process started with pid $WATCHDOG_PID

    trap "stop_server" EXIT SIGTERM SIGINT

    while true; do
        # python3 s.py &

        if [[ "$SERVER_TYPE" == "gs" ]]; then
            /docker-entrypoint.sh java -jar /usr/local/jetty/start.jar &
        elif [[ "$SERVER_TYPE" == "mp" ]]; then
            /usr/local/tomcat/bin/catalina.sh run &
        fi

        SERVER_PID=$!
        echo $! > "$SERVER_PIDFILE"
        log Server process started with pid $SERVER_PID

        # waiting for server to boot fully
        wait_server_up "$CHECK_URL" "$SERVER_PID"

        # retrieve server ID
        fetch_server_id "$SERVER_TYPE" "$CHECK_URL"
        SERVER_ID=$?
        echo "$SERVER_ID" > "$SERVER_SIDFILE"

        # register server against load balancer
        notify_lb $SERVER_UP_URL

        # wait background server process to finish with proper signal handling
        # loop to check if the process is still running
        while kill -0 "$SERVER_PID" 2>/dev/null; do
            sleep 1
        done

        log "Server process of ${SERVER_PID} exited. Watchdog is going to restart the process."

        # unregister server against load balancer
        # notify_lb $SERVER_DOWN_URL -> moved to pre_stop.sh
    done

elif [ "$1" = stop ]; then
    kill -TERM `cat "$PIDFILE"`
else
    nohup "$0" -daemon
fi