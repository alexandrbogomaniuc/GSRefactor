#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/cluster-hosts.sh
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"
# shellcheck source=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/phase7-cassandra.sh
source "${SCRIPT_DIR}/lib/phase7-cassandra.sh"

CASSANDRA_CONTAINER="$(cluster_hosts_get CASSANDRA_REFACTOR_CONTAINER refactor-c1-1)"
TABLE_LIST_FILE=""
OUTPUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra"
TS="$(date -u '+%Y%m%d-%H%M%S')"

usage() {
  cat <<USAGE
Usage: $(basename "$0") --table-list FILE [options]

Required:
  --table-list FILE  File with one keyspace.table per line

Options:
  --container NAME   Cassandra container (default: ${CASSANDRA_CONTAINER})
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

if [[ -z "$TABLE_LIST_FILE" || ! -f "$TABLE_LIST_FILE" ]]; then
  echo "table-list file is required and must exist" >&2
  exit 1
fi

mkdir -p "$OUTPUT_DIR"
OUT_FILE="${OUTPUT_DIR}/phase7-cassandra-table-counts-${CASSANDRA_CONTAINER}-${TS}.txt"
: > "$OUT_FILE"

set +e
phase7_cqlsh_exec "${CASSANDRA_CONTAINER}" "SELECT release_version FROM system.local;" > /dev/null
code=$?
set -e
if [[ $code -ne 0 ]]; then
  if [[ $code -eq 3 ]]; then
    phase7_write_docker_api_denied_stub "${OUT_FILE}" "${CASSANDRA_CONTAINER}" "table_counts"
    echo "table_counts=${OUT_FILE}"
    exit 3
  fi
  exit "$code"
fi

while IFS= read -r line; do
  table="$(echo "$line" | sed 's/[[:space:]]//g')"
  [[ -z "$table" || "$table" =~ ^# ]] && continue
  keyspace="${table%%.*}"
  table_name="${table#*.}"
  if [[ "$keyspace" == "$table_name" ]]; then
    echo "skip_invalid=${table}" >> "$OUT_FILE"
    continue
  fi
  echo "query=${keyspace}.${table_name}" >> "$OUT_FILE"
  set +e
  phase7_cqlsh_exec "${CASSANDRA_CONTAINER}" "SELECT COUNT(*) FROM ${keyspace}.${table_name};" >> "$OUT_FILE"
  code=$?
  set -e
  if [[ $code -ne 0 ]]; then
    if [[ $code -eq 3 ]]; then
      phase7_write_docker_api_denied_stub "${OUT_FILE}" "${CASSANDRA_CONTAINER}" "table_counts"
      echo "table_counts=${OUT_FILE}"
      exit 3
    fi
    echo "query_failed=${keyspace}.${table_name}" >> "$OUT_FILE"
  fi
  echo >> "$OUT_FILE"
done < "$TABLE_LIST_FILE"

echo "table_counts=${OUT_FILE}"
