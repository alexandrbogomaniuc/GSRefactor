#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
GEN="${ROOT}/gs-server/deploy/scripts/phase9-abs-rename-w0-approval-artifact-generate.sh"
EXEC="${ROOT}/gs-server/deploy/scripts/phase9-abs-rename-w0-text-replace.sh"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  cat <<USAGE
Usage: $(basename "$0")

Smoke-tests Phase 9 W0 approval artifact generation and apply guard flow.
USAGE
  exit 0
fi

SCAN_ROOT="${TMP_DIR}/gs-server"
mkdir -p "${SCAN_ROOT}/config"
cat > "${SCAN_ROOT}/config/a.xml" <<'TXT'
maxquest nucleus maxquest
TXT
cat > "${SCAN_ROOT}/config/b.xml" <<'TXT'
maxquest
TXT

cat > "${TMP_DIR}/map.json" <<'JSON'
{
  "type":"phase9-abs-compatibility-map","version":1,"scope":"gs-server",
  "waves":[{"id":"W0","label":"w0","allowsAutomaticApply":true,"pathProfile":"w0_safe_targets"}],
  "pathProfiles":{"w0_safe_targets":{"mode":"all"}},
  "mappings":[
    {"legacy":"maxquest","replacement":"abs","replacementUpper":"ABS","category":"brand","defaultWave":"W0","reviewOnly":false},
    {"legacy":"nucleus","replacement":"abs","replacementUpper":"ABS","category":"brand","defaultWave":"W0","reviewOnly":false},
    {"legacy":"mq","replacement":"abs","replacementUpper":"ABS","category":"token","defaultWave":"W0","reviewOnly":true}
  ]
}
JSON

cat > "${TMP_DIR}/patch-plan.md" <<'MD'
# Phase 9 ABS Rename Patch-Plan Export (W0)

## File Plan: config/a.xml

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `maxquest` | `abs` / `ABS` | 2 |
| `nucleus` | `abs` / `ABS` | 1 |

## File Plan: config/b.xml

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `maxquest` | `abs` / `ABS` | 1 |
MD

DRY_OUT="${TMP_DIR}/dry.out"
"${EXEC}" --root "${SCAN_ROOT}" --map-file "${TMP_DIR}/map.json" --patch-plan "${TMP_DIR}/patch-plan.md" --out-dir "${TMP_DIR}" --wave W0 --mode dry-run >"${DRY_OUT}"
DRY_REPORT="$(sed -n 's/^run_report=//p' "${DRY_OUT}" | tail -n1)"
[[ -f "${DRY_REPORT}" ]] || { echo "FAIL: dry-run report missing" >&2; exit 2; }

# Apply without approval should fail.
if "${EXEC}" --root "${SCAN_ROOT}" --map-file "${TMP_DIR}/map.json" --patch-plan "${TMP_DIR}/patch-plan.md" --out-dir "${TMP_DIR}" --wave W0 --mode apply >/dev/null 2>"${TMP_DIR}/no-approval.err"; then
  echo "FAIL: apply without approval should fail" >&2
  exit 3
fi
grep -q 'Missing --approval-file for --mode apply' "${TMP_DIR}/no-approval.err" || { echo "FAIL: missing no-approval error" >&2; cat "${TMP_DIR}/no-approval.err" >&2; exit 4; }

GEN_OUT="${TMP_DIR}/gen.out"
"${GEN}" --dry-run-report "${DRY_REPORT}" --out-dir "${TMP_DIR}" --wave W0 --approver "qa-user" --approval-id "w0-test-1" >"${GEN_OUT}"
APPROVAL="$(sed -n 's/^approval_artifact=//p' "${GEN_OUT}" | tail -n1)"
[[ -f "${APPROVAL}" ]] || { echo "FAIL: approval artifact missing" >&2; exit 5; }
grep -q '"type": "phase9-abs-rename-w0-apply-approval"' "${APPROVAL}" || { echo "FAIL: approval type missing" >&2; cat "${APPROVAL}" >&2; exit 6; }
grep -q '"allowedFiles"' "${APPROVAL}" || { echo "FAIL: allowedFiles missing" >&2; exit 7; }
grep -q '"patchPlanSha256"' "${APPROVAL}" || { echo "FAIL: patchPlanSha256 missing" >&2; cat "${APPROVAL}" >&2; exit 7; }

# Apply with approval should pass and rewrite both files.
APPLY_OUT="${TMP_DIR}/apply-ok.out"
"${EXEC}" --root "${SCAN_ROOT}" --map-file "${TMP_DIR}/map.json" --patch-plan "${TMP_DIR}/patch-plan.md" --out-dir "${TMP_DIR}" --wave W0 --mode apply --approval-file "${APPROVAL}" >"${APPLY_OUT}"
APPLY_REPORT="$(sed -n 's/^run_report=//p' "${APPLY_OUT}" | tail -n1)"
[[ -f "${APPLY_REPORT}" ]] || { echo "FAIL: apply report missing" >&2; exit 8; }
grep -q 'Allowed files in artifact: 2' "${APPLY_REPORT}" || { echo "FAIL: approval metadata missing in report" >&2; cat "${APPLY_REPORT}" >&2; exit 9; }
grep -q 'Patch-plan SHA-256:' "${APPLY_REPORT}" || { echo "FAIL: patch-plan hash missing in apply report" >&2; cat "${APPLY_REPORT}" >&2; exit 9; }
grep -q '^abs abs abs$' "${SCAN_ROOT}/config/a.xml" || { echo "FAIL: file a not rewritten" >&2; exit 10; }
grep -q '^abs$' "${SCAN_ROOT}/config/b.xml" || { echo "FAIL: file b not rewritten" >&2; exit 11; }

# Approval allowlist mismatch should block.
node - <<'NODE' "${APPROVAL}"
const fs = require('fs');
const f = process.argv[2];
const j = JSON.parse(fs.readFileSync(f, 'utf8'));
j.allowedFiles = ['config/a.xml'];
fs.writeFileSync(f + '.narrow', JSON.stringify(j, null, 2));
NODE
if "${EXEC}" --root "${SCAN_ROOT}" --map-file "${TMP_DIR}/map.json" --patch-plan "${TMP_DIR}/patch-plan.md" --out-dir "${TMP_DIR}" --wave W0 --mode apply --approval-file "${APPROVAL}.narrow" >/dev/null 2>"${TMP_DIR}/mismatch.err"; then
  echo "FAIL: allowlist mismatch should fail" >&2
  exit 12
fi
grep -q 'patch plan contains files not approved' "${TMP_DIR}/mismatch.err" || { echo "FAIL: missing allowlist mismatch error" >&2; cat "${TMP_DIR}/mismatch.err" >&2; exit 13; }

# Patch-plan digest mismatch should block apply (tamper approval hash, keep patch-plan path same).
node - <<'NODE' "${APPROVAL}"
const fs = require('fs');
const f = process.argv[2];
const j = JSON.parse(fs.readFileSync(f, 'utf8'));
j.patchPlanSha256 = 'deadbeef' + String(j.patchPlanSha256 || '').slice(8);
fs.writeFileSync(f + '.badhash', JSON.stringify(j, null, 2));
NODE
if "${EXEC}" --root "${SCAN_ROOT}" --map-file "${TMP_DIR}/map.json" --patch-plan "${TMP_DIR}/patch-plan.md" --out-dir "${TMP_DIR}" --wave W0 --mode apply --approval-file "${APPROVAL}.badhash" >/dev/null 2>"${TMP_DIR}/digest.err"; then
  echo "FAIL: digest mismatch should fail" >&2
  exit 14
fi
grep -q 'patchPlanSha256 mismatch' "${TMP_DIR}/digest.err" || { echo "FAIL: missing digest mismatch error" >&2; cat "${TMP_DIR}/digest.err" >&2; exit 15; }

echo "PASS: phase9 abs w0 approval artifact + apply guard smoke"
