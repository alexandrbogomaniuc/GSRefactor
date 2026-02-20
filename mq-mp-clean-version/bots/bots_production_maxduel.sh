#!/bin/bash

export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
export PATH="$PATH:$JAVA_HOME/bin"
export JAVA="$JAVA_HOME/bin/java"

PID_FILE="/var/run/bots.pid"
JAR_FILE_DIR="/www/html/mp/bots"

function start() {
    if [ -f "$PID_FILE" ]; then
        PID=`cat ${PID_FILE}`
        ps -p $PID -o command
        if [[ "0" = "$?" ]]; then
            echo "Java program is already running with PID: $(cat $PID_FILE)"
            exit 1
        fi
    fi

    nohup $JAVA -Xms32M -Xmx512M -DMY_GPER_STOP_ID=mp-bot -DFAKE_MQB_API=true \
    -DBOT_SERVER_KAFKA_HOST=kafka1.gsmp.lan -DBOT_SERVER_KAFKA_PORT=9092 -DBOT_SERVER_KAFKA_TOPIC=to_bs_topic \
    -DDOMAIN_LAUNCH_URL=http://default-beta.discreetgaming.com -DMQB_SITE_BOT_API_URL=https://ats.maxduel.com/ \
    -DBOT_MAX_CRASH_ROCK_PERCENT=25 -DBOT_MAX_CRASH_MEDIUM_PERCENT=43 -DBOT_MAX_CRASH_AGGRESSIVE_PERCENT=100 \
    -DMQB_SITE_SECRET_API_KEY=aH8qpfc@O^78iDe3jTpO -DBASIC_AUTH_PASSWORD=ZGV2OkttTVo1RlZiYXdmcGo2 -cp \
    $JAR_FILE_DIR/mp-bots-1.0.0-SNAPSHOT.jar:$JAR_FILE_DIR/lib/* com.betsoft.casino.bots.ManagedStarter > /dev/null 2>&1 &

    echo $! > "$PID_FILE"
    echo "Java program started with PID: $(cat $PID_FILE)"
}

function stop() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        echo "Stopping the program with PID: $PID"
        kill "$PID"
        rm "$PID_FILE"
        echo "Java program stopped"
    else
        PID=`pgrep -af "java" | grep com.betsoft.casino.bots | cut -d' ' -f1`
        if [ -z "PID" ]; then 
            echo "Java program is not running";
            exit 1
        fi
    fi

    kill -SIGTERM "$PID"
    echo "Sent SIGTERM to process $PID."

    for i in {1..10}; do
      if ! ps -p "$PID" > /dev/null; then
        echo "Process $PID stopped after SIGTERM."
        rm -f $PID_FILE
        exit 0
      fi
      sleep 1
    done

    echo "Process $PID did not stop after SIGTERM. Sending SIGKILL."
    kill -SIGKILL "$PID"
    rm -f $PID_FILE
}

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <start|stop|restart>"
    exit 1
fi

if [ "$1" = "start" ]; then
    start
elif [ "$1" = "stop" ]; then
    stop
elif [ "$1" = "restart" ]; then
    stop
    start
else
    echo "Unknown command: $1"
    exit 1
fi

exit 0
