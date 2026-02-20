#!/usr/bin/env bash
set -euo pipefail

GS_ROOT="/Users/alexb/Documents/Dev/Dev_new/gs-server"
MP_ROOT="/Users/alexb/Documents/Dev/Dev_new/mp-server"
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra"
TS="$(date -u '+%Y%m%d-%H%M%S')"
OUT_FILE="${OUT_DIR}/phase7-cassandra-driver-inventory-${TS}.txt"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --gs-root DIR   GS root (default: ${GS_ROOT})
  --mp-root DIR   MP root (default: ${MP_ROOT})
  --out-dir DIR   Output dir (default: ${OUT_DIR})
  -h, --help      Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --gs-root)
      GS_ROOT="$2"; shift 2 ;;
    --mp-root)
      MP_ROOT="$2"; shift 2 ;;
    --out-dir)
      OUT_DIR="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

mkdir -p "$OUT_DIR"

{
  echo "timestamp_utc=$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
  echo "gs_root=${GS_ROOT}"
  echo "mp_root=${MP_ROOT}"
  echo
  echo "== GS Cassandra driver properties =="
  rg -n "cassandra.driver.version|cassandra-driver-core|cassandra-driver-mapping" \
    "${GS_ROOT}/cassandra-cache/pom.xml" -S || true
  echo
  echo "== MP Cassandra driver properties =="
  rg -n "cassandra.driver.version|cassandra-driver-core|spring-data-cassandra" \
    "${MP_ROOT}/pom.xml" "${MP_ROOT}/persistance/pom.xml" -S || true
} > "$OUT_FILE"

echo "driver_inventory=${OUT_FILE}"
