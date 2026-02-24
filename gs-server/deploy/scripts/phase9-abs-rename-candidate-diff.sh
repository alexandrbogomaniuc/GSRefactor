#!/usr/bin/env bash
set -euo pipefail

ROOT="/Users/alexb/Documents/Dev/Dev_new/gs-server"
MAP_FILE="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json"
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase9"
WAVE="W0"
MAX_FILES_PER_MAPPING="10"
SCANNER="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase9-abs-rename-candidate-scan.sh"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Runs Phase 9 candidate scanner twice (full vs wave path profile) and writes a
comparison report with per-legacy deltas.

Options:
  --root DIR                Default: ${ROOT}
  --map-file FILE           Default: ${MAP_FILE}
  --out-dir DIR             Default: ${OUT_DIR}
  --wave ID                 Default: ${WAVE}
  --max-files-per-mapping N Default: ${MAX_FILES_PER_MAPPING}
  -h, --help                Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --root) ROOT="$2"; shift 2 ;;
    --map-file) MAP_FILE="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    --wave) WAVE="$2"; shift 2 ;;
    --max-files-per-mapping) MAX_FILES_PER_MAPPING="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
TS="$(date -u +%Y%m%d-%H%M%S)"
REPORT="${OUT_DIR}/phase9-abs-rename-candidate-diff-${WAVE}-${TS}.md"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT
FULL_OUT_DIR="${TMP_DIR}/full"
PROFILE_OUT_DIR="${TMP_DIR}/profile"
mkdir -p "${FULL_OUT_DIR}" "${PROFILE_OUT_DIR}"

"${SCANNER}" --root "${ROOT}" --map-file "${MAP_FILE}" --out-dir "${FULL_OUT_DIR}" --wave "${WAVE}" --max-files-per-mapping "${MAX_FILES_PER_MAPPING}" > "${TMP_DIR}/full.out"
FULL_REPORT="$(sed -n 's/^report=//p' "${TMP_DIR}/full.out" | tail -n1)"

"${SCANNER}" --root "${ROOT}" --map-file "${MAP_FILE}" --out-dir "${PROFILE_OUT_DIR}" --wave "${WAVE}" --max-files-per-mapping "${MAX_FILES_PER_MAPPING}" --safe-targets-only true > "${TMP_DIR}/safe.out"
SAFE_REPORT="$(sed -n 's/^report=//p' "${TMP_DIR}/safe.out" | tail -n1)"

node - <<'NODE' "$FULL_REPORT" "$SAFE_REPORT" "$REPORT" "$WAVE"
const fs = require('fs');
const [fullReport, safeReport, outFile, wave] = process.argv.slice(2);
function parseReport(file) {
  const text = fs.readFileSync(file, 'utf8');
  const lines = text.split(/\r?\n/);
  const meta = {};
  for (const line of lines) {
    const m = line.match(/^- ([^:]+): (.*)$/);
    if (m) meta[m[1].trim()] = m[2].trim();
  }
  const rows = {};
  let inTable = false;
  for (const line of lines) {
    if (line.startsWith('| Legacy | Category | Wave |')) { inTable = true; continue; }
    if (!inTable) continue;
    if (line.startsWith('|---|')) continue;
    if (!line.startsWith('|')) break;
    const parts = line.split('|').slice(1, -1).map(s => s.trim());
    if (parts.length < 7) continue;
    const legacy = parts[0].replace(/^`|`$/g, '');
    rows[legacy] = {
      category: parts[1], wave: parts[2], reviewOnly: parts[3],
      lineHits: Number(parts[4]), files: Number(parts[5]), disposition: parts[6]
    };
  }
  return { meta, rows, path: file };
}
const full = parseReport(fullReport);
const safe = parseReport(safeReport);
const keys = Array.from(new Set([...Object.keys(full.rows), ...Object.keys(safe.rows)])).sort();
const diffRows = keys.map(k => {
  const a = full.rows[k] || { lineHits: 0, files: 0, disposition: 'NO_HIT' };
  const b = safe.rows[k] || { lineHits: 0, files: 0, disposition: 'NO_HIT' };
  return {
    legacy: k,
    fullLineHits: a.lineHits,
    safeLineHits: b.lineHits,
    deltaLineHits: b.lineHits - a.lineHits,
    fullFiles: a.files,
    safeFiles: b.files,
    deltaFiles: b.files - a.files,
    fullDisposition: a.disposition,
    safeDisposition: b.disposition
  };
});
const totalFull = Number(full.meta['Total line hits'] || 0);
const totalSafe = Number(safe.meta['Total line hits'] || 0);
const filtered = Number(safe.meta['Filtered-out line hits (path profile)'] || 0);
const profile = safe.meta['Effective path profile'] || 'unknown';
const out = [];
out.push(`# Phase 9 ABS Candidate Scan Diff (${wave})`);
out.push('');
out.push(`- Full report: ${full.path}`);
out.push(`- Profile report: ${safe.path}`);
out.push(`- Wave: ${wave}`);
out.push(`- Profile: ${profile}`);
out.push(`- Total line hits (full): ${totalFull}`);
out.push(`- Total line hits (profile): ${totalSafe}`);
out.push(`- Filtered-out line hits (profile report): ${filtered}`);
out.push('');
out.push('| Legacy | Full Hits | Profile Hits | Delta Hits | Full Files | Profile Files | Delta Files | Full Disp | Profile Disp |');
out.push('|---|---:|---:|---:|---:|---:|---:|---|---|');
for (const r of diffRows) {
  out.push(`| \`${r.legacy}\` | ${r.fullLineHits} | ${r.safeLineHits} | ${r.deltaLineHits} | ${r.fullFiles} | ${r.safeFiles} | ${r.deltaFiles} | ${r.fullDisposition} | ${r.safeDisposition} |`);
}
fs.writeFileSync(outFile, out.join('\n') + '\n');
console.log(`full_report=${full.path}`);
console.log(`profile_report=${safe.path}`);
console.log(`diff_report=${outFile}`);
console.log(`profile=${profile}`);
NODE
