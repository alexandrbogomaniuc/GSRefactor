#!/usr/bin/env bash
set -euo pipefail
ROOT="/Users/alexb/Documents/Dev/Dev_new"
MAP_FILE="${ROOT}/gs-server/deploy/config/phase9-abs-compatibility-map.json"
VALIDATOR="${ROOT}/gs-server/deploy/scripts/phase9-abs-compatibility-map-validate.sh"

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  cat <<USAGE
Usage: $(basename "$0")

Runs a smoke validation for the Phase 9 ABS compatibility mapping manifest.
USAGE
  exit 0
fi

out="$("${VALIDATOR}" --map-file "${MAP_FILE}")"
printf '%s\n' "$out"
printf '%s\n' "$out" | grep -q '^PHASE9_ABS_MAP_OK$' || { echo "FAIL: validator success marker missing" >&2; exit 3; }
node - <<'NODE' "$MAP_FILE"
const fs = require('fs');
const m = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
const legacy = new Map(m.mappings.map(x => [String(x.legacy).toLowerCase(), x]));
for (const key of ['com.dgphoenix','dgphoenix','mq']) {
  if (!legacy.has(key)) throw new Error(`missing mapping ${key}`);
}
if (legacy.get('com.dgphoenix').defaultWave !== 'W3') throw new Error('com.dgphoenix defaultWave must be W3');
if (legacy.get('mq').reviewOnly !== true) throw new Error('mq must be reviewOnly');
console.log('PHASE9_ABS_MAP_SMOKE_OK');
NODE
