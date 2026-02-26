#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

TMP="$(mktemp -d)"
trap 'rm -rf "${TMP}"' EXIT
OUT="${TMP}/out"
mkdir -p "${OUT}"

runtime_blocked="${TMP}/runtime-blocked.md"
runtime_go="${TMP}/runtime-go.md"
verify_ok="${TMP}/verify-ok.md"

cat > "${runtime_blocked}" <<'EOF'
# Phase 4 Protocol Runtime Evidence (test)
- bankId: 6275
- transport: docker
- allowMissingRuntime: true
- runtime_readiness: SKIP_DOCKER_API_DENIED
- parity_check: SKIP_RUNTIME_NOT_READY
- wallet_shadow_probe: SKIP_RUNTIME_NOT_READY
- json_security_probe: SKIPPED
EOF

cat > "${runtime_go}" <<'EOF'
# Phase 4 Protocol Runtime Evidence (test)
- bankId: 6275
- transport: host
- allowMissingRuntime: false
- runtime_readiness: PASS
- parity_check: PASS
- wallet_shadow_probe: PASS
- json_security_probe: SKIPPED
EOF

cat > "${verify_ok}" <<'EOF'
# Local Verification
- pass: 66
- fail: 0
- skip: 0
EOF

SCRIPT="${REPO_ROOT}/gs-server/deploy/scripts/phase4-protocol-status-report-generate.sh"

blocked_run="$(${SCRIPT} --runtime-evidence "${runtime_blocked}" --verify-report "${verify_ok}" --out-dir "${OUT}")"
echo "${blocked_run}" | grep -q 'phase4_status=TESTED_NO_GO_RUNTIME_BLOCKED'
blocked_report="$(echo "${blocked_run}" | awk -F= '/^report=/{print $2}')"
grep -q 'decision: No-Go (runtime adapter execution blocked/unavailable; tooling and logic gates are passing)' "${blocked_report}"

go_run="$(${SCRIPT} --runtime-evidence "${runtime_go}" --verify-report "${verify_ok}" --out-dir "${OUT}")"
echo "${go_run}" | grep -q 'phase4_status=TESTED_GO_RUNTIME_PARITY_READY'
go_report="$(echo "${go_run}" | awk -F= '/^report=/{print $2}')"
grep -q 'decision: Go (runtime parity checks and verification suite passing)' "${go_report}"

echo "PHASE4_STATUS_REPORT_SMOKE_OK"
