#!/bin/sh
#./run-archiver.sh . cassandra_gamesession_and_bet
#./run-archiver.sh . cassandra_gamesession_and_bet 10.02.2015
java -DGS_SERVER_ID=1 -Dlog4j.shutdownHookEnabled=true \
    -classpath "./casino-archiver.jar:/www/html/gs/ROOT/WEB-INF/lib/*:/www/html/gs/ROOT/WEB-INF/classes/" \
    com.abs.casino.support.Archiver "$@"
