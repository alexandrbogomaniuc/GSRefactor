#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
SCANNER="${ROOT}/gs-server/deploy/scripts/phase9-abs-rename-candidate-scan.sh"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  cat <<USAGE
Usage: $(basename "$0")

Smoke-tests Phase 9 ABS rename candidate scanner and review-only blocking.
USAGE
  exit 0
fi

SCAN_ROOT="${TMP_DIR}/gs-server"
mkdir -p "${SCAN_ROOT}/src" "${TMP_DIR}/out"
mkdir -p "${SCAN_ROOT}/config" "${SCAN_ROOT}/src/main/java/demo"
cat > "${SCAN_ROOT}/src/a.txt" <<'TXT'
MaxQuest launch label
TXT
cat > "${SCAN_ROOT}/src/b.txt" <<'TXT'
mq_token_should_be_reviewed
TXT
cat > "${SCAN_ROOT}/src/c.txt" <<'TXT'
package com.dgphoenix.demo;
TXT
cat > "${SCAN_ROOT}/config/safe.xml" <<'TXT'
maxquest config label
TXT
cat > "${SCAN_ROOT}/src/main/java/demo/Unsafe.java" <<'TXT'
// maxquest code path should be filtered in safe-target mode
TXT

report_out="${TMP_DIR}/scan.out"
"${SCANNER}" --root "${SCAN_ROOT}" --wave W0 --out-dir "${TMP_DIR}/out" >"${report_out}"
report_file="$(sed -n 's/^report=//p' "${report_out}" | tail -n1)"
[[ -f "${report_file}" ]] || { echo "FAIL: report not created" >&2; exit 2; }

grep -q '| `maxquest` |' "${report_file}" || { echo "FAIL: maxquest row missing" >&2; exit 3; }
grep -q '| `mq` |' "${report_file}" || { echo "FAIL: mq row missing" >&2; exit 4; }
grep -q 'REVIEW_ONLY_HIT' "${report_file}" || { echo "FAIL: review-only disposition missing" >&2; exit 5; }

"${SCANNER}" --root "${SCAN_ROOT}" --wave W0 --safe-targets-only true --out-dir "${TMP_DIR}/out" >"${TMP_DIR}/safe.out"
safe_report="$(sed -n 's/^report=//p' "${TMP_DIR}/safe.out" | tail -n1)"
[[ -f "${safe_report}" ]] || { echo "FAIL: safe-target report not created" >&2; exit 51; }
grep -q 'Safe targets only: true' "${safe_report}" || { echo "FAIL: safe-target flag missing in report" >&2; exit 52; }
grep -q '/config/safe.xml' "${safe_report}" || { echo "FAIL: safe xml candidate missing" >&2; exit 53; }
if grep -q '/src/main/java/demo/Unsafe.java' "${safe_report}"; then
  echo "FAIL: unsafe java path should be filtered in safe-target mode" >&2
  exit 54
fi

set +e
"${SCANNER}" --root "${SCAN_ROOT}" --wave W0 --enforce-auto-apply true --out-dir "${TMP_DIR}/out" >"${TMP_DIR}/blocked.out" 2>&1
blocked_rc=$?
set -e
[[ ${blocked_rc} -eq 2 ]] || { echo "FAIL: expected blocked rc=2, got ${blocked_rc}" >&2; cat "${TMP_DIR}/blocked.out" >&2; exit 6; }
grep -q 'auto_apply_status=BLOCKED' "${TMP_DIR}/blocked.out" || { echo "FAIL: blocked status marker missing" >&2; exit 7; }
grep -q 'BLOCKED_REVIEW_ONLY:mq' "${TMP_DIR}/blocked.out" || { echo "FAIL: mq block reason missing" >&2; exit 8; }

echo "PASS: phase9 abs candidate scanner smoke"
