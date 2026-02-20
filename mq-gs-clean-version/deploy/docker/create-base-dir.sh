#!/bin/bash

if [[ $EUID -eq 0 ]]; then
   echo "You should not run script under root" 
   exit 1
fi

if [ -z ${BASE_WEBAPP_DIR} ]; then
   echo "Specify base directory for your webapps:";
   read BASE_WEBAPP_DIR;
fi


mkdir -p ${BASE_WEBAPP_DIR}/logs/gs \
	${BASE_WEBAPP_DIR}/logs/nginx \
	${BASE_WEBAPP_DIR}/logs/cassandra \
	${BASE_WEBAPP_DIR}/webapps/gs \
	${BASE_WEBAPP_DIR}/static/info \
	${BASE_WEBAPP_DIR}/static/jackpots \
	${BASE_WEBAPP_DIR}/static/winners \
	${BASE_WEBAPP_DIR}/default-configs/;

chmod -R a+xrw ${BASE_WEBAPP_DIR}/logs/gs \
	${BASE_WEBAPP_DIR}/logs \
	${BASE_WEBAPP_DIR}/static;

chmod a+xr ${BASE_WEBAPP_DIR}/webapps/gs \
	${BASE_WEBAPP_DIR}/default-configs/;