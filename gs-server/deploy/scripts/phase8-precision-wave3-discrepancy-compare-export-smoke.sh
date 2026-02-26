#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

usage() {
  cat <<USAGE
Usage: $(basename "$0")

Deterministic smoke for Phase 8 Wave 3 discrepancy compare/export CLI. Creates
synthetic A/B export JSON, runs strict and demo policies, and validates JSON/MD outputs.
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
A_JSON="${TMP_DIR}/a.json"
B_JSON="${TMP_DIR}/b.json"
OUT_STRICT="${TMP_DIR}/strict.json"
OUT_DEMO="${TMP_DIR}/demo.json"
OUT_MD="${TMP_DIR}/demo.md"
OUT_OVERRIDE="${TMP_DIR}/override.json"
OUT_TXT1="${TMP_DIR}/strict.out"
OUT_TXT2="${TMP_DIR}/demo.out"
OUT_TXT3="${TMP_DIR}/override.out"

cat > "${A_JSON}" <<'JSON'
{
  "generatedAtUtc": "2026-02-23T16:04:00Z",
  "parser": "phase8-precision-wave3-discrepancy-export",
  "sourceFiles": ["sample-a.log"],
  "totalSnapshotLines": 4,
  "metricCount": 2,
  "metrics": {
    "baseBetMinorUnitsScale2": { "metric": "baseBetMinorUnitsScale2", "snapshots": 2, "maxMismatchCount": 0, "mismatchEventsObserved": 0 },
    "templateMaxBetScale2": { "metric": "templateMaxBetScale2", "snapshots": 2, "maxMismatchCount": 1, "mismatchEventsObserved": 1 }
  }
}
JSON

cat > "${B_JSON}" <<'JSON'
{
  "generatedAtUtc": "2026-02-23T16:20:00Z",
  "parser": "phase8-precision-wave3-discrepancy-export",
  "sourceFiles": ["sample-b.log"],
  "totalSnapshotLines": 7,
  "metricCount": 3,
  "metrics": {
    "baseBetMinorUnitsScale2": { "metric": "baseBetMinorUnitsScale2", "snapshots": 3, "maxMismatchCount": 0, "mismatchEventsObserved": 0 },
    "templateMaxBetScale2": { "metric": "templateMaxBetScale2", "snapshots": 3, "maxMismatchCount": 2, "mismatchEventsObserved": 2 },
    "newMetricScale2": { "metric": "newMetricScale2", "snapshots": 1, "maxMismatchCount": 0, "mismatchEventsObserved": 0 }
  }
}
JSON

SCRIPT="${REPO_ROOT}/gs-server/deploy/scripts/phase8-precision-wave3-discrepancy-compare-export.sh"
"${SCRIPT}" --a-file "${A_JSON}" --b-file "${B_JSON}" --policy strict --out-file "${OUT_STRICT}" --pretty true > "${OUT_TXT1}"
"${SCRIPT}" --a-file "${A_JSON}" --b-file "${B_JSON}" --policy demo_sample_pass --out-file "${OUT_DEMO}" --md-out-file "${OUT_MD}" --pretty true > "${OUT_TXT2}"
"${SCRIPT}" --a-file "${A_JSON}" --b-file "${B_JSON}" --policy strict \
  --threshold-mismatch-a 1 --threshold-mismatch-b 1 --threshold-mismatch-delta 1 --threshold-snapshot-delta 10 --allow-new-metrics-in-b true \
  --out-file "${OUT_OVERRIDE}" --pretty true > "${OUT_TXT3}"

node - <<'NODE' "${OUT_STRICT}" "${OUT_DEMO}" "${OUT_MD}" "${OUT_OVERRIDE}" "${OUT_TXT1}" "${OUT_TXT2}" "${OUT_TXT3}"
const fs = require('fs');
const [strictJson, demoJson, demoMd, overrideJson, strictOut, demoOut, overrideOut] = process.argv.slice(2);
const strict = JSON.parse(fs.readFileSync(strictJson, 'utf8'));
const demo = JSON.parse(fs.readFileSync(demoJson, 'utf8'));
const override = JSON.parse(fs.readFileSync(overrideJson, 'utf8'));
const md = fs.readFileSync(demoMd, 'utf8');
const strictText = fs.readFileSync(strictOut, 'utf8');
const demoText = fs.readFileSync(demoOut, 'utf8');
const overrideText = fs.readFileSync(overrideOut, 'utf8');
function ok(name, cond) {
  if (!cond) { console.log(`FAIL ${name}`); process.exitCode = 1; }
  else console.log(`PASS ${name}`);
}
ok('strict summary emitted', /summary overall=FAIL policy=strict/.test(strictText));
ok('strict policy name', strict.thresholds.profile === 'strict');
ok('strict fails', strict.summary.overall === 'FAIL' && strict.summary.failRules > 0);
ok('demo summary emitted', /summary overall=PASS policy=demo_sample_pass/.test(demoText));
ok('demo policy name', demo.thresholds.profile === 'demo_sample_pass');
ok('demo passes', demo.summary.overall === 'PASS' && demo.summary.failRules === 0);
ok('metric union count', demo.summary.unionMetricCount === 3 && demo.metrics.length === 3);
ok('new metric only in B flagged', demo.metrics.some((m) => m.metric === 'newMetricScale2' && m.compareOnlyInB === true));
ok('markdown includes policy line', md.includes('policy: demo_sample_pass (Demo Sample Pass)'));
ok('markdown includes table header', md.includes('| metric | aMaxMismatch | bMaxMismatch | deltaMismatch |'));
ok('override summary emitted', /summary overall=PASS policy=strict overrides=5/.test(overrideText));
ok('override path keeps seed profile', override.thresholds.profile === 'strict' && override.thresholds.profileLabel === 'Strict Gate');
ok('override path passes', override.summary.overall === 'PASS' && override.summary.failRules === 0);
ok('override values applied', override.thresholds.mismatchA === 1 && override.thresholds.snapshotDelta === 10 && override.thresholds.allowNewMetricsInB === true);
ok('override metadata present', override.thresholds.overridesApplied && Object.keys(override.thresholds.overridesApplied).length === 5);
if (process.exitCode) process.exit(process.exitCode);
NODE
