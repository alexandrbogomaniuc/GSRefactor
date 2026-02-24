#!/usr/bin/env bash
set -euo pipefail
ROOT="/Users/alexb/Documents/Dev/Dev_new"
SCRIPT="${ROOT}/gs-server/deploy/scripts/phase9-abs-rename-patch-plan-export.sh"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  cat <<USAGE
Usage: $(basename "$0")

Smoke-tests Phase 9 per-file grouped patch-plan export generation.
USAGE
  exit 0
fi

SCAN_ROOT="${TMP_DIR}/gs-server"
mkdir -p "${SCAN_ROOT}/config" "${SCAN_ROOT}/templates"
cat > "${SCAN_ROOT}/config/a.xml" <<'TXT'
maxquest and nucleus
TXT
cat > "${SCAN_ROOT}/templates/b.jsp" <<'TXT'
Betsoft gaming and maxquest
TXT
cat > "${SCAN_ROOT}/templates/c.jsp" <<'TXT'
mq only token here
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

cat > "${TMP_DIR}/scan.md" <<'MD'
# Phase 9 ABS Rename Candidate Scan (test)

- Wave filter: W0
- Effective path profile: w0_safe_targets

| Legacy | Category | Wave | ReviewOnly | Line Hits | Files | Disposition |
|---|---|---|---:|---:|---:|---|
| `maxquest` | brand | W0 | no | 2 | 2 | AUTO_CANDIDATE |
| `nucleus` | brand | W0 | no | 1 | 1 | AUTO_CANDIDATE |
| `mq` | token | W0 | yes | 1 | 1 | REVIEW_ONLY_HIT |

## Top Files: maxquest (AUTO_CANDIDATE)

| File | Hits |
|---|---:|
| `__CONFIG_A__` | 1 |
| `__TEMPLATE_B__` | 1 |

## Top Files: nucleus (AUTO_CANDIDATE)

| File | Hits |
|---|---:|
| `__CONFIG_A__` | 1 |

## Top Files: mq (REVIEW_ONLY_HIT)

| File | Hits |
|---|---:|
| `__TEMPLATE_C__` | 1 |
MD

escaped_config_a="${SCAN_ROOT}/config/a.xml"
escaped_template_b="${SCAN_ROOT}/templates/b.jsp"
escaped_template_c="${SCAN_ROOT}/templates/c.jsp"
escaped_config_a="${escaped_config_a//\//\\/}"
escaped_template_b="${escaped_template_b//\//\\/}"
escaped_template_c="${escaped_template_c//\//\\/}"
sed -i '' \
  -e "s/__CONFIG_A__/${escaped_config_a}/g" \
  -e "s/__TEMPLATE_B__/${escaped_template_b}/g" \
  -e "s/__TEMPLATE_C__/${escaped_template_c}/g" \
  "${TMP_DIR}/scan.md"

OUT="${TMP_DIR}/run.out"
"${SCRIPT}" --root "${SCAN_ROOT}" --map-file "${TMP_DIR}/map.json" --scan-report "${TMP_DIR}/scan.md" --out-dir "${TMP_DIR}" --wave W0 >"${OUT}"
REPORT="$(sed -n 's/^patch_plan_report=//p' "${OUT}" | tail -n1)"
[[ -f "${REPORT}" ]] || { echo "FAIL: report missing" >&2; exit 2; }

grep -q 'Grouped files: 2' "${REPORT}" || { echo "FAIL: grouped file count" >&2; cat "${REPORT}" >&2; exit 3; }
grep -q '| `config/a.xml` | 2 | 2 |' "${REPORT}" || { echo "FAIL: grouped config row" >&2; cat "${REPORT}" >&2; exit 4; }
grep -q '| `templates/b.jsp` | 1 | 1 |' "${REPORT}" || { echo "FAIL: grouped jsp row" >&2; cat "${REPORT}" >&2; exit 5; }
grep -q '| `mq` | 1 | 1 | REVIEW_ONLY_HIT |' "${REPORT}" || { echo "FAIL: review-only exclusion missing" >&2; cat "${REPORT}" >&2; exit 6; }
grep -q '## File Plan: config/a.xml' "${REPORT}" || { echo "FAIL: file plan section missing" >&2; exit 7; }
grep -q '\[maxquest\]' "${REPORT}" || { echo "FAIL: maxquest snippet missing" >&2; cat "${REPORT}" >&2; exit 8; }
grep -q '\[nucleus\]' "${REPORT}" || { echo "FAIL: nucleus snippet missing" >&2; cat "${REPORT}" >&2; exit 9; }
if grep -q 'templates/c.jsp' "${REPORT}"; then
  echo "FAIL: review-only file leaked into patch plan" >&2
  cat "${REPORT}" >&2
  exit 10
fi

echo "PASS: phase9 abs patch-plan export smoke"
