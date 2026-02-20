#!/bin/bash

# CRONTAB:
# 15 0 * * * /www/html/mp/bots/bots_restart_scheduler.sh games-cp.mdtest.io     >> /www/logs/tomcat.mp/bots_restart_scheduler.log
# 15 0 * * * /www/html/mp/bots/bots_restart_scheduler.sh games-cp.mdbase.io     >> /www/logs/tomcat.mp/bots_restart_scheduler.log
# 15 0 * * * /www/html/mp/bots/bots_restart_scheduler.sh games-cp.maxduel.com   >> /www/logs/tomcat.mp/bots_restart_scheduler.log

DOMAIN_NAME=${1}

echo "$(date +%Y-%m-%d--%H:%M:%S) - CURL REQUEST TO DISABLE BOTS"
curl -I https://${DOMAIN_NAME}/ats/enable_AtsService.jsp?enabled=false
echo ""

echo "$(date +%Y-%m-%d--%H:%M:%S) - SLEEP 1200"
sleep 1200
echo ""

echo "$(date +%Y-%m-%d--%H:%M:%S) - BOTS.SH STOP"
/www/html/mp/bots/bots.sh stop
echo ""

echo "$(date +%Y-%m-%d--%H:%M:%S) - SLEEP 5"
sleep 5
echo ""

echo "$(date +%Y-%m-%d--%H:%M:%S) - BOTS.SH START"
/www/html/mp/bots/bots.sh start
echo ""

echo "$(date +%Y-%m-%d--%H:%M:%S) - SLEEP 10"
sleep 10
echo ""

echo "$(date +%Y-%m-%d--%H:%M:%S) - CURL REQUEST TO ENABLE BOTS"
curl -I https://${DOMAIN_NAME}/ats/enable_AtsService.jsp?enabled=true
echo ""

echo "$(date +%Y-%m-%d--%H:%M:%S) - SCRIPT FINISHED"
echo ""
