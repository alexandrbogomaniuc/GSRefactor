#!/usr/bin/env bash
set -euo pipefail

SCRIPT="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/security-hardening-status-report-generate.sh"
TMP="$(mktemp -d)"
trap 'rm -rf "${TMP}"' EXIT
VR="${TMP}/vr.md"
echo -e "# VR\n- pass: 72\n- fail: 0\n- skip: 0" > "${VR}"
run="$(${SCRIPT} --verify-report "${VR}" --out-dir "${TMP}")"
echo "${run}" | grep -Eq 'overall_status=(TESTED_NO_GO_DEPENDENCY_LOCK_AUDIT_PENDING|TESTED_SECURITY_HARDENING_COMPLETE)'
echo "SECURITY_HARDENING_STATUS_REPORT_SMOKE_OK"
