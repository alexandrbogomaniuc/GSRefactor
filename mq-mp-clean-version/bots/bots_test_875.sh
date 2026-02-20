#!/bin/bash
export JAVA_HOME=/usr/lib/jvm/java-8
export JAVA=$JAVA_HOME/bin/java
JAR_FILE_DIR="/www/html/mp/bots"

$JAVA -Xms32M -Xmx512M -cp $JAR_FILE_DIR/mp-bots-1.0.0-SNAPSHOT.jar:$JAR_FILE_DIR/lib/* com.betsoft.casino.bots.Starter --botPrefix=bot --nickname=ByDarjan --botsCount=50 --gameId=875 --bankId=6274 --pass=12345678Aa! --emailPrefix=darjan+bot --url=http://default-test.maxquest.com/battlegroundstartgamev2.do --tokenGenerationUrl=https://wallet-test.maxquestgame.com/gentoken --googleAuthUrl=https://www.googleapis.com/identitytoolkit/v3/relyingparty/verifyPassword?key=AIzaSyBhSsSs7ZTVE1856-LTFpAs1-bcdK7DFlI