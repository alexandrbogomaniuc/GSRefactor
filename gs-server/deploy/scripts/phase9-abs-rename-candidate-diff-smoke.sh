#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
SCANNER_DIFF="${ROOT}/gs-server/deploy/scripts/phase9-abs-rename-candidate-diff.sh"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  cat <<USAGE
Usage: $(basename "$0")

Smoke-tests Phase 9 candidate diff report generation (full vs wave profile).
USAGE
  exit 0
fi

SCAN_ROOT="${TMP_DIR}/gs-server"
mkdir -p "${SCAN_ROOT}/config" "${SCAN_ROOT}/src/main/java/demo"
cat > "${SCAN_ROOT}/config/a.xml" <<'TXT'
maxquest
mq
TXT
cat > "${SCAN_ROOT}/src/main/java/demo/A.java" <<'TXT'
maxquest
mq
TXT

cat > "${TMP_DIR}/map.json" <<'JSON'
{
  "type": "phase9-abs-compatibility-map",
  "version": 1,
  "scope": "gs-server",
  "waves": [{"id":"W0","label":"w0","allowsAutomaticApply":true,"pathProfile":"w0_safe_targets"}],
  "pathProfiles": {
    "full_scan": {"mode":"all"},
    "w0_safe_targets": {"mode":"extension_and_path_filters","includeExtensions":["xml"],"excludePathContains":["/src/main/java/"]}
  },
  "mappings": [
    {"legacy":"maxquest","replacement":"abs","replacementUpper":"ABS","category":"brand","defaultWave":"W0","reviewOnly":false},
    {"legacy":"mq","replacement":"abs","replacementUpper":"ABS","category":"token","defaultWave":"W0","reviewOnly":true}
  ]
}
JSON

out="${TMP_DIR}/diff.out"
"${SCANNER_DIFF}" --root "${SCAN_ROOT}" --map-file "${TMP_DIR}/map.json" --out-dir "${TMP_DIR}" --wave W0 >"${out}"
diff_report="$(sed -n 's/^diff_report=//p' "${out}" | tail -n1)"
[[ -f "${diff_report}" ]] || { echo "FAIL: diff report missing" >&2; exit 2; }
grep -q 'Profile: w0_safe_targets' "${diff_report}" || { echo "FAIL: profile line missing" >&2; exit 3; }
grep -q '| `maxquest` | 2 | 1 | -1 |' "${diff_report}" || { echo "FAIL: maxquest diff row incorrect" >&2; cat "${diff_report}" >&2; exit 4; }
grep -q '| `mq` | 2 | 1 | -1 |' "${diff_report}" || { echo "FAIL: mq diff row incorrect" >&2; cat "${diff_report}" >&2; exit 5; }

echo "PASS: phase9 abs candidate diff smoke"
