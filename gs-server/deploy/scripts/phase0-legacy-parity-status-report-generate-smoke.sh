#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

TMP="$(mktemp -d)"
trap 'rm -rf "${TMP}"' EXIT
OUT="${TMP}/out"; mkdir -p "${OUT}"

P0="${TMP}/p0.md"; F="${TMP}/f.md"; P56="${TMP}/p56.md"; MB="${TMP}/mb.md"; MS="${TMP}/ms.md"; MP="${TMP}/mp.md"; VR="${TMP}/vr.md"
cat > "${P0}" <<'EOF'
/frbcheck.do
P0-FRB-01
### GF-5 FRB
phase0-reconnect-facade-fallback
phase0-parity-harness.sh
phase0/parity-execution/
EOF
cat > "${F}" <<'EOF'
FRB issue notes
MP reconnect notes
EOF
cat > "${P56}" <<'EOF'
Bonus/FRB
TESTED_NO_GO_RUNTIME_BLOCKED
EOF
cat > "${MB}" <<'EOF'
isMultiplayer=false
skip multiplayer code paths entirely
EOF
cat > "${MS}" <<'EOF'
Legacy multiplayer launch/session behavior remains authoritative
shadow-only and fail-open
EOF
cat > "${MP}" <<'EOF'
isMultiplayer=false
bank_multiplayer_disabled
EOF
echo -e "# VR\n- pass: 72\n- fail: 0\n- skip: 0" > "${VR}"

SCRIPT="${REPO_ROOT}/gs-server/deploy/scripts/phase0-legacy-parity-status-report-generate.sh"
run="$(${SCRIPT} --phase0-doc "${P0}" --launch-forensics-doc "${F}" --phase5-6-closure-doc "${P56}" --mp-boundary-doc "${MB}" --mp-shadow-doc "${MS}" --mp-policy-doc "${MP}" --verify-report "${VR}" --out-dir "${OUT}")"
echo "${run}" | grep -q 'overall_status=TESTED_GUARDED_LEGACY_PARITY_BASELINE_COMPLETE'
echo "${run}" | grep -q 'frb_bonus_parity_status=TESTED_PARITY_SUITE_STABILIZED'
echo "${run}" | grep -q 'multiplayer_legacy_compat_status=TESTED_COMPATIBILITY_GUARDED_DEFERRED_RUNTIME_VALIDATION'
echo "PHASE0_LEGACY_PARITY_STATUS_REPORT_SMOKE_OK"
