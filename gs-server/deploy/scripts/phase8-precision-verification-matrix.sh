#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
POLICY_FILE="${ROOT}/gs-server/deploy/config/phase8-precision-policy.json"
OUT_DIR="${ROOT}/docs/phase8/precision"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Generates a Phase 8 precision verification matrix report from the versioned policy file.
This is a GS-side planning/verification artifact (no runtime activation).

Options:
  --policy-file PATH   Default: ${POLICY_FILE}
  --out-dir DIR        Default: ${OUT_DIR}
  -h, --help           Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --policy-file) POLICY_FILE="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
TS="$(date -u +%Y%m%d-%H%M%S)"
REPORT="${OUT_DIR}/phase8-precision-verification-matrix-${TS}.md"

node - "${POLICY_FILE}" "${REPORT}" <<'NODE'
const fs = require('fs');
const [policyFile, reportFile] = process.argv.slice(2);
const raw = fs.readFileSync(policyFile, 'utf8');
const p = JSON.parse(raw);

if (p.schemaVersion !== 1) throw new Error(`Unsupported schemaVersion: ${p.schemaVersion}`);
if (!Array.isArray(p.currencyPolicies) || p.currencyPolicies.length === 0) throw new Error('currencyPolicies missing');
if (!Array.isArray(p.verificationCategories) || p.verificationCategories.length === 0) throw new Error('verificationCategories missing');

for (const c of p.currencyPolicies) {
  if (!c.currency || typeof c.currency !== 'string') throw new Error('currency entry missing currency');
  if (!Number.isInteger(c.minorUnitScale)) throw new Error(`currency ${c.currency} invalid minorUnitScale`);
}

const scaleCounts = new Map();
const statusCounts = new Map();
for (const c of p.currencyPolicies) {
  scaleCounts.set(c.minorUnitScale, (scaleCounts.get(c.minorUnitScale) || 0) + 1);
  statusCounts.set(c.phase8Status, (statusCounts.get(c.phase8Status) || 0) + 1);
}

const blocking = p.verificationCategories.filter(x => x.blocking);
const nonBlocking = p.verificationCategories.filter(x => !x.blocking);
const allBlockingCleared = blocking.every(x => String(x.status).includes('done') || String(x.status).includes('cleared'));

const lines = [];
lines.push(`# Phase 8 Precision Verification Matrix (${new Date().toISOString()} UTC)`);
lines.push('');
lines.push('- source-policy: `' + policyFile + '`');
lines.push(`- schemaVersion: ${p.schemaVersion}`);
lines.push(`- defaultMinorUnitScale: ${p.defaultMinorUnitScale}`);
lines.push(`- allowedMinorUnitScales: ${(p.allowedMinorUnitScales || []).join(', ')}`);
lines.push(`- currencyPolicies: ${p.currencyPolicies.length}`);
lines.push(`- verificationCategories: ${p.verificationCategories.length}`);
lines.push(`- blockingCategories: ${blocking.length}`);
lines.push(`- phase8ReadyToClose: ${allBlockingCleared ? 'yes' : 'no'}`);
lines.push('');
lines.push('## Currency Matrix');
lines.push('');
lines.push('| Currency | Scale | Phase8 Status | Canary | Notes |');
lines.push('|---|---:|---|---|---|');
for (const c of p.currencyPolicies) {
  lines.push(`| ${c.currency} | ${c.minorUnitScale} | ${c.phase8Status} | ${c.canaryEligible ? 'yes' : 'no'} | ${String(c.notes || '').replace(/\|/g, '/')} |`);
}
lines.push('');
lines.push('## Verification Categories');
lines.push('');
lines.push('| Category | Status | Blocking | Evidence |');
lines.push('|---|---|---|---|');
for (const v of p.verificationCategories) {
  lines.push(`| ${v.category} | ${v.status} | ${v.blocking ? 'yes' : 'no'} | ${v.evidence || ''} |`);
}
lines.push('');
lines.push('## Summary Counts');
lines.push('');
lines.push('### By Minor Unit Scale');
for (const [scale, count] of [...scaleCounts.entries()].sort((a, b) => a[0] - b[0])) {
  lines.push(`- scale ${scale}: ${count}`);
}
lines.push('');
lines.push('### By Currency Status');
for (const [status, count] of [...statusCounts.entries()].sort((a, b) => String(a[0]).localeCompare(String(b[0])))) {
  lines.push(`- ${status}: ${count}`);
}
lines.push('');
lines.push('## Next Actions (Generated)');
if (blocking.length === 0) {
  lines.push('- No blocking categories listed in policy.');
} else {
  for (const v of blocking) {
    lines.push(`- ${v.category}: ${v.status} -> resolve before Phase 8 closure`);
  }
}
lines.push('');
lines.push('## Notes');
lines.push('- This matrix is a GS-side planning/verification artifact and does not activate precision behavior by itself.');
lines.push('- Runtime activation remains gated by Wave 3 apply-mode properties and canary validation.');

fs.writeFileSync(reportFile, lines.join('\n') + '\n');
NODE

echo "report=${REPORT}"
