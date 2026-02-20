#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

BANK_ID="6275"
GAME_ID="838"
MODE="real"
LANG="en"
TOKEN="test_user_6275"
GS_CONTAINER="refactor-gs-1"
SESSION_CONTAINER="refactor-session-service-1"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID              Default: ${BANK_ID}
  --game-id ID              Default: ${GAME_ID}
  --token TOKEN             Default: ${TOKEN}
  --gs-container NAME       Default: ${GS_CONTAINER}
  --session-container NAME  Default: ${SESSION_CONTAINER}
  --mode MODE               Default: ${MODE}
  --lang LANG               Default: ${LANG}
  -h, --help                Show this help
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
    --gs-container)
      GS_CONTAINER="$2"; shift 2 ;;
    --session-container)
      SESSION_CONTAINER="$2"; shift 2 ;;
    --mode)
      MODE="$2"; shift 2 ;;
    --lang)
      LANG="$2"; shift 2 ;;
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

session_api_to_file() {
  local path="$1"
  local out_file="$2"
  docker exec "${SESSION_CONTAINER}" sh -lc "wget -qO- 'http://127.0.0.1:18073${path}'" > "${out_file}"
}

gs_launch_to_file() {
  local out_file="$1"
  local url="http://127.0.0.1:8080/cwstartgamev2.do?bankId=${BANK_ID}&gameId=${GAME_ID}&mode=${MODE}&token=${TOKEN}&lang=${LANG}"
  docker exec "${GS_CONTAINER}" sh -lc "curl -sS -o /tmp/phase5-canary-launch.html -w '%{http_code}' '${url}'" > "${out_file}"
}

count_sessions_from_file() {
  local file="$1"
  grep -o '"sessionId":"[^"]*"' "${file}" | wc -l | tr -d ' '
}

tmp_dir="$(mktemp -d)"
before_file="${tmp_dir}/before.json"
after_file="${tmp_dir}/after.json"
decision_file="${tmp_dir}/decision.json"
http_file="${tmp_dir}/launch_http.txt"
trap 'rm -rf "${tmp_dir}"' EXIT

session_api_to_file "/api/v1/sessions?bankId=${BANK_ID}" "${before_file}"
before_count="$(count_sessions_from_file "${before_file}")"

session_api_to_file "/api/v1/routing/decision?bankId=${BANK_ID}" "${decision_file}"
if ! grep -q '"routeToSessionService":true' "${decision_file}"; then
  echo "FAIL: canary route decision is not enabled for bank ${BANK_ID}" >&2
  echo "Decision payload: $(cat "${decision_file}")" >&2
  exit 2
fi

gs_launch_to_file "${http_file}"
http_code="$(cat "${http_file}" | tr -d '\r\n')"
sleep 1
session_api_to_file "/api/v1/sessions?bankId=${BANK_ID}" "${after_file}"
after_count="$(count_sessions_from_file "${after_file}")"

echo "Canary probe summary"
echo "  bankId: ${BANK_ID}"
echo "  decision: routeToSessionService=true"
echo "  launch_http: ${http_code}"
echo "  sessions_before: ${before_count}"
echo "  sessions_after: ${after_count}"

if [[ "${http_code}" != "200" && "${http_code}" != "302" ]]; then
  echo "FAIL: launch returned unexpected HTTP ${http_code}" >&2
  exit 3
fi

if (( after_count <= before_count )); then
  echo "FAIL: no new session observed in session-service outbox path" >&2
  exit 4
fi

echo "PASS: session-service shadow create observed (${before_count} -> ${after_count})."
