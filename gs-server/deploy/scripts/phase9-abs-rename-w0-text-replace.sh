#!/usr/bin/env bash
set -euo pipefail

ROOT="/Users/alexb/Documents/Dev/Dev_new/gs-server"
MAP_FILE="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json"
PATCH_PLAN_REPORT=""
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase9"
WAVE="W0"
MODE="dry-run"
MAX_FILES="200"
APPROVAL_FILE=""

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Manifest-guarded Phase 9 W0 text replacement executor for review-approved patch-plan
exports. Supports dry-run and apply modes. Blocks review-only mappings.

Options:
  --root DIR              Source root for file updates (default: ${ROOT})
  --map-file FILE         Manifest (default: ${MAP_FILE})
  --patch-plan FILE       Patch-plan export report (default: latest in out-dir)
  --out-dir DIR           Report directory (default: ${OUT_DIR})
  --wave ID               Wave label for reporting (default: ${WAVE})
  --mode MODE             dry-run|apply (default: ${MODE})
  --max-files N           Safety cap for file sections processed (default: ${MAX_FILES})
  --approval-file FILE    Required for --mode apply (explicit approval artifact with file allowlist)
  -h, --help              Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --root) ROOT="$2"; shift 2 ;;
    --map-file) MAP_FILE="$2"; shift 2 ;;
    --patch-plan) PATCH_PLAN_REPORT="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    --wave) WAVE="$2"; shift 2 ;;
    --mode) MODE="$2"; shift 2 ;;
    --max-files) MAX_FILES="$2"; shift 2 ;;
    --approval-file) APPROVAL_FILE="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

if [[ "${MODE}" != "dry-run" && "${MODE}" != "apply" ]]; then
  echo "Invalid --mode: ${MODE}" >&2
  exit 1
fi
if [[ "${MODE}" == "apply" && -z "${APPROVAL_FILE}" ]]; then
  echo "Missing --approval-file for --mode apply" >&2
  exit 2
fi

mkdir -p "${OUT_DIR}"
if [[ -z "${PATCH_PLAN_REPORT}" ]]; then
  PATCH_PLAN_REPORT="$(ls -1t "${OUT_DIR}"/phase9-abs-rename-patch-plan-${WAVE}-*.md 2>/dev/null | head -n1 || true)"
fi
if [[ -z "${PATCH_PLAN_REPORT}" || ! -f "${PATCH_PLAN_REPORT}" ]]; then
  echo "Missing --patch-plan and no patch plan found in ${OUT_DIR}" >&2
  exit 2
fi
if [[ ! -f "${MAP_FILE}" ]]; then
  echo "Missing --map-file: ${MAP_FILE}" >&2
  exit 2
fi
if [[ ! -d "${ROOT}" ]]; then
  echo "Missing --root directory: ${ROOT}" >&2
  exit 2
fi
if [[ -n "${APPROVAL_FILE}" && ! -f "${APPROVAL_FILE}" ]]; then
  echo "Missing --approval-file: ${APPROVAL_FILE}" >&2
  exit 2
fi

TS="$(date -u +%Y%m%d-%H%M%S)"
RUN_REPORT="${OUT_DIR}/phase9-abs-rename-w0-text-replace-${MODE}-${TS}.md"

node - <<'NODE' "$MAP_FILE" "$PATCH_PLAN_REPORT" "$RUN_REPORT" "$ROOT" "$WAVE" "$MODE" "$MAX_FILES" "$APPROVAL_FILE"
const fs = require('fs');
const path = require('path');

const [mapFile, patchPlanFile, runReportFile, root, wave, mode, maxFilesRaw, approvalFileRaw] = process.argv.slice(2);
const maxFiles = Math.max(1, parseInt(maxFilesRaw, 10) || 200);
const rootResolved = path.resolve(root);
const approvalFile = String(approvalFileRaw || '').trim();

function fail(msg, code = 2) {
  console.error(`FAIL: ${msg}`);
  process.exit(code);
}
function stripTicks(v) { return String(v || '').replace(/^`|`$/g, ''); }
function countLiteral(hay, needle) {
  if (!needle) return 0;
  let count = 0;
  let idx = 0;
  while (true) {
    idx = hay.indexOf(needle, idx);
    if (idx === -1) break;
    count++; idx += needle.length;
  }
  return count;
}

const manifest = JSON.parse(fs.readFileSync(mapFile, 'utf8'));
if (manifest.type !== 'phase9-abs-compatibility-map') fail('invalid manifest type');
const reviewOnlySet = new Set((manifest.mappings || []).filter(m => m.reviewOnly === true).map(m => String(m.legacy).toLowerCase()));

const text = fs.readFileSync(patchPlanFile, 'utf8');
const lines = text.split(/\r?\n/);
const filePlans = [];
let i = 0;
while (i < lines.length) {
  const header = lines[i].match(/^## File Plan: (.+)$/);
  if (!header) { i++; continue; }
  const relFile = header[1].trim();
  i++;
  while (i < lines.length && !lines[i].startsWith('| Legacy | Replacement | Hits In File |')) i++;
  if (i >= lines.length) break;
  i++; // header
  if (i < lines.length && lines[i].startsWith('|---|')) i++;
  const mappings = [];
  while (i < lines.length && lines[i].startsWith('|')) {
    const parts = lines[i].split('|').slice(1, -1).map(s => s.trim());
    if (parts.length >= 3) {
      const legacy = stripTicks(parts[0]);
      const replacementParts = parts[1].split('/').map(s => stripTicks(s.trim()));
      mappings.push({
        legacy,
        replacement: replacementParts[0] || 'abs',
        replacementUpper: replacementParts[1] || 'ABS',
        hitsInFileDeclared: Number(parts[2]) || 0
      });
    }
    i++;
  }
  filePlans.push({ relFile, mappings });
}
if (filePlans.length === 0) fail('no file plan sections found in patch-plan report');
if (filePlans.length > maxFiles) fail(`file plan count ${filePlans.length} exceeds --max-files ${maxFiles}`);

const blocked = [];
for (const fp of filePlans) {
  for (const m of fp.mappings) {
    if (reviewOnlySet.has(m.legacy.toLowerCase())) {
      blocked.push({ file: fp.relFile, legacy: m.legacy });
    }
  }
}
if (blocked.length > 0) {
  const preview = blocked.slice(0, 10).map(b => `${b.legacy}@${b.file}`).join(', ');
  fail(`review-only mappings present in patch plan: ${preview}`, 3);
}

let approval = null;
let approvalMeta = null;
if (mode === 'apply') {
  if (!approvalFile) fail('approval file required for apply mode', 5);
  let parsed;
  try {
    parsed = JSON.parse(fs.readFileSync(approvalFile, 'utf8'));
  } catch (err) {
    fail(`invalid approval file JSON: ${approvalFile}`, 5);
  }
  if (!parsed || parsed.type !== 'phase9-abs-rename-w0-apply-approval' || Number(parsed.version) !== 1) {
    fail(`invalid approval artifact type/version: ${approvalFile}`, 5);
  }
  if (parsed.approved !== true) fail('approval artifact approved flag must be true', 5);
  if (String(parsed.wave || '') !== String(wave)) fail(`approval artifact wave mismatch: ${parsed.wave} != ${wave}`, 5);
  const approvedPatchPlan = String(parsed.patchPlan || '').trim();
  if (!approvedPatchPlan) fail('approval artifact missing patchPlan', 5);
  const patchPlanBase = path.basename(path.resolve(patchPlanFile));
  const approvedPatchPlanBase = path.basename(path.resolve(approvedPatchPlan));
  if (patchPlanBase !== approvedPatchPlanBase) {
    fail(`approval artifact patchPlan mismatch: ${approvedPatchPlanBase} != ${patchPlanBase}`, 5);
  }
  if (!Array.isArray(parsed.allowedFiles) || parsed.allowedFiles.length === 0) {
    fail('approval artifact allowedFiles must be non-empty array', 5);
  }
  const allowed = new Set(parsed.allowedFiles.map(v => String(v)));
  const filePlanSet = new Set(filePlans.map(fp => fp.relFile));
  const disallowed = [...filePlanSet].filter(f => !allowed.has(f));
  if (disallowed.length > 0) {
    fail(`patch plan contains files not approved: ${disallowed.slice(0, 10).join(', ')}`, 6);
  }
  const extraApproved = [...allowed].filter(f => !filePlanSet.has(f));
  approval = { allowed, extraApprovedCount: extraApproved.length };
  approvalMeta = {
    approvalFile,
    approver: parsed.approver || '',
    approvalId: parsed.approvalId || '',
    approvedAt: parsed.approvedAt || '',
    allowedFiles: parsed.allowedFiles.length,
    extraApprovedCount: extraApproved.length,
    notes: parsed.notes || ''
  };
}

const results = [];
let filesChanged = 0;
let totalPlanned = 0;
let totalApplied = 0;
for (const fp of filePlans) {
  const absFile = path.resolve(rootResolved, fp.relFile);
  if (!(absFile === rootResolved || absFile.startsWith(rootResolved + path.sep))) {
    fail(`file escapes root: ${fp.relFile}`, 4);
  }
  if (!fs.existsSync(absFile) || !fs.statSync(absFile).isFile()) {
    results.push({ file: fp.relFile, status: 'MISSING', mappings: fp.mappings, planned: 0, applied: 0, changed: false });
    continue;
  }
  let content = fs.readFileSync(absFile, 'utf8');
  let nextContent = content;
  const mappingRows = [];
  let filePlanned = 0;
  let fileApplied = 0;
  for (const m of fp.mappings) {
    const planned = countLiteral(nextContent, m.legacy);
    filePlanned += planned;
    totalPlanned += planned;
    let applied = 0;
    if (mode === 'apply' && planned > 0) {
      nextContent = nextContent.split(m.legacy).join(m.replacement);
      applied = planned;
      fileApplied += applied;
      totalApplied += applied;
    }
    mappingRows.push({ ...m, planned, applied });
  }
  const changed = mode === 'apply' && nextContent !== content;
  if (changed) {
    fs.writeFileSync(absFile, nextContent);
    filesChanged += 1;
  }
  results.push({ file: fp.relFile, status: 'OK', mappings: mappingRows, planned: filePlanned, applied: fileApplied, changed });
}

const out = [];
out.push(`# Phase 9 ABS W0 Text Replace (${mode})`);
out.push('');
out.push(`- Patch-plan source: ${patchPlanFile}`);
out.push(`- Manifest: ${mapFile}`);
out.push(`- Root: ${rootResolved}`);
out.push(`- Wave: ${wave}`);
out.push(`- Mode: ${mode}`);
out.push(`- File sections processed: ${filePlans.length}`);
out.push(`- Files changed: ${filesChanged}`);
out.push(`- Total planned literal replacements (exact-case): ${totalPlanned}`);
out.push(`- Total applied literal replacements (exact-case): ${totalApplied}`);
if (approvalMeta) {
  out.push(`- Approval artifact: ${approvalMeta.approvalFile}`);
  out.push(`- Approval id: ${approvalMeta.approvalId || '(none)'}`);
  out.push(`- Approved by: ${approvalMeta.approver || '(none)'}`);
  out.push(`- Approved at: ${approvalMeta.approvedAt || '(none)'}`);
  out.push(`- Allowed files in artifact: ${approvalMeta.allowedFiles}`);
  out.push(`- Extra approved files not in patch plan: ${approvalMeta.extraApprovedCount}`);
}
out.push('');
out.push('## File Results');
out.push('');
out.push('| File | Status | Planned | Applied | Changed |');
out.push('|---|---|---:|---:|---|');
for (const r of results) {
  out.push(`| \`${r.file}\` | ${r.status} | ${r.planned} | ${r.applied} | ${r.changed ? 'yes' : 'no'} |`);
}
for (const r of results) {
  out.push('');
  out.push(`## File Mapping Results: ${r.file}`);
  out.push('');
  out.push('| Legacy | Replacement | Declared Hits | Planned | Applied |');
  out.push('|---|---|---:|---:|---:|');
  for (const m of r.mappings) {
    out.push(`| \`${m.legacy}\` | \`${m.replacement}\` / \`${m.replacementUpper}\` | ${m.hitsInFileDeclared} | ${m.planned} | ${m.applied} |`);
  }
}
fs.writeFileSync(runReportFile, out.join('\n') + '\n');
console.log(`run_report=${runReportFile}`);
console.log(`mode=${mode}`);
console.log(`file_sections=${filePlans.length}`);
console.log(`files_changed=${filesChanged}`);
console.log(`planned_replacements=${totalPlanned}`);
console.log(`applied_replacements=${totalApplied}`);
if (approvalMeta) {
  console.log(`approval_file=${approvalMeta.approvalFile}`);
  console.log(`approval_allowed_files=${approvalMeta.allowedFiles}`);
}
NODE
