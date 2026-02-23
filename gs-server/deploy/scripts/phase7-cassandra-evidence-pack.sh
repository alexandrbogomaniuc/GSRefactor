#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/cluster-hosts.sh
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

CASSANDRA_CONTAINER="$(cluster_hosts_get CASSANDRA_REFACTOR_CONTAINER refactor-c1-1)"
OUTPUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra"
TABLE_LIST_FILE="/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra/critical-tables.txt"
LIMIT=1
TS="$(date -u '+%Y%m%d-%H%M%S')"
MANIFEST="${OUTPUT_DIR}/phase7-cassandra-evidence-pack-${TS}.manifest.txt"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --container NAME   Cassandra container (default: ${CASSANDRA_CONTAINER})
  --output-dir DIR   Output directory (default: ${OUTPUT_DIR})
  --table-list FILE  Critical table list (default: ${TABLE_LIST_FILE})
  --limit N          Query smoke limit (default: ${LIMIT})
  -h, --help         Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --container)
      CASSANDRA_CONTAINER="$2"; shift 2 ;;
    --output-dir)
      OUTPUT_DIR="$2"; shift 2 ;;
    --table-list)
      TABLE_LIST_FILE="$2"; shift 2 ;;
    --limit)
      LIMIT="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

mkdir -p "$OUTPUT_DIR"
: > "$MANIFEST"

log_manifest() {
  echo "$1" >> "$MANIFEST"
}

run_cmd() {
  local label="$1"
  shift
  echo "running=${label}"
  if output="$($@ 2>&1)"; then
    echo "${output}"
    log_manifest "${label}:PASS"
    log_manifest "${label}:OUTPUT:${output//$'\n'/ | }"
  else
    code=$?
    echo "${output}" >&2
    log_manifest "${label}:FAIL:${code}"
    log_manifest "${label}:OUTPUT:${output//$'\n'/ | }"
  fi
}

run_cmd preflight \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-preflight.sh \
  --container "$CASSANDRA_CONTAINER" --output-dir "$OUTPUT_DIR"

run_cmd driver_inventory \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-driver-inventory.sh \
  --out-dir "$OUTPUT_DIR"

run_cmd schema_export \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-schema-export.sh \
  --container "$CASSANDRA_CONTAINER" --output-dir "$OUTPUT_DIR"

run_cmd table_counts \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-table-counts.sh \
  --container "$CASSANDRA_CONTAINER" --table-list "$TABLE_LIST_FILE" --output-dir "$OUTPUT_DIR"

run_cmd query_smoke \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase7-cassandra-query-smoke.sh \
  --container "$CASSANDRA_CONTAINER" --table-list "$TABLE_LIST_FILE" --limit "$LIMIT" --output-dir "$OUTPUT_DIR"

echo "manifest=${MANIFEST}"
