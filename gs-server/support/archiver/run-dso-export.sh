#!/bin/sh
java -DGS_SERVER_ID=1 -Xms256m -Xmx768m \
    -classpath "./casino-archiver.jar:/www/html/gs/ROOT/WEB-INF/lib/*:/www/html/gs/ROOT/WEB-INF/classes/" \
    com.abs.casino.support.DsoExport "$@"