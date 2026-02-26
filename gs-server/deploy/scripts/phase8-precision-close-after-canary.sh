#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
POLICY_FILE="${ROOT}/gs-server/deploy/config/phase8-precision-policy.json"
CHECKLIST_FILE="${ROOT}/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json"
EVIDENCE_DIR="${ROOT}/docs/phase8/precision"
EVIDENCE_REPORT=""
MATRIX_SCRIPT="${ROOT}/gs-server/deploy/scripts/phase8-precision-verification-matrix.sh"
MATRIX_OUT_DIR="${ROOT}/docs/phase8/precision"
POLICY_SYNC_SCRIPT="${ROOT}/gs-server/deploy/scripts/sync-phase8-precision-policy.sh"
DASHBOARD_SYNC_SCRIPT="${ROOT}/gs-server/deploy/scripts/sync-modernization-dashboard-embedded-data.sh"
SYNC_POLICY_COPY="true"
SYNC_DASHBOARD="true"
UPDATE_CHECKLIST="true"
DOCS_DIR="${ROOT}/docs"
DOC_TS=""
DOC_NUMBER="auto"
DOC_SLUG="phase8-precision-runtime-canary-phase-closure"
REQUIRE_STATUS="READY"
MIN_DUAL_CALC_LOG_LINES="1"
DRY_RUN="false"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Closes Phase 8 after a successful non-prod runtime precision canary by:
  1) validating runtime evidence (status=READY, dual-calc logs > threshold),
  2) clearing the nonprod_canary_runtime policy blocker,
  3) regenerating the Phase 8 precision matrix and requiring phase8ReadyToClose=yes,
  4) marking checklist item pu-precision-audit as done,
  5) syncing policy copy + dashboard embedded progress (optional).

Options:
  --policy-file PATH             Default: ${POLICY_FILE}
  --checklist-file PATH          Default: ${CHECKLIST_FILE}
  --evidence-dir DIR             Default: ${EVIDENCE_DIR}
  --evidence-report PATH         Use specific canary evidence report (default: latest in evidence-dir)
  --matrix-script PATH           Default: ${MATRIX_SCRIPT}
  --matrix-out-dir DIR           Default: ${MATRIX_OUT_DIR}
  --policy-sync-script PATH      Default: ${POLICY_SYNC_SCRIPT}
  --dashboard-sync-script PATH   Default: ${DASHBOARD_SYNC_SCRIPT}
  --sync-policy-copy B           true|false (default: ${SYNC_POLICY_COPY})
  --sync-dashboard B             true|false (default: ${SYNC_DASHBOARD})
  --update-checklist B           true|false (default: ${UPDATE_CHECKLIST})
  --docs-dir DIR                 Default: ${DOCS_DIR}
  --doc-number N|auto            Default: ${DOC_NUMBER}
  --doc-ts TS                    UTC timestamp suffix (default: now)
  --doc-slug SLUG                Default: ${DOC_SLUG}
  --require-status STATUS        Default: ${REQUIRE_STATUS}
  --min-dual-calc-log-lines N    Default: ${MIN_DUAL_CALC_LOG_LINES}
  --dry-run B                    true|false (default: ${DRY_RUN})
  -h, --help                     Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --policy-file) POLICY_FILE="$2"; shift 2 ;;
    --checklist-file) CHECKLIST_FILE="$2"; shift 2 ;;
    --evidence-dir) EVIDENCE_DIR="$2"; shift 2 ;;
    --evidence-report) EVIDENCE_REPORT="$2"; shift 2 ;;
    --matrix-script) MATRIX_SCRIPT="$2"; shift 2 ;;
    --matrix-out-dir) MATRIX_OUT_DIR="$2"; shift 2 ;;
    --policy-sync-script) POLICY_SYNC_SCRIPT="$2"; shift 2 ;;
    --dashboard-sync-script) DASHBOARD_SYNC_SCRIPT="$2"; shift 2 ;;
    --sync-policy-copy) SYNC_POLICY_COPY="$(echo "$2" | tr '[:upper:]' '[:lower:]')"; shift 2 ;;
    --sync-dashboard) SYNC_DASHBOARD="$(echo "$2" | tr '[:upper:]' '[:lower:]')"; shift 2 ;;
    --update-checklist) UPDATE_CHECKLIST="$(echo "$2" | tr '[:upper:]' '[:lower:]')"; shift 2 ;;
    --docs-dir) DOCS_DIR="$2"; shift 2 ;;
    --doc-number) DOC_NUMBER="$2"; shift 2 ;;
    --doc-ts) DOC_TS="$2"; shift 2 ;;
    --doc-slug) DOC_SLUG="$2"; shift 2 ;;
    --require-status) REQUIRE_STATUS="$2"; shift 2 ;;
    --min-dual-calc-log-lines) MIN_DUAL_CALC_LOG_LINES="$2"; shift 2 ;;
    --dry-run) DRY_RUN="$(echo "$2" | tr '[:upper:]' '[:lower:]')"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

if [[ ! "${MIN_DUAL_CALC_LOG_LINES}" =~ ^[0-9]+$ ]]; then
  echo "Invalid --min-dual-calc-log-lines: ${MIN_DUAL_CALC_LOG_LINES}" >&2
  exit 1
fi

if [[ -z "${EVIDENCE_REPORT}" ]]; then
  EVIDENCE_REPORT="$(ls -1t "${EVIDENCE_DIR}"/phase8-precision-nonprod-canary-evidence-*.md 2>/dev/null | head -n1 || true)"
fi
if [[ -z "${EVIDENCE_REPORT}" || ! -f "${EVIDENCE_REPORT}" ]]; then
  echo "Missing canary evidence report (use --evidence-report)." >&2
  exit 2
fi

status_line="$(rg -n '^status=' "${EVIDENCE_REPORT}" | tail -n1 | sed -E 's/^[0-9]+:status=//')"
log_lines_line="$(rg -n '^precision_dual_calc_log_lines=' "${EVIDENCE_REPORT}" | tail -n1 | sed -E 's/^[0-9]+:precision_dual_calc_log_lines=//')"
readiness_report_line="$(rg -n '^matrix_report=' "${EVIDENCE_REPORT}" | tail -n1 | sed -E 's/^[0-9]+:matrix_report=//')"

if [[ "${status_line}" != "${REQUIRE_STATUS}" ]]; then
  echo "Runtime evidence status mismatch: expected ${REQUIRE_STATUS}, got ${status_line:-<missing>}" >&2
  exit 3
fi
if [[ ! "${log_lines_line}" =~ ^[0-9]+$ ]]; then
  echo "Missing or invalid precision_dual_calc_log_lines in evidence report" >&2
  exit 3
fi
if (( log_lines_line < MIN_DUAL_CALC_LOG_LINES )); then
  echo "Not enough precision dual-calc logs: got ${log_lines_line}, need >= ${MIN_DUAL_CALC_LOG_LINES}" >&2
  exit 3
fi

if [[ -z "${DOC_TS}" ]]; then
  DOC_TS="$(date -u +%Y%m%d-%H%M%S)"
fi

if [[ "${DOC_NUMBER}" == "auto" ]]; then
  next_doc_num="$(ls -1 "${DOCS_DIR}"/*.md 2>/dev/null | xargs -n1 basename 2>/dev/null | sed -E 's/^([0-9]+)-.*$/\1/' | rg '^[0-9]+$' | sort -n | tail -n1 | awk '{print $1 + 1}')"
  if [[ -z "${next_doc_num}" ]]; then
    next_doc_num="1"
  fi
else
  next_doc_num="${DOC_NUMBER}"
fi
DOC_REL="docs/${next_doc_num}-${DOC_SLUG}-${DOC_TS}.md"
DOC_FILE="${ROOT}/${DOC_REL}"
mkdir -p "$(dirname "${DOC_FILE}")"
mkdir -p "${MATRIX_OUT_DIR}"

if [[ "${DRY_RUN}" == "true" ]]; then
  echo "dry_run=true"
  echo "evidence_report=${EVIDENCE_REPORT}"
  echo "validated_status=${status_line}"
  echo "validated_precision_dual_calc_log_lines=${log_lines_line}"
  echo "closure_doc=${DOC_FILE}"
  echo "matrix_script=${MATRIX_SCRIPT}"
  echo "policy_file=${POLICY_FILE}"
  echo "checklist_file=${CHECKLIST_FILE}"
  exit 0
fi

node - "${POLICY_FILE}" "${DOC_REL}" <<'NODE'
const fs = require('fs');
const [policyFile, docRel] = process.argv.slice(2);
const p = JSON.parse(fs.readFileSync(policyFile, 'utf8'));
if (!Array.isArray(p.verificationCategories)) throw new Error('verificationCategories missing');
let found = false;
for (const v of p.verificationCategories) {
  if (v.category === 'nonprod_canary_runtime') {
    v.status = 'runtime_canary_executed_evidence_captured_cleared';
    v.blocking = false;
    v.evidence = docRel;
    found = true;
  }
}
if (!found) throw new Error('nonprod_canary_runtime category missing');
p.updatedAtUtc = new Date().toISOString();
fs.writeFileSync(policyFile, JSON.stringify(p, null, 2) + '\n');
NODE

if [[ "${SYNC_POLICY_COPY}" == "true" ]]; then
  "${POLICY_SYNC_SCRIPT}"
fi

matrix_line="$(${MATRIX_SCRIPT} --policy-file "${POLICY_FILE}" --out-dir "${MATRIX_OUT_DIR}")"
matrix_report="${matrix_line#report=}"
phase8_ready="$(rg -n '^- phase8ReadyToClose: ' "${matrix_report}" | sed -E 's/.*: (yes|no)$/\1/' | head -n1)"
blocking_count="$(rg -n '^- blockingCategories: ' "${matrix_report}" | sed -E 's/.*: ([0-9]+)/\1/' | head -n1)"
if [[ "${phase8_ready}" != "yes" ]]; then
  echo "Phase 8 matrix did not close: phase8ReadyToClose=${phase8_ready:-unknown}" >&2
  exit 4
fi
if [[ "${blocking_count:-}" != "0" ]]; then
  echo "Phase 8 matrix blocking count not zero: ${blocking_count:-unknown}" >&2
  exit 4
fi

if [[ "${UPDATE_CHECKLIST}" == "true" ]]; then
  node - "${CHECKLIST_FILE}" "${DOC_REL}" "$(date -u +%Y-%m-%d)" <<'NODE'
const fs = require('fs');
const [checklistFile, docRel, updatedAt] = process.argv.slice(2);
const c = JSON.parse(fs.readFileSync(checklistFile, 'utf8'));
if (!Array.isArray(c.sections)) throw new Error('sections missing');
let found = false;
for (const s of c.sections) {
  for (const item of (s.items || [])) {
    if (item.id === 'pu-precision-audit') {
      item.status = 'done';
      item.evidence = docRel;
      found = true;
    }
  }
}
if (!found) throw new Error('checklist item pu-precision-audit missing');
c.updatedAt = updatedAt;
fs.writeFileSync(checklistFile, JSON.stringify(c, null, 2) + '\n');
NODE
fi

{
  echo "# Phase 8 Precision Runtime Canary Closure (${DOC_TS} UTC)"
  echo
  echo "## Outcome"
  echo "Phase 8 precision/min-bet modernization is closed after successful non-prod runtime canary evidence validation and policy/matrix finalization."
  echo
  echo "## Runtime Evidence Validation"
  echo "- evidence_report: \`${EVIDENCE_REPORT}\`"
  echo "- readiness_status: \`${status_line}\`"
  echo "- precision_dual_calc_log_lines: \`${log_lines_line}\`"
  if [[ -n "${readiness_report_line}" ]]; then
    echo "- readiness_matrix_report_snapshot: \`${readiness_report_line}\`"
  fi
  echo
  echo "## Closure Actions Applied"
  echo "- Cleared policy blocker \`nonprod_canary_runtime\` in \`${POLICY_FILE}\`"
  echo "- Regenerated verification matrix and confirmed \`phase8ReadyToClose: yes\` / \`blockingCategories: 0\`"
  if [[ "${UPDATE_CHECKLIST}" == "true" ]]; then
    echo "- Marked checklist item \`pu-precision-audit\` as \`done\`"
  fi
  if [[ "${SYNC_POLICY_COPY}" == "true" ]]; then
    echo "- Synced GS classpath precision policy copy"
  fi
  if [[ "${SYNC_DASHBOARD}" == "true" ]]; then
    echo "- Synced embedded dashboard checklist snapshot for file:// mode"
  fi
  echo
  echo "## Generated Matrix"
  echo "- matrix_report: \`${matrix_report}\`"
  echo "- phase8ReadyToClose: \`${phase8_ready}\`"
  echo "- blockingCategories: \`${blocking_count}\`"
} > "${DOC_FILE}"

if [[ "${SYNC_DASHBOARD}" == "true" ]]; then
  "${DASHBOARD_SYNC_SCRIPT}"
fi

echo "closure_doc=${DOC_FILE}"
echo "matrix_report=${matrix_report}"
echo "phase8_ready_to_close=${phase8_ready}"
echo "blocking_categories=${blocking_count}"
echo "checklist_updated=${UPDATE_CHECKLIST}"
