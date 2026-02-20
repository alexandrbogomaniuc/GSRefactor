#!/usr/bin/env bash
set -euo pipefail

BANK_ID="6275"
GAME_ID="838"
MODE="real"
LANG="en"
TOKEN="test_user_6275"
TRANSPORT="host"
GS_BASE_URL="http://127.0.0.1:18081"
GAMEPLAY_BASE_URL="http://127.0.0.1:18074"
GS_CONTAINER="refactor-gs-1"
GAMEPLAY_CONTAINER="refactor-gameplay-orchestrator-1"
REQUIRE_REDIS_HIT="false"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID               Default: ${BANK_ID}
  --game-id ID               Default: ${GAME_ID}
  --token TOKEN              Default: ${TOKEN}
  --mode MODE                Default: ${MODE}
  --lang LANG                Default: ${LANG}
  --transport MODE           host|docker (default: ${TRANSPORT})
  --gs-base-url URL          Default: ${GS_BASE_URL}
  --gameplay-base-url URL    Default: ${GAMEPLAY_BASE_URL}
  --gs-container NAME        Default: ${GS_CONTAINER}
  --gameplay-container NAME  Default: ${GAMEPLAY_CONTAINER}
  --require-redis-hit BOOL   true|false (default: ${REQUIRE_REDIS_HIT})
  -h, --help                 Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bank-id)
      BANK_ID="$2"; shift 2 ;;
    --game-id)
      GAME_ID="$2"; shift 2 ;;
    --token)
      TOKEN="$2"; shift 2 ;;
    --mode)
      MODE="$2"; shift 2 ;;
    --lang)
      LANG="$2"; shift 2 ;;
    --transport)
      TRANSPORT="$2"; shift 2 ;;
    --gs-base-url)
      GS_BASE_URL="$2"; shift 2 ;;
    --gameplay-base-url)
      GAMEPLAY_BASE_URL="$2"; shift 2 ;;
    --gs-container)
      GS_CONTAINER="$2"; shift 2 ;;
    --gameplay-container)
      GAMEPLAY_CONTAINER="$2"; shift 2 ;;
    --require-redis-hit)
      REQUIRE_REDIS_HIT="$(echo "$2" | tr '[:upper:]' '[:lower:]')"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1" >&2
    exit 1
  }
}

require_cmd curl
require_cmd grep
require_cmd wc
require_cmd tr
require_cmd mktemp

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
    curl -fsS "${GAMEPLAY_BASE_URL}${path}" > "${out_file}"
  else
    docker exec "${GAMEPLAY_CONTAINER}" sh -lc "curl -fsS 'http://127.0.0.1:18074${path}'" > "${out_file}"
  fi
}

api_put_json_to_file() {
  local path="$1"
  local payload="$2"
  local out_file="$3"
  if [[ "${TRANSPORT}" == "host" ]]; then
    curl -fsS -X PUT \
      -H "Content-Type: application/json" \
      --data "${payload}" \
      "${GAMEPLAY_BASE_URL}${path}" > "${out_file}"
  else
    docker exec "${GAMEPLAY_CONTAINER}" sh -lc "curl -fsS -X PUT -H 'Content-Type: application/json' --data '${payload}' 'http://127.0.0.1:18074${path}'" > "${out_file}"
  fi
}

gs_launch_http_code_to_file() {
  local out_file="$1"
  local url="${GS_BASE_URL}/cwstartgamev2.do?bankId=${BANK_ID}&gameId=${GAME_ID}&mode=${MODE}&token=${TOKEN}&lang=${LANG}"
  if [[ "${TRANSPORT}" == "host" ]]; then
    curl -sS -o /dev/null -w '%{http_code}' "${url}" > "${out_file}"
  else
    local docker_url="http://127.0.0.1:8080/cwstartgamev2.do?bankId=${BANK_ID}&gameId=${GAME_ID}&mode=${MODE}&token=${TOKEN}&lang=${LANG}"
    docker exec "${GS_CONTAINER}" sh -lc "curl -sS -o /dev/null -w '%{http_code}' '${docker_url}'" > "${out_file}"
  fi
}

count_launch_intents_from_file() {
  local file="$1"
  grep -o '"type":"launch"' "${file}" | wc -l | tr -d ' '
}

extract_string_field() {
  local key="$1"
  local file="$2"
  grep -o "\"${key}\":\"[^\"]*\"" "${file}" | head -n1 | cut -d'"' -f4
}

extract_bool_field() {
  local key="$1"
  local file="$2"
  grep -o "\"${key}\":[^,}]*" "${file}" | head -n1 | cut -d':' -f2 | tr -d '[:space:]'
}

tmp_dir="$(mktemp -d)"
before_file="${tmp_dir}/before.json"
after_file="${tmp_dir}/after.json"
decision_file="${tmp_dir}/decision.json"
health_file="${tmp_dir}/health.json"
http_file="${tmp_dir}/launch_http.txt"
state_put_file="${tmp_dir}/state_put.json"
state_get_file="${tmp_dir}/state_get.json"
trap 'rm -rf "${tmp_dir}"' EXIT

api_get_to_file "/health" "${health_file}"
configured_backend="$(extract_string_field "configuredBackend" "${health_file}")"

api_get_to_file "/api/v1/gameplay/intents?bankId=${BANK_ID}&type=launch" "${before_file}"
before_count="$(count_launch_intents_from_file "${before_file}")"

api_get_to_file "/api/v1/gameplay/routing/decision?bankId=${BANK_ID}&isMultiplayer=false" "${decision_file}"
if ! grep -q '"routeToGameplayService":true' "${decision_file}"; then
  echo "FAIL: gameplay canary route decision is not enabled for bank ${BANK_ID}" >&2
  echo "Decision payload: $(cat "${decision_file}")" >&2
  exit 2
fi

gs_launch_http_code_to_file "${http_file}"
http_code="$(cat "${http_file}" | tr -d '\r\n')"
sleep 1
api_get_to_file "/api/v1/gameplay/intents?bankId=${BANK_ID}&type=launch" "${after_file}"
after_count="$(count_launch_intents_from_file "${after_file}")"

state_key="canary-${BANK_ID}-$(date +%s)"
state_payload="{\"seed\":\"bank-${BANK_ID}-seed\",\"state\":{\"round\":1,\"step\":\"wager\"},\"context\":{\"bankId\":\"${BANK_ID}\",\"gameId\":\"${GAME_ID}\"},\"ttlSeconds\":120}"
api_put_json_to_file "/api/v1/gameplay/state-blobs/${state_key}" "${state_payload}" "${state_put_file}"
api_get_to_file "/api/v1/gameplay/state-blobs/${state_key}" "${state_get_file}"

cache_backend="$(extract_string_field "cacheBackend" "${state_get_file}")"
degraded_from_redis="$(extract_bool_field "degradedFromRedis" "${state_get_file}")"
fingerprint="$(extract_string_field "fingerprint" "${state_get_file}")"

echo "Gameplay canary probe summary"
echo "  transport: ${TRANSPORT}"
echo "  bankId: ${BANK_ID}"
echo "  decision: routeToGameplayService=true"
echo "  launch_http: ${http_code}"
echo "  launch_intents_before: ${before_count}"
echo "  launch_intents_after: ${after_count}"
echo "  state_cache_configured_backend: ${configured_backend:-unknown}"
echo "  state_blob_cache_backend: ${cache_backend:-unknown}"
echo "  state_blob_degraded_from_redis: ${degraded_from_redis:-unknown}"
echo "  state_blob_fingerprint: ${fingerprint:-missing}"

if [[ "${http_code}" != "200" && "${http_code}" != "302" ]]; then
  echo "FAIL: launch returned unexpected HTTP ${http_code}" >&2
  exit 3
fi

if (( after_count <= before_count )); then
  echo "FAIL: no new launch intent observed in gameplay-orchestrator shadow path" >&2
  exit 4
fi

if [[ -z "${fingerprint}" ]]; then
  echo "FAIL: state-blob fingerprint is missing" >&2
  exit 5
fi

if [[ "${REQUIRE_REDIS_HIT}" == "true" && "${cache_backend}" != "redis" ]]; then
  echo "FAIL: expected Redis state-blob hit but backend was '${cache_backend:-unknown}'" >&2
  exit 6
fi

echo "PASS: gameplay-orchestrator launch shadow and state-blob flow verified (${before_count} -> ${after_count})."
