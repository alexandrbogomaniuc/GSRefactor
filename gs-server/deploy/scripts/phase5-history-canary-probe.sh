#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/dev/null
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

BANK_ID="6275"
SESSION_ID=""
EVENT_TYPE="round_settle"
TRANSPORT="host"
HISTORY_BASE_URL="$(cluster_hosts_http_url HISTORY_SERVICE_EXTERNAL_HOST HISTORY_SERVICE_EXTERNAL_PORT 127.0.0.1 18077)"
HISTORY_CONTAINER="refactor-history-service-1"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID             Default: ${BANK_ID}
  --session-id ID          Optional (auto-generated if empty)
  --event-type TYPE        Default: ${EVENT_TYPE}
  --transport MODE         host|docker (default: ${TRANSPORT})
  --history-base-url URL   Default: ${HISTORY_BASE_URL}
  --history-container NAME Default: ${HISTORY_CONTAINER}
  -h, --help               Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bank-id)
      BANK_ID="$2"; shift 2 ;;
    --session-id)
      SESSION_ID="$2"; shift 2 ;;
    --event-type)
      EVENT_TYPE="$2"; shift 2 ;;
    --transport)
      TRANSPORT="$2"; shift 2 ;;
    --history-base-url)
      HISTORY_BASE_URL="$2"; shift 2 ;;
    --history-container)
      HISTORY_CONTAINER="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

if [[ -z "${SESSION_ID}" ]]; then
  SESSION_ID="canary-session-$(date +%s)"
fi

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1" >&2
    exit 1
  }
}

require_cmd curl
require_cmd grep

if [[ "${TRANSPORT}" != "host" && "${TRANSPORT}" != "docker" ]]; then
  echo "Invalid --transport: ${TRANSPORT}" >&2
  exit 1
fi

if [[ "${TRANSPORT}" == "docker" ]]; then
  require_cmd docker
fi

api_get_to_file() {
  local path="$1"
  local out_file="$2"
  if [[ "${TRANSPORT}" == "host" ]]; then
    curl -fsS "${HISTORY_BASE_URL}${path}" > "${out_file}"
  else
    docker exec "${HISTORY_CONTAINER}" sh -lc "curl -fsS 'http://127.0.0.1:18077${path}'" > "${out_file}"
  fi
}

api_post_json_to_file() {
  local path="$1"
  local payload="$2"
  local out_file="$3"
  if [[ "${TRANSPORT}" == "host" ]]; then
    curl -fsS -X POST \
      -H "Content-Type: application/json" \
      --data "${payload}" \
      "${HISTORY_BASE_URL}${path}" > "${out_file}"
  else
    docker exec "${HISTORY_CONTAINER}" sh -lc "curl -fsS -X POST -H 'Content-Type: application/json' --data '${payload}' 'http://127.0.0.1:18077${path}'" > "${out_file}"
  fi
}

tmp_dir="$(mktemp -d)"
decision_file="${tmp_dir}/decision.json"
append_file="${tmp_dir}/append.json"
query_file="${tmp_dir}/query.json"
trap 'rm -rf "${tmp_dir}"' EXIT

operation_id="history-${BANK_ID}-${SESSION_ID}-${EVENT_TYPE}"
payload="{\"bankId\":\"${BANK_ID}\",\"sessionId\":\"${SESSION_ID}\",\"operationId\":\"${operation_id}\",\"eventType\":\"${EVENT_TYPE}\",\"payload\":{\"source\":\"phase5-history-canary\"}}"

api_get_to_file "/api/v1/history/routing/decision?bankId=${BANK_ID}" "${decision_file}"
if ! grep -q '"routeToHistoryService":true' "${decision_file}"; then
  echo "FAIL: history-service canary route decision is not enabled for bank ${BANK_ID}" >&2
  echo "Decision payload: $(cat "${decision_file}")" >&2
  exit 2
fi

api_post_json_to_file "/api/v1/history/records" "${payload}" "${append_file}"
if ! grep -q "\"operationId\":\"${operation_id}\"" "${append_file}"; then
  echo "FAIL: appended history record does not contain expected operationId" >&2
  echo "Append payload: $(cat "${append_file}")" >&2
  exit 3
fi

api_get_to_file "/api/v1/history/records?bankId=${BANK_ID}&sessionId=${SESSION_ID}&eventType=${EVENT_TYPE}" "${query_file}"
if ! grep -q "\"operationId\":\"${operation_id}\"" "${query_file}"; then
  echo "FAIL: history query does not include appended operationId" >&2
  echo "Query payload: $(cat "${query_file}")" >&2
  exit 4
fi

echo "History canary probe summary"
echo "  bankId: ${BANK_ID}"
echo "  sessionId: ${SESSION_ID}"
echo "  eventType: ${EVENT_TYPE}"
echo "  decision: routeToHistoryService=true"
echo "PASS: history canary append/query flow verified."
