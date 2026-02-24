#!/usr/bin/env bash
set -euo pipefail
ROOT="/Users/alexb/Documents/Dev/Dev_new"
PLAN="${ROOT}/gs-server/deploy/scripts/phase9-abs-rename-execution-plan.sh"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  cat <<USAGE
Usage: $(basename "$0")

Smoke-tests Phase 9 execution plan generation from a candidate scan report.
USAGE
  exit 0
fi

cat > "${TMP_DIR}/map.json" <<'JSON'
{
  "type":"phase9-abs-compatibility-map","version":1,"scope":"gs-server",
  "waves":[{"id":"W0","label":"w0","allowsAutomaticApply":true,"pathProfile":"w0_safe_targets"}],
  "pathProfiles":{"w0_safe_targets":{"mode":"all"}},
  "mappings":[
    {"legacy":"maxquest","replacement":"abs","replacementUpper":"ABS","category":"brand","defaultWave":"W0","reviewOnly":false},
    {"legacy":"mq","replacement":"abs","replacementUpper":"ABS","category":"token","defaultWave":"W0","reviewOnly":true}
  ]
}
JSON

cat > "${TMP_DIR}/scan.md" <<'MD'
# Phase 9 ABS Rename Candidate Scan (test)

- Wave filter: W0
- Effective path profile: w0_safe_targets

| Legacy | Category | Wave | ReviewOnly | Line Hits | Files | Disposition |
|---|---|---|---:|---:|---:|---|
| `maxquest` | brand | W0 | no | 10 | 2 | AUTO_CANDIDATE |
| `mq` | token | W0 | yes | 40 | 4 | REVIEW_ONLY_HIT |

## Top Files: maxquest (AUTO_CANDIDATE)

| File | Hits |
|---|---:|
| `/tmp/a.xml` | 6 |
| `/tmp/b.jsp` | 4 |

## Top Files: mq (REVIEW_ONLY_HIT)

| File | Hits |
|---|---:|
| `/tmp/c.jsp` | 40 |
MD

out="${TMP_DIR}/plan.out"
"${PLAN}" --map-file "${TMP_DIR}/map.json" --scan-report "${TMP_DIR}/scan.md" --out-dir "${TMP_DIR}" --wave W0 >"${out}"
plan_report="$(sed -n 's/^plan_report=//p' "${out}" | tail -n1)"
[[ -f "${plan_report}" ]] || { echo "FAIL: plan report missing" >&2; exit 2; }
grep -q 'Auto-candidate mappings: 1' "${plan_report}" || { echo "FAIL: auto-candidate count missing" >&2; exit 3; }
grep -q '| `maxquest` | `abs` / `ABS` | 10 | 2 |' "${plan_report}" || { echo "FAIL: maxquest plan row missing" >&2; exit 4; }
grep -q '| `mq` | 40 | 4 | REVIEW_ONLY_HIT |' "${plan_report}" || { echo "FAIL: review-only exclusion row missing" >&2; exit 5; }
grep -q 'review-and-replace-nonruntime-string' "${plan_report}" || { echo "FAIL: suggested action missing" >&2; exit 6; }

echo "PASS: phase9 abs execution plan smoke"
