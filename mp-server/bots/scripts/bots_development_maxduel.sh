#!/bin/bash

export JAVA_HOME="/opt/java/oracle-jdk8"
export PATH="$JAVA_HOME/bin:$PATH"

JAR_FILE_DIR="/app"

"$JAVA_HOME/bin/java" -Xms32M -Xmx512M \
  -DMY_GPER_STOP_ID=mp-bot \
  -DFAKE_MQB_API=true \
  -DBOT_SERVER_KAFKA_HOST=kafka1.gsmp.lan \
  -DBOT_SERVER_KAFKA_PORT=9092 \
  -DBOT_SERVER_KAFKA_TOPIC=to_bs_topic \
  -DDOMAIN_LAUNCH_URL=http://default-beta.discreetgaming.com \
  -DMQB_SITE_BOT_API_URL=https://ats.mdtest.io/ \
  -DBOT_MAX_CRASH_ROCK_PERCENT=25 \
  -DBOT_MAX_CRASH_MEDIUM_PERCENT=43 \
  -DBOT_MAX_CRASH_AGGRESSIVE_PERCENT=100 \
  -DMQB_SITE_SECRET_API_KEY=aH8qpfc@O^78iDe3jTpO \
  -DBASIC_AUTH_PASSWORD=ZGV2OkttTVo1RlZiYXdmcGo2 \
  -cp "$JAR_FILE_DIR/mp-bots-1.0.0-SNAPSHOT.jar:$JAR_FILE_DIR/lib/*" \
  com.betsoft.casino.bots.ManagedStarter
