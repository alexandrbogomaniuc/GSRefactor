#!/usr/bin/env bash
set -euo pipefail

DRY_RUN_REPORT=""
PATCH_PLAN_REPORT=""
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase9"
WAVE="W0"
APPROVER=""
NOTES=""
APPROVAL_ID=""

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Generates a Phase 9 W0 apply approval artifact JSON from a dry-run report and/or
patch-plan report. Artifact contains an explicit file allowlist and patch-plan link.

Options:
  --dry-run-report FILE   Input dry-run report (preferred source for file allowlist)
  --patch-plan FILE       Patch-plan report (required if no dry-run report)
  --out-dir DIR           Output directory (default: ${OUT_DIR})
  --wave ID               Wave (default: ${WAVE})
  --approver NAME         Optional approver name (default: generated placeholder)
  --notes TEXT            Optional notes
  --approval-id ID        Optional approval id (default: generated)
  -h, --help              Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run-report) DRY_RUN_REPORT="$2"; shift 2 ;;
    --patch-plan) PATCH_PLAN_REPORT="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    --wave) WAVE="$2"; shift 2 ;;
    --approver) APPROVER="$2"; shift 2 ;;
    --notes) NOTES="$2"; shift 2 ;;
    --approval-id) APPROVAL_ID="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
if [[ -z "${DRY_RUN_REPORT}" && -z "${PATCH_PLAN_REPORT}" ]]; then
  DRY_RUN_REPORT="$(ls -1t "${OUT_DIR}"/phase9-abs-rename-w0-text-replace-dry-run-*.md 2>/dev/null | head -n1 || true)"
fi
if [[ -n "${DRY_RUN_REPORT}" && ! -f "${DRY_RUN_REPORT}" ]]; then
  echo "Missing --dry-run-report: ${DRY_RUN_REPORT}" >&2
  exit 2
fi
if [[ -n "${PATCH_PLAN_REPORT}" && ! -f "${PATCH_PLAN_REPORT}" ]]; then
  echo "Missing --patch-plan: ${PATCH_PLAN_REPORT}" >&2
  exit 2
fi

TS="$(date -u +%Y%m%d-%H%M%S)"
OUT_FILE="${OUT_DIR}/phase9-abs-rename-w0-apply-approval-${TS}.json"

node - <<'NODE' "$DRY_RUN_REPORT" "$PATCH_PLAN_REPORT" "$OUT_FILE" "$WAVE" "$APPROVER" "$NOTES" "$APPROVAL_ID"
const fs = require('fs');
const path = require('path');

const [dryRunReportFileRaw, patchPlanReportFileRaw, outFile, wave, approverRaw, notesRaw, approvalIdRaw] = process.argv.slice(2);
const dryRunReportFile = String(dryRunReportFileRaw || '').trim();
let patchPlanReportFile = String(patchPlanReportFileRaw || '').trim();
const approver = String(approverRaw || '').trim() || 'PENDING_APPROVER';
const notes = String(notesRaw || '').trim();
const approvalId = String(approvalIdRaw || '').trim() || `phase9-${wave.toLowerCase()}-${Date.now()}`;

function fail(msg, code=2) { console.error(`FAIL: ${msg}`); process.exit(code); }
function stripTicks(v) { return String(v||'').replace(/^`|`$/g, ''); }

let sourceText = '';
let sourceType = '';
if (dryRunReportFile) {
  sourceText = fs.readFileSync(dryRunReportFile, 'utf8');
  sourceType = 'dry-run';
} else if (patchPlanReportFile) {
  sourceText = fs.readFileSync(patchPlanReportFile, 'utf8');
  sourceType = 'patch-plan';
} else {
  fail('dry-run report or patch-plan report required');
}

if (!patchPlanReportFile && sourceType === 'dry-run') {
  const m = sourceText.match(/^- Patch-plan source: (.+)$/m);
  if (!m) fail('dry-run report missing Patch-plan source line');
  patchPlanReportFile = m[1].trim();
}
if (!patchPlanReportFile) fail('patch-plan report not resolved');

const fileSet = new Set();
if (sourceType === 'dry-run') {
  const lines = sourceText.split(/\r?\n/);
  let inTable = false;
  for (const line of lines) {
    if (line.startsWith('| File | Status | Planned | Applied | Changed |')) { inTable = true; continue; }
    if (!inTable) continue;
    if (line.startsWith('|---|')) continue;
    if (!line.startsWith('|')) break;
    const parts = line.split('|').slice(1,-1).map(s => s.trim());
    if (parts.length >= 5) {
      const file = stripTicks(parts[0]);
      const status = parts[1];
      if (file && status === 'OK') fileSet.add(file);
    }
  }
} else {
  const re = /^## File Plan: (.+)$/gm;
  let m;
  while ((m = re.exec(sourceText)) !== null) fileSet.add(m[1].trim());
}
if (fileSet.size === 0) fail('no files parsed for approval allowlist');

const artifact = {
  type: 'phase9-abs-rename-w0-apply-approval',
  version: 1,
  wave,
  approved: true,
  approvalId,
  approver,
  approvedAt: new Date().toISOString(),
  patchPlan: patchPlanReportFile,
  source: sourceType === 'dry-run' ? dryRunReportFile : patchPlanReportFile,
  allowedFiles: [...fileSet].sort(),
  notes
};
fs.writeFileSync(outFile, JSON.stringify(artifact, null, 2) + '\n');
console.log(`approval_artifact=${outFile}`);
console.log(`allowed_files=${artifact.allowedFiles.length}`);
console.log(`patch_plan=${artifact.patchPlan}`);
NODE
