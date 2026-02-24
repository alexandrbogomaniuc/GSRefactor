#!/usr/bin/env bash
set -euo pipefail

MAP_FILE="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json"
SCAN_REPORT=""
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase9"
WAVE="W0"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Generates a Phase 9 rename execution review plan from a candidate scan report.
This does not modify files; it produces a reviewable checklist and file shortlist.

Options:
  --scan-report FILE   Input candidate scan report (default: latest for wave in out-dir)
  --map-file FILE      Default: ${MAP_FILE}
  --out-dir DIR        Default: ${OUT_DIR}
  --wave ID            Default: ${WAVE}
  -h, --help           Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --scan-report) SCAN_REPORT="$2"; shift 2 ;;
    --map-file) MAP_FILE="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    --wave) WAVE="$2"; shift 2 ;;
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

TS="$(date -u +%Y%m%d-%H%M%S)"
PLAN_REPORT="${OUT_DIR}/phase9-abs-rename-execution-plan-${WAVE}-${TS}.md"

node - <<'NODE' "$MAP_FILE" "$SCAN_REPORT" "$PLAN_REPORT" "$WAVE"
const fs = require('fs');
const [mapFile, scanReportFile, planReportFile, wave] = process.argv.slice(2);
const map = JSON.parse(fs.readFileSync(mapFile, 'utf8'));
const report = fs.readFileSync(scanReportFile, 'utf8');
const lines = report.split(/\r?\n/);

const mappingByLegacy = new Map((map.mappings || []).map(m => [String(m.legacy).toLowerCase(), m]));

const rows = new Map();
let inMainTable = false;
for (const line of lines) {
  if (line.startsWith('| Legacy | Category | Wave |')) { inMainTable = true; continue; }
  if (!inMainTable) continue;
  if (line.startsWith('|---|')) continue;
  if (!line.startsWith('|')) break;
  const parts = line.split('|').slice(1,-1).map(s => s.trim());
  if (parts.length < 7) continue;
  const legacy = parts[0].replace(/^`|`$/g, '');
  rows.set(legacy.toLowerCase(), {
    legacy,
    category: parts[1],
    wave: parts[2],
    reviewOnly: parts[3] === 'yes',
    lineHits: Number(parts[4]),
    fileHits: Number(parts[5]),
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
    const parts = line.split('|').slice(1,-1).map(s => s.trim());
    if (parts.length >= 2) {
      const file = parts[0].replace(/^`|`$/g, '');
      const hits = Number(parts[1]);
      rows.get(currentLegacy)?.topFiles.push({ file, hits });
      continue;
    }
  }
  if (inTopTable && !line.startsWith('|')) {
    currentLegacy = null;
    inTopTable = false;
  }
}

const planCandidates = [...rows.values()]
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

const reviewOnlyHits = [...rows.values()].filter(r => r.wave === wave && r.reviewOnly && r.lineHits > 0);
const totalCandidateFiles = planCandidates.reduce((sum, r) => sum + r.topFiles.length, 0);

const out = [];
out.push(`# Phase 9 ABS Rename Execution Plan (${wave})`);
out.push('');
out.push(`- Source scan report: ${scanReportFile}`);
out.push(`- Manifest: ${mapFile}`);
out.push(`- Wave: ${wave}`);
out.push(`- Plan mode: review-only (no file changes applied)`);
out.push(`- Auto-candidate mappings: ${planCandidates.length}`);
out.push(`- Review-only blockers with hits (excluded): ${reviewOnlyHits.length}`);
out.push(`- Candidate top-file rows (summed): ${totalCandidateFiles}`);
out.push('');
out.push('## Review Checklist');
out.push('');
out.push('- Confirm scan report uses expected wave profile (for W0, `w0_safe_targets`).');
out.push('- Confirm review-only mappings (especially `mq`) remain excluded from execution plan.');
out.push('- Review top files per mapping for external samples/secrets/generated artifacts before editing.');
out.push('- Apply replacements in isolated commit(s) by mapping or file group, then rerun verification suite.');
out.push('');
out.push('## Candidate Mappings (Auto-Candidate Only)');
out.push('');
out.push('| Legacy | Replacement | Hits | Files |');
out.push('|---|---|---:|---:|');
for (const c of planCandidates) {
  out.push(`| \`${c.legacy}\` | \`${c.replacement}\` / \`${c.replacementUpper}\` | ${c.lineHits} | ${c.fileHits} |`);
}
out.push('');
out.push('## Excluded Review-Only Mappings With Hits');
out.push('');
out.push('| Legacy | Hits | Files | Disposition |');
out.push('|---|---:|---:|---|');
for (const r of reviewOnlyHits) {
  out.push(`| \`${r.legacy}\` | ${r.lineHits} | ${r.fileHits} | ${r.disposition} |`);
}
for (const c of planCandidates) {
  out.push('');
  out.push(`## File Shortlist: ${c.legacy} -> ${c.replacement}`);
  out.push('');
  out.push('| File | Hits | Suggested Action |');
  out.push('|---|---:|---|');
  for (const f of c.topFiles) {
    out.push(`| \`${f.file}\` | ${f.hits} | review-and-replace-nonruntime-string |`);
  }
}
fs.writeFileSync(planReportFile, out.join('\n') + '\n');
console.log(`plan_report=${planReportFile}`);
console.log(`auto_candidate_mappings=${planCandidates.length}`);
console.log(`review_only_blockers=${reviewOnlyHits.length}`);
NODE
