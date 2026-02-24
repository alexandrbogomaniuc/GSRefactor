#!/usr/bin/env bash
set -euo pipefail

OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/validation/legacy-mixed-topology"
DRY_RUN="false"
REFACTOR_GS_BASE_URL="http://127.0.0.1:18081"
LEGACY_MP_BASE_URL="http://127.0.0.1:8088"
LEGACY_CLIENT_BASE_URL="http://127.0.0.1:8090"
BANK_ID="6275"
GAME_ID="838"
TIMEOUT_SEC="5"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Generate/execute the dedicated mixed-topology validation wave:
  refactored GS + legacy MP/client infrastructure.

Options:
  --dry-run B              true|false (default: ${DRY_RUN})
  --refactor-gs-url URL    Default: ${REFACTOR_GS_BASE_URL}
  --legacy-mp-url URL      Default: ${LEGACY_MP_BASE_URL}
  --legacy-client-url URL  Default: ${LEGACY_CLIENT_BASE_URL}
  --bank-id ID             Default: ${BANK_ID}
  --game-id ID             Default: ${GAME_ID}
  --timeout-sec N          Default: ${TIMEOUT_SEC}
  --out-dir DIR            Default: ${OUT_DIR}
  -h, --help               Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN="$2"; shift 2 ;;
    --refactor-gs-url) REFACTOR_GS_BASE_URL="$2"; shift 2 ;;
    --legacy-mp-url) LEGACY_MP_BASE_URL="$2"; shift 2 ;;
    --legacy-client-url) LEGACY_CLIENT_BASE_URL="$2"; shift 2 ;;
    --bank-id) BANK_ID="$2"; shift 2 ;;
    --game-id) GAME_ID="$2"; shift 2 ;;
    --timeout-sec) TIMEOUT_SEC="$2"; shift 2 ;;
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
TS="$(date -u +%Y%m%d-%H%M%S)"
REPORT="${OUT_DIR}/legacy-mixed-topology-validation-${TS}.md"

probe_url() {
  local url="$1"
  local code
  if code="$(curl -sS -m "${TIMEOUT_SEC}" -o /dev/null -w "%{http_code}" "${url}" 2>/dev/null)"; then
    echo "${code}"
  else
    echo "000"
  fi
}

probe_tcp_from_url() {
  local url="$1"
  local host_port pathless host port
  pathless="${url#http://}"
  pathless="${pathless#https://}"
  host_port="${pathless%%/*}"
  host="${host_port%%:*}"
  port="${host_port##*:}"
  if [[ -z "${host}" || -z "${port}" || "${host}" == "${port}" ]]; then
    echo "closed"
    return 0
  fi
  if command -v nc >/dev/null 2>&1 && nc -z "${host}" "${port}" >/dev/null 2>&1; then
    echo "open"
  else
    echo "closed"
  fi
}

gs_url="${REFACTOR_GS_BASE_URL}/cwstartgamev2.do?bankId=${BANK_ID}&gameId=${GAME_ID}&lang=en&sessionId=legacymix${TS}&login=legacymix${TS}&currency=EUR&mode=real"
gs_code="SKIPPED"
mp_code="SKIPPED"
mp_tcp="SKIPPED"
client_code="SKIPPED"
status="NO_GO_RUNTIME_ENDPOINTS_UNREACHABLE"

if [[ "${DRY_RUN}" == "true" ]]; then
  status="DRY_RUN_READY"
else
  gs_code="$(probe_url "${gs_url}")"
  mp_code="$(probe_url "${LEGACY_MP_BASE_URL}")"
  if [[ "${mp_code}" == "000" ]]; then
    mp_tcp="$(probe_tcp_from_url "${LEGACY_MP_BASE_URL}")"
  else
    mp_tcp="open"
  fi
  client_code="$(probe_url "${LEGACY_CLIENT_BASE_URL}")"
  if [[ "${gs_code}" != "000" && "${client_code}" != "000" && ( "${mp_code}" != "000" || "${mp_tcp}" == "open" ) ]]; then
    status="READY_FOR_MANUAL_FULL_FLOW_EXECUTION"
  fi
fi

{
  echo "# Legacy Mixed-Topology Validation Pack"
  echo
  echo "- refactorGsBaseUrl: ${REFACTOR_GS_BASE_URL}"
  echo "- legacyMpBaseUrl: ${LEGACY_MP_BASE_URL}"
  echo "- legacyClientBaseUrl: ${LEGACY_CLIENT_BASE_URL}"
  echo "- bankId: ${BANK_ID}"
  echo "- gameId: ${GAME_ID}"
  echo "- dryRun: ${DRY_RUN}"
  echo "- status: ${status}"
  echo "- probe_refactor_gs_http: ${gs_code}"
  echo "- probe_legacy_mp_http: ${mp_code}"
  echo "- probe_legacy_mp_tcp: ${mp_tcp}"
  echo "- probe_legacy_client_http: ${client_code}"
  echo
  echo "## Validation Flow Checklist"
  echo
  echo "1. Start game from refactored GS using legacy-compatible launch endpoint (cwstartgamev2.do)."
  echo "2. Verify legacy MP routing/bypass behavior for bank/game combination."
  echo "3. Verify legacy client asset/template load and launch handoff."
  echo "4. Run reconnect scenario after session handoff."
  echo "5. Run FRB scenario (award/check/use/cancel or end-of-flow return path) if enabled for bank/game."
  echo "6. Capture GS/MP/client logs and timestamps for each step."
  echo
  echo "## Notes"
  echo
  echo "- This pack validates environment reachability and provides a repeatable operator checklist."
  echo "- Use live legacy infrastructure endpoints in non-prod first; canary only after parity evidence passes."
} > "${REPORT}"

echo "report=${REPORT}"
echo "status=${status}"
