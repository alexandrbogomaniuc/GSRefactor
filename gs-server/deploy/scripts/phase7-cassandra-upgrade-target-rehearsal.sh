#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
OUT_DIR="${ROOT}/docs/phase7/cassandra"
TABLE_LIST_FILE="${OUT_DIR}/critical-tables.txt"
SOURCE_CONTAINER="gp3-c1-1"
TARGET_CONTAINER="refactor-c1-refactor-1"
WAIT_SECONDS=180
DRY_RUN="false"
TS="$(date -u '+%Y%m%d-%H%M%S')"
REPORT="${OUT_DIR}/phase7-cassandra-upgrade-target-rehearsal-${TS}.md"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --source-container NAME  Default: ${SOURCE_CONTAINER}
  --target-container NAME  Default: ${TARGET_CONTAINER}
  --table-list FILE        Default: ${TABLE_LIST_FILE}
  --output-dir DIR         Default: ${OUT_DIR}
  --wait-seconds N         Default: ${WAIT_SECONDS}
  --dry-run true|false     Default: ${DRY_RUN}
  -h, --help               Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --source-container) SOURCE_CONTAINER="$2"; shift 2 ;;
    --target-container) TARGET_CONTAINER="$2"; shift 2 ;;
    --table-list) TABLE_LIST_FILE="$2"; shift 2 ;;
    --output-dir) OUT_DIR="$2"; shift 2 ;;
    --wait-seconds) WAIT_SECONDS="$2"; shift 2 ;;
    --dry-run) DRY_RUN="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown arg: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"

run() {
  if [[ "${DRY_RUN}" == "true" ]]; then
    echo "DRY_RUN: $*"
    return 0
  fi
  "$@"
}

capture_line_value() {
  local line="$1"
  local key="$2"
  printf '%s\n' "${line#${key}=}"
}

{
  echo "# Phase 7 Cassandra Upgrade Target Rehearsal"
  echo
  echo "- Timestamp (UTC): $(date -u '+%Y-%m-%dT%H:%M:%SZ')"
  echo "- Source container: ${SOURCE_CONTAINER}"
  echo "- Target container: ${TARGET_CONTAINER}"
  echo "- Dry run: ${DRY_RUN}"
  echo
  echo "## Plan"
  echo "1. Bootstrap target DB and copy keyspace schema + critical tables."
  echo "2. Run target preflight/schema/count/query evidence scripts."
  echo "3. Export source and target schema and generate schema diff."
  echo "4. Generate Phase 7 rehearsal report from latest manifest (target side)."
  echo
} > "${REPORT}"

bootstrap_cmd=(
  "${ROOT}/gs-server/deploy/scripts/phase7-cassandra-target-bootstrap-and-critical-copy.sh"
  --source-container "${SOURCE_CONTAINER}"
  --target-container "${TARGET_CONTAINER}"
  --table-list "${TABLE_LIST_FILE}"
  --output-dir "${OUT_DIR}"
  --wait-seconds "${WAIT_SECONDS}"
)

echo "Running bootstrap/copy step..." >> "${REPORT}"
set +e
bootstrap_output="$(run "${bootstrap_cmd[@]}" 2>&1)"
bootstrap_code=$?
set -e
printf '```text\n%s\n```\n\n' "${bootstrap_output}" >> "${REPORT}"

if [[ "${DRY_RUN}" == "true" ]]; then
  {
    echo "- Result: DRY_RUN_READY"
    echo "- Next command (live): \`${bootstrap_cmd[*]}\`"
  } >> "${REPORT}"
  echo "report=${REPORT}"
  exit 0
fi

if [[ ${bootstrap_code} -ne 0 ]]; then
  {
    echo "- Result: NO_GO_BOOTSTRAP_COPY_FAILED"
    echo "- bootstrap_exit_code: ${bootstrap_code}"
  } >> "${REPORT}"
  echo "report=${REPORT}"
  exit ${bootstrap_code}
fi

manifest_out="$("${ROOT}/gs-server/deploy/scripts/phase7-cassandra-evidence-pack.sh" --container "${TARGET_CONTAINER}" --table-list "${TABLE_LIST_FILE}" --output-dir "${OUT_DIR}" 2>&1 || true)"
latest_manifest="$(printf '%s\n' "${manifest_out}" | awk -F= '/^manifest=/{print $2}' | tail -n 1)"
if [[ -z "${latest_manifest}" || ! -f "${latest_manifest}" ]]; then
  latest_manifest="$(ls -1t "${OUT_DIR}"/phase7-cassandra-evidence-pack-*.manifest.txt 2>/dev/null | head -n 1 || true)"
fi
source_schema="$("${ROOT}/gs-server/deploy/scripts/phase7-cassandra-schema-export.sh" --container "${SOURCE_CONTAINER}" --output-dir "${OUT_DIR}" | awk -F= '/^schema_export=/{print $2}' | tail -n 1)"
target_schema="$("${ROOT}/gs-server/deploy/scripts/phase7-cassandra-schema-export.sh" --container "${TARGET_CONTAINER}" --output-dir "${OUT_DIR}" | awk -F= '/^schema_export=/{print $2}' | tail -n 1)"
if [[ -z "${source_schema}" || -z "${target_schema}" || ! -f "${source_schema}" || ! -f "${target_schema}" ]]; then
  {
    echo "- Result: NO_GO_SCHEMA_EXPORT_FAILED_POST_BOOTSTRAP"
    echo "- source_schema: ${source_schema:-<empty>}"
    echo "- target_schema: ${target_schema:-<empty>}"
  } >> "${REPORT}"
  echo "report=${REPORT}"
  exit 4
fi

schema_diff_out="$("${ROOT}/gs-server/deploy/scripts/phase7-cassandra-schema-diff.sh" --source "${source_schema}" --target "${target_schema}" --output-dir "${OUT_DIR}" 2>&1 || true)"

rehearsal_out=""
if [[ -n "${latest_manifest}" ]]; then
  rehearsal_out="$("${ROOT}/gs-server/deploy/scripts/phase7-cassandra-rehearsal-report-generate.sh" --manifest "${latest_manifest}" 2>&1 || true)"
fi

{
  echo "## Schema Compare"
  echo "- Source schema: \`${source_schema}\`"
  echo "- Target schema: \`${target_schema}\`"
  printf '```text\n%s\n```\n\n' "${schema_diff_out}"
  echo "## Rehearsal Report Generator"
  if [[ -n "${latest_manifest}" ]]; then
    echo "- Manifest: \`${latest_manifest}\`"
    if [[ -n "${manifest_out}" ]]; then
      printf '```text\n%s\n```\n\n' "${manifest_out}"
    fi
    printf '```text\n%s\n```\n\n' "${rehearsal_out}"
  else
    echo "- Manifest: none found"
  fi
  echo "- Result: REVIEW_GENERATED_ARTIFACTS"
} >> "${REPORT}"

echo "report=${REPORT}"
