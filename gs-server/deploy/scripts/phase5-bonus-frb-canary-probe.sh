#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/dev/null
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

BANK_ID="6275"
ACCOUNT_ID=""
FRB_ID=""
TRANSPORT="host"
BONUS_BASE_URL="$(cluster_hosts_http_url BONUS_FRB_SERVICE_EXTERNAL_HOST BONUS_FRB_SERVICE_EXTERNAL_PORT 127.0.0.1 18076)"
BONUS_CONTAINER="refactor-bonus-frb-service-1"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID            Default: ${BANK_ID}
  --account-id ID         Optional (auto-generated if empty)
  --frb-id ID             Optional (auto-generated if empty)
  --transport MODE        host|docker (default: ${TRANSPORT})
  --bonus-base-url URL    Default: ${BONUS_BASE_URL}
  --bonus-container NAME  Default: ${BONUS_CONTAINER}
  -h, --help              Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bank-id)
      BANK_ID="$2"; shift 2 ;;
    --account-id)
      ACCOUNT_ID="$2"; shift 2 ;;
    --frb-id)
      FRB_ID="$2"; shift 2 ;;
    --transport)
      TRANSPORT="$2"; shift 2 ;;
    --bonus-base-url)
      BONUS_BASE_URL="$2"; shift 2 ;;
    --bonus-container)
      BONUS_CONTAINER="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

if [[ -z "${ACCOUNT_ID}" ]]; then
  ACCOUNT_ID="canary-account-$(date +%s)"
fi
if [[ -z "${FRB_ID}" ]]; then
  FRB_ID="canary-frb-$(date +%s)"
fi

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "Missing required command: $1" >&2
    exit 1
  }
}

require_cmd curl
require_cmd grep
require_cmd tr

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
    curl -fsS "${BONUS_BASE_URL}${path}" > "${out_file}"
  else
    docker exec "${BONUS_CONTAINER}" sh -lc "curl -fsS 'http://127.0.0.1:18076${path}'" > "${out_file}"
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
      "${BONUS_BASE_URL}${path}" > "${out_file}"
  else
    docker exec "${BONUS_CONTAINER}" sh -lc "curl -fsS -X POST -H 'Content-Type: application/json' --data '${payload}' 'http://127.0.0.1:18076${path}'" > "${out_file}"
  fi
}

tmp_dir="$(mktemp -d)"
decision_file="${tmp_dir}/decision.json"
check_before_file="${tmp_dir}/check_before.json"
consume_file="${tmp_dir}/consume.json"
release_file="${tmp_dir}/release.json"
check_after_file="${tmp_dir}/check_after.json"
trap 'rm -rf "${tmp_dir}"' EXIT

api_get_to_file "/api/v1/bonus/frb/routing/decision?bankId=${BANK_ID}" "${decision_file}"
if ! grep -q '"routeToBonusFrbService":true' "${decision_file}"; then
  echo "FAIL: bonus-frb-service canary route decision is not enabled for bank ${BANK_ID}" >&2
  echo "Decision payload: $(cat "${decision_file}")" >&2
  exit 2
fi

api_get_to_file "/api/v1/bonus/frb/check?bankId=${BANK_ID}&accountId=${ACCOUNT_ID}&frbId=${FRB_ID}" "${check_before_file}"
if ! grep -q '"status":"AVAILABLE"' "${check_before_file}"; then
  echo "FAIL: initial FRB status is not AVAILABLE" >&2
  echo "Check payload: $(cat "${check_before_file}")" >&2
  exit 3
fi

consume_payload="{\"bankId\":\"${BANK_ID}\",\"accountId\":\"${ACCOUNT_ID}\",\"frbId\":\"${FRB_ID}\",\"operationId\":\"consume-${BANK_ID}-${ACCOUNT_ID}-${FRB_ID}\"}"
api_post_json_to_file "/api/v1/bonus/frb/consume" "${consume_payload}" "${consume_file}"
if ! grep -q '"status":"CONSUMED"' "${consume_file}"; then
  echo "FAIL: consume did not produce CONSUMED status" >&2
  echo "Consume payload: $(cat "${consume_file}")" >&2
  exit 4
fi

release_payload="{\"bankId\":\"${BANK_ID}\",\"accountId\":\"${ACCOUNT_ID}\",\"frbId\":\"${FRB_ID}\",\"operationId\":\"release-${BANK_ID}-${ACCOUNT_ID}-${FRB_ID}\"}"
api_post_json_to_file "/api/v1/bonus/frb/release" "${release_payload}" "${release_file}"
if ! grep -q '"status":"AVAILABLE"' "${release_file}"; then
  echo "FAIL: release did not return AVAILABLE status" >&2
  echo "Release payload: $(cat "${release_file}")" >&2
  exit 5
fi

api_get_to_file "/api/v1/bonus/frb/check?bankId=${BANK_ID}&accountId=${ACCOUNT_ID}&frbId=${FRB_ID}" "${check_after_file}"
if ! grep -q '"status":"AVAILABLE"' "${check_after_file}"; then
  echo "FAIL: final FRB status is not AVAILABLE" >&2
  echo "Final check payload: $(cat "${check_after_file}")" >&2
  exit 6
fi

echo "Bonus/FRB canary probe summary"
echo "  bankId: ${BANK_ID}"
echo "  accountId: ${ACCOUNT_ID}"
echo "  frbId: ${FRB_ID}"
echo "  decision: routeToBonusFrbService=true"
echo "PASS: bonus-frb canary check/consume/release flow verified."
