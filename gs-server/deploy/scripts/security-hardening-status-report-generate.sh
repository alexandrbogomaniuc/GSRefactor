#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
VERIFY_REPORT=""
AUDIT_SUMMARY=""
OUT_DIR="${ROOT}/docs/security"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Generate security hardening baseline status report for refactor services and protocol security tooling.

Options:
  --verify-report FILE   Default: latest local verification suite report
  --audit-summary FILE   Default: latest ${ROOT}/docs/security/dependency-audit/audit-summary-prod.json
  --out-dir DIR          Default: ${OUT_DIR}
  -h, --help             Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --verify-report) VERIFY_REPORT="$2"; shift 2 ;;
    --audit-summary) AUDIT_SUMMARY="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
if [[ -z "${VERIFY_REPORT}" ]]; then
  VERIFY_REPORT="$(ls -1t ${ROOT}/docs/quality/local-verification/phase5-6-local-verification-*.md 2>/dev/null | head -n1 || true)"
fi
if [[ -z "${AUDIT_SUMMARY}" ]]; then
  AUDIT_SUMMARY="$(ls -1t ${ROOT}/docs/security/dependency-audit/audit-summary-prod.json 2>/dev/null | head -n1 || true)"
fi
[[ -f "${VERIFY_REPORT}" ]] || { echo "Missing verify report: ${VERIFY_REPORT}" >&2; exit 2; }
if [[ -n "${AUDIT_SUMMARY}" && ! -f "${AUDIT_SUMMARY}" ]]; then
  echo "Missing audit summary: ${AUDIT_SUMMARY}" >&2
  exit 2
fi

TS="$(date -u +%Y%m%d-%H%M%S)"
REPORT="${OUT_DIR}/security-hardening-status-report-${TS}.md"

node - <<'NODE' "${ROOT}" "${VERIFY_REPORT}" "${AUDIT_SUMMARY}" "${REPORT}"
const fs = require('fs');
const path = require('path');
const [root, verifyFile, auditSummaryFile, outFile] = process.argv.slice(2);
const read = p => fs.readFileSync(p, 'utf8');
const pick = (re, src, d='') => ((src.match(re)||[])[1] || d).toString().trim();

const docs = {
  hmac: path.join(root, 'docs/38-json-protocol-hmac-security-v1.md'),
  logic: path.join(root, 'docs/81-phase4-protocol-json-security-logic-smoke-and-suite-gate-20260223-135000.md'),
  runtime: path.join(root, 'docs/82-phase4-protocol-json-security-runtime-probe-tooling-20260223-144000.md'),
  taxonomy: path.join(root, 'docs/27-error-taxonomy-v1.md'),
};
for (const p of Object.values(docs)) if (!fs.existsSync(p)) { console.error(`Missing doc: ${p}`); process.exit(2); }
const scripts = {
  logicSmoke: path.join(root, 'gs-server/deploy/scripts/phase4-protocol-security-logic-smoke.sh'),
  runtimeProbe: path.join(root, 'gs-server/deploy/scripts/phase4-protocol-json-security-canary-probe.sh'),
};
for (const p of Object.values(scripts)) if (!fs.existsSync(p)) { console.error(`Missing script: ${p}`); process.exit(2); }

const servicesDir = path.join(root, 'gs-server/refactor-services');
const serviceDirs = fs.readdirSync(servicesDir, {withFileTypes:true}).filter(d => d.isDirectory()).map(d => d.name).sort();
let pkgCount = 0, lockCount = 0, unpinnedCount = 0;
const serviceRows = [];
for (const name of serviceDirs) {
  const pj = path.join(servicesDir, name, 'package.json');
  const pl = path.join(servicesDir, name, 'package-lock.json');
  if (fs.existsSync(pj)) {
    pkgCount++;
    const pkg = JSON.parse(read(pj));
    const deps = Object.assign({}, pkg.dependencies || {}, pkg.devDependencies || {});
    let unpinned = 0;
    for (const v of Object.values(deps)) {
      const s = String(v);
      if (/^(latest|\*|x|X)$/i.test(s) || /^[~^]/.test(s)) unpinned++;
    }
    if (unpinned > 0) unpinnedCount += unpinned;
    if (fs.existsSync(pl)) lockCount++;
    serviceRows.push({name, hasPackageJson:true, hasLock:fs.existsSync(pl), unpinned});
  }
}

const taxonomy = read(docs.taxonomy);
const hmac = read(docs.hmac);
const logicDoc = read(docs.logic);
const runtimeDoc = read(docs.runtime);
const verify = read(verifyFile);
const vr = { pass:Number(pick(/^- pass:\s*(\d+)$/m, verify,'0')), fail:Number(pick(/^- fail:\s*(\d+)$/m, verify,'0')), skip:Number(pick(/^- skip:\s*(\d+)$/m, verify,'0')) };
let auditSummary = null;
if (auditSummaryFile && fs.existsSync(auditSummaryFile)) {
  try { auditSummary = JSON.parse(read(auditSummaryFile)); } catch (e) { auditSummary = { parseError: String(e) }; }
}
const auditTotals = auditSummary && auditSummary.totals ? auditSummary.totals : null;

const checks = [];
const add = (k, ok) => checks.push([k, ok ? 'PASS' : 'FAIL']);
add('json_hmac_security_doc', hmac.includes('HMAC-SHA256') || /HMAC/i.test(hmac));
add('protocol_security_logic_doc', logicDoc.includes('hash/replay') || /security/i.test(logicDoc));
add('protocol_security_runtime_probe_doc', runtimeDoc.includes('runtime') && runtimeDoc.includes('probe'));
add('error_taxonomy_safe_envelope', /Never expose stack traces/i.test(taxonomy) && /traceId`?\s+on every error/i.test(taxonomy));
add('refactor_service_package_inventory', pkgCount >= 8);
add('security_tooling_scripts_present', true);

const baselineDocsPass = checks.every(([k,v]) => !k.startsWith('refactor_service') ? v==='PASS' : true);
let overall = 'NO_GO_BASELINE_GAPS';
let decision = 'No-Go (security hardening baseline gaps detected)';
if (vr.fail > 0) {
  overall = 'NO_GO_VERIFICATION_FAILURE';
  decision = 'No-Go (verification suite failures present)';
} else if (baselineDocsPass && pkgCount >= 8 && lockCount < pkgCount) {
  overall = 'TESTED_NO_GO_DEPENDENCY_LOCK_AUDIT_PENDING';
  decision = 'No-Go (security baseline and protocol hardening tooling are complete; dependency lockfiles/audit execution are still pending)';
} else if (baselineDocsPass && pkgCount >= 8 && lockCount === pkgCount && !auditTotals) {
  overall = 'TESTED_NO_GO_DEPENDENCY_LOCK_AUDIT_PENDING';
  decision = 'No-Go (lockfiles are present but audit execution evidence is missing)';
} else if (auditTotals && (Number(auditTotals.critical||0) > 0 || Number(auditTotals.high||0) > 0 || Number(auditTotals.moderate||0) > 0 || Number(auditTotals.low||0) > 0)) {
  overall = 'NO_GO_DEPENDENCY_AUDIT_FINDINGS';
  decision = 'No-Go (dependency audit findings remain after lockfile generation)';
} else if (baselineDocsPass && pkgCount >= 8 && lockCount === pkgCount) {
  overall = 'TESTED_SECURITY_HARDENING_COMPLETE';
  decision = 'Go (security baseline, dependency lockfiles, and production audit summary are complete)';
}

const out = [];
out.push('# Security Hardening Status Report');
out.push('');
out.push(`- Verification suite: ${verifyFile}`);
out.push(`- verification pass/fail/skip: ${vr.pass}/${vr.fail}/${vr.skip}`);
out.push(`- refactor service package.json count: ${pkgCount}`);
out.push(`- refactor service package-lock.json count: ${lockCount}`);
out.push(`- approximate unpinned dependency entries (^/~/*/latest): ${unpinnedCount}`);
out.push(`- audit_summary_file: ${auditSummaryFile || 'none'}`);
if (auditTotals) {
  out.push(`- audit prod vulnerabilities low/moderate/high/critical: ${Number(auditTotals.low||0)}/${Number(auditTotals.moderate||0)}/${Number(auditTotals.high||0)}/${Number(auditTotals.critical||0)}`);
}
out.push(`- overall_status: ${overall}`);
out.push(`- decision: ${decision}`);
out.push('');
out.push('## Baseline Checks');
out.push('');
out.push('| Check | Status |');
out.push('|---|---|');
for (const [k,v] of checks) out.push(`| ${k} | ${v} |`);
out.push('');
out.push('## Refactor Service Dependency Inventory');
out.push('');
out.push('| Service | package.json | package-lock.json | approxUnpinnedDeps |');
out.push('|---|---|---|---:|');
for (const r of serviceRows) out.push(`| ${r.name} | yes | ${r.hasLock ? 'yes' : 'no'} | ${r.unpinned} |`);
out.push('');
out.push('## Notes');
out.push('');
out.push('- This report validates baseline hardening docs/tooling and dependency inventory visibility.');
if (!auditTotals) {
  out.push('- Missing audit summary evidence keeps runtime dependency hardening in no-go state for cutover-level approval.');
} else {
  out.push('- `approxUnpinnedDeps` is a package.json hygiene signal; lockfiles + audit summary are the release gate for this report.');
}

fs.writeFileSync(outFile, out.join('\n') + '\n');
console.log(`report=${outFile}`);
console.log(`overall_status=${overall}`);
NODE
