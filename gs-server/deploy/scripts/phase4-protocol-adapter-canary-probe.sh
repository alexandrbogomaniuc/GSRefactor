#!/usr/bin/env bash
set -euo pipefail

BANK_ID="6275"
GAME_ID="838"
MODE="real"
LANG="en"
TOKEN="test_user_6275"
GS_CONTAINER="refactor-gs-1"
PROTOCOL_CONTAINER="refactor-protocol-adapter-1"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID              Default: ${BANK_ID}
  --game-id ID              Default: ${GAME_ID}
  --mode MODE               Default: ${MODE}
  --lang LANG               Default: ${LANG}
  --token TOKEN             Default: ${TOKEN}
  --gs-container NAME       Default: ${GS_CONTAINER}
  --protocol-container NAME Default: ${PROTOCOL_CONTAINER}
  -h, --help                Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bank-id)
      BANK_ID="$2"; shift 2 ;;
    --game-id)
      GAME_ID="$2"; shift 2 ;;
    --mode)
      MODE="$2"; shift 2 ;;
    --lang)
      LANG="$2"; shift 2 ;;
    --token)
      TOKEN="$2"; shift 2 ;;
    --gs-container)
      GS_CONTAINER="$2"; shift 2 ;;
    --protocol-container)
      PROTOCOL_CONTAINER="$2"; shift 2 ;;
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

require_cmd docker
require_cmd grep
require_cmd wc
require_cmd tr

protocol_api_to_file() {
  local path="$1"
  local out_file="$2"
  docker exec "${PROTOCOL_CONTAINER}" sh -lc "wget -qO- 'http://127.0.0.1:18078${path}'" > "${out_file}"
}

gs_launch_to_file() {
  local out_file="$1"
  local url="http://127.0.0.1:8080/cwstartgamev2.do?bankId=${BANK_ID}&gameId=${GAME_ID}&mode=${MODE}&token=${TOKEN}&lang=${LANG}"
  docker exec "${GS_CONTAINER}" sh -lc "curl -sS -o /tmp/phase4-protocol-launch.html -w '%{http_code}' '${url}'" > "${out_file}"
}

count_protocol_events_from_file() {
  local file="$1"
  grep -o '"type":"PROTOCOL_REQUEST_EVALUATED"' "${file}" | wc -l | tr -d ' '
}

tmp_dir="$(mktemp -d)"
before_file="${tmp_dir}/before.json"
after_file="${tmp_dir}/after.json"
decision_file="${tmp_dir}/decision.json"
http_file="${tmp_dir}/launch_http.txt"
trap 'rm -rf "${tmp_dir}"' EXIT

protocol_api_to_file "/api/v1/protocol/events?limit=500" "${before_file}"
before_count="$(count_protocol_events_from_file "${before_file}")"

protocol_api_to_file "/api/v1/protocol/routing/decision?bankId=${BANK_ID}" "${decision_file}"
if ! grep -q '"routeToProtocolAdapter":true' "${decision_file}"; then
  echo "FAIL: protocol-adapter canary route decision is not enabled for bank ${BANK_ID}" >&2
  echo "Decision payload: $(cat "${decision_file}")" >&2
  exit 2
fi

gs_launch_to_file "${http_file}"
http_code="$(cat "${http_file}" | tr -d '\r\n')"
sleep 1
protocol_api_to_file "/api/v1/protocol/events?limit=500" "${after_file}"
after_count="$(count_protocol_events_from_file "${after_file}")"

echo "Protocol adapter canary probe summary"
echo "  bankId: ${BANK_ID}"
echo "  decision: routeToProtocolAdapter=true"
echo "  launch_http: ${http_code}"
echo "  protocol_events_before: ${before_count}"
echo "  protocol_events_after: ${after_count}"

if [[ "${http_code}" != "200" && "${http_code}" != "302" ]]; then
  echo "FAIL: launch returned unexpected HTTP ${http_code}" >&2
  exit 3
fi

if (( after_count <= before_count )); then
  echo "FAIL: no new protocol normalize event observed in protocol-adapter shadow path" >&2
  exit 4
fi

echo "PASS: protocol-adapter shadow normalize observed (${before_count} -> ${after_count})."
