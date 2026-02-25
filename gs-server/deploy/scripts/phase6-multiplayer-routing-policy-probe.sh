#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./lib/cluster-hosts.sh
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

BANK_ID="6275"
GAME_ID="838"
SESSION_ID=""
TRANSPORT="host"
MULTIPLAYER_BASE_URL="$(cluster_hosts_http_url MULTIPLAYER_SERVICE_EXTERNAL_HOST MULTIPLAYER_SERVICE_EXTERNAL_PORT 127.0.0.1 18079)"
MULTIPLAYER_CONTAINER="refactor-multiplayer-service-1"
EXPECT_BANK_MP_ENABLED="false"
EXPECT_NON_MP_REASON="non_multiplayer_game"
EXPECT_NON_MP_ROUTE="false"
EXPECT_MP_REASON="bank_multiplayer_disabled"
EXPECT_MP_ROUTE="false"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID                    Default: ${BANK_ID}
  --game-id ID                    Default: ${GAME_ID}
  --session-id ID                 Optional (auto-generated if empty)
  --transport MODE                host|docker (default: ${TRANSPORT})
  --multiplayer-base-url URL      Default: ${MULTIPLAYER_BASE_URL}
  --multiplayer-container NAME    Default: ${MULTIPLAYER_CONTAINER}
  --expect-bank-mp-enabled BOOL   true|false (default: ${EXPECT_BANK_MP_ENABLED})
  --expect-non-mp-reason VALUE    Default: ${EXPECT_NON_MP_REASON}
  --expect-non-mp-route BOOL      true|false (default: ${EXPECT_NON_MP_ROUTE})
  --expect-mp-reason VALUE        Default: ${EXPECT_MP_REASON}
  --expect-mp-route BOOL          true|false (default: ${EXPECT_MP_ROUTE})
  -h, --help                      Show this help
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
    --transport)
      TRANSPORT="$2"; shift 2 ;;
    --multiplayer-base-url)
      MULTIPLAYER_BASE_URL="$2"; shift 2 ;;
    --multiplayer-container)
      MULTIPLAYER_CONTAINER="$2"; shift 2 ;;
    --expect-bank-mp-enabled)
      EXPECT_BANK_MP_ENABLED="$2"; shift 2 ;;
    --expect-non-mp-reason)
      EXPECT_NON_MP_REASON="$2"; shift 2 ;;
    --expect-non-mp-route)
      EXPECT_NON_MP_ROUTE="$2"; shift 2 ;;
    --expect-mp-reason)
      EXPECT_MP_REASON="$2"; shift 2 ;;
    --expect-mp-route)
      EXPECT_MP_ROUTE="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

if [[ -z "${SESSION_ID}" ]]; then
  SESSION_ID="mp-policy-session-$(date +%s)"
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
for expected_bool in "${EXPECT_BANK_MP_ENABLED}" "${EXPECT_NON_MP_ROUTE}" "${EXPECT_MP_ROUTE}"; do
  if [[ "${expected_bool}" != "true" && "${expected_bool}" != "false" ]]; then
    echo "Invalid boolean expectation: ${expected_bool}. Use true|false." >&2
    exit 1
  fi
done
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

assert_contains() {
  local file="$1"
  local pattern="$2"
  local msg="$3"
  if ! grep -q "$pattern" "$file"; then
    echo "FAIL: ${msg}" >&2
    echo "Payload: $(cat "$file")" >&2
    exit 2
  fi
}

tmp_dir="$(mktemp -d)"
non_mp_file="${tmp_dir}/non_mp.json"
mp_file="${tmp_dir}/mp.json"
trap 'rm -rf "${tmp_dir}"' EXIT

api_get_to_file "/api/v1/multiplayer/routing/decision?bankId=${BANK_ID}&gameId=${GAME_ID}&sessionId=${SESSION_ID}&isMultiplayer=false" "${non_mp_file}"
assert_contains "${non_mp_file}" "\"routeToMultiplayerService\":${EXPECT_NON_MP_ROUTE}" 'non-multiplayer route expectation mismatch'
assert_contains "${non_mp_file}" "\"reason\":\"${EXPECT_NON_MP_REASON}\"" "non-multiplayer path reason mismatch"
assert_contains "${non_mp_file}" '"requestedMultiplayer":false' 'non-multiplayer path requestedMultiplayer flag mismatch'
assert_contains "${non_mp_file}" "\"bankAllowsMultiplayer\":${EXPECT_BANK_MP_ENABLED}" 'bankAllowsMultiplayer mismatch on non-multiplayer request'

api_get_to_file "/api/v1/multiplayer/routing/decision?bankId=${BANK_ID}&gameId=${GAME_ID}&sessionId=${SESSION_ID}&isMultiplayer=true" "${mp_file}"
assert_contains "${mp_file}" "\"routeToMultiplayerService\":${EXPECT_MP_ROUTE}" 'multiplayer route expectation mismatch'
assert_contains "${mp_file}" "\"reason\":\"${EXPECT_MP_REASON}\"" 'multiplayer path reason mismatch'
assert_contains "${mp_file}" '"requestedMultiplayer":true' 'multiplayer path requestedMultiplayer flag mismatch'
assert_contains "${mp_file}" "\"bankAllowsMultiplayer\":${EXPECT_BANK_MP_ENABLED}" 'bankAllowsMultiplayer mismatch on multiplayer request'

echo "Multiplayer routing policy probe summary"
echo "  bankId: ${BANK_ID}"
echo "  gameId: ${GAME_ID}"
echo "  sessionId: ${SESSION_ID}"
echo "  non-mp: route=${EXPECT_NON_MP_ROUTE} reason=${EXPECT_NON_MP_REASON}"
echo "  mp:     route=${EXPECT_MP_ROUTE} reason=${EXPECT_MP_REASON}"
echo "PASS: multiplayer routing policy (isMultiplayer bypass + bank capability gate) verified."
