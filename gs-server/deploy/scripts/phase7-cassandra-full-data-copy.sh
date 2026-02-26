#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
OUT_DIR="${ROOT}/docs/phase7/cassandra/full-copy"
SOURCE_CONTAINER="gp3-c1-1"
TARGET_CONTAINER="refactor-c1-refactor-1"
KEYSPACES_CSV="rcasinoscks,rcasinoks"
TRUNCATE_TARGET="false"
WAIT_SECONDS="10"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Copy all tables from legacy Cassandra container into refactor Cassandra target
using cqlsh COPY TO/FROM (table-by-table), with optional target truncate.

Options:
  --source-container NAME   Default: ${SOURCE_CONTAINER}
  --target-container NAME   Default: ${TARGET_CONTAINER}
  --keyspaces CSV           Default: ${KEYSPACES_CSV}
  --truncate-target B       true|false (default: ${TRUNCATE_TARGET})
  --wait-seconds N          Sleep between export/import retries (default: ${WAIT_SECONDS})
  --out-dir DIR             Default: ${OUT_DIR}
  -h, --help                Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --source-container) SOURCE_CONTAINER="$2"; shift 2 ;;
    --target-container) TARGET_CONTAINER="$2"; shift 2 ;;
    --keyspaces) KEYSPACES_CSV="$2"; shift 2 ;;
    --truncate-target) TRUNCATE_TARGET="$2"; shift 2 ;;
    --wait-seconds) WAIT_SECONDS="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown arg: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
TS="$(date -u +%Y%m%d-%H%M%S)"
RUN_DIR="${OUT_DIR}/run-${TS}"
CSV_DIR="${RUN_DIR}/csv"
LOG_DIR="${RUN_DIR}/logs"
mkdir -p "${CSV_DIR}" "${LOG_DIR}"
REPORT="${OUT_DIR}/phase7-cassandra-full-data-copy-${TS}.md"
STATUS_TSV="${RUN_DIR}/status.tsv"
TABLES_FILE="${RUN_DIR}/tables.txt"

cqlsh_bin_exec() {
  local container="$1"
  shift
  docker exec "${container}" sh -lc '
    CQLSH_BIN="$(command -v cqlsh || true)"
    if [ -z "$CQLSH_BIN" ] && [ -x /opt/cassandra/bin/cqlsh ]; then
      CQLSH_BIN=/opt/cassandra/bin/cqlsh
    fi
    if [ -z "$CQLSH_BIN" ]; then
      echo "cqlsh_not_found" >&2
      exit 127
    fi
    exec "$CQLSH_BIN" "$@"
  ' sh "$@"
}

cql_query() {
  local container="$1"
  local query="$2"
  cqlsh_bin_exec "${container}" -e "${query}"
}

discover_tables_for_keyspace() {
  local ks="$1"
  cql_query "${SOURCE_CONTAINER}" "DESCRIBE KEYSPACE ${ks}" \
    | awk -v ks="${ks}" '
        /^CREATE TABLE / {
          # CREATE TABLE keyspace.table (
          split($3, a, ".")
          gsub(/[[:space:]]+$/, "", a[2])
          print ks "." a[2]
        }
      ' \
    | sed 's/[[:space:]]*$//' \
    | sed '/^[[:space:]]*$/d'
}

IFS=',' read -r -a keyspaces <<< "${KEYSPACES_CSV}"
> "${TABLES_FILE}"
for ks in "${keyspaces[@]}"; do
  ks="$(echo "${ks}" | xargs)"
  [[ -z "${ks}" ]] && continue
  discover_tables_for_keyspace "${ks}" >> "${TABLES_FILE}"
done
sort -u -o "${TABLES_FILE}" "${TABLES_FILE}"

echo -e "table\tstage\tstatus\tdetail" > "${STATUS_TSV}"

record_status() {
  printf '%s\t%s\t%s\t%s\n' "$1" "$2" "$3" "$4" >> "${STATUS_TSV}"
}

truncate_failed=0
if [[ "${TRUNCATE_TARGET}" == "true" ]]; then
  while IFS= read -r table; do
    [[ -z "${table}" ]] && continue
    if cql_query "${TARGET_CONTAINER}" "TRUNCATE ${table};" >"${LOG_DIR}/${table//./_}.truncate.log" 2>"${LOG_DIR}/${table//./_}.truncate.err"; then
      record_status "${table}" "truncate" "OK" ""
    else
      record_status "${table}" "truncate" "FAIL" "$(tail -n 1 "${LOG_DIR}/${table//./_}.truncate.err" 2>/dev/null | tr '\t' ' ')"
      truncate_failed=$((truncate_failed + 1))
    fi
  done < "${TABLES_FILE}"
fi

export_fail=0
import_fail=0
empty_tables=0
ok_tables=0
total_rows=0

while IFS= read -r table; do
  [[ -z "${table}" ]] && continue
  csv_file="${CSV_DIR}/${table//./_}.csv"
  csv_clean="${CSV_DIR}/${table//./_}.clean.csv"
  exp_log="${LOG_DIR}/${table//./_}.export.log"
  exp_err="${LOG_DIR}/${table//./_}.export.err"
  imp_log="${LOG_DIR}/${table//./_}.import.log"
  imp_err="${LOG_DIR}/${table//./_}.import.err"

  if cql_query "${SOURCE_CONTAINER}" "COPY ${table} TO STDOUT;" > "${csv_file}" 2> "${exp_err}"; then
    record_status "${table}" "export" "OK" ""
  else
    record_status "${table}" "export" "FAIL" "$(tail -n 1 "${exp_err}" 2>/dev/null | tr '\t' ' ')"
    export_fail=$((export_fail + 1))
    continue
  fi

  awk 'NF { print }' "${csv_file}" > "${csv_clean}"
  rows="$(wc -l < "${csv_clean}" | tr -d ' ')"
  if [[ "${rows}" == "0" ]]; then
    empty_tables=$((empty_tables + 1))
    record_status "${table}" "import" "EMPTY" "0"
    continue
  fi

  target_csv="/tmp/${table//./_}.csv"
  if docker cp "${csv_clean}" "${TARGET_CONTAINER}:${target_csv}" > /dev/null 2>&1 \
    && cql_query "${TARGET_CONTAINER}" "COPY ${table} FROM '${target_csv}';" > "${imp_log}" 2> "${imp_err}"; then
    ok_tables=$((ok_tables + 1))
    total_rows=$((total_rows + rows))
    record_status "${table}" "import" "OK" "${rows}"
  else
    if grep -q "Batch too large" "${imp_err}" 2>/dev/null \
      && cql_query "${TARGET_CONTAINER}" "TRUNCATE ${table};" >> "${imp_log}" 2>> "${imp_err}" \
      && cql_query "${TARGET_CONTAINER}" "COPY ${table} FROM '${target_csv}' WITH MINBATCHSIZE='1' AND MAXBATCHSIZE='1';" >> "${imp_log}" 2>> "${imp_err}"; then
      ok_tables=$((ok_tables + 1))
      total_rows=$((total_rows + rows))
      record_status "${table}" "import" "OK_RETRY_BATCH1" "${rows}"
    else
      import_fail=$((import_fail + 1))
      record_status "${table}" "import" "FAIL" "$(tail -n 1 "${imp_err}" 2>/dev/null | tr '\t' ' ')"
      sleep "${WAIT_SECONDS}"
    fi
  fi
  docker exec "${TARGET_CONTAINER}" sh -lc "rm -f '${target_csv}'" > /dev/null 2>&1 || true
done < "${TABLES_FILE}"

total_tables="$(wc -l < "${TABLES_FILE}" | tr -d ' ')"
imported_tables="$(awk -F'\t' '$2=="import" && $3=="OK"{c++} END{print c+0}' "${STATUS_TSV}")"
failed_tables="$(awk -F'\t' '$3=="FAIL"{c++} END{print c+0}' "${STATUS_TSV}")"
empty_imports="$(awk -F'\t' '$2=="import" && $3=="EMPTY"{c++} END{print c+0}' "${STATUS_TSV}")"

{
  echo "# Phase 7 Cassandra Full Data Copy"
  echo
  echo "- Timestamp (UTC): $(date -u '+%Y-%m-%dT%H:%M:%SZ')"
  echo "- Source container: ${SOURCE_CONTAINER}"
  echo "- Target container: ${TARGET_CONTAINER}"
  echo "- Keyspaces: ${KEYSPACES_CSV}"
  echo "- truncateTarget: ${TRUNCATE_TARGET}"
  echo "- totalTables: ${total_tables}"
  echo "- importedTablesOk: ${imported_tables}"
  echo "- emptyTables: ${empty_imports}"
  echo "- failedStages: ${failed_tables}"
  echo "- totalImportedCsvRows: ${total_rows}"
  echo "- runDir: ${RUN_DIR}"
  echo "- statusTsv: ${STATUS_TSV}"
  echo
  echo "## Failures"
  awk -F'\t' 'NR>1 && $3=="FAIL"{printf("- %s [%s] %s\n",$1,$2,$4)}' "${STATUS_TSV}"
  if [[ "${failed_tables}" == "0" ]]; then
    echo "- none"
  fi
} > "${REPORT}"

echo "report=${REPORT}"
echo "status_tsv=${STATUS_TSV}"
echo "run_dir=${RUN_DIR}"
echo "summary=total_tables:${total_tables},imported_ok:${imported_tables},empty:${empty_imports},failed:${failed_tables},rows:${total_rows}"
