#!/usr/bin/env bash
set -euo pipefail

TMP="$(mktemp -d)"
trap 'rm -rf "${TMP}"' EXIT
OUT="${TMP}/out"
mkdir -p "${OUT}"

mk_runtime() {
  local file="$1" readiness="$2" probe1="$3" probe2="${4:-}"
  {
    echo "# Runtime Evidence"
    echo "- transport: host"
    echo "- readiness_check: ${readiness}"
    echo "- primary_probe: ${probe1}"
    if [[ -n "${probe2}" ]]; then
      echo "- secondary_probe: ${probe2}"
    fi
    if [[ "${readiness}" != "PASS" ]]; then
      echo
      echo "NOT_READY: fix failed checks before running canary probe"
      echo "FAIL docker socket not accessible"
    fi
  } > "${file}"
}

VERIFY_OK="${TMP}/verify-ok.md"
VERIFY_FAIL="${TMP}/verify-fail.md"
echo -e "# VR\n- pass: 68\n- fail: 0\n- skip: 0" > "${VERIFY_OK}"
echo -e "# VR\n- pass: 67\n- fail: 1\n- skip: 0" > "${VERIFY_FAIL}"

G="${TMP}/g.md"; W="${TMP}/w.md"; B="${TMP}/b.md"; H="${TMP}/h.md"; M="${TMP}/m.md"
mk_runtime "${G}" "FAIL" "SKIPPED"
mk_runtime "${W}" "FAIL" "SKIPPED"
mk_runtime "${B}" "FAIL" "SKIPPED"
mk_runtime "${H}" "FAIL" "SKIPPED"
mk_runtime "${M}" "FAIL" "SKIPPED" "SKIPPED"

SCRIPT="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-service-extraction-status-report-generate.sh"

run1="$(${SCRIPT} --verify-report "${VERIFY_OK}" --gameplay-evidence "${G}" --wallet-evidence "${W}" --bonus-evidence "${B}" --history-evidence "${H}" --mp-evidence "${M}" --out-dir "${OUT}")"
echo "${run1}" | grep -q 'overall_status=TESTED_NO_GO_RUNTIME_BLOCKED'
r1="$(echo "${run1}" | awk -F= '/^report=/{print $2}')"
grep -q 'decision: No-Go (service runtime parity/canary execution blocked/unavailable; tooling/shadow/canary coverage implemented)' "${r1}"

run2="$(${SCRIPT} --verify-report "${VERIFY_FAIL}" --gameplay-evidence "${G}" --wallet-evidence "${W}" --bonus-evidence "${B}" --history-evidence "${H}" --mp-evidence "${M}" --out-dir "${OUT}")"
echo "${run2}" | grep -q 'overall_status=NO_GO_VERIFICATION_FAILURE'

echo "PHASE56_SERVICE_STATUS_REPORT_SMOKE_OK"
