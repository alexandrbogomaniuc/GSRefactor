#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
MAP_FILE="${REPO_ROOT}/gs-server/deploy/config/phase9-abs-compatibility-map.json"
SCAN_REPORT=""
OUT_DIR="${REPO_ROOT}/docs/phase9"
WAVE="W0"
CONTEXT_LINES="0"
MAX_SNIPPETS_PER_FILE="8"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Generates a Phase 9 review-only per-file grouped patch-plan export from a
candidate scan report. No files are modified.

Options:
  --root DIR                 Source root for snippet extraction (default: ${ROOT})
  --scan-report FILE         Input candidate scan report (default: latest in out-dir)
  --map-file FILE            Manifest (default: ${MAP_FILE})
  --out-dir DIR              Output directory (default: ${OUT_DIR})
  --wave ID                  Wave filter (default: ${WAVE})
  --context-lines N          rg context lines for snippets (default: ${CONTEXT_LINES})
  --max-snippets-per-file N  Cap snippets per file section (default: ${MAX_SNIPPETS_PER_FILE})
  -h, --help                 Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --root) ROOT="$2"; shift 2 ;;
    --scan-report) SCAN_REPORT="$2"; shift 2 ;;
    --map-file) MAP_FILE="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    --wave) WAVE="$2"; shift 2 ;;
    --context-lines) CONTEXT_LINES="$2"; shift 2 ;;
    --max-snippets-per-file) MAX_SNIPPETS_PER_FILE="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
if [[ -z "${SCAN_REPORT}" ]]; then
  SCAN_REPORT="$(ls -1t "${OUT_DIR}"/phase9-abs-rename-candidate-scan-*.md 2>/dev/null | head -n1 || true)"
fi
if [[ -z "${SCAN_REPORT}" || ! -f "${SCAN_REPORT}" ]]; then
  echo "Missing --scan-report and no scan report found in ${OUT_DIR}" >&2
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

TS="$(date -u +%Y%m%d-%H%M%S)"
PATCH_PLAN_REPORT="${OUT_DIR}/phase9-abs-rename-patch-plan-${WAVE}-${TS}.md"
TMP_JSON="$(mktemp)"
trap 'rm -f "${TMP_JSON}"' EXIT

node - <<'NODE' "$MAP_FILE" "$SCAN_REPORT" "$PATCH_PLAN_REPORT" "$WAVE" "$ROOT" "$CONTEXT_LINES" "$MAX_SNIPPETS_PER_FILE" "$TMP_JSON"
const fs = require('fs');
const cp = require('child_process');
const path = require('path');

const [mapFile, scanReportFile, patchPlanReportFile, wave, root, contextLinesRaw, maxSnippetsRaw, outJsonFile] = process.argv.slice(2);
const contextLines = Math.max(0, parseInt(contextLinesRaw, 10) || 0);
const maxSnippetsPerFile = Math.max(1, parseInt(maxSnippetsRaw, 10) || 8);
const map = JSON.parse(fs.readFileSync(mapFile, 'utf8'));
const report = fs.readFileSync(scanReportFile, 'utf8');
const lines = report.split(/\r?\n/);

const mappingByLegacy = new Map((map.mappings || []).map(m => [String(m.legacy).toLowerCase(), m]));

function stripTicks(v) {
  return String(v || '').replace(/^`|`$/g, '');
}

const rows = new Map();
let inMainTable = false;
for (const line of lines) {
  if (line.startsWith('| Legacy | Category | Wave |')) { inMainTable = true; continue; }
  if (!inMainTable) continue;
  if (line.startsWith('|---|')) continue;
  if (!line.startsWith('|')) break;
  const parts = line.split('|').slice(1, -1).map(s => s.trim());
  if (parts.length < 7) continue;
  const legacy = stripTicks(parts[0]);
  rows.set(legacy.toLowerCase(), {
    legacy,
    wave: parts[2],
    reviewOnly: parts[3] === 'yes',
    lineHits: Number(parts[4]) || 0,
    fileHits: Number(parts[5]) || 0,
    disposition: parts[6],
    topFiles: []
  });
}

let currentLegacy = null;
let inTopTable = false;
for (const line of lines) {
  const header = line.match(/^## Top Files: (.+?) \(/);
  if (header) {
    currentLegacy = header[1].trim().toLowerCase();
    inTopTable = false;
    continue;
  }
  if (!currentLegacy) continue;
  if (line.startsWith('| File | Hits |')) { inTopTable = true; continue; }
  if (inTopTable && line.startsWith('|---|')) continue;
  if (inTopTable && line.startsWith('|')) {
    const parts = line.split('|').slice(1, -1).map(s => s.trim());
    if (parts.length >= 2) {
      const file = stripTicks(parts[0]);
      const hits = Number(parts[1]) || 0;
      rows.get(currentLegacy)?.topFiles.push({ file, hits });
      continue;
    }
  }
  if (inTopTable && !line.startsWith('|')) {
    currentLegacy = null;
    inTopTable = false;
  }
}

const autoCandidates = [...rows.values()]
  .filter(r => r.wave === wave)
  .filter(r => r.disposition === 'AUTO_CANDIDATE')
  .filter(r => r.lineHits > 0)
  .map(r => {
    const m = mappingByLegacy.get(r.legacy.toLowerCase()) || {};
    return {
      ...r,
      replacement: m.replacement || 'abs',
      replacementUpper: m.replacementUpper || 'ABS'
    };
  });

const grouped = new Map();
for (const c of autoCandidates) {
  for (const tf of c.topFiles || []) {
    const key = String(tf.file);
    if (!grouped.has(key)) {
      grouped.set(key, { file: key, totalHits: 0, mappings: [], snippets: [] });
    }
    const g = grouped.get(key);
    g.totalHits += tf.hits;
    g.mappings.push({ legacy: c.legacy, replacement: c.replacement, replacementUpper: c.replacementUpper, hits: tf.hits });
  }
}

function relToRoot(p) {
  const normalizedRoot = path.resolve(root);
  const normalizedFile = path.resolve(p);
  if (normalizedFile === normalizedRoot) return '.';
  if (normalizedFile.startsWith(normalizedRoot + path.sep)) {
    return normalizedFile.slice(normalizedRoot.length + 1).replace(/\\/g, '/');
  }
  return p;
}

function collectSnippets(filePath, mappings) {
  const snippets = [];
  const seen = new Set();
  for (const m of mappings) {
    const args = ['-n', '-i', '-F'];
    if (contextLines > 0) args.push(`-C${contextLines}`);
    args.push(m.legacy, filePath);
    let out = '';
    try {
      out = cp.execFileSync('rg', args, { encoding: 'utf8', maxBuffer: 4 * 1024 * 1024 });
    } catch (err) {
      if (typeof err.status === 'number' && err.status === 1) {
        out = '';
      } else {
        out = '';
      }
    }
    if (!out) continue;
    const rows = out.split(/\r?\n/).filter(Boolean);
    for (const row of rows) {
      // Skip rg context separators but preserve actual hit lines.
      if (row === '--') continue;
      const id = `${m.legacy}|${row}`;
      if (seen.has(id)) continue;
      seen.add(id);
      snippets.push({ legacy: m.legacy, text: row });
      if (snippets.length >= maxSnippetsPerFile) return snippets;
    }
  }
  return snippets;
}

for (const g of grouped.values()) {
  const abs = path.isAbsolute(g.file) ? g.file : path.join(root, g.file);
  if (fs.existsSync(abs) && fs.statSync(abs).isFile()) {
    g.snippets = collectSnippets(abs, g.mappings);
  }
  g.relFile = relToRoot(abs);
  g.mappings.sort((a, b) => b.hits - a.hits || a.legacy.localeCompare(b.legacy));
}

const groups = [...grouped.values()].sort((a, b) => b.totalHits - a.totalHits || a.relFile.localeCompare(b.relFile));
const reviewOnlyHits = [...rows.values()].filter(r => r.wave === wave && r.reviewOnly && r.lineHits > 0);

const out = [];
out.push(`# Phase 9 ABS Rename Patch-Plan Export (${wave})`);
out.push('');
out.push(`- Source scan report: ${scanReportFile}`);
out.push(`- Manifest: ${mapFile}`);
out.push(`- Root: ${root}`);
out.push(`- Wave: ${wave}`);
out.push(`- Plan mode: review-only (per-file grouped patch plan; no file changes)`);
out.push(`- Auto-candidate mappings: ${autoCandidates.length}`);
out.push(`- Grouped files: ${groups.length}`);
out.push(`- Review-only mappings with hits (excluded): ${reviewOnlyHits.length}`);
out.push(`- Snippet context lines: ${contextLines}`);
out.push(`- Max snippets per file: ${maxSnippetsPerFile}`);
out.push('');
out.push('## File Groups (Review Queue)');
out.push('');
out.push('| File | Total Hits | Mapping Count | Suggested Action |');
out.push('|---|---:|---:|---|');
for (const g of groups) {
  out.push(`| \`${g.relFile}\` | ${g.totalHits} | ${g.mappings.length} | review-and-replace-nonruntime-string (wave ${wave}) |`);
}
out.push('');
out.push('## Excluded Review-Only Mappings With Hits');
out.push('');
out.push('| Legacy | Hits | Files | Disposition |');
out.push('|---|---:|---:|---|');
for (const r of reviewOnlyHits) {
  out.push(`| \`${r.legacy}\` | ${r.lineHits} | ${r.fileHits} | ${r.disposition} |`);
}
for (const g of groups) {
  out.push('');
  out.push(`## File Plan: ${g.relFile}`);
  out.push('');
  out.push(`- total hits (summed across mappings): ${g.totalHits}`);
  out.push(`- mapping count: ${g.mappings.length}`);
  out.push(`- suggested action: review-and-replace-nonruntime-string`);
  out.push('');
  out.push('| Legacy | Replacement | Hits In File |');
  out.push('|---|---|---:|');
  for (const m of g.mappings) {
    out.push(`| \`${m.legacy}\` | \`${m.replacement}\` / \`${m.replacementUpper}\` | ${m.hits} |`);
  }
  out.push('');
  out.push('### Snippet Preview');
  out.push('');
  if (!g.snippets.length) {
    out.push('_No snippet preview available (file missing or no rg matches on recheck)._');
  } else {
    out.push('```text');
    for (const s of g.snippets) out.push(`[${s.legacy}] ${s.text}`);
    out.push('```');
  }
}

fs.writeFileSync(patchPlanReportFile, out.join('\n') + '\n');
const summary = {
  type: 'phase9-abs-rename-patch-plan-export',
  version: 1,
  wave,
  autoCandidateMappings: autoCandidates.length,
  groupedFiles: groups.length,
  reviewOnlyHits: reviewOnlyHits.length,
  contextLines,
  maxSnippetsPerFile,
  reportFile: patchPlanReportFile
};
fs.writeFileSync(outJsonFile, JSON.stringify(summary));
console.log(`patch_plan_report=${patchPlanReportFile}`);
console.log(`grouped_files=${groups.length}`);
console.log(`auto_candidate_mappings=${autoCandidates.length}`);
console.log(`review_only_hits=${reviewOnlyHits.length}`);
NODE
