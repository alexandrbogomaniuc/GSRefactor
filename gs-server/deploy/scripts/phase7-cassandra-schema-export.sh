#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
# shellcheck source=/dev/null
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"
# shellcheck source=/dev/null
source "${SCRIPT_DIR}/lib/phase7-cassandra.sh"

CASSANDRA_CONTAINER="$(cluster_hosts_get CASSANDRA_REFACTOR_CONTAINER refactor-c1-1)"
OUTPUT_DIR="${REPO_ROOT}/docs/phase7/cassandra"
TS="$(date -u '+%Y%m%d-%H%M%S')"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --container NAME  Cassandra container (default: ${CASSANDRA_CONTAINER})
  --output-dir DIR  Output directory (default: ${OUTPUT_DIR})
  -h, --help        Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --container)
      CASSANDRA_CONTAINER="$2"; shift 2 ;;
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

mkdir -p "$OUTPUT_DIR"
OUT_FILE="${OUTPUT_DIR}/phase7-cassandra-schema-${CASSANDRA_CONTAINER}-${TS}.cql"
set +e
phase7_cqlsh_exec "${CASSANDRA_CONTAINER}" "DESCRIBE SCHEMA;" > "$OUT_FILE"
code=$?
set -e
if [[ $code -ne 0 ]]; then
  if [[ $code -eq 3 ]]; then
    phase7_write_docker_api_denied_stub "${OUT_FILE}" "${CASSANDRA_CONTAINER}" "schema_export"
    echo "schema_export=${OUT_FILE}"
    exit 3
  fi
  exit "$code"
fi
echo "schema_export=${OUT_FILE}"
