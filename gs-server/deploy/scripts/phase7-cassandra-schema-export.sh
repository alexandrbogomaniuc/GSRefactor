#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/cluster-hosts.sh
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

CASSANDRA_CONTAINER="$(cluster_hosts_get CASSANDRA_REFACTOR_CONTAINER refactor-c1-1)"
OUTPUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra"
TS="$(date -u '+%Y%m%d-%H%M%S')"
OUT_FILE="${OUTPUT_DIR}/phase7-cassandra-schema-${CASSANDRA_CONTAINER}-${TS}.cql"

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
docker exec "${CASSANDRA_CONTAINER}" cqlsh -e "DESCRIBE SCHEMA;" > "$OUT_FILE"
echo "schema_export=${OUT_FILE}"
