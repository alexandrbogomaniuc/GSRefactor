#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
# shellcheck source=/dev/null
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

CASSANDRA_CONTAINER="$(cluster_hosts_get CASSANDRA_REFACTOR_CONTAINER refactor-c1-1)"
OUTPUT_DIR="${REPO_ROOT}/docs/phase7/cassandra"
TABLE_LIST_FILE="${REPO_ROOT}/docs/phase7/cassandra/critical-tables.txt"
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
  local code
  echo "running=${label}"
  if output="$($@ 2>&1)"; then
    echo "${output}"
    log_manifest "${label}:PASS"
    log_manifest "${label}:OUTPUT:${output//$'\n'/ | }"
  else
    code=$?
    echo "${output}" >&2
    if [[ $code -eq 3 ]]; then
      log_manifest "${label}:SKIP:DOCKER_API_DENIED"
    else
      log_manifest "${label}:FAIL:${code}"
    fi
    log_manifest "${label}:OUTPUT:${output//$'\n'/ | }"
  fi
}

run_cmd preflight \
  ${REPO_ROOT}/gs-server/deploy/scripts/phase7-cassandra-preflight.sh \
  --container "$CASSANDRA_CONTAINER" --output-dir "$OUTPUT_DIR"

run_cmd driver_inventory \
  ${REPO_ROOT}/gs-server/deploy/scripts/phase7-cassandra-driver-inventory.sh \
  --out-dir "$OUTPUT_DIR"

run_cmd schema_export \
  ${REPO_ROOT}/gs-server/deploy/scripts/phase7-cassandra-schema-export.sh \
  --container "$CASSANDRA_CONTAINER" --output-dir "$OUTPUT_DIR"

run_cmd table_counts \
  ${REPO_ROOT}/gs-server/deploy/scripts/phase7-cassandra-table-counts.sh \
  --container "$CASSANDRA_CONTAINER" --table-list "$TABLE_LIST_FILE" --output-dir "$OUTPUT_DIR"

run_cmd query_smoke \
  ${REPO_ROOT}/gs-server/deploy/scripts/phase7-cassandra-query-smoke.sh \
  --container "$CASSANDRA_CONTAINER" --table-list "$TABLE_LIST_FILE" --limit "$LIMIT" --output-dir "$OUTPUT_DIR"

echo "manifest=${MANIFEST}"
