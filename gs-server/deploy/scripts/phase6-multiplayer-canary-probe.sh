#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/dev/null
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

BANK_ID="6275"
GAME_ID="838"
SESSION_ID=""
PLAYER_ID="canary-player"
TRANSPORT="host"
MULTIPLAYER_BASE_URL="$(cluster_hosts_http_url MULTIPLAYER_SERVICE_EXTERNAL_HOST MULTIPLAYER_SERVICE_EXTERNAL_PORT 127.0.0.1 18079)"
MULTIPLAYER_CONTAINER="refactor-multiplayer-service-1"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID                 Default: ${BANK_ID}
  --game-id ID                 Default: ${GAME_ID}
  --session-id ID              Optional (auto-generated if empty)
  --player-id ID               Default: ${PLAYER_ID}
  --transport MODE             host|docker (default: ${TRANSPORT})
  --multiplayer-base-url URL   Default: ${MULTIPLAYER_BASE_URL}
  --multiplayer-container NAME Default: ${MULTIPLAYER_CONTAINER}
  -h, --help                   Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bank-id)
      BANK_ID="$2"; shift 2 ;;
    --game-id)
      GAME_ID="$2"; shift 2 ;;
    --session-id)
      SESSION_ID="$2"; shift 2 ;;
    --player-id)
      PLAYER_ID="$2"; shift 2 ;;
    --transport)
      TRANSPORT="$2"; shift 2 ;;
    --multiplayer-base-url)
      MULTIPLAYER_BASE_URL="$2"; shift 2 ;;
    --multiplayer-container)
      MULTIPLAYER_CONTAINER="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

if [[ -z "${SESSION_ID}" ]]; then
  SESSION_ID="mp-canary-session-$(date +%s)"
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
    curl -fsS "${MULTIPLAYER_BASE_URL}${path}" > "${out_file}"
  else
    docker exec "${MULTIPLAYER_CONTAINER}" sh -lc "curl -fsS 'http://127.0.0.1:18079${path}'" > "${out_file}"
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
      "${MULTIPLAYER_BASE_URL}${path}" > "${out_file}"
  else
    docker exec "${MULTIPLAYER_CONTAINER}" sh -lc "curl -fsS -X POST -H 'Content-Type: application/json' --data '${payload}' 'http://127.0.0.1:18079${path}'" > "${out_file}"
  fi
}

tmp_dir="$(mktemp -d)"
decision_file="${tmp_dir}/decision.json"
sync_file="${tmp_dir}/sync.json"
query_file="${tmp_dir}/query.json"
trap 'rm -rf "${tmp_dir}"' EXIT

api_get_to_file "/api/v1/multiplayer/routing/decision?bankId=${BANK_ID}&gameId=${GAME_ID}&sessionId=${SESSION_ID}&isMultiplayer=true" "${decision_file}"
if ! grep -q '"routeToMultiplayerService":true' "${decision_file}"; then
  echo "FAIL: multiplayer-service canary route decision is not enabled for bank ${BANK_ID}" >&2
  echo "Decision payload: $(cat "${decision_file}")" >&2
  exit 2
fi

sync_payload="{\"bankId\":\"${BANK_ID}\",\"sessionId\":\"${SESSION_ID}\",\"playerId\":\"${PLAYER_ID}\",\"status\":\"SYNCED\",\"operationId\":\"sync-${BANK_ID}-${SESSION_ID}\"}"
api_post_json_to_file "/api/v1/multiplayer/session/sync" "${sync_payload}" "${sync_file}"
if ! grep -q '"status":"SYNCED"' "${sync_file}"; then
  echo "FAIL: multiplayer session sync did not return SYNCED status" >&2
  echo "Sync payload: $(cat "${sync_file}")" >&2
  exit 3
fi

api_get_to_file "/api/v1/multiplayer/sessions?bankId=${BANK_ID}&sessionId=${SESSION_ID}" "${query_file}"
if ! grep -q "\"sessionId\":\"${SESSION_ID}\"" "${query_file}"; then
  echo "FAIL: multiplayer query does not include sessionId ${SESSION_ID}" >&2
  echo "Query payload: $(cat "${query_file}")" >&2
  exit 4
fi

echo "Multiplayer canary probe summary"
echo "  bankId: ${BANK_ID}"
echo "  gameId: ${GAME_ID}"
echo "  sessionId: ${SESSION_ID}"
echo "  decision: routeToMultiplayerService=true"
echo "PASS: multiplayer routing+sync flow verified."
