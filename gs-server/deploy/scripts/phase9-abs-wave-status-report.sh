#!/usr/bin/env bash
set -euo pipefail

BLOCKLIST_FILE="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-wave-status-blocklist.json"
PATCH_PLAN_FILE="/Users/alexb/Documents/Dev/Dev_new/docs/phase9/phase9-abs-rename-patch-plan-W0-20260224-094711.md"
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase9"
VERIFY_REPORT=""
WAVE="W0"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Generates a Phase 9 wave status report from the W0 patch-plan, applied-wave artifacts,
and explicit deferred/blocklist reasons.

Options:
  --blocklist FILE      Default: ${BLOCKLIST_FILE}
  --patch-plan FILE     Default: ${PATCH_PLAN_FILE}
  --verify-report FILE  Optional verification suite report (default: latest)
  --out-dir DIR         Default: ${OUT_DIR}
  --wave ID             Default: ${WAVE}
  -h, --help            Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --blocklist) BLOCKLIST_FILE="$2"; shift 2 ;;
    --patch-plan) PATCH_PLAN_FILE="$2"; shift 2 ;;
    --verify-report) VERIFY_REPORT="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    --wave) WAVE="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
if [[ -z "${VERIFY_REPORT}" ]]; then
  VERIFY_REPORT="$(ls -1t /Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-*.md 2>/dev/null | head -n1 || true)"
fi
if [[ ! -f "${BLOCKLIST_FILE}" ]]; then echo "Missing --blocklist: ${BLOCKLIST_FILE}" >&2; exit 2; fi
if [[ ! -f "${PATCH_PLAN_FILE}" ]]; then echo "Missing --patch-plan: ${PATCH_PLAN_FILE}" >&2; exit 2; fi
if [[ -n "${VERIFY_REPORT}" && ! -f "${VERIFY_REPORT}" ]]; then echo "Missing --verify-report: ${VERIFY_REPORT}" >&2; exit 2; fi

TS="$(date -u +%Y%m%d-%H%M%S)"
REPORT="${OUT_DIR}/phase9-abs-wave-status-${WAVE}-${TS}.md"

node - <<'NODE' "$BLOCKLIST_FILE" "$PATCH_PLAN_FILE" "$VERIFY_REPORT" "$REPORT" "$WAVE"
const fs = require('fs');
const path = require('path');
const [blocklistFile, patchPlanFile, verifyReportFileRaw, reportFile, wave] = process.argv.slice(2);
const verifyReportFile = String(verifyReportFileRaw || '').trim();

function fail(msg, code=2){ console.error(`FAIL: ${msg}`); process.exit(code); }

const blocklist = JSON.parse(fs.readFileSync(blocklistFile, 'utf8'));
if (blocklist.type !== 'phase9-abs-wave-status-blocklist') fail('invalid blocklist type');
const patchPlan = fs.readFileSync(patchPlanFile, 'utf8');

const allPatchFiles = [];
const re = /^## File Plan: (.+)$/gm;
let m;
while ((m = re.exec(patchPlan)) !== null) allPatchFiles.push(m[1].trim());
if (allPatchFiles.length === 0) fail('no file plans found in patch plan');

const applied = new Set((((blocklist.applied||{})[wave]) || []).map(String));
const blockedRows = (((blocklist.blocked||{})[wave]) || []).map(r => ({...r, file: String(r.file)}));
const blocked = new Map(blockedRows.map(r => [r.file, r]));

const uncovered = allPatchFiles.filter(f => !applied.has(f) && !blocked.has(f));
const orphanApplied = [...applied].filter(f => !allPatchFiles.includes(f));
const orphanBlocked = blockedRows.filter(r => !allPatchFiles.includes(r.file));

let verifySummary = null;
if (verifyReportFile) {
  const vr = fs.readFileSync(verifyReportFile, 'utf8');
  const pass = (vr.match(/^- pass: (\d+)/m)||[])[1];
  const fail = (vr.match(/^- fail: (\d+)/m)||[])[1];
  const skip = (vr.match(/^- skip: (\d+)/m)||[])[1];
  verifySummary = { file: verifyReportFile, pass: Number(pass||0), fail: Number(fail||0), skip: Number(skip||0) };
}

const blockedByReason = new Map();
for (const r of blockedRows) blockedByReason.set(r.reasonCode, (blockedByReason.get(r.reasonCode)||0) + 1);
const blockedMappings = Array.isArray(blocklist.blockedMappings) ? blocklist.blockedMappings : [];

const coverageOk = uncovered.length === 0;
const verifyOk = !verifySummary || verifySummary.fail === 0;
const wavePilotStatus = (applied.size >= 2 && coverageOk && verifyOk) ? 'TESTED_CONTROLLED_WAVE_COMPLETE' : 'INCOMPLETE';
const broaderPhaseStatus = (coverageOk && verifyOk) ? 'TESTED_NO_GO_PENDING_APPROVALS_AND_WRAPPERS' : 'INCOMPLETE';

const out = [];
out.push(`# Phase 9 ABS Wave Status Report (${wave})`);
out.push('');
out.push(`- Patch-plan source: ${patchPlanFile}`);
out.push(`- Blocklist source: ${blocklistFile}`);
if (verifySummary) out.push(`- Verification suite: ${verifySummary.file}`);
out.push(`- Wave: ${wave}`);
out.push(`- Patch-plan files total: ${allPatchFiles.length}`);
out.push(`- Applied files (recorded): ${applied.size}`);
out.push(`- Deferred/blocked files (recorded): ${blockedRows.length}`);
out.push(`- Uncovered files (must be 0): ${uncovered.length}`);
out.push(`- Orphan applied entries (not in patch-plan): ${orphanApplied.length}`);
out.push(`- Orphan blocked entries (not in patch-plan): ${orphanBlocked.length}`);
if (verifySummary) {
  out.push(`- Verification pass/fail/skip: ${verifySummary.pass}/${verifySummary.fail}/${verifySummary.skip}`);
}
out.push(`- Wave pilot status: ${wavePilotStatus}`);
out.push(`- Phase 9 broader status: ${broaderPhaseStatus}`);
out.push('');
out.push('## Applied Files');
out.push('');
for (const f of [...applied].sort()) out.push(`- ${f}`);
out.push('');
out.push('## Deferred/Blocked Files');
out.push('');
out.push('| File | Reason Code | Reason |');
out.push('|---|---|---|');
for (const r of blockedRows.sort((a,b)=>a.file.localeCompare(b.file))) {
  out.push(`| \`${r.file}\` | ${r.reasonCode} | ${r.reason} |`);
}
out.push('');
out.push('## Deferred Counts By Reason');
out.push('');
out.push('| Reason Code | Count |');
out.push('|---|---:|');
for (const [reason, count] of [...blockedByReason.entries()].sort()) out.push(`| ${reason} | ${count} |`);
out.push('');
out.push('## Blocked Mappings (Global)');
out.push('');
out.push('| Legacy | Reason Code | Reason |');
out.push('|---|---|---|');
for (const r of blockedMappings) out.push(`| \`${r.legacy}\` | ${r.reasonCode} | ${r.reason} |`);
out.push('');
out.push('## Gaps / Validation');
out.push('');
if (uncovered.length === 0) out.push('- No uncovered patch-plan files.'); else uncovered.forEach(f => out.push(`- UNCOVERED: ${f}`));
if (orphanApplied.length === 0) out.push('- No orphan applied entries.'); else orphanApplied.forEach(f => out.push(`- ORPHAN_APPLIED: ${f}`));
if (orphanBlocked.length === 0) out.push('- No orphan blocked entries.'); else orphanBlocked.forEach(r => out.push(`- ORPHAN_BLOCKED: ${r.file}`));

fs.writeFileSync(reportFile, out.join('\n') + '\n');
console.log(`report=${reportFile}`);
console.log(`patch_plan_files=${allPatchFiles.length}`);
console.log(`applied_files=${applied.size}`);
console.log(`blocked_files=${blockedRows.length}`);
console.log(`uncovered_files=${uncovered.length}`);
console.log(`wave_pilot_status=${wavePilotStatus}`);
console.log(`phase9_status=${broaderPhaseStatus}`);
NODE
