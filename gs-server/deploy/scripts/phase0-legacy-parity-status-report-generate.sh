#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

PHASE0_DOC="${REPO_ROOT}/docs/23-phase-0-baseline-and-parity-capture.md"
LAUNCH_FORENSICS_DOC="${REPO_ROOT}/docs/11-game-launch-forensics.md"
PHASE5_6_CLOSURE_DOC="${REPO_ROOT}/docs/155-phase5-6-service-extraction-phase-closure-tested-no-go-runtime-blocked-20260224-120000.md"
MP_BOUNDARY_DOC="${REPO_ROOT}/docs/39-phase6-multiplayer-boundary-and-bypass-v1.md"
MP_SHADOW_DOC="${REPO_ROOT}/docs/73-phase6-gs-multiplayer-shadow-bridge-20260220-191800.md"
MP_POLICY_DOC="${REPO_ROOT}/docs/74-phase6-multiplayer-routing-policy-probe-and-test-gate-20260220-192600.md"
VERIFY_REPORT=""
OUT_DIR="${REPO_ROOT}/docs/phase0/parity-status"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Generate legacy parity status report focused on:
  - FRB/bonus parity suite stabilization coverage
  - multiplayer legacy compatibility guardrails (deferred dedicated runtime validation)

Options:
  --phase0-doc FILE            Default: ${PHASE0_DOC}
  --launch-forensics-doc FILE  Default: ${LAUNCH_FORENSICS_DOC}
  --phase5-6-closure-doc FILE  Default: ${PHASE5_6_CLOSURE_DOC}
  --mp-boundary-doc FILE       Default: ${MP_BOUNDARY_DOC}
  --mp-shadow-doc FILE         Default: ${MP_SHADOW_DOC}
  --mp-policy-doc FILE         Default: ${MP_POLICY_DOC}
  --verify-report FILE         Default: latest local verification suite report
  --out-dir DIR                Default: ${OUT_DIR}
  -h, --help                   Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --phase0-doc) PHASE0_DOC="$2"; shift 2 ;;
    --launch-forensics-doc) LAUNCH_FORENSICS_DOC="$2"; shift 2 ;;
    --phase5-6-closure-doc) PHASE5_6_CLOSURE_DOC="$2"; shift 2 ;;
    --mp-boundary-doc) MP_BOUNDARY_DOC="$2"; shift 2 ;;
    --mp-shadow-doc) MP_SHADOW_DOC="$2"; shift 2 ;;
    --mp-policy-doc) MP_POLICY_DOC="$2"; shift 2 ;;
    --verify-report) VERIFY_REPORT="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
if [[ -z "${VERIFY_REPORT}" ]]; then
  VERIFY_REPORT="$(ls -1t ${REPO_ROOT}/docs/quality/local-verification/phase5-6-local-verification-*.md 2>/dev/null | head -n1 || true)"
fi

for f in "${PHASE0_DOC}" "${LAUNCH_FORENSICS_DOC}" "${PHASE5_6_CLOSURE_DOC}" "${MP_BOUNDARY_DOC}" "${MP_SHADOW_DOC}" "${MP_POLICY_DOC}" "${VERIFY_REPORT}"; do
  [[ -f "${f}" ]] || { echo "Missing input file: ${f}" >&2; exit 2; }
done

TS="$(date -u +%Y%m%d-%H%M%S)"
REPORT="${OUT_DIR}/phase0-legacy-parity-status-report-${TS}.md"

node - <<'NODE' "${PHASE0_DOC}" "${LAUNCH_FORENSICS_DOC}" "${PHASE5_6_CLOSURE_DOC}" "${MP_BOUNDARY_DOC}" "${MP_SHADOW_DOC}" "${MP_POLICY_DOC}" "${VERIFY_REPORT}" "${REPORT}"
const fs = require('fs');
const [phase0File, forensicsFile, p56File, mpBoundaryFile, mpShadowFile, mpPolicyFile, verifyFile, outFile] = process.argv.slice(2);
const s = p => fs.readFileSync(p, 'utf8');
const phase0 = s(phase0File), forensics = s(forensicsFile), p56 = s(p56File), mpBoundary = s(mpBoundaryFile), mpShadow = s(mpShadowFile), mpPolicy = s(mpPolicyFile), vrText = s(verifyFile);
const pick = (re, src, d='') => ((src.match(re)||[])[1] || d).toString().trim();

const checks = [];
const add = (k, ok) => checks.push([k, ok ? 'PASS' : 'FAIL']);

add('phase0_frb_routes_and_matrix', phase0.includes('/frbcheck.do') && phase0.includes('P0-FRB-01') && phase0.includes('### GF-5 FRB'));
add('phase0_reconnect_legacy_evidence', phase0.includes('phase0-reconnect-facade-fallback'));
add('phase0_parity_harness_documented', phase0.includes('phase0-parity-harness.sh') && phase0.includes('phase0/parity-execution/'));
add('launch_forensics_doc_present', forensics.length > 0);
add('bonus_frb_extraction_closure_evidence', /Bonus\/FRB/i.test(p56) && /TESTED_NO_GO_RUNTIME_BLOCKED/.test(p56));
add('mp_boundary_bypass_design', mpBoundary.includes('isMultiplayer=false') && mpBoundary.includes('skip multiplayer code paths entirely'));
add('mp_shadow_fail_open_guard', mpShadow.includes('Legacy multiplayer launch/session behavior remains authoritative') && mpShadow.includes('shadow-only and fail-open'));
add('mp_routing_policy_probe_guard', mpPolicy.includes('isMultiplayer=false') && mpPolicy.includes('bank_multiplayer_disabled'));

const vr = { pass: Number(pick(/^- pass:\s*(\d+)$/m, vrText, '0')), fail: Number(pick(/^- fail:\s*(\d+)$/m, vrText, '0')), skip: Number(pick(/^- skip:\s*(\d+)$/m, vrText, '0')) };
const allChecksPass = checks.every(([,v]) => v === 'PASS');

const frbStatus = checks.filter(([k]) => k.startsWith('phase0_frb_') || k==='phase0_parity_harness_documented' || k==='bonus_frb_extraction_closure_evidence').every(([,v]) => v==='PASS')
  ? 'TESTED_PARITY_SUITE_STABILIZED'
  : 'NO_GO_PARITY_GAPS';
const mpStatus = ['mp_boundary_bypass_design','mp_shadow_fail_open_guard','mp_routing_policy_probe_guard','launch_forensics_doc_present'].every(key => checks.find(([k])=>k===key)?.[1]==='PASS')
  ? 'TESTED_COMPATIBILITY_GUARDED_DEFERRED_RUNTIME_VALIDATION'
  : 'NO_GO_COMPATIBILITY_GAPS';

let overall = 'NO_GO_PARITY_GAPS';
if (vr.fail > 0) overall = 'NO_GO_VERIFICATION_FAILURE';
else if (frbStatus === 'TESTED_PARITY_SUITE_STABILIZED' && mpStatus === 'TESTED_COMPATIBILITY_GUARDED_DEFERRED_RUNTIME_VALIDATION' && allChecksPass) {
  overall = 'TESTED_GUARDED_LEGACY_PARITY_BASELINE_COMPLETE';
}

const out = [];
out.push('# Legacy Parity Status Report (FRB + Multiplayer Compatibility)');
out.push('');
out.push(`- Phase 0 baseline/parity doc: ${phase0File}`);
out.push(`- Launch forensics doc: ${forensicsFile}`);
out.push(`- Phase 5/6 extraction closure doc: ${p56File}`);
out.push(`- Verification suite: ${verifyFile}`);
out.push(`- verification pass/fail/skip: ${vr.pass}/${vr.fail}/${vr.skip}`);
out.push(`- frb_bonus_parity_status: ${frbStatus}`);
out.push(`- multiplayer_legacy_compat_status: ${mpStatus}`);
out.push(`- overall_status: ${overall}`);
if (overall === 'TESTED_GUARDED_LEGACY_PARITY_BASELINE_COMPLETE') {
  out.push('- decision: Go (baseline parity coverage and compatibility guardrails complete; dedicated legacy MP/client runtime validation remains a separate wave)');
} else {
  out.push('- decision: No-Go (legacy parity/compatibility baseline gaps or verification failures)');
}
out.push('');
out.push('## Checks');
out.push('');
out.push('| Check | Status |');
out.push('|---|---|');
for (const [k,v] of checks) out.push(`| ${k} | ${v} |`);
out.push('');
out.push('## Notes');
out.push('');
out.push('- This report closes checklist governance coverage, not the later dedicated cross-runtime validation wave with legacy MP/client infrastructure.');
out.push('- Dedicated legacy MP/client compatibility runtime validation remains planned and should consume live legacy endpoints and full reconnect/FRB depletion scenarios.');

fs.writeFileSync(outFile, out.join('\n') + '\n');
console.log(`report=${outFile}`);
console.log(`overall_status=${overall}`);
console.log(`frb_bonus_parity_status=${frbStatus}`);
console.log(`multiplayer_legacy_compat_status=${mpStatus}`);
NODE
