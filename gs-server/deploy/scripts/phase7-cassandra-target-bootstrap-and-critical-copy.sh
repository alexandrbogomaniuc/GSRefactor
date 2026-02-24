#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="/Users/alexb/Documents/Dev/Dev_new"
# shellcheck source=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/phase7-cassandra.sh
source "${SCRIPT_DIR}/lib/phase7-cassandra.sh"
COMPOSE_FILE="${ROOT}/gs-server/deploy/docker/refactor/docker-compose.yml"
ENV_FILE="${ROOT}/gs-server/deploy/docker/refactor/.env"
PROJECT="refactor"
TARGET_SERVICE="c1-refactor"
SOURCE_CONTAINER="refactor-c1-1"
TARGET_CONTAINER="refactor-c1-refactor-1"
TABLE_LIST_FILE="${ROOT}/docs/phase7/cassandra/critical-tables.txt"
OUT_DIR="${ROOT}/docs/phase7/cassandra"
TS="$(date -u '+%Y%m%d-%H%M%S')"
REPORT="${OUT_DIR}/phase7-cassandra-target-bootstrap-and-critical-copy-${TS}.md"
SCHEMA_DIR="${OUT_DIR}/target-bootstrap-${TS}"
WAIT_SECONDS=180

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --target-service NAME     Default: ${TARGET_SERVICE}
  --source-container NAME   Default: ${SOURCE_CONTAINER}
  --target-container NAME   Default: ${TARGET_CONTAINER}
  --table-list FILE         Default: ${TABLE_LIST_FILE}
  --output-dir DIR          Default: ${OUT_DIR}
  --wait-seconds N          Default: ${WAIT_SECONDS}
  -h, --help                Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --target-service) TARGET_SERVICE="$2"; shift 2 ;;
    --source-container) SOURCE_CONTAINER="$2"; shift 2 ;;
    --target-container) TARGET_CONTAINER="$2"; shift 2 ;;
    --table-list) TABLE_LIST_FILE="$2"; shift 2 ;;
    --output-dir) OUT_DIR="$2"; shift 2 ;;
    --wait-seconds) WAIT_SECONDS="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown arg: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
mkdir -p "${SCHEMA_DIR}"

compose() {
  docker compose -p "${PROJECT}" -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" "$@"
}

cql_exec_service() {
  local service="$1"
  local cql="$2"
  compose exec -T "${service}" sh -lc '
    CQLSH_BIN="$(command -v cqlsh || true)";
    if [ -z "$CQLSH_BIN" ] && [ -x /opt/cassandra/bin/cqlsh ]; then CQLSH_BIN=/opt/cassandra/bin/cqlsh; fi;
    if [ -z "$CQLSH_BIN" ]; then echo "cqlsh_not_found" >&2; exit 127; fi;
    "$CQLSH_BIN" -e "$1"
  ' -- "${cql}"
}

cql_source() { phase7_cqlsh_exec "${SOURCE_CONTAINER}" "$1"; }
cql_target() { cql_exec_service "${TARGET_SERVICE}" "$1"; }

wait_for_cql_target() {
  local deadline=$(( $(date +%s) + WAIT_SECONDS ))
  while (( $(date +%s) < deadline )); do
    if cql_exec_service "${TARGET_SERVICE}" "SHOW VERSION;" >/dev/null 2>&1; then
      return 0
    fi
    sleep 3
  done
  return 1
}

wait_for_cql_source() {
  local deadline=$(( $(date +%s) + WAIT_SECONDS ))
  while (( $(date +%s) < deadline )); do
    if phase7_cqlsh_exec "${SOURCE_CONTAINER}" "SHOW VERSION;" >/dev/null 2>&1; then
      return 0
    fi
    sleep 3
  done
  return 1
}

echo "Starting target service ${TARGET_SERVICE}..."
compose up -d "${TARGET_SERVICE}" >/dev/null

if ! wait_for_cql_source; then
  echo "Source Cassandra not ready: ${SOURCE_CONTAINER}" >&2
  exit 2
fi
if ! wait_for_cql_target; then
  echo "Target Cassandra not ready: ${TARGET_SERVICE}" >&2
  exit 2
fi

keyspaces=()
while IFS= read -r ks; do
  [[ -n "${ks}" ]] && keyspaces+=("${ks}")
done < <(awk -F. '/^[[:space:]]*#/ {next} /^[[:space:]]*$/ {next} {print $1}' "${TABLE_LIST_FILE}" | sort -u)

schema_import_status=()
schema_import_fail=0
sanitize_schema_for_cassandra4() {
  local in_file="$1"
  local out_file="$2"
  # Minimal first-pass compatibility translation for legacy 2.1 schema dumps:
  # remove table options dropped in newer Cassandra versions.
  sed \
    -e '/^[[:space:]]*CREATE KEYSPACE[[:space:]][[:space:]]*/d' \
    -e '/^[[:space:]]*AND[[:space:]]*dclocal_read_repair_chance[[:space:]]*=/d' \
    -e '/^[[:space:]]*AND[[:space:]]*read_repair_chance[[:space:]]*=/d' \
    -e "s/^[[:space:]]*AND[[:space:]]*caching[[:space:]]*=[[:space:]]*'ALL'/    AND caching = {'keys': 'ALL', 'rows_per_partition': 'ALL'}/" \
    -e "s/^[[:space:]]*AND[[:space:]]*caching[[:space:]]*=[[:space:]]*'KEYS_ONLY'/    AND caching = {'keys': 'ALL', 'rows_per_partition': 'NONE'}/" \
    -e "s/^[[:space:]]*AND[[:space:]]*caching[[:space:]]*=[[:space:]]*'ROWS_ONLY'/    AND caching = {'keys': 'NONE', 'rows_per_partition': 'ALL'}/" \
    -e "s/^[[:space:]]*AND[[:space:]]*caching[[:space:]]*=[[:space:]]*'NONE'/    AND caching = {'keys': 'NONE', 'rows_per_partition': 'NONE'}/" \
    -e "s/^[[:space:]]*AND[[:space:]]*caching[[:space:]]*=[[:space:]]*'\\({.*}\\)'/    AND caching = \\1/" \
    -e "/^[[:space:]]*AND[[:space:]]*caching[[:space:]]*=/ s/\\\"/'/g" \
    "${in_file}" > "${out_file}"
}
for ks in "${keyspaces[@]}"; do
  out_ks="${SCHEMA_DIR}/schema-${ks}.cql"
  out_ks_sanitized="${SCHEMA_DIR}/schema-${ks}.c4.cql"
  import_log="${out_ks}.import.log"
  import_err="${out_ks}.import.err"
  if cql_source "DESCRIBE KEYSPACE ${ks};" > "${out_ks}" 2>"${out_ks}.err"; then
    sanitize_schema_for_cassandra4 "${out_ks}" "${out_ks_sanitized}"
    if cql_target "CREATE KEYSPACE IF NOT EXISTS ${ks} WITH replication = {'class':'SimpleStrategy','replication_factor':1};" >/dev/null 2>&1; then
      :
    fi
    if compose exec -T "${TARGET_SERVICE}" sh -lc '
      CQLSH_BIN="$(command -v cqlsh || true)";
      if [ -z "$CQLSH_BIN" ] && [ -x /opt/cassandra/bin/cqlsh ]; then CQLSH_BIN=/opt/cassandra/bin/cqlsh; fi;
      if [ -z "$CQLSH_BIN" ]; then echo "cqlsh_not_found" >&2; exit 127; fi;
      "$CQLSH_BIN"
    ' < "${out_ks_sanitized}" >"${import_log}" 2>"${import_err}"; then
      schema_import_status+=("${ks}:OK")
    else
      if [[ -s "${import_err}" ]] && ! grep -Evq '^[[:space:]]*<stdin>:[0-9]+:AlreadyExists: Keyspace .+ already exists[[:space:]]*$' "${import_err}"; then
        schema_import_status+=("${ks}:OK_ALREADY_EXISTS")
      else
        schema_import_status+=("${ks}:FAIL")
        schema_import_fail=1
      fi
    fi
  else
    schema_import_status+=("${ks}:EXPORT_FAIL")
    schema_import_fail=1
  fi
done

copy_rows=()
copy_fail=0
while IFS= read -r table; do
  [[ -z "${table}" || "${table}" =~ ^# ]] && continue
  ks="${table%%.*}"
  tb="${table##*.}"
  csv="${SCHEMA_DIR}/${ks}.${tb}.csv"
  csv_import="${SCHEMA_DIR}/${ks}.${tb}.import.csv"
  src_err="${SCHEMA_DIR}/${ks}.${tb}.copy_to.err"
  dst_err="${SCHEMA_DIR}/${ks}.${tb}.copy_from.err"
  dst_log="${SCHEMA_DIR}/${ks}.${tb}.copy_from.log"
  if cql_source "COPY ${ks}.${tb} TO STDOUT;" > "${csv}" 2>"${src_err}"; then
    awk 'NF { print }' "${csv}" > "${csv_import}"
    if [[ ! -s "${csv_import}" ]]; then
      copy_rows+=("${table}:EMPTY")
      continue
    fi
    if cql_target "COPY ${ks}.${tb} FROM STDIN;" < "${csv_import}" >"${dst_log}" 2>"${dst_err}"; then
      rows="$(wc -l < "${csv_import}" | tr -d ' ')"
      copy_rows+=("${table}:OK:${rows}")
    else
      copy_rows+=("${table}:IMPORT_FAIL")
      copy_fail=1
    fi
  else
    copy_rows+=("${table}:EXPORT_FAIL")
    copy_fail=1
  fi
done < "${TABLE_LIST_FILE}"

# Capture quick preflight/version snapshots using existing phase7 scripts (container-aware, helper fallback supported)
set +e
preflight_out="$("${ROOT}/gs-server/deploy/scripts/phase7-cassandra-preflight.sh" --container "${TARGET_CONTAINER}" --output-dir "${OUT_DIR}" 2>&1)"
preflight_code=$?
schema_out="$("${ROOT}/gs-server/deploy/scripts/phase7-cassandra-schema-export.sh" --container "${TARGET_CONTAINER}" --output-dir "${OUT_DIR}" 2>&1)"
schema_code=$?
counts_out="$("${ROOT}/gs-server/deploy/scripts/phase7-cassandra-table-counts.sh" --container "${TARGET_CONTAINER}" --table-list "${TABLE_LIST_FILE}" --output-dir "${OUT_DIR}" 2>&1)"
counts_code=$?
query_out="$("${ROOT}/gs-server/deploy/scripts/phase7-cassandra-query-smoke.sh" --container "${TARGET_CONTAINER}" --table-list "${TABLE_LIST_FILE}" --output-dir "${OUT_DIR}" 2>&1)"
query_code=$?
set -e

legacy_ver="$(cql_source "SELECT release_version, cluster_name FROM system.local;" | tail -n +4 | head -n 1 | tr -s ' ')"
target_ver="$(cql_target "SELECT release_version, cluster_name FROM system.local;" | tail -n +4 | head -n 1 | tr -s ' ')"

{
  echo "# Phase 7 Cassandra Target Bootstrap And Critical Copy"
  echo
  echo "- Timestamp (UTC): $(date -u '+%Y-%m-%dT%H:%M:%SZ')"
  echo "- Source container: ${SOURCE_CONTAINER}"
  echo "- Target service/container: ${TARGET_SERVICE} / ${TARGET_CONTAINER}"
  echo "- Legacy version snapshot: \`${legacy_ver}\`"
  echo "- Target version snapshot: \`${target_ver}\`"
  echo "- Target image intent: separate upgrade rehearsal target (keep existing c1 unchanged)"
  echo
  echo "## Keyspace Schema Import"
  for row in "${schema_import_status[@]}"; do
    echo "- ${row}"
  done
  echo
  echo "## Critical Table Data Copy"
  for row in "${copy_rows[@]}"; do
    echo "- ${row}"
  done
  echo
  echo "## Target Validation Script Results"
  echo "- preflight code=${preflight_code} :: ${preflight_out##*$'\\n'}"
  echo "- schema-export code=${schema_code} :: ${schema_out##*$'\\n'}"
  echo "- table-counts code=${counts_code} :: ${counts_out##*$'\\n'}"
  echo "- query-smoke code=${query_code} :: ${query_out##*$'\\n'}"
  echo
  if [[ ${schema_import_fail} -eq 0 && ${copy_fail} -eq 0 ]]; then
    echo "- Result: PARTIAL_SUCCESS_CRITICAL_TABLE_COPY_COMPLETE"
  else
    echo "- Result: NO_GO_PARTIAL_FAILURE_REVIEW_ERRORS"
  fi
  echo "- Artifact dir: \`${SCHEMA_DIR}\`"
} > "${REPORT}"

echo "report=${REPORT}"
echo "artifact_dir=${SCHEMA_DIR}"
