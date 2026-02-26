#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

MAP_FILE="${REPO_ROOT}/gs-server/deploy/config/phase9-abs-compatibility-map.json"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [--map-file FILE]

Validates the Phase 9 ABS compatibility mapping manifest (GS scope).
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --map-file)
      MAP_FILE="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

node - <<'NODE' "$MAP_FILE"
const fs = require('fs');
const file = process.argv[2];
const raw = fs.readFileSync(file, 'utf8');
const data = JSON.parse(raw);
function fail(msg){ console.error('FAIL: ' + msg); process.exit(2); }
if (data.type !== 'phase9-abs-compatibility-map') fail('type mismatch');
if (data.version !== 1) fail('version must be 1');
if (data.scope !== 'gs-server') fail('scope must be gs-server');
if (!Array.isArray(data.waves) || data.waves.length < 5) fail('waves missing/incomplete');
if (!Array.isArray(data.mappings) || data.mappings.length < 5) fail('mappings missing/incomplete');
const waveIds = new Set(data.waves.map(w => w.id));
const seenLegacy = new Set();
let reviewOnlyCount = 0;
let riskyMqOk = false;
for (const [idx, m] of data.mappings.entries()) {
  if (!m || typeof m !== 'object') fail(`mapping[${idx}] not object`);
  for (const key of ['legacy','replacement','replacementUpper','category','defaultWave']) {
    if (typeof m[key] !== 'string' || !m[key].trim()) fail(`mapping[${idx}] missing ${key}`);
  }
  const legacyKey = m.legacy.toLowerCase();
  if (seenLegacy.has(legacyKey)) fail(`duplicate legacy key: ${m.legacy}`);
  seenLegacy.add(legacyKey);
  if (!waveIds.has(m.defaultWave)) fail(`mapping[${idx}] unknown wave ${m.defaultWave}`);
  if (m.reviewOnly === true) reviewOnlyCount += 1;
  if (legacyKey === 'mq') {
    if (m.reviewOnly !== true) fail('mq must be reviewOnly=true');
    riskyMqOk = true;
  }
}
if (!riskyMqOk) fail('mq mapping entry missing');
if (reviewOnlyCount < 3) fail('expected at least 3 reviewOnly mappings');
console.log('PHASE9_ABS_MAP_OK');
console.log(`mappings=${data.mappings.length}`);
console.log(`waves=${data.waves.length}`);
console.log(`reviewOnly=${reviewOnlyCount}`);
NODE
