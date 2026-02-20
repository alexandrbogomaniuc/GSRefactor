#!/usr/bin/env bash
set -euo pipefail

CASSANDRA_CONTAINER="refactor-c1-1"
TABLE_LIST_FILE=""
OUTPUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra"
TS="$(date -u '+%Y%m%d-%H%M%S')"
OUT_FILE="${OUTPUT_DIR}/phase7-cassandra-table-counts-${CASSANDRA_CONTAINER}-${TS}.txt"

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
: > "$OUT_FILE"

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
  docker exec "${CASSANDRA_CONTAINER}" cqlsh -e "SELECT COUNT(*) FROM ${keyspace}.${table_name};" >> "$OUT_FILE" || {
    echo "query_failed=${keyspace}.${table_name}" >> "$OUT_FILE"
  }
  echo >> "$OUT_FILE"
done < "$TABLE_LIST_FILE"

echo "table_counts=${OUT_FILE}"
