#!/usr/bin/env bash

set -euo pipefail

DEV_ROOT="${DEV_ROOT:-/Users/alexb/Documents/Dev}"
NGS_DIR="${NGS_DIR:-$DEV_ROOT/new-games-server}"
NGS_BASE_URL="${NGS_BASE_URL:-http://localhost:6400}"
GS_INTERNAL_BASE_URL="${GS_INTERNAL_BASE_URL:-http://localhost:81}"
LAUNCH_URL="${LAUNCH_URL:-http://localhost/cwstartgamev2.do?bankId=6274&gameId=00010&mode=real&token=bav_game_session_001&lang=en}"
GAME_ID="${GAME_ID:-10}"
BET_AMOUNT="${BET_AMOUNT:-100}"
BET_TYPE="${BET_TYPE:-risk-medium}"
AUTO_START_NGS="${AUTO_START_NGS:-1}"

required_commands=(curl jq)
for cmd in "${required_commands[@]}"; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Missing required command: $cmd" >&2
    exit 1
  fi
done

if [[ ! -d "$NGS_DIR" ]]; then
  echo "Missing NGS directory: $NGS_DIR" >&2
  exit 1
fi

ngs_pid=""
cleanup() {
  if [[ -n "$ngs_pid" ]]; then
    kill "$ngs_pid" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

health_url="$NGS_BASE_URL/healthz"
if ! curl -fsS "$health_url" >/dev/null 2>&1; then
  if [[ "$AUTO_START_NGS" != "1" ]]; then
    echo "NGS is not reachable at $NGS_BASE_URL and AUTO_START_NGS=$AUTO_START_NGS" >&2
    exit 1
  fi

  (
    cd "$NGS_DIR"
    npm run dev >/tmp/new-games-server-runtime-e2e.log 2>&1
  ) &
  ngs_pid="$!"

  ready="0"
  for _ in $(seq 1 80); do
    if curl -fsS "$health_url" >/dev/null 2>&1; then
      ready="1"
      break
    fi
    sleep 0.5
  done

  if [[ "$ready" != "1" ]]; then
    echo "NGS did not become ready at $health_url" >&2
    tail -n 80 /tmp/new-games-server-runtime-e2e.log >&2 || true
    exit 1
  fi
fi

launch_headers="$(curl -sS -D - -o /dev/null "$LAUNCH_URL")"
launch_status="$(printf '%s\n' "$launch_headers" | awk 'NR==1{print $2}')"
launch_location="$(printf '%s\n' "$launch_headers" | awk 'BEGIN{IGNORECASE=1}/^Location:/{print $2}' | tr -d '\r')"
if [[ "$launch_status" != "302" ]]; then
  echo "Launch URL did not return 302. Status=$launch_status" >&2
  exit 1
fi

sid="$(printf '%s' "$launch_location" | sed -n 's/.*[?&]SID=\([^&]*\).*/\1/p')"
bank_id="$(printf '%s' "$launch_location" | sed -n 's/.*[?&]BANKID=\([^&]*\).*/\1/p')"
if [[ -z "$sid" ]]; then
  echo "Failed to parse SID from launch Location header." >&2
  exit 1
fi
if [[ -z "$bank_id" ]]; then
  bank_id="6274"
fi

session_validate_body="$(curl -sS -w $'\nHTTP_STATUS:%{http_code}' \
  -X POST "$GS_INTERNAL_BASE_URL/gs-internal/newgames/v1/session/validate" \
  -H 'Content-Type: application/json' \
  -H 'X-NGS-Contract: v1' \
  --data "{\"sessionId\":\"$sid\",\"bankId\":$bank_id}")"
session_validate_status="${session_validate_body##*HTTP_STATUS:}"
session_validate_json="${session_validate_body%HTTP_STATUS:*}"
if [[ "$session_validate_status" != "200" ]]; then
  expected_sid="$(printf '%s' "$session_validate_json" | sed -n 's/.*expected:\([^;) ]*\).*/\1/p')"
  if [[ "$session_validate_status" == "400" && -n "$expected_sid" ]]; then
    sid="$expected_sid"
    session_validate_retry="$(curl -sS -w $'\nHTTP_STATUS:%{http_code}' \
      -X POST "$GS_INTERNAL_BASE_URL/gs-internal/newgames/v1/session/validate" \
      -H 'Content-Type: application/json' \
      -H 'X-NGS-Contract: v1' \
      --data "{\"sessionId\":\"$sid\",\"bankId\":$bank_id}")"
    session_validate_status="${session_validate_retry##*HTTP_STATUS:}"
    session_validate_json="${session_validate_retry%HTTP_STATUS:*}"
  fi
fi
if [[ "$session_validate_status" != "200" ]]; then
  echo "GS session validate failed: status=$session_validate_status" >&2
  echo "$session_validate_json" >&2
  exit 1
fi

post_json() {
  local url="$1"
  local payload="$2"
  curl -sS -w $'\nHTTP_STATUS:%{http_code}' -H 'Content-Type: application/json' -X POST "$url" --data "$payload"
}

open_payload="{\"sessionId\":\"$sid\",\"bankId\":$bank_id,\"gsInternalBaseUrl\":\"$GS_INTERNAL_BASE_URL\",\"gameId\":$GAME_ID}"
open_response="$(post_json "$NGS_BASE_URL/v1/opengame" "$open_payload")"
open_status="${open_response##*HTTP_STATUS:}"
open_body="${open_response%HTTP_STATUS:*}"
if [[ "$open_status" != "200" ]]; then
  echo "opengame failed: status=$open_status" >&2
  echo "$open_body" >&2
  exit 1
fi

unique_id="$(date +%s)"
place_payload="{\"sessionId\":\"$sid\",\"requestCounter\":1,\"clientOperationId\":\"e2e-place-$unique_id\",\"bets\":[{\"betType\":\"$BET_TYPE\",\"betAmount\":$BET_AMOUNT}]}"
place_response="$(post_json "$NGS_BASE_URL/v1/placebet" "$place_payload")"
place_status="${place_response##*HTTP_STATUS:}"
place_body="${place_response%HTTP_STATUS:*}"
if [[ "$place_status" != "200" ]]; then
  echo "placebet failed: status=$place_status" >&2
  echo "$place_body" >&2
  exit 1
fi

round_id="$(printf '%s' "$place_body" | jq -r '.roundId // empty')"
if [[ -z "$round_id" ]]; then
  echo "placebet response does not contain roundId" >&2
  echo "$place_body" >&2
  exit 1
fi

collect_payload="{\"sessionId\":\"$sid\",\"requestCounter\":2,\"roundId\":\"$round_id\",\"clientOperationId\":\"e2e-collect-$unique_id\"}"
collect_response="$(post_json "$NGS_BASE_URL/v1/collect" "$collect_payload")"
collect_status="${collect_response##*HTTP_STATUS:}"
collect_body="${collect_response%HTTP_STATUS:*}"
if [[ "$collect_status" != "200" ]]; then
  echo "collect failed: status=$collect_status" >&2
  echo "$collect_body" >&2
  exit 1
fi

history_payload="{\"sessionId\":\"$sid\",\"requestCounter\":3,\"pageNumber\":0}"
history_response="$(post_json "$NGS_BASE_URL/v1/readhistory" "$history_payload")"
history_status="${history_response##*HTTP_STATUS:}"
history_body="${history_response%HTTP_STATUS:*}"
if [[ "$history_status" != "200" ]]; then
  echo "readhistory failed: status=$history_status" >&2
  echo "$history_body" >&2
  exit 1
fi

printf 'LaunchStatus=%s\n' "$launch_status"
printf 'LaunchLocation=%s\n' "$launch_location"
printf 'SID=%s\n' "$sid"
printf 'GSValidateStatus=%s\n' "$session_validate_status"
printf 'Open=%s\n' "$(printf '%s' "$open_body" | jq -c '{status:200,balance,playerId,requestCounter,gameId}')"
printf 'Place=%s\n' "$(printf '%s' "$place_body" | jq -c '{status:200,roundId,balance,requestCounter,totalWinAmount:(.math.totalWinAmount)}')"
printf 'Collect=%s\n' "$(printf '%s' "$collect_body" | jq -c '{status:200,roundId,balance,winAmount,requestCounter}')"
printf 'History=%s\n' "$(printf '%s' "$history_body" | jq -c '{status:200,historyCount:(.history|length),requestCounter}')"

echo "E2E OK"
