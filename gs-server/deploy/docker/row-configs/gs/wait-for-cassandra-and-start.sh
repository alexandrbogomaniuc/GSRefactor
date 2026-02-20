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

wait_for_service "Cassandra" "c1" "9042"
wait_for_service "ZooKeeper" "zookeeper" "2181"
wait_for_service "Kafka" "kafka" "9092"
