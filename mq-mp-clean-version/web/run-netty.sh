#!/bin/sh
java -DMP_SERVER_ID=1 -cp .:./target/web-mp-casino/WEB-INF/classes/:./target/web-mp-casino/WEB-INF/lib/* com.betsoft.casino.mp.web.NettyServer
