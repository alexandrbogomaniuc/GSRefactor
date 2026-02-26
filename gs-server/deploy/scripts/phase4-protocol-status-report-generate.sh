#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

RUNTIME_EVIDENCE=""
VERIFY_REPORT=""
OUT_DIR="${REPO_ROOT}/docs/phase4/protocol"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Generate a Phase 4 protocol adapter status report from runtime evidence-pack output
and the shared local verification suite report.

Options:
  --runtime-evidence FILE   Default: latest phase4 runtime evidence report
  --verify-report FILE      Default: latest local verification suite report
  --out-dir DIR             Default: ${OUT_DIR}
  -h, --help                Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --runtime-evidence) RUNTIME_EVIDENCE="$2"; shift 2 ;;
    --verify-report) VERIFY_REPORT="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
if [[ -z "${RUNTIME_EVIDENCE}" ]]; then
  RUNTIME_EVIDENCE="$(ls -1t ${REPO_ROOT}/docs/phase4/protocol/phase4-protocol-runtime-evidence-*.md 2>/dev/null | head -n1 || true)"
fi
if [[ -z "${VERIFY_REPORT}" ]]; then
  VERIFY_REPORT="$(ls -1t ${REPO_ROOT}/docs/quality/local-verification/phase5-6-local-verification-*.md 2>/dev/null | head -n1 || true)"
fi

[[ -f "${RUNTIME_EVIDENCE}" ]] || { echo "Missing runtime evidence report: ${RUNTIME_EVIDENCE}" >&2; exit 2; }
[[ -f "${VERIFY_REPORT}" ]] || { echo "Missing verify report: ${VERIFY_REPORT}" >&2; exit 2; }

TS="$(date -u +%Y%m%d-%H%M%S)"
REPORT="${OUT_DIR}/phase4-protocol-status-report-${TS}.md"

node - <<'NODE' "${RUNTIME_EVIDENCE}" "${VERIFY_REPORT}" "${REPORT}"
const fs = require('fs');
const [runtimeFile, verifyFile, outFile] = process.argv.slice(2);

const runtime = fs.readFileSync(runtimeFile, 'utf8');
const verify = fs.readFileSync(verifyFile, 'utf8');

function pick(re, src, dflt='') {
  const m = src.match(re);
  return m ? String(m[1]).trim() : dflt;
}

const info = {
  bankId: pick(/^- bankId:\s*(.+)$/m, runtime),
  transport: pick(/^- transport:\s*(.+)$/m, runtime),
  allowMissingRuntime: pick(/^- allowMissingRuntime:\s*(.+)$/m, runtime),
  runtimeReadiness: pick(/^- runtime_readiness:\s*(.+)$/m, runtime),
  parity: pick(/^- parity_check:\s*(.+)$/m, runtime),
  wallet: pick(/^- wallet_shadow_probe:\s*(.+)$/m, runtime),
  security: pick(/^- json_security_probe:\s*(.+)$/m, runtime)
};

const vr = {
  pass: Number(pick(/^- pass:\s*(\d+)$/m, verify, '0')),
  fail: Number(pick(/^- fail:\s*(\d+)$/m, verify, '0')),
  skip: Number(pick(/^- skip:\s*(\d+)$/m, verify, '0'))
};

function isPass(v){ return v === 'PASS'; }
function isSkip(v){ return /^SKIP/.test(v); }
function isBlockedReadiness(v){ return /^SKIP_/.test(v) || /NOT_READY/i.test(v); }

const runtimeBlocked = isBlockedReadiness(info.runtimeReadiness) || isSkip(info.parity) || isSkip(info.wallet);
const runtimeGo = isPass(info.runtimeReadiness) && isPass(info.parity) && isPass(info.wallet) &&
  (isPass(info.security) || info.security === 'SKIPPED' || /^SKIP/.test(info.security));

let phase4Status = 'INCOMPLETE';
let phase4Decision = 'No decision';
if (runtimeGo && vr.fail === 0) {
  phase4Status = 'TESTED_GO_RUNTIME_PARITY_READY';
  phase4Decision = 'Go (runtime parity checks and verification suite passing)';
} else if (runtimeBlocked && vr.fail === 0) {
  phase4Status = 'TESTED_NO_GO_RUNTIME_BLOCKED';
  phase4Decision = 'No-Go (runtime adapter execution blocked/unavailable; tooling and logic gates are passing)';
} else if (vr.fail > 0) {
  phase4Status = 'NO_GO_VERIFICATION_FAILURE';
  phase4Decision = 'No-Go (verification suite failures present)';
} else {
  phase4Status = 'NO_GO_RUNTIME_FAILURE';
  phase4Decision = 'No-Go (runtime parity/wallet checks failed)';
}

const out = [];
out.push('# Phase 4 Protocol Adapter Status Report');
out.push('');
out.push(`- Runtime evidence source: ${runtimeFile}`);
out.push(`- Verification suite source: ${verifyFile}`);
out.push(`- bankId: ${info.bankId}`);
out.push(`- transport: ${info.transport}`);
out.push(`- allowMissingRuntime: ${info.allowMissingRuntime}`);
out.push(`- runtime_readiness: ${info.runtimeReadiness}`);
out.push(`- parity_check: ${info.parity}`);
out.push(`- wallet_shadow_probe: ${info.wallet}`);
out.push(`- json_security_probe: ${info.security}`);
out.push(`- verification pass/fail/skip: ${vr.pass}/${vr.fail}/${vr.skip}`);
out.push(`- phase4_status: ${phase4Status}`);
out.push(`- decision: ${phase4Decision}`);
out.push('');
out.push('## Interpretation');
out.push('');
if (phase4Status === 'TESTED_NO_GO_RUNTIME_BLOCKED') {
  out.push('- Phase 4 design/tooling foundations are testable and passing local logic gates.');
  out.push('- Runtime parity execution is currently blocked by environment/runtime availability (for example Docker API denied or services not deployed).');
  out.push('- This is a valid tested no-go state for cutover while preserving backward compatibility.');
} else if (phase4Status === 'TESTED_GO_RUNTIME_PARITY_READY') {
  out.push('- Phase 4 runtime parity checks are passing and the adapter path is ready for controlled rollout.');
} else {
  out.push('- Review runtime evidence and verification outputs before any rollout decision.');
}
out.push('');
out.push('## Delivery Checklist Mapping');
out.push('');
out.push('- Canonical model + adapter foundation: implemented earlier (Phase 4 scaffold and adapter docs).');
out.push('- JSON/XML parity suite: present (`phase4-json-xml-parity-check.sh`) and included in runtime evidence-pack flow.');
out.push('- Per-bank protocol mode routing: implemented in protocol adapter service and configuration-driven tooling.');
out.push('- Runtime decision state for current environment: see `phase4_status` above.');

fs.writeFileSync(outFile, out.join('\n') + '\n');
console.log(`report=${outFile}`);
console.log(`phase4_status=${phase4Status}`);
console.log(`decision=${phase4Decision}`);
NODE
