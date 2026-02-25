#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GS_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
REPO_ROOT="$(cd "${GS_ROOT}/.." && pwd)"
OUT_DIR="${REPO_ROOT}/docs/phase7/cassandra"
TS="$(date -u '+%Y%m%d-%H%M%S')"
OUT_FILE=""

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --repo-root DIR  Repository root that contains gs-server/mp-server (default: ${REPO_ROOT})
  --out-dir DIR    Output directory (default: ${OUT_DIR})
  -h, --help       Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo-root)
      REPO_ROOT="$2"
      OUT_DIR="${REPO_ROOT}/docs/phase7/cassandra"
      shift 2 ;;
    --out-dir)
      OUT_DIR="$2"
      shift 2 ;;
    -h|--help)
      usage
      exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

OUT_FILE="${OUT_DIR}/phase7-cassandra-driver-migration-backlog-${TS}.md"
mkdir -p "$OUT_DIR"

scan_targets=("${REPO_ROOT}/gs-server" "${REPO_ROOT}/mp-server")

driver3_files="$(rg -l --glob "*.java" "import com\\.datastax\\.driver\\.core\\." "${scan_targets[@]}" || true)"

total_files=0
gs_files=0
mp_files=0
if [[ -n "${driver3_files}" ]]; then
  total_files="$(printf '%s\n' "${driver3_files}" | wc -l | tr -d ' ')"
  gs_files="$(printf '%s\n' "${driver3_files}" | rg "^${REPO_ROOT}/gs-server/" | wc -l | tr -d ' ')"
  mp_files="$(printf '%s\n' "${driver3_files}" | rg "^${REPO_ROOT}/mp-server/" | wc -l | tr -d ' ')"
fi

{
  echo "# Cassandra Driver Migration Backlog"
  echo
  echo "timestamp_utc: $(date -u '+%Y-%m-%dT%H:%M:%SZ')"
  echo "repo_root: ${REPO_ROOT}"
  echo
  echo "## Summary"
  echo "- Java files importing driver3 API: **${total_files}**"
  echo "- GS files: **${gs_files}**"
  echo "- MP files: **${mp_files}**"
  echo

  echo "## Module Hotspots (by path group)"
  if [[ -n "${driver3_files}" ]]; then
    printf '%s\n' "${driver3_files}" \
      | sed "s#^${REPO_ROOT}/##" \
      | awk -F/ '
          {
            if ($1 == "gs-server" && $2 == "cassandra-cache") key = $1 "/" $2 "/" $3;
            else if ($1 == "gs-server" && $2 == "promo") key = $1 "/" $2 "/" $3;
            else if ($1 == "mp-server") key = $1 "/" $2;
            else key = $1 "/" $2;
            counts[key]++
          }
          END {
            for (k in counts) {
              print counts[k] "\t" k
            }
          }' \
      | sort -rn \
      | awk -F'\t' '{printf "- %s files: `%s`\n", $1, $2}'
  else
    echo "- No driver3 import files found."
  fi
  echo

  echo "## Top Files By Driver3 Import Lines"
  if [[ -n "${driver3_files}" ]]; then
    rg -n --glob "*.java" "import com\\.datastax\\.driver\\.core\\." "${scan_targets[@]}" \
      | cut -d: -f1 \
      | sed "s#^${REPO_ROOT}/##" \
      | sort | uniq -c | sort -rn | head -n 40 \
      | awk '{count=$1; $1=""; sub(/^ /, "", $0); printf "- %s lines: `%s`\n", count, $0}'
  else
    echo "- No data."
  fi
  echo

  echo "## Driver3 Type Usage (import-based)"
  if [[ -n "${driver3_files}" ]]; then
    rg -N --no-filename --glob "*.java" -o "import com\\.datastax\\.driver\\.core\\.[A-Za-z0-9_]+" "${scan_targets[@]}" \
      | sed -E 's/^import com\.datastax\.driver\.core\.//' \
      | sort | uniq -c | sort -rn | head -n 40 \
      | awk '{count=$1; $1=""; sub(/^ /, "", $0); printf "- %s: `%s`\n", count, $0}'
  else
    echo "- No data."
  fi
  echo

  echo "## Recommended Migration Order (Wave 2+)"
  echo '- 1) `gs-server/cassandra-cache/cache`'
  echo '- 2) `gs-server/cassandra-cache/common-persisters`'
  echo '- 3) `mp-server/persistance`'
  echo '- 4) `gs-server/promo/persisters`'
  echo "- 5) remaining low-count modules"
  echo

  echo "## API Mapping Starters (driver3 -> driver4)"
  echo '- `Session` -> `CqlSession`'
  echo '- `ResultSet` -> `com.datastax.oss.driver.api.core.cql.ResultSet`'
  echo '- `Row` -> `com.datastax.oss.driver.api.core.cql.Row`'
  echo '- `Statement` -> `SimpleStatement` / `BoundStatement`'
  echo '- `ConsistencyLevel` -> `DefaultConsistencyLevel`'
  echo '- `querybuilder` -> `com.datastax.oss.driver.api.querybuilder`'
  echo '- `DataType` -> `com.datastax.oss.driver.api.core.type.DataTypes`'
} > "${OUT_FILE}"

echo "driver_migration_backlog=${OUT_FILE}"
