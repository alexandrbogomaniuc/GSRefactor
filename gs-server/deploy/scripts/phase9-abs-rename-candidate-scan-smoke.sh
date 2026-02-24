#!/usr/bin/env bash
set -euo pipefail

ROOT="/Users/alexb/Documents/Dev/Dev_new"
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
cat > "${SCAN_ROOT}/src/a.txt" <<'TXT'
MaxQuest launch label
TXT
cat > "${SCAN_ROOT}/src/b.txt" <<'TXT'
mq_token_should_be_reviewed
TXT
cat > "${SCAN_ROOT}/src/c.txt" <<'TXT'
package com.dgphoenix.demo;
TXT

report_out="${TMP_DIR}/scan.out"
"${SCANNER}" --root "${SCAN_ROOT}" --wave W0 --out-dir "${TMP_DIR}/out" >"${report_out}"
report_file="$(sed -n 's/^report=//p' "${report_out}" | tail -n1)"
[[ -f "${report_file}" ]] || { echo "FAIL: report not created" >&2; exit 2; }

grep -q '| `maxquest` |' "${report_file}" || { echo "FAIL: maxquest row missing" >&2; exit 3; }
grep -q '| `mq` |' "${report_file}" || { echo "FAIL: mq row missing" >&2; exit 4; }
grep -q 'REVIEW_ONLY_HIT' "${report_file}" || { echo "FAIL: review-only disposition missing" >&2; exit 5; }

set +e
"${SCANNER}" --root "${SCAN_ROOT}" --wave W0 --enforce-auto-apply true --out-dir "${TMP_DIR}/out" >"${TMP_DIR}/blocked.out" 2>&1
blocked_rc=$?
set -e
[[ ${blocked_rc} -eq 2 ]] || { echo "FAIL: expected blocked rc=2, got ${blocked_rc}" >&2; cat "${TMP_DIR}/blocked.out" >&2; exit 6; }
grep -q 'auto_apply_status=BLOCKED' "${TMP_DIR}/blocked.out" || { echo "FAIL: blocked status marker missing" >&2; exit 7; }
grep -q 'BLOCKED_REVIEW_ONLY:mq' "${TMP_DIR}/blocked.out" || { echo "FAIL: mq block reason missing" >&2; exit 8; }

echo "PASS: phase9 abs candidate scanner smoke"
