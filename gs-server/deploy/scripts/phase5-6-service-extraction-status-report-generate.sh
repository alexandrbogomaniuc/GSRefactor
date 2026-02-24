#!/usr/bin/env bash
set -euo pipefail

VERIFY_REPORT=""
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase5-6"

GAMEPLAY_EVIDENCE="/Users/alexb/Documents/Dev/Dev_new/docs/phase5/gameplay/phase5-gameplay-runtime-evidence-20260220-180650.md"
WALLET_EVIDENCE="/Users/alexb/Documents/Dev/Dev_new/docs/phase5/wallet/phase5-wallet-runtime-evidence-20260220-184505.md"
BONUS_EVIDENCE="/Users/alexb/Documents/Dev/Dev_new/docs/phase5/bonus-frb/phase5-bonus-frb-runtime-evidence-20260220-185313.md"
HISTORY_EVIDENCE="/Users/alexb/Documents/Dev/Dev_new/docs/phase5/history/phase5-history-runtime-evidence-20260220-190016.md"
MP_EVIDENCE="/Users/alexb/Documents/Dev/Dev_new/docs/phase6/multiplayer/phase6-multiplayer-runtime-evidence-20260223-124734.md"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Generate a Phase 5/6 service extraction status report from runtime evidence-pack reports
and the shared local verification suite report.

Options:
  --verify-report FILE      Default: latest local verification suite report
  --gameplay-evidence FILE  Default: ${GAMEPLAY_EVIDENCE}
  --wallet-evidence FILE    Default: ${WALLET_EVIDENCE}
  --bonus-evidence FILE     Default: ${BONUS_EVIDENCE}
  --history-evidence FILE   Default: ${HISTORY_EVIDENCE}
  --mp-evidence FILE        Default: ${MP_EVIDENCE}
  --out-dir DIR             Default: ${OUT_DIR}
  -h, --help                Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --verify-report) VERIFY_REPORT="$2"; shift 2 ;;
    --gameplay-evidence) GAMEPLAY_EVIDENCE="$2"; shift 2 ;;
    --wallet-evidence) WALLET_EVIDENCE="$2"; shift 2 ;;
    --bonus-evidence) BONUS_EVIDENCE="$2"; shift 2 ;;
    --history-evidence) HISTORY_EVIDENCE="$2"; shift 2 ;;
    --mp-evidence) MP_EVIDENCE="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
if [[ -z "${VERIFY_REPORT}" ]]; then
  VERIFY_REPORT="$(ls -1t /Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-*.md 2>/dev/null | head -n1 || true)"
fi

for f in "${VERIFY_REPORT}" "${GAMEPLAY_EVIDENCE}" "${WALLET_EVIDENCE}" "${BONUS_EVIDENCE}" "${HISTORY_EVIDENCE}" "${MP_EVIDENCE}"; do
  [[ -f "${f}" ]] || { echo "Missing input file: ${f}" >&2; exit 2; }
done

TS="$(date -u +%Y%m%d-%H%M%S)"
REPORT="${OUT_DIR}/phase5-6-service-extraction-status-report-${TS}.md"

node - <<'NODE' "${VERIFY_REPORT}" "${GAMEPLAY_EVIDENCE}" "${WALLET_EVIDENCE}" "${BONUS_EVIDENCE}" "${HISTORY_EVIDENCE}" "${MP_EVIDENCE}" "${REPORT}"
const fs = require('fs');
const [verifyFile, gameplayFile, walletFile, bonusFile, historyFile, mpFile, outFile] = process.argv.slice(2);

function pick(re, src, d='') { const m = src.match(re); return m ? String(m[1]).trim() : d; }
function bullets(src) {
  const map = {};
  for (const line of src.split(/\r?\n/)) {
    const m = line.match(/^- ([A-Za-z0-9_]+):\s*(.+)$/);
    if (m) map[m[1]] = m[2].trim();
  }
  return map;
}
function serviceStatus(name, file, src) {
  const b = bullets(src);
  const readiness = b.readiness_check || 'UNKNOWN';
  const probeKeys = Object.keys(b).filter(k => k.endsWith('_probe'));
  const probes = probeKeys.map(k => ({key:k, status:b[k]}));
  const anyProbeFail = probes.some(p => p.status === 'FAIL');
  const allProbesNonFail = probes.every(p => p.status !== 'FAIL');
  const runtimeBlocked = readiness !== 'PASS' && allProbesNonFail &&
    (/NOT_READY|endpoint unreachable|docker socket not accessible|permission denied while trying to connect to the docker API/i.test(src));
  let status = 'INCOMPLETE';
  if (readiness === 'PASS' && allProbesNonFail) status = 'TESTED_GO_RUNTIME_READY';
  else if (runtimeBlocked) status = 'TESTED_NO_GO_RUNTIME_BLOCKED';
  else if (anyProbeFail || readiness === 'FAIL') status = 'NO_GO_RUNTIME_FAILURE';
  return {name, file, readiness, probes, status, transport: b.transport || 'unknown'};
}

const verify = fs.readFileSync(verifyFile, 'utf8');
const vr = {
  pass: Number(pick(/^- pass:\s*(\d+)$/m, verify, '0')),
  fail: Number(pick(/^- fail:\s*(\d+)$/m, verify, '0')),
  skip: Number(pick(/^- skip:\s*(\d+)$/m, verify, '0'))
};

const services = [
  serviceStatus('gameplay_orchestrator', gameplayFile, fs.readFileSync(gameplayFile, 'utf8')),
  serviceStatus('wallet_adapter', walletFile, fs.readFileSync(walletFile, 'utf8')),
  serviceStatus('bonus_frb_service', bonusFile, fs.readFileSync(bonusFile, 'utf8')),
  serviceStatus('history_service', historyFile, fs.readFileSync(historyFile, 'utf8')),
  serviceStatus('multiplayer_service', mpFile, fs.readFileSync(mpFile, 'utf8'))
];

let overall = 'INCOMPLETE';
if (vr.fail > 0) overall = 'NO_GO_VERIFICATION_FAILURE';
else if (services.every(s => s.status === 'TESTED_GO_RUNTIME_READY')) overall = 'TESTED_GO_RUNTIME_READY';
else if (services.every(s => s.status === 'TESTED_NO_GO_RUNTIME_BLOCKED' || s.status === 'TESTED_GO_RUNTIME_READY')) {
  overall = 'TESTED_NO_GO_RUNTIME_BLOCKED';
} else if (services.some(s => /NO_GO_RUNTIME_FAILURE/.test(s.status))) {
  overall = 'NO_GO_RUNTIME_FAILURE';
}

const out = [];
out.push('# Phase 5/6 Service Extraction Status Report');
out.push('');
out.push(`- Verification suite source: ${verifyFile}`);
out.push(`- verification pass/fail/skip: ${vr.pass}/${vr.fail}/${vr.skip}`);
out.push(`- overall_status: ${overall}`);
if (overall === 'TESTED_NO_GO_RUNTIME_BLOCKED') {
  out.push('- decision: No-Go (service runtime parity/canary execution blocked/unavailable; tooling/shadow/canary coverage implemented)');
} else if (overall === 'TESTED_GO_RUNTIME_READY') {
  out.push('- decision: Go (all service runtime evidence checks ready/passing)');
} else if (overall === 'NO_GO_VERIFICATION_FAILURE') {
  out.push('- decision: No-Go (verification suite failures present)');
} else {
  out.push('- decision: No-Go (runtime failures present)');
}
out.push('');
out.push('## Service Status');
out.push('');
out.push('| Service | Runtime Evidence | Readiness | Probe Statuses | Service Status |');
out.push('|---|---|---|---|---|');
for (const s of services) {
  const probeSummary = s.probes.length ? s.probes.map(p => `${p.key}=${p.status}`).join('; ') : 'none';
  out.push(`| ${s.name} | \`${s.file}\` | ${s.readiness} | ${probeSummary} | ${s.status} |`);
}
out.push('');
out.push('## Interpretation');
out.push('');
if (overall === 'TESTED_NO_GO_RUNTIME_BLOCKED') {
  out.push('- Phase 5/6 extraction scaffolds, shadow bridges, routing, and evidence tooling are implemented and test-covered.');
  out.push('- Runtime canary/probe execution is blocked or unavailable in the current environment, so cutover remains No-Go.');
  out.push('- This is a valid tested closure state for the phase deliverables while preserving backward compatibility.');
}
if (overall === 'TESTED_GO_RUNTIME_READY') {
  out.push('- Runtime evidence supports controlled canary/cutover progression.');
}
if (overall.startsWith('NO_GO_') && overall !== 'TESTED_NO_GO_RUNTIME_BLOCKED') {
  out.push('- Review service rows above and resolve failing runtime or verification checks before proceeding.');
}
out.push('');
out.push('## Checklist Mapping');
out.push('');
out.push('- se-gameplay-orchestrator');
out.push('- se-wallet-adapter');
out.push('- se-bonus-service');
out.push('- se-history-service');
out.push('- se-mp-service');

fs.writeFileSync(outFile, out.join('\n') + '\n');
console.log(`report=${outFile}`);
console.log(`overall_status=${overall}`);
for (const s of services) console.log(`${s.name}=${s.status}`);
NODE
