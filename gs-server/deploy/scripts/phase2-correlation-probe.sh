#!/usr/bin/env bash
set -euo pipefail

BASE_URL="http://127.0.0.1:18080"
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase2/correlation-probes"
TIMEOUT_SEC=20
WAIT_READY_SEC=30

TRACE_ID="probe-trace-001"
SESSION_ID="probe-session-001"
BANK_ID="6274"
GAME_ID="838"
OPERATION_ID="probe-op-001"
CONFIG_VERSION="cfg-probe-001"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --base-url) BASE_URL="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    --trace-id) TRACE_ID="$2"; shift 2 ;;
    --session-id) SESSION_ID="$2"; shift 2 ;;
    --bank-id) BANK_ID="$2"; shift 2 ;;
    --game-id) GAME_ID="$2"; shift 2 ;;
    --operation-id) OPERATION_ID="$2"; shift 2 ;;
    --config-version) CONFIG_VERSION="$2"; shift 2 ;;
    --timeout-sec) TIMEOUT_SEC="$2"; shift 2 ;;
    --wait-ready-sec) WAIT_READY_SEC="$2"; shift 2 ;;
    *)
      echo "Unknown option: $1" >&2
      exit 1
      ;;
  esac
done

TIMESTAMP="$(date -u +%Y%m%d-%H%M%S)"
mkdir -p "$OUT_DIR"
HEADERS_FILE="$OUT_DIR/correlation-headers-${TIMESTAMP}.txt"
BODY_FILE="$OUT_DIR/correlation-body-${TIMESTAMP}.txt"
REPORT_FILE="$OUT_DIR/correlation-probe-${TIMESTAMP}.md"

TARGET_URL="${BASE_URL}/cwstartgamev2.do?bankId=${BANK_ID}&gameId=${GAME_ID}&mode=real&token=invalid_probe_token&lang=en"

wait_for_ready() {
  local attempts=$((WAIT_READY_SEC * 2))
  local i=0
  while [[ $i -lt $attempts ]]; do
    local code
    set +e
    code=$(curl -sS -m "${TIMEOUT_SEC}" -o /dev/null -w "%{http_code}" "${BASE_URL}/")
    local rc=$?
    set -e
    if [[ $rc -eq 0 && "$code" != "502" && "$code" != "000" ]]; then
      return 0
    fi
    sleep 0.5
    i=$((i + 1))
  done
  return 1
}

READY_STATUS="READY"
if ! wait_for_ready; then
  READY_STATUS="NOT_READY_TIMEOUT"
fi

HTTP_CODE=$(curl -sS -m "${TIMEOUT_SEC}" \
  -H "X-Trace-Id: ${TRACE_ID}" \
  -H "X-Session-Id: ${SESSION_ID}" \
  -H "X-Bank-Id: ${BANK_ID}" \
  -H "X-Game-Id: ${GAME_ID}" \
  -H "X-Operation-Id: ${OPERATION_ID}" \
  -H "X-Config-Version: ${CONFIG_VERSION}" \
  -D "${HEADERS_FILE}" \
  -o "${BODY_FILE}" \
  -w "%{http_code}" \
  "${TARGET_URL}")

expect_header() {
  local header_name="$1"
  local expected="$2"
  if rg -qi "^${header_name}:\\s*${expected}\\s*$" "${HEADERS_FILE}"; then
    echo "PASS"
  else
    echo "FAIL"
  fi
}

TRACE_STATUS=$(expect_header "X-Trace-Id" "${TRACE_ID}")
SESSION_STATUS=$(expect_header "X-Session-Id" "${SESSION_ID}")
OP_STATUS=$(expect_header "X-Operation-Id" "${OPERATION_ID}")
CFG_STATUS=$(expect_header "X-Config-Version" "${CONFIG_VERSION}")

cat > "${REPORT_FILE}" <<REPORT
# Phase 2 Correlation Probe

- Timestamp (UTC): $(date -u +"%Y-%m-%d %H:%M:%S")
- Base URL: ${BASE_URL}
- Target URL: ${TARGET_URL}
- HTTP: ${HTTP_CODE}

## Request headers sent
- X-Trace-Id: ${TRACE_ID}
- X-Session-Id: ${SESSION_ID}
- X-Bank-Id: ${BANK_ID}
- X-Game-Id: ${GAME_ID}
- X-Operation-Id: ${OPERATION_ID}
- X-Config-Version: ${CONFIG_VERSION}

## Pre-check
- Static readiness: ${READY_STATUS}

## Echo validation
| Header | Status |
|---|---|
| X-Trace-Id | ${TRACE_STATUS} |
| X-Session-Id | ${SESSION_STATUS} |
| X-Operation-Id | ${OP_STATUS} |
| X-Config-Version | ${CFG_STATUS} |

## Artifacts
- Response headers: \`${HEADERS_FILE}\`
- Response body: \`${BODY_FILE}\`
REPORT

echo "Correlation probe report: ${REPORT_FILE}"
