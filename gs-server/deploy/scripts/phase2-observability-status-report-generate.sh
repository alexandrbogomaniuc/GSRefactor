#!/usr/bin/env bash
set -euo pipefail

TRACE_DOC="/Users/alexb/Documents/Dev/Dev_new/docs/29-trace-correlation-standard-v1.md"
TAXONOMY_DOC="/Users/alexb/Documents/Dev/Dev_new/docs/27-error-taxonomy-v1.md"
CORR_PROBE_DOC="/Users/alexb/Documents/Dev/Dev_new/docs/phase2/correlation-probes/correlation-probe-20260220-104035.md"
RUNBOOK_DOC="/Users/alexb/Documents/Dev/Dev_new/docs/60-support-modernization-runbook-page-20260220-182600.md"
RUNBOOK_STATUS_DOC="/Users/alexb/Documents/Dev/Dev_new/docs/61-support-runbook-status-snapshot-20260220-183000.md"
DASHBOARD_DOC="/Users/alexb/Documents/Dev/Dev_new/docs/36-modernization-visual-dashboard.md"
VERIFY_REPORT=""
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase2/observability"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Generate an observability baseline status report (trace/correlation, error taxonomy,
dashboard/runbook visibility, and runtime correlation probe evidence).

Options:
  --trace-doc FILE           Default: ${TRACE_DOC}
  --taxonomy-doc FILE        Default: ${TAXONOMY_DOC}
  --correlation-probe FILE   Default: ${CORR_PROBE_DOC}
  --runbook-doc FILE         Default: ${RUNBOOK_DOC}
  --runbook-status-doc FILE  Default: ${RUNBOOK_STATUS_DOC}
  --dashboard-doc FILE       Default: ${DASHBOARD_DOC}
  --verify-report FILE       Default: latest local verification suite report
  --out-dir DIR              Default: ${OUT_DIR}
  -h, --help                 Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --trace-doc) TRACE_DOC="$2"; shift 2 ;;
    --taxonomy-doc) TAXONOMY_DOC="$2"; shift 2 ;;
    --correlation-probe) CORR_PROBE_DOC="$2"; shift 2 ;;
    --runbook-doc) RUNBOOK_DOC="$2"; shift 2 ;;
    --runbook-status-doc) RUNBOOK_STATUS_DOC="$2"; shift 2 ;;
    --dashboard-doc) DASHBOARD_DOC="$2"; shift 2 ;;
    --verify-report) VERIFY_REPORT="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
if [[ -z "${VERIFY_REPORT}" ]]; then
  VERIFY_REPORT="$(ls -1t /Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-*.md 2>/dev/null | head -n1 || true)"
fi

for f in "${TRACE_DOC}" "${TAXONOMY_DOC}" "${CORR_PROBE_DOC}" "${RUNBOOK_DOC}" "${RUNBOOK_STATUS_DOC}" "${DASHBOARD_DOC}" "${VERIFY_REPORT}"; do
  [[ -f "${f}" ]] || { echo "Missing input file: ${f}" >&2; exit 2; }
done

TS="$(date -u +%Y%m%d-%H%M%S)"
REPORT="${OUT_DIR}/phase2-observability-status-report-${TS}.md"

node - <<'NODE' "${TRACE_DOC}" "${TAXONOMY_DOC}" "${CORR_PROBE_DOC}" "${RUNBOOK_DOC}" "${RUNBOOK_STATUS_DOC}" "${DASHBOARD_DOC}" "${VERIFY_REPORT}" "${REPORT}"
const fs = require('fs');
const [traceFile, taxonomyFile, corrFile, runbookFile, runbookStatusFile, dashFile, verifyFile, outFile] = process.argv.slice(2);

function read(p){ return fs.readFileSync(p,'utf8'); }
function hasAll(src, arr){ return arr.every(s => src.includes(s)); }
function pick(re, src, d=''){ const m=src.match(re); return m?String(m[1]).trim():d; }

const trace = read(traceFile);
const taxonomy = read(taxonomyFile);
const corr = read(corrFile);
const runbook = read(runbookFile);
const runbookStatus = read(runbookStatusFile);
const dash = read(dashFile);
const verify = read(verifyFile);

const traceFields = ['traceId','sessionId','bankId','gameId','operationId','configVersion'];
const traceFieldStatus = hasAll(trace, traceFields.map(f => `\`${f}\``) ) ? 'PASS' : 'FAIL';
const alertThresholdStatus = hasAll(trace, ['Missing-field rate alert threshold', '>0.1% over 5 minutes', 'P1 incident']) ? 'PASS' : 'FAIL';
const transportMappingStatus = hasAll(trace, ['### HTTP', '### WebSocket', '### Kafka']) ? 'PASS' : 'FAIL';

const errorCats = ['validation','auth','state','dependency','rate_limit','internal'];
const errorTaxonomyStatus = hasAll(taxonomy, errorCats.map(c => `\`${c}\``)) && hasAll(taxonomy, ['Canonical Error Envelope','Initial Stable Codes']) ? 'PASS' : 'FAIL';

const corrEchoStatus = ['X-Trace-Id','X-Session-Id','X-Operation-Id','X-Config-Version'].every(h =>
  new RegExp(`\\|\\s*${h.replace(/[-/\\^$*+?.()|[\\]{}]/g, '\\$&')}\\s*\\|\\s*PASS\\s*\\|`).test(corr)
) ? 'PASS' : 'FAIL';

const runbookBaselineStatus = (runbook.includes('Phase 4 protocol adapter readiness + evidence-pack') &&
  runbook.includes('Phase 5 gameplay/Redis readiness + evidence-pack') &&
  runbookStatus.includes('runtime status snapshot panel')) ? 'PASS' : 'FAIL';

const dashboardBaselineStatus = hasAll(dash, ['Section-level and overall progress bars','Filter modes: all/open/done','Checkbox toggles are local/browser-side temporary overrides']) ? 'PASS' : 'FAIL';

const vr = {
  pass: Number(pick(/^- pass:\s*(\d+)$/m, verify, '0')),
  fail: Number(pick(/^- fail:\s*(\d+)$/m, verify, '0')),
  skip: Number(pick(/^- skip:\s*(\d+)$/m, verify, '0'))
};

const checks = [
  ['trace_correlation_standard', traceFieldStatus],
  ['trace_transport_mappings', transportMappingStatus],
  ['alerting_baseline_thresholds', alertThresholdStatus],
  ['error_taxonomy', errorTaxonomyStatus],
  ['runtime_correlation_probe', corrEchoStatus],
  ['operator_runbook_status_snapshot', runbookBaselineStatus],
  ['operator_dashboard_baseline', dashboardBaselineStatus]
];

const allPass = checks.every(([,s]) => s === 'PASS');
const overall = (allPass && vr.fail === 0) ? 'TESTED_BASELINE_COMPLETE' : (vr.fail > 0 ? 'NO_GO_VERIFICATION_FAILURE' : 'NO_GO_BASELINE_GAPS');
const decision = overall === 'TESTED_BASELINE_COMPLETE'
  ? 'Go (observability baseline deliverables and validation evidence complete)'
  : (overall === 'NO_GO_VERIFICATION_FAILURE'
      ? 'No-Go (verification suite failures present)'
      : 'No-Go (missing observability baseline artifacts/checks)');

const out = [];
out.push('# Phase 2 Observability Baseline Status Report');
out.push('');
out.push(`- Trace doc: ${traceFile}`);
out.push(`- Error taxonomy doc: ${taxonomyFile}`);
out.push(`- Correlation probe evidence: ${corrFile}`);
out.push(`- Runbook doc: ${runbookFile}`);
out.push(`- Runbook status doc: ${runbookStatusFile}`);
out.push(`- Dashboard doc: ${dashFile}`);
out.push(`- Verification suite: ${verifyFile}`);
out.push(`- verification pass/fail/skip: ${vr.pass}/${vr.fail}/${vr.skip}`);
out.push(`- overall_status: ${overall}`);
out.push(`- decision: ${decision}`);
out.push('');
out.push('## Checks');
out.push('');
out.push('| Check | Status |');
out.push('|---|---|');
for (const [k,s] of checks) out.push(`| ${k} | ${s} |`);
out.push('');
out.push('## Deliverable Mapping');
out.push('');
out.push('- Trace/correlation standard (`traceId`, `sessionId`, `bankId`, `gameId`, `operationId`, `configVersion`)');
out.push('- Dashboards and alerting baseline (operator runbook snapshot + progress dashboard docs; alert thresholds in trace standard)');
out.push('- Error taxonomy baseline');

fs.writeFileSync(outFile, out.join('\n') + '\n');
console.log(`report=${outFile}`);
console.log(`overall_status=${overall}`);
NODE
