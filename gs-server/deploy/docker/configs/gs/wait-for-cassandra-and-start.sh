#!/bin/bash

rewrite_cluster_hosts_if_present() {
  cluster_host="${CASSANDRA_HOST:-c1}"
  cluster_port="${CASSANDRA_PORT:-9042}"
  cluster_target="${cluster_host}:${cluster_port}"

  for cfg in \
    /var/lib/jetty/webapps/ROOT/WEB-INF/classes/ClusterConfig.xml \
    /var/lib/jetty/webapps/ROOT/WEB-INF/classes/SCClusterConfig.xml \
    /var/lib/jetty/webapps/ROOT/WEB-INF/classes/BigStorageClusterConfig.xml \
    /www/html/gs/ROOT/export/ClusterConfig.xml \
    /www/html/gs/ROOT/export/SCClusterConfig.xml \
    /www/html/gs/ROOT/export/BigStorageClusterConfig.xml; do
    if [ -f "$cfg" ]; then
      sed -i "s#<hosts>[^<]*</hosts>#<hosts>${cluster_target}</hosts>#" "$cfg"
      echo "Patched Cassandra cluster hosts in ${cfg} -> ${cluster_target}"
    fi
  done
}

rewrite_runtime_cluster_hosts_properties_if_present() {
  cfg="/var/lib/jetty/webapps/ROOT/WEB-INF/classes/cluster-hosts.properties"
  if [ ! -f "$cfg" ]; then
    return 0
  fi

  sed -i "s#^CASSANDRA_HOST=.*#CASSANDRA_HOST=${CASSANDRA_HOST}#" "$cfg"
  sed -i "s#^CASSANDRA_PORT=.*#CASSANDRA_PORT=${CASSANDRA_PORT}#" "$cfg"
  sed -i "s#^ZOOKEEPER_HOST=.*#ZOOKEEPER_HOST=${ZOOKEEPER_HOST}#" "$cfg"
  sed -i "s#^ZOOKEEPER_PORT=.*#ZOOKEEPER_PORT=${ZOOKEEPER_PORT}#" "$cfg"
  sed -i "s#^KAFKA_HOST=.*#KAFKA_HOST=${KAFKA_HOST}#" "$cfg"
  sed -i "s#^KAFKA_PORT=.*#KAFKA_PORT=${KAFKA_PORT}#" "$cfg"
  echo "Patched runtime cluster-hosts.properties -> CASSANDRA_HOST=${CASSANDRA_HOST}, KAFKA_HOST=${KAFKA_HOST}, ZOOKEEPER_HOST=${ZOOKEEPER_HOST}"
}

wait_for_service() {
  service_name="$1"
  host="$2"
  port="$3"
  retries=1

  echo "Checking if ${service_name} is up and running on ${host}:${port} ..."
  nc -z "${host}" "${port}"
  status=$?

  while (( status != 0 )); do
    echo "${service_name} doesn't reply on ${host}:${port}. Sleeping and retrying... retry ${retries}"
    sleep 2s
    nc -z "${host}" "${port}"
    status=$?
    let "retries++"
  done

  echo "${service_name} startup completed successfully --- OK"
}

CASSANDRA_HOST="${CASSANDRA_HOST:-c1}"
CASSANDRA_PORT="${CASSANDRA_PORT:-9042}"
ZOOKEEPER_HOST="${ZOOKEEPER_HOST:-zookeeper}"
ZOOKEEPER_PORT="${ZOOKEEPER_PORT:-2181}"
KAFKA_HOST="${KAFKA_HOST:-kafka}"
KAFKA_PORT="${KAFKA_PORT:-9092}"

rewrite_cluster_hosts_if_present
rewrite_runtime_cluster_hosts_properties_if_present
wait_for_service "Cassandra" "${CASSANDRA_HOST}" "${CASSANDRA_PORT}"
wait_for_service "ZooKeeper" "${ZOOKEEPER_HOST}" "${ZOOKEEPER_PORT}"
wait_for_service "Kafka" "${KAFKA_HOST}" "${KAFKA_PORT}"
