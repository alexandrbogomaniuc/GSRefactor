#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/cluster-hosts.sh
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"
# shellcheck source=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/phase7-cassandra.sh
source "${SCRIPT_DIR}/lib/phase7-cassandra.sh"

CASSANDRA_CONTAINER="$(cluster_hosts_get CASSANDRA_REFACTOR_CONTAINER refactor-c1-1)"
TABLE_LIST_FILE="/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/critical-tables.txt"
OUTPUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra"
LIMIT=1
TS="$(date -u '+%Y%m%d-%H%M%S')"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --container NAME   Cassandra container (default: ${CASSANDRA_CONTAINER})
  --table-list FILE  keyspace.table list file (default: ${TABLE_LIST_FILE})
  --limit N          LIMIT for sample query (default: ${LIMIT})
  --output-dir DIR   Output directory (default: ${OUTPUT_DIR})
  -h, --help         Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --container)
      CASSANDRA_CONTAINER="$2"; shift 2 ;;
    --table-list)
      TABLE_LIST_FILE="$2"; shift 2 ;;
    --limit)
      LIMIT="$2"; shift 2 ;;
    --output-dir)
      OUTPUT_DIR="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

if [[ ! -f "$TABLE_LIST_FILE" ]]; then
  echo "table-list file not found: ${TABLE_LIST_FILE}" >&2
  exit 1
fi

if ! [[ "$LIMIT" =~ ^[0-9]+$ ]] || (( LIMIT < 1 )); then
  echo "limit must be a positive integer" >&2
  exit 1
fi

mkdir -p "$OUTPUT_DIR"
OUT_FILE="${OUTPUT_DIR}/phase7-cassandra-query-smoke-${CASSANDRA_CONTAINER}-${TS}.log"

pass_count=0
fail_count=0
summary_status="READY"

set +e
phase7_cqlsh_exec "${CASSANDRA_CONTAINER}" "SELECT release_version FROM system.local;" > /dev/null
code=$?
set -e
if [[ $code -ne 0 ]]; then
  if [[ $code -eq 3 ]]; then
    phase7_write_docker_api_denied_stub "${OUT_FILE}" "${CASSANDRA_CONTAINER}" "query_smoke"
    echo "query_smoke_log=${OUT_FILE}"
    exit 3
  fi
  exit "$code"
fi

{
  echo "timestamp_utc=$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
  echo "container=${CASSANDRA_CONTAINER}"
  echo "table_list=${TABLE_LIST_FILE}"
  echo "limit=${LIMIT}"
  echo

  while IFS= read -r line; do
    table="$(echo "$line" | sed 's/[[:space:]]//g')"
    [[ -z "$table" || "$table" =~ ^# ]] && continue

    keyspace="${table%%.*}"
    table_name="${table#*.}"
    if [[ "$keyspace" == "$table_name" ]]; then
      echo "skip_invalid=${table}"
      continue
    fi

    echo "== ${keyspace}.${table_name} =="
    set +e
    phase7_cqlsh_exec "${CASSANDRA_CONTAINER}" "SELECT * FROM ${keyspace}.${table_name} LIMIT ${LIMIT};"
    code=$?
    set -e
    if [[ $code -eq 0 ]]; then
      echo "status=PASS"
      pass_count=$((pass_count + 1))
    else
      if [[ $code -eq 3 ]]; then
        summary_status="SKIP_DOCKER_API_DENIED"
        echo "status=SKIP_DOCKER_API_DENIED"
        echo "summary_status=${summary_status}"
        echo "summary_pass=${pass_count}"
        echo "summary_fail=${fail_count}"
        exit 3
      fi
      echo "status=FAIL"
      fail_count=$((fail_count + 1))
    fi
    echo
  done < "$TABLE_LIST_FILE"

  echo "summary_status=${summary_status}"
  echo "summary_pass=${pass_count}"
  echo "summary_fail=${fail_count}"
} > "$OUT_FILE"

echo "query_smoke_log=${OUT_FILE}"
if [[ "${summary_status}" == "SKIP_DOCKER_API_DENIED" ]]; then
  exit 3
fi
if (( fail_count > 0 )); then
  exit 2
fi
