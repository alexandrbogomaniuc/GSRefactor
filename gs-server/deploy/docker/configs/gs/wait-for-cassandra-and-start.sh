#!/bin/bash

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

wait_for_service "Cassandra" "${CASSANDRA_HOST}" "${CASSANDRA_PORT}"
wait_for_service "ZooKeeper" "${ZOOKEEPER_HOST}" "${ZOOKEEPER_PORT}"
wait_for_service "Kafka" "${KAFKA_HOST}" "${KAFKA_PORT}"
