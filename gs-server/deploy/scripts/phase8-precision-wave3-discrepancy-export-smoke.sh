#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<USAGE
Usage: $(basename "$0")

Deterministic smoke for Phase 8 Wave 3 discrepancy export parser. Builds a synthetic log,
runs the export script, and validates the aggregated JSON summary.
USAGE
}

if [[ ${1:-} == "-h" || ${1:-} == "--help" ]]; then
  usage
  exit 0
fi
if [[ $# -gt 0 ]]; then
  echo "Unknown argument: $1" >&2
  usage
  exit 1
fi

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT
LOG_FILE="${TMP_DIR}/gs.log"
OUT_FILE="${TMP_DIR}/export.json"

cat > "${LOG_FILE}" <<'LOG'
2026-02-23 16:10:00 INFO [main] phase8-precision-dual-calc metric=baseBetMinorUnitsScale2 checkCount=1 mismatchCount=0 bankId=6275 gameId=dragonestone legacy=3000 generalized=3000
2026-02-23 16:10:01 INFO [main] phase8-precision-dual-calc metric=templateMaxBetScale2 checkCount=1 mismatchCount=0 templateMaxCredits=25 legacy=2500.0 generalized=2500.0
2026-02-23 16:10:10 INFO [main] phase8-precision-dual-calc metric=baseBetMinorUnitsScale2 checkCount=1000 mismatchCount=0 bankId=6275 gameId=dragonestone legacy=3000 generalized=3000
2026-02-23 16:10:11 WARN [main] phase8-precision-dual-calc metric=templateMaxBetScale2 checkCount=2 mismatchCount=1 templateMaxCredits=25 legacy=2500.0 generalized=2499.0
LOG

SCRIPT="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-export.sh"
"${SCRIPT}" --log-file "${LOG_FILE}" --out-file "${OUT_FILE}" --pretty true > "${TMP_DIR}/run.out"

node - <<'NODE' "${OUT_FILE}" "${TMP_DIR}/run.out"
const fs = require('fs');
const [jsonFile, runOut] = process.argv.slice(2);
const data = JSON.parse(fs.readFileSync(jsonFile, 'utf8'));
const txt = fs.readFileSync(runOut, 'utf8');
function ok(name, cond) {
  if (!cond) {
    console.log(`FAIL ${name}`);
    process.exitCode = 1;
  } else {
    console.log(`PASS ${name}`);
  }
}
ok('summary line emitted', /summary totalSnapshotLines=4 metricCount=2/.test(txt));
ok('totalSnapshotLines', data.totalSnapshotLines === 4);
ok('metricCount', data.metricCount === 2);
ok('base metric snapshots', data.metrics.baseBetMinorUnitsScale2.snapshots === 2);
ok('base metric maxCheckCount', data.metrics.baseBetMinorUnitsScale2.maxCheckCount === 1000);
ok('base metric last ids', data.metrics.baseBetMinorUnitsScale2.lastBankId === '6275' && data.metrics.baseBetMinorUnitsScale2.lastGameId === 'dragonestone');
ok('template metric mismatch observed', data.metrics.templateMaxBetScale2.maxMismatchCount === 1);
ok('template metric last values', data.metrics.templateMaxBetScale2.lastLegacy === 2500 && data.metrics.templateMaxBetScale2.lastGeneralized === 2499);
ok('template metric mismatch event count tracks nonzero snapshots', data.metrics.templateMaxBetScale2.mismatchEventsObserved === 1);
if (process.exitCode) process.exit(process.exitCode);
NODE
