#!/usr/bin/env bash
set -euo pipefail

SOURCE_SCHEMA=""
TARGET_SCHEMA=""
OUTPUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase7/cassandra"
TS="$(date -u '+%Y%m%d-%H%M%S')"

usage() {
  cat <<USAGE
Usage: $(basename "$0") --source FILE --target FILE [options]

Required:
  --source FILE     Source schema export file
  --target FILE     Target schema export file

Options:
  --output-dir DIR  Output directory (default: ${OUTPUT_DIR})
  -h, --help        Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --source)
      SOURCE_SCHEMA="$2"; shift 2 ;;
    --target)
      TARGET_SCHEMA="$2"; shift 2 ;;
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

if [[ -z "$SOURCE_SCHEMA" || -z "$TARGET_SCHEMA" ]]; then
  echo "source and target files are required" >&2
  usage
  exit 1
fi

if [[ ! -f "$SOURCE_SCHEMA" ]]; then
  echo "source schema not found: ${SOURCE_SCHEMA}" >&2
  exit 1
fi
if [[ ! -f "$TARGET_SCHEMA" ]]; then
  echo "target schema not found: ${TARGET_SCHEMA}" >&2
  exit 1
fi

mkdir -p "$OUTPUT_DIR"
OUT_FILE="${OUTPUT_DIR}/phase7-cassandra-schema-diff-${TS}.patch"

diff -u "$SOURCE_SCHEMA" "$TARGET_SCHEMA" > "$OUT_FILE" || true

if [[ ! -s "$OUT_FILE" ]]; then
  echo "schema_diff=NONE"
  echo "schema_diff_file=${OUT_FILE}"
  exit 0
fi

echo "schema_diff=FOUND"
echo "schema_diff_file=${OUT_FILE}"
exit 2
