#!/usr/bin/env bash
set -euo pipefail

TMP="$(mktemp -d)"
trap 'rm -rf "${TMP}"' EXIT
OUT="${TMP}/out"; mkdir -p "${OUT}"

CHECKLIST="${TMP}/checklist.json"
VR="${TMP}/vr.md"
P4="${TMP}/p4.md"
P56="${TMP}/p56.md"
P7="${TMP}/p7.md"
SEC="${TMP}/sec.md"
LEG="${TMP}/leg.md"

cat > "${CHECKLIST}" <<'EOF'
{"sections":[{"id":"s","items":[{"id":"a","status":"done"},{"id":"b","status":"done"}]}]}
EOF
cat > "${VR}" <<'EOF'
# VR
- pass: 76
- fail: 0
- skip: 0
EOF
echo "- phase4_status: TESTED_NO_GO_RUNTIME_BLOCKED" > "${P4}"
echo "- overall_status: TESTED_NO_GO_RUNTIME_BLOCKED" > "${P56}"
echo "No-Go" > "${P7}"
echo "- overall_status: TESTED_NO_GO_DEPENDENCY_LOCK_AUDIT_PENDING" > "${SEC}"
echo "- overall_status: TESTED_GUARDED_LEGACY_PARITY_BASELINE_COMPLETE" > "${LEG}"

SCRIPT="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/program-deploy-readiness-status-report.sh"
run="$(bash "${SCRIPT}" --checklist "${CHECKLIST}" --verify-report "${VR}" --phase4-report "${P4}" --phase56-report "${P56}" --phase7-doc "${P7}" --security-report "${SEC}" --legacy-report "${LEG}" --out-dir "${OUT}")"
echo "${run}" | grep -q 'overall_status=NO_GO_CUTOVER_PENDING_VALIDATION'
echo "PROGRAM_DEPLOY_READINESS_STATUS_SMOKE_OK"
