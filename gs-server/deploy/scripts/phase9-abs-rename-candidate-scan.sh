#!/usr/bin/env bash
set -euo pipefail

ROOT="/Users/alexb/Documents/Dev/Dev_new/gs-server"
MAP_FILE="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/phase9-abs-compatibility-map.json"
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase9"
WAVE=""
ENFORCE_AUTO_APPLY="false"
MAX_FILES_PER_MAPPING="10"
SAFE_TARGETS_ONLY="false"
PATH_PROFILE=""

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Manifest-driven Phase 9 ABS rename candidate scanner (GS scope).
Produces wave-specific candidate report and can block unsafe auto-apply plans
when review-only mappings (for example mq) are present.

Options:
  --root DIR                Default: ${ROOT}
  --map-file FILE           Default: ${MAP_FILE}
  --out-dir DIR             Default: ${OUT_DIR}
  --wave ID                 Optional wave filter (e.g. W0, W1, W3). Default: all
  --enforce-auto-apply B    true|false (default: ${ENFORCE_AUTO_APPLY})
  --max-files-per-mapping N Default: ${MAX_FILES_PER_MAPPING}
  --safe-targets-only B     true|false (default: ${SAFE_TARGETS_ONLY})
  --path-profile NAME       Optional manifest path profile override
  -h, --help                Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --root)
      ROOT="$2"; shift 2 ;;
    --map-file)
      MAP_FILE="$2"; shift 2 ;;
    --out-dir)
      OUT_DIR="$2"; shift 2 ;;
    --wave)
      WAVE="$2"; shift 2 ;;
    --enforce-auto-apply)
      ENFORCE_AUTO_APPLY="$2"; shift 2 ;;
    --max-files-per-mapping)
      MAX_FILES_PER_MAPPING="$2"; shift 2 ;;
    --safe-targets-only)
      SAFE_TARGETS_ONLY="$2"; shift 2 ;;
    --path-profile)
      PATH_PROFILE="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

if [[ "${ENFORCE_AUTO_APPLY}" != "true" && "${ENFORCE_AUTO_APPLY}" != "false" ]]; then
  echo "Invalid --enforce-auto-apply: ${ENFORCE_AUTO_APPLY}" >&2
  exit 1
fi
if [[ "${SAFE_TARGETS_ONLY}" != "true" && "${SAFE_TARGETS_ONLY}" != "false" ]]; then
  echo "Invalid --safe-targets-only: ${SAFE_TARGETS_ONLY}" >&2
  exit 1
fi

mkdir -p "${OUT_DIR}"
TS="$(date -u +%Y%m%d-%H%M%S)"
REPORT="${OUT_DIR}/phase9-abs-rename-candidate-scan-${TS}.md"
TMP_JSON="$(mktemp)"
trap 'rm -f "${TMP_JSON}"' EXIT

node - <<'NODE' "${MAP_FILE}" "${ROOT}" "${REPORT}" "${WAVE}" "${ENFORCE_AUTO_APPLY}" "${MAX_FILES_PER_MAPPING}" "${TMP_JSON}" "${SAFE_TARGETS_ONLY}" "${PATH_PROFILE}"
const fs = require('fs');
const cp = require('child_process');
const path = require('path');

const [mapFile, root, reportFile, waveFilterRaw, enforceRaw, maxFilesRaw, outJsonFile, safeTargetsOnlyRaw, pathProfileOverrideRaw] = process.argv.slice(2);
const waveFilter = (waveFilterRaw || '').trim();
const enforceAutoApply = String(enforceRaw) === 'true';
const maxFilesPerMapping = Math.max(1, parseInt(maxFilesRaw, 10) || 10);
const safeTargetsOnly = String(safeTargetsOnlyRaw) === 'true';
const pathProfileOverride = String(pathProfileOverrideRaw || '').trim();
const map = JSON.parse(fs.readFileSync(mapFile, 'utf8'));

function fail(msg, code = 2) {
  console.error(`FAIL: ${msg}`);
  process.exit(code);
}
if (map.type !== 'phase9-abs-compatibility-map') fail('invalid manifest type');
if (!Array.isArray(map.waves) || !Array.isArray(map.mappings)) fail('manifest missing waves/mappings');
if (!map.pathProfiles || typeof map.pathProfiles !== 'object') fail('manifest missing pathProfiles');
if (!fs.existsSync(root)) fail(`root not found: ${root}`);

const waves = new Map(map.waves.map(w => [w.id, w]));
if (waveFilter && !waves.has(waveFilter)) fail(`unknown wave: ${waveFilter}`);
if (pathProfileOverride && !map.pathProfiles[pathProfileOverride]) fail(`unknown path profile: ${pathProfileOverride}`);
const selectedMappings = map.mappings.filter(m => !waveFilter || m.defaultWave === waveFilter);
if (selectedMappings.length === 0) fail(`no mappings selected${waveFilter ? ' for wave ' + waveFilter : ''}`);

let effectivePathProfileName = 'full_scan';
if (pathProfileOverride) {
  effectivePathProfileName = pathProfileOverride;
} else if (safeTargetsOnly) {
  if (waveFilter && waves.get(waveFilter)?.pathProfile) {
    effectivePathProfileName = waves.get(waveFilter).pathProfile;
  } else {
    effectivePathProfileName = 'w0_safe_targets';
  }
}
const effectivePathProfile = map.pathProfiles[effectivePathProfileName];
if (!effectivePathProfile) fail(`effective path profile missing: ${effectivePathProfileName}`);

function runRgLines(args) {
  try {
    return cp.execFileSync('rg', args, { encoding: 'utf8', maxBuffer: 50 * 1024 * 1024 });
  } catch (err) {
    if (typeof err.status === 'number' && err.status === 1) {
      return '';
    }
    throw err;
  }
}

function scanPattern(legacy) {
  const baseArgs = [
    '-i', '-F', '--no-messages', '--hidden',
    '--glob', '!.git', '--glob', '!target', '--glob', '!node_modules',
    '--glob', '!*.class', '--glob', '!*.jar', '--glob', '!*.log',
    '--glob', '!**/deploy/scripts/phase9-*', '--glob', '!**/deploy/config/phase9-*',
    '-n', legacy, root
  ];
  const out = runRgLines(baseArgs);
  const lines = out ? out.split(/\r?\n/).filter(Boolean) : [];
  const includeExts = new Set(
    Array.isArray(effectivePathProfile.includeExtensions)
      ? effectivePathProfile.includeExtensions.map(x => String(x).toLowerCase())
      : []
  );
  const excludeContains = Array.isArray(effectivePathProfile.excludePathContains)
    ? effectivePathProfile.excludePathContains.map(x => String(x))
    : [];
  function pathAllowed(file) {
    if (effectivePathProfile.mode === 'all') return true;
    const normalized = String(file).replace(/\\/g, '/');
    if (effectivePathProfile.mode === 'extension_and_path_filters') {
      const ext = (normalized.split('.').pop() || '').toLowerCase();
      if (includeExts.size > 0 && !includeExts.has(ext)) return false;
      for (const part of excludeContains) {
        if (part && normalized.includes(part)) return false;
      }
      return true;
    }
    return true;
  }
  const fileCounts = new Map();
  let filteredLines = 0;
  for (const line of lines) {
    const idx = line.indexOf(':');
    if (idx <= 0) continue;
    const file = line.slice(0, idx);
    if (!pathAllowed(file)) {
      filteredLines += 1;
      continue;
    }
    fileCounts.set(file, (fileCounts.get(file) || 0) + 1);
  }
  const topFiles = [...fileCounts.entries()]
    .sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
    .slice(0, maxFilesPerMapping)
    .map(([file, count]) => ({ file, count }));
  const keptLineHits = [...fileCounts.values()].reduce((a, b) => a + b, 0);
  return { lineHits: keptLineHits, uniqueFiles: fileCounts.size, topFiles, filteredLines };
}

const waveInfo = waveFilter ? waves.get(waveFilter) : null;
let reviewOnlyHits = 0;
let autoCandidateMappings = 0;
let totalLineHits = 0;
let totalUniqueFiles = 0;
let totalFilteredLines = 0;
const rows = selectedMappings.map((m) => {
  const stats = scanPattern(String(m.legacy));
  totalLineHits += stats.lineHits;
  totalUniqueFiles += stats.uniqueFiles;
  totalFilteredLines += stats.filteredLines;
  const reviewOnly = m.reviewOnly === true;
  const wave = waves.get(m.defaultWave) || { allowsAutomaticApply: false };
  let disposition = 'NO_HIT';
  if (stats.lineHits > 0 && reviewOnly) {
    disposition = 'REVIEW_ONLY_HIT';
    reviewOnlyHits += 1;
  } else if (stats.lineHits > 0 && !reviewOnly) {
    disposition = wave.allowsAutomaticApply ? 'AUTO_CANDIDATE' : 'MANUAL_CANDIDATE';
    if (disposition === 'AUTO_CANDIDATE') autoCandidateMappings += 1;
  }
  return {
    ...m,
    lineHits: stats.lineHits,
    uniqueFiles: stats.uniqueFiles,
    disposition,
    topFiles: stats.topFiles,
    waveAllowsAutomaticApply: !!wave.allowsAutomaticApply
  };
});

let blockReason = '';
if (enforceAutoApply) {
  if (waveFilter && waveInfo && !waveInfo.allowsAutomaticApply) {
    blockReason = `BLOCKED_WAVE_NOT_AUTOMATIC:${waveFilter}`;
  } else if (rows.some(r => r.disposition === 'REVIEW_ONLY_HIT')) {
    const risky = rows.filter(r => r.disposition === 'REVIEW_ONLY_HIT').map(r => r.legacy).join(',');
    blockReason = `BLOCKED_REVIEW_ONLY:${risky}`;
  }
}

const nowIso = new Date().toISOString().replace(/\.\d{3}Z$/, 'Z');
const reportLines = [];
reportLines.push(`# Phase 9 ABS Rename Candidate Scan (${nowIso.replace('T', ' ')})`);
reportLines.push('');
reportLines.push(`- Root scanned: ${root}`);
reportLines.push(`- Manifest: ${mapFile}`);
reportLines.push(`- Wave filter: ${waveFilter || 'ALL'}`);
reportLines.push(`- Enforce auto-apply: ${enforceAutoApply}`);
reportLines.push(`- Safe targets only: ${safeTargetsOnly}`);
reportLines.push(`- Effective path profile: ${effectivePathProfileName}`);
reportLines.push(`- Total mappings scanned: ${rows.length}`);
reportLines.push(`- Total line hits: ${totalLineHits}`);
reportLines.push(`- Total unique-file hits (sum per mapping): ${totalUniqueFiles}`);
reportLines.push(`- Filtered-out line hits (path profile): ${totalFilteredLines}`);
reportLines.push(`- Auto-candidate mappings: ${autoCandidateMappings}`);
reportLines.push(`- Review-only mappings with hits: ${reviewOnlyHits}`);
reportLines.push(`- Auto-apply status: ${blockReason ? 'BLOCKED' : 'READY_OR_REPORT_ONLY'}`);
if (blockReason) reportLines.push(`- Block reason: ${blockReason}`);
reportLines.push('');
reportLines.push('| Legacy | Category | Wave | ReviewOnly | Line Hits | Files | Disposition |');
reportLines.push('|---|---|---|---:|---:|---:|---|');
for (const r of rows) {
  reportLines.push(`| \`${String(r.legacy).replace(/`/g,'')}\` | ${r.category} | ${r.defaultWave} | ${r.reviewOnly === true ? 'yes' : 'no'} | ${r.lineHits} | ${r.uniqueFiles} | ${r.disposition} |`);
}
for (const r of rows.filter(r => r.topFiles && r.topFiles.length > 0)) {
  reportLines.push('');
  reportLines.push(`## Top Files: ${r.legacy} (${r.disposition})`);
  reportLines.push('');
  reportLines.push('| File | Hits |');
  reportLines.push('|---|---:|');
  for (const f of r.topFiles) {
    reportLines.push(`| \`${f.file}\` | ${f.count} |`);
  }
}
fs.writeFileSync(reportFile, reportLines.join('\n') + '\n');

const summary = {
  type: 'phase9-abs-rename-candidate-scan-summary',
  version: 1,
  manifestVersion: map.version,
  wave: waveFilter || 'ALL',
  enforceAutoApply,
  safeTargetsOnly,
  pathProfile: effectivePathProfileName,
  rows: rows.length,
  totalLineHits,
  totalFilteredLines,
  autoCandidateMappings,
  reviewOnlyHits,
  blocked: !!blockReason,
  blockReason,
  reportFile
};
fs.writeFileSync(outJsonFile, JSON.stringify(summary));
console.log(`report=${reportFile}`);
console.log(`auto_apply_status=${blockReason ? 'BLOCKED' : 'READY_OR_REPORT_ONLY'}`);
if (blockReason) {
  console.log(`block_reason=${blockReason}`);
  process.exit(2);
}
NODE
