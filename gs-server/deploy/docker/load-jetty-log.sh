/bin/bash
mkdir -p logs && docker ps | grep jetty-gs | awk '{ print $1 }' | xargs docker logs >logs/jetty.log 2>logs/stderr.log
