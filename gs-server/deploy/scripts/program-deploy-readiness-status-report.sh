#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
CHECKLIST_JSON="${ROOT}/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json"
OUT_DIR="${ROOT}/docs/release-readiness"
VERIFY_REPORT=""
PHASE4_REPORT=""
PHASE56_REPORT=""
PHASE7_DOC=""
PHASE7_MISMATCH_TSV=""
SECURITY_REPORT=""
LEGACY_PARITY_REPORT=""
LEGACY_MIXED_REPORT=""

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Generate program deploy/cutover readiness status from latest phase closure/status reports.

Options:
  --checklist FILE        Default: ${CHECKLIST_JSON}
  --verify-report FILE    Default: latest local verification report
  --phase4-report FILE    Default: latest phase4 protocol status report
  --phase56-report FILE   Default: latest phase5-6 service extraction status report
  --phase7-doc FILE       Default: docs/134 phase7 no-go rehearsal closure
  --phase7-mismatch FILE  Default: latest phase7 full-copy count-mismatches TSV (if present)
  --security-report FILE  Default: latest security hardening status report
  --legacy-report FILE    Default: latest legacy parity status report
  --legacy-mixed FILE     Default: latest manual full-flow result, else manual result, else preflight report
  --out-dir DIR           Default: ${OUT_DIR}
  -h, --help              Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --checklist) CHECKLIST_JSON="$2"; shift 2 ;;
    --verify-report) VERIFY_REPORT="$2"; shift 2 ;;
    --phase4-report) PHASE4_REPORT="$2"; shift 2 ;;
    --phase56-report) PHASE56_REPORT="$2"; shift 2 ;;
    --phase7-doc) PHASE7_DOC="$2"; shift 2 ;;
    --phase7-mismatch) PHASE7_MISMATCH_TSV="$2"; shift 2 ;;
    --security-report) SECURITY_REPORT="$2"; shift 2 ;;
    --legacy-report) LEGACY_PARITY_REPORT="$2"; shift 2 ;;
    --legacy-mixed) LEGACY_MIXED_REPORT="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
[[ -n "${VERIFY_REPORT}" ]] || VERIFY_REPORT="$(ls -1t "${ROOT}"/docs/quality/local-verification/phase5-6-local-verification-*.md 2>/dev/null | head -n1 || true)"
[[ -n "${PHASE4_REPORT}" ]] || PHASE4_REPORT="$(ls -1t "${ROOT}"/docs/phase4/protocol/phase4-protocol-status-report-*.md 2>/dev/null | head -n1 || true)"
[[ -n "${PHASE56_REPORT}" ]] || PHASE56_REPORT="$(ls -1t "${ROOT}"/docs/phase5-6/phase5-6-service-extraction-status-report-*.md 2>/dev/null | head -n1 || true)"
[[ -n "${PHASE7_DOC}" ]] || PHASE7_DOC="${ROOT}/docs/134-phase7-cassandra-rehearsal-report-tested-no-go-and-phase-deliverable-closure-20260224-090000.md"
[[ -n "${PHASE7_MISMATCH_TSV}" ]] || PHASE7_MISMATCH_TSV="$(ls -1td "${ROOT}"/docs/phase7/cassandra/full-copy/run-* 2>/dev/null | head -n1 | xargs -I{} sh -c 'f="{}/count-mismatches-source-vs-target.tsv"; [ -f "$f" ] && printf "%s" "$f"' || true)"
[[ -n "${SECURITY_REPORT}" ]] || SECURITY_REPORT="$(ls -1t "${ROOT}"/docs/security/security-hardening-status-report-*.md 2>/dev/null | head -n1 || true)"
[[ -n "${LEGACY_PARITY_REPORT}" ]] || LEGACY_PARITY_REPORT="$(ls -1t "${ROOT}"/docs/phase0/parity-status/phase0-legacy-parity-status-report-*.md 2>/dev/null | head -n1 || true)"
[[ -n "${LEGACY_MIXED_REPORT}" ]] || LEGACY_MIXED_REPORT="$(ls -1t "${ROOT}"/docs/validation/legacy-mixed-topology/legacy-mixed-topology-manual-full-flow-*.md 2>/dev/null | head -n1 || true)"
[[ -n "${LEGACY_MIXED_REPORT}" ]] || LEGACY_MIXED_REPORT="$(ls -1t "${ROOT}"/docs/validation/legacy-mixed-topology/legacy-mixed-topology-manual-result-*.md 2>/dev/null | head -n1 || true)"
[[ -n "${LEGACY_MIXED_REPORT}" ]] || LEGACY_MIXED_REPORT="$(ls -1t "${ROOT}"/docs/validation/legacy-mixed-topology/legacy-mixed-topology-validation-*.md 2>/dev/null | head -n1 || true)"

for f in "${CHECKLIST_JSON}" "${VERIFY_REPORT}" "${PHASE4_REPORT}" "${PHASE56_REPORT}" "${PHASE7_DOC}" "${SECURITY_REPORT}" "${LEGACY_PARITY_REPORT}" "${LEGACY_MIXED_REPORT}"; do
  [[ -f "${f}" ]] || { echo "Missing input file: ${f}" >&2; exit 2; }
done
if [[ -n "${PHASE7_MISMATCH_TSV}" && ! -f "${PHASE7_MISMATCH_TSV}" ]]; then
  echo "Missing input file: ${PHASE7_MISMATCH_TSV}" >&2
  exit 2
fi

TS="$(date -u +%Y%m%d-%H%M%S)"
REPORT="${OUT_DIR}/program-deploy-readiness-status-${TS}.md"

node - <<'NODE' "${CHECKLIST_JSON}" "${VERIFY_REPORT}" "${PHASE4_REPORT}" "${PHASE56_REPORT}" "${PHASE7_DOC}" "${PHASE7_MISMATCH_TSV}" "${SECURITY_REPORT}" "${LEGACY_PARITY_REPORT}" "${LEGACY_MIXED_REPORT}" "${REPORT}"
const fs = require('fs');
const [checklistFile, verifyFile, phase4File, phase56File, phase7File, phase7MismatchFile, securityFile, legacyFile, legacyMixedFile, outFile] = process.argv.slice(2);
const read = p => fs.readFileSync(p, 'utf8');
const normalized = s => s.includes('\\n') ? s.replace(/\\n/g, '\n') : s;
const pick = (re, src, d='') => ((src.match(re)||[])[1] || d).toString().trim();

const checklist = JSON.parse(read(checklistFile));
let total=0, done=0, open=[];
for (const sec of checklist.sections || []) {
  for (const it of sec.items || []) {
    total++;
    if (it.status === 'done') done++;
    else open.push(`${sec.id}/${it.id}:${it.status}`);
  }
}

const verify = read(verifyFile);
const vr = { pass:Number(pick(/^- pass:\s*(\d+)$/m, verify,'0')), fail:Number(pick(/^- fail:\s*(\d+)$/m, verify,'0')), skip:Number(pick(/^- skip:\s*(\d+)$/m, verify,'0')) };

const phase4 = normalized(read(phase4File));
const phase56 = normalized(read(phase56File));
const phase7 = normalized(read(phase7File));
const phase7Mismatch = phase7MismatchFile ? read(phase7MismatchFile) : '';
const security = normalized(read(securityFile));
const legacy = normalized(read(legacyFile));
const legacyMixed = normalized(read(legacyMixedFile));

const statuses = {
  phase4: pick(/^- phase4_status:\s*(.+)$/m, phase4, pick(/^- overall_status:\s*(.+)$/m, phase4, 'UNKNOWN')),
  phase56: pick(/^- overall_status:\s*(.+)$/m, phase56, 'UNKNOWN'),
  security: pick(/^- overall_status:\s*(.+)$/m, security, 'UNKNOWN'),
  legacy: pick(/^- overall_status:\s*(.+)$/m, legacy, 'UNKNOWN'),
  legacyMixed: pick(/^- status:\s*(.+)$/m, legacyMixed, pick(/^- overall_status:\s*(.+)$/m, legacyMixed, 'UNKNOWN')),
  phase7NoGo: (() => {
    if (phase7MismatchFile) return phase7Mismatch.trim().length === 0 ? 'NO' : 'YES';
    return /No-Go/i.test(phase7) ? 'YES' : 'NO';
  })()
};

const blockers = [];
if (open.length) blockers.push({id:'checklist_not_complete', severity:'HIGH', note:`Open items: ${open.length}`});
if (vr.fail > 0) blockers.push({id:'verification_failures', severity:'HIGH', note:`verification fail=${vr.fail}`});
if (statuses.phase4 !== 'TESTED_GO_RUNTIME_PARITY_READY') blockers.push({id:'phase4_runtime_parity_go_missing', severity:'HIGH', note:statuses.phase4});
if (statuses.phase56 !== 'TESTED_GO_RUNTIME_READY') blockers.push({id:'phase5_6_runtime_go_missing', severity:'HIGH', note:statuses.phase56});
if (statuses.security !== 'TESTED_SECURITY_HARDENING_COMPLETE') blockers.push({id:'security_dependency_audit_pending', severity:'HIGH', note:statuses.security});
if (statuses.phase7NoGo === 'YES') blockers.push({id:'cassandra_data_parity_no_go', severity:'HIGH', note:'phase7 rehearsal no-go'});
if (statuses.legacy !== 'TESTED_GUARDED_LEGACY_PARITY_BASELINE_COMPLETE') blockers.push({id:'legacy_parity_baseline_incomplete', severity:'MEDIUM', note:statuses.legacy});
if (statuses.legacyMixed !== 'MANUAL_FULL_FLOW_PASS') {
  const note = statuses.legacyMixed === 'READY_FOR_MANUAL_FULL_FLOW_EXECUTION'
    ? 'preflight ready; manual mixed-topology full flow execution pending'
    : `mixed-topology status=${statuses.legacyMixed}`;
  blockers.push({id:'legacy_mp_client_live_validation_pending', severity:'HIGH', note});
}

const overall = blockers.length === 0 ? 'GO_FOR_DEPLOY_AND_CANARY' : 'NO_GO_CUTOVER_PENDING_VALIDATION';
const out = [];
out.push('# Program Deploy / Cutover Readiness Status');
out.push('');
out.push(`- Checklist completion: ${done}/${total}`);
out.push(`- Verification suite: ${verifyFile}`);
out.push(`- verification pass/fail/skip: ${vr.pass}/${vr.fail}/${vr.skip}`);
out.push(`- phase4_protocol_status: ${statuses.phase4}`);
out.push(`- phase5_6_extraction_status: ${statuses.phase56}`);
out.push(`- legacy_parity_status: ${statuses.legacy}`);
out.push(`- legacy_mixed_topology_status: ${statuses.legacyMixed}`);
out.push(`- security_hardening_status: ${statuses.security}`);
out.push(`- phase7_cassandra_rehearsal_no_go: ${statuses.phase7NoGo}`);
out.push(`- overall_status: ${overall}`);
out.push('');
out.push('## Cutover Blockers');
out.push('');
out.push('| Blocker | Severity | Note |');
out.push('|---|---|---|');
if (blockers.length === 0) {
  out.push('| none | - | No current blockers in aggregated evidence inputs |');
} else {
  for (const b of blockers) out.push(`| ${b.id} | ${b.severity} | ${b.note} |`);
}
out.push('');
out.push('## Next Mandatory Actions');
out.push('');
if (blockers.length === 0) {
  out.push('1. Proceed with controlled deploy/canary approval (change window, rollback owner, monitoring watch).');
  out.push('2. Capture a fresh operator sign-off record referencing this report and the latest phase evidence docs.');
  out.push('3. If promoting beyond canary, repeat the runtime evidence packs after deploy to the target environment.');
} else {
  out.push('1. Resolve the listed blockers and regenerate this readiness report.');
  out.push('2. Re-run any affected runtime evidence packs after each blocker fix.');
  out.push('3. Capture updated operator approval evidence before cutover decision.');
}

fs.writeFileSync(outFile, out.join('\n') + '\n');
console.log(`report=${outFile}`);
console.log(`overall_status=${overall}`);
console.log(`blocker_count=${blockers.length}`);
NODE
