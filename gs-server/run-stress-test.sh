#!/bin/sh
java -Xms1024m -Xmx2048m -Dlog4j.debug -classpath ".:./slottest-1.0.jar:/www/html/gs/ROOT/WEB-INF/lib/*" com.dgphoenix.casino.stresstest.Starter http://games.xxx.com gs1.xxx.com ana1z 500 15000 200 100 9999999 1 COMMON_WALLET real 121 27 USD bla.com ALL 300 3000

