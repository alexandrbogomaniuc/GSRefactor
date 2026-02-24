#!/usr/bin/env bash
set -euo pipefail
ROOT="/Users/alexb/Documents/Dev/Dev_new"
SCRIPT="${ROOT}/gs-server/deploy/scripts/phase9-abs-rename-w0-text-replace.sh"
GEN="${ROOT}/gs-server/deploy/scripts/phase9-abs-rename-w0-approval-artifact-generate.sh"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  cat <<USAGE
Usage: $(basename "$0")

Smoke-tests Phase 9 W0 text replacement dry-run/apply executor.
USAGE
  exit 0
fi

SCAN_ROOT="${TMP_DIR}/gs-server"
mkdir -p "${SCAN_ROOT}/config"
cat > "${SCAN_ROOT}/config/a.xml" <<'TXT'
maxquest nucleus maxquest
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

cat > "${TMP_DIR}/patch-plan-safe.md" <<'MD'
# Phase 9 ABS Rename Patch-Plan Export (W0)

## File Plan: config/a.xml

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `maxquest` | `abs` / `ABS` | 2 |
| `nucleus` | `abs` / `ABS` | 1 |
MD

DRY_OUT="${TMP_DIR}/dry.out"
"${SCRIPT}" --root "${SCAN_ROOT}" --map-file "${TMP_DIR}/map.json" --patch-plan "${TMP_DIR}/patch-plan-safe.md" --out-dir "${TMP_DIR}" --wave W0 --mode dry-run >"${DRY_OUT}"
DRY_REPORT="$(sed -n 's/^run_report=//p' "${DRY_OUT}" | tail -n1)"
[[ -f "${DRY_REPORT}" ]] || { echo "FAIL: dry-run report missing" >&2; exit 2; }
grep -q 'Mode: dry-run' "${DRY_REPORT}" || { echo "FAIL: dry-run mode missing" >&2; exit 3; }
grep -q 'Total planned literal replacements (exact-case): 3' "${DRY_REPORT}" || { echo "FAIL: dry-run planned count" >&2; cat "${DRY_REPORT}" >&2; exit 4; }
grep -q '| `config/a.xml` | OK | 3 | 0 | no |' "${DRY_REPORT}" || { echo "FAIL: dry-run file row" >&2; cat "${DRY_REPORT}" >&2; exit 5; }
grep -q 'maxquest nucleus maxquest' "${SCAN_ROOT}/config/a.xml" || { echo "FAIL: dry-run modified file" >&2; exit 6; }

APPLY_OUT="${TMP_DIR}/apply.out"
GEN_OUT="${TMP_DIR}/gen.out"
"${GEN}" --dry-run-report "${DRY_REPORT}" --out-dir "${TMP_DIR}" --wave W0 --approver "smoke" --approval-id "smoke-apply" >"${GEN_OUT}"
APPROVAL="$(sed -n 's/^approval_artifact=//p' "${GEN_OUT}" | tail -n1)"
[[ -f "${APPROVAL}" ]] || { echo "FAIL: approval artifact missing" >&2; exit 7; }
"${SCRIPT}" --root "${SCAN_ROOT}" --map-file "${TMP_DIR}/map.json" --patch-plan "${TMP_DIR}/patch-plan-safe.md" --out-dir "${TMP_DIR}" --wave W0 --mode apply --approval-file "${APPROVAL}" >"${APPLY_OUT}"
APPLY_REPORT="$(sed -n 's/^run_report=//p' "${APPLY_OUT}" | tail -n1)"
[[ -f "${APPLY_REPORT}" ]] || { echo "FAIL: apply report missing" >&2; exit 8; }
grep -q 'Mode: apply' "${APPLY_REPORT}" || { echo "FAIL: apply mode missing" >&2; exit 9; }
grep -q 'Total applied literal replacements (exact-case): 3' "${APPLY_REPORT}" || { echo "FAIL: apply count" >&2; cat "${APPLY_REPORT}" >&2; exit 10; }
grep -q 'Patch-plan SHA-256:' "${APPLY_REPORT}" || { echo "FAIL: patch-plan sha missing" >&2; cat "${APPLY_REPORT}" >&2; exit 10; }
grep -q '| `config/a.xml` | OK | 3 | 3 | yes |' "${APPLY_REPORT}" || { echo "FAIL: apply file row" >&2; cat "${APPLY_REPORT}" >&2; exit 11; }
grep -q '^abs abs abs$' "${SCAN_ROOT}/config/a.xml" || { echo "FAIL: apply did not rewrite file" >&2; cat "${SCAN_ROOT}/config/a.xml" >&2; exit 12; }

cat > "${TMP_DIR}/patch-plan-blocked.md" <<'MD'
# Phase 9 ABS Rename Patch-Plan Export (W0)

## File Plan: config/a.xml

| Legacy | Replacement | Hits In File |
|---|---|---:|
| `mq` | `abs` / `ABS` | 1 |
MD

if "${SCRIPT}" --root "${SCAN_ROOT}" --map-file "${TMP_DIR}/map.json" --patch-plan "${TMP_DIR}/patch-plan-blocked.md" --out-dir "${TMP_DIR}" --wave W0 --mode dry-run >/dev/null 2>"${TMP_DIR}/blocked.err"; then
  echo "FAIL: review-only mapping should block" >&2
  exit 13
fi
grep -q 'review-only mappings present in patch plan' "${TMP_DIR}/blocked.err" || { echo "FAIL: missing review-only block error" >&2; cat "${TMP_DIR}/blocked.err" >&2; exit 14; }

echo "PASS: phase9 abs w0 text replace smoke"
