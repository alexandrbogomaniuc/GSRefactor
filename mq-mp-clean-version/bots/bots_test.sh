#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-8
export JAVA=$JAVA_HOME/bin/java
PID_FILE="/tmp/bots_pid.txt"
JAR_FILE_DIR="/www/html/mp/bots"

function start() {
        if [ -f "$PID_FILE" ]; then
            echo "Java program is already running with PID: $(cat $PID_FILE)"
        else
            nohup $JAVA -Xms32M -Xmx512M -DBOT_SERVER_THRIFT_HOST=gs1 -DMY_GPER_STOP_ID=mp-bot -DBOT_SERVER_THRIFT_PORT=6400 -DFAKE_MQB_API=true \
            -DBOT_SERVER_KAFKA_HOST=kafka1.gsmp.lan -DBOT_SERVER_KAFKA_PORT=9092 -DBOT_SERVER_KAFKA_TOPIC=to_bs_topic \
            -DDOMAIN_LAUNCH_URL=http://default-beta.discreetgaming.com -DMQB_SITE_BOT_API_URL=https://ats-test.maxquestgame.com/ \
            -DBOT_MAX_CRASH_ROCK_PERCENT=25 -DBOT_MAX_CRASH_MEDIUM_PERCENT=43 -DBOT_MAX_CRASH_AGGRESSIVE_PERCENT=100 \
            -DMQB_SITE_SECRET_API_KEY=aH8qpfc@O^78iDe3jTpO -DBASIC_AUTH_PASSWORD=ZGV2OkttTVo1RlZiYXdmcGo2 -cp \
            $JAR_FILE_DIR/mp-bots-1.0.0-SNAPSHOT.jar:$JAR_FILE_DIR/lib/* com.betsoft.casino.bots.ManagedStarter > /dev/null 2>&1 &
            echo $! > "$PID_FILE"
            echo "Java program started with PID: $(cat $PID_FILE)"
        fi
}

function stop() {
        if [ -f "$PID_FILE" ]; then
            PID=$(cat "$PID_FILE")
            echo "Stopping the program with PID: $PID"
            kill "$PID"
            rm "$PID_FILE"
            echo "Java program stopped"
        else
            echo "Java program is not running"
        fi
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
