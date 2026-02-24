#!/usr/bin/env bash
set -euo pipefail

TMP="$(mktemp -d)"
trap 'rm -rf "${TMP}"' EXIT
OUT="${TMP}/out"
mkdir -p "${OUT}"

TRACE="${TMP}/trace.md"
TAX="${TMP}/taxonomy.md"
CORR="${TMP}/corr.md"
RB="${TMP}/rb.md"
RBS="${TMP}/rbs.md"
DASH="${TMP}/dash.md"
VR_OK="${TMP}/vr-ok.md"
VR_FAIL="${TMP}/vr-fail.md"

cat > "${TRACE}" <<'EOF'
`traceId` `sessionId` `bankId` `gameId` `operationId` `configVersion`
### HTTP
### WebSocket
### Kafka
Missing-field rate alert threshold
>0.1% over 5 minutes
P1 incident
EOF
cat > "${TAX}" <<'EOF'
Canonical Error Envelope
Initial Stable Codes
`validation` `auth` `state` `dependency` `rate_limit` `internal`
EOF
cat > "${CORR}" <<'EOF'
| X-Trace-Id | PASS |
| X-Session-Id | PASS |
| X-Operation-Id | PASS |
| X-Config-Version | PASS |
EOF
cat > "${RB}" <<'EOF'
Phase 4 protocol adapter readiness + evidence-pack
Phase 5 gameplay/Redis readiness + evidence-pack
EOF
cat > "${RBS}" <<'EOF'
runtime status snapshot panel
EOF
cat > "${DASH}" <<'EOF'
Section-level and overall progress bars
Filter modes: all/open/done
Checkbox toggles are local/browser-side temporary overrides
EOF
echo -e "# VR\n- pass: 70\n- fail: 0\n- skip: 0" > "${VR_OK}"
echo -e "# VR\n- pass: 69\n- fail: 1\n- skip: 0" > "${VR_FAIL}"

SCRIPT="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase2-observability-status-report-generate.sh"
run1="$(${SCRIPT} --trace-doc "${TRACE}" --taxonomy-doc "${TAX}" --correlation-probe "${CORR}" --runbook-doc "${RB}" --runbook-status-doc "${RBS}" --dashboard-doc "${DASH}" --verify-report "${VR_OK}" --out-dir "${OUT}")"
echo "${run1}" | grep -q 'overall_status=TESTED_BASELINE_COMPLETE'
r1="$(echo "${run1}" | awk -F= '/^report=/{print $2}')"
grep -q '| trace_correlation_standard | PASS |' "${r1}"

run2="$(${SCRIPT} --trace-doc "${TRACE}" --taxonomy-doc "${TAX}" --correlation-probe "${CORR}" --runbook-doc "${RB}" --runbook-status-doc "${RBS}" --dashboard-doc "${DASH}" --verify-report "${VR_FAIL}" --out-dir "${OUT}")"
echo "${run2}" | grep -q 'overall_status=NO_GO_VERIFICATION_FAILURE'

echo "PHASE2_OBSERVABILITY_STATUS_REPORT_SMOKE_OK"
