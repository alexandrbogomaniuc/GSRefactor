#!/usr/bin/env bash
set -euo pipefail

MODE="dry-run"
BASE_URL="http://localhost:18080"
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution"
FIXTURE_FILE=""
TIMEOUT_SEC=20

BANK_ID="${BANK_ID:-}"
GAME_ID="${GAME_ID:-}"
TOKEN="${TOKEN:-}"
LANG_CODE="${LANG_CODE:-en}"
GAME_MODE="${GAME_MODE:-real}"

USER_ID="${USER_ID:-}"
CURRENCY="${CURRENCY:-}"
WAGER_AMOUNT="${WAGER_AMOUNT:-}"
SETTLE_AMOUNT="${SETTLE_AMOUNT:-}"
EXT_BONUS_ID="${EXT_BONUS_ID:-}"
BONUS_HASH="${BONUS_HASH:-}"

NEG_BANK_ID="${NEG_BANK_ID:-999999}"
NEG_GAME_ID="${NEG_GAME_ID:-838}"
NEG_TOKEN="${NEG_TOKEN:-invalid_token}"

print_help() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --mode dry-run|run         Default: dry-run
  --base-url URL             Default: http://localhost:18080
  --fixture-file PATH        Optional env file with fixture values
  --out-dir PATH             Default: /Users/alexb/Documents/Dev/Dev_new/docs/phase0/parity-execution
  --timeout-sec N            Default: 20
  -h, --help                 Show help

Fixture keys (env file or environment):
  BANK_ID, GAME_ID, TOKEN, LANG_CODE, GAME_MODE,
  USER_ID, CURRENCY, WAGER_AMOUNT, SETTLE_AMOUNT, EXT_BONUS_ID, BONUS_HASH,
  NEG_BANK_ID, NEG_GAME_ID, NEG_TOKEN
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --mode)
      MODE="$2"; shift 2 ;;
    --base-url)
      BASE_URL="$2"; shift 2 ;;
    --fixture-file)
      FIXTURE_FILE="$2"; shift 2 ;;
    --out-dir)
      OUT_DIR="$2"; shift 2 ;;
    --timeout-sec)
      TIMEOUT_SEC="$2"; shift 2 ;;
    -h|--help)
      print_help; exit 0 ;;
    *)
      echo "Unknown option: $1" >&2
      print_help
      exit 1 ;;
  esac
done

if [[ "$MODE" != "dry-run" && "$MODE" != "run" ]]; then
  echo "Invalid --mode '$MODE' (expected dry-run or run)" >&2
  exit 1
fi

if [[ -n "$FIXTURE_FILE" ]]; then
  if [[ ! -f "$FIXTURE_FILE" ]]; then
    echo "Fixture file not found: $FIXTURE_FILE" >&2
    exit 1
  fi
  # shellcheck source=/dev/null
  source "$FIXTURE_FILE"
fi

TIMESTAMP="$(date -u +%Y%m%d-%H%M%S)"
mkdir -p "$OUT_DIR"
REPORT_FILE="$OUT_DIR/phase0-parity-${TIMESTAMP}.md"

cat > "$REPORT_FILE" <<REPORT
# Phase 0 Parity Execution Report

- Timestamp (UTC): $(date -u +"%Y-%m-%d %H:%M:%S")
- Mode: $MODE
- Base URL: $BASE_URL
- Fixture file: ${FIXTURE_FILE:-<none>}

| Test ID | Flow | Status | HTTP | Evidence |
|---|---|---|---|---|
REPORT

append_row() {
  local test_id="$1"
  local flow="$2"
  local status="$3"
  local http_code="$4"
  local evidence="$5"
  printf '| %s | %s | %s | %s | %s |\n' "$test_id" "$flow" "$status" "$http_code" "$evidence" >> "$REPORT_FILE"
}

run_case() {
  local test_id="$1"
  local flow="$2"
  local method="$3"
  local url="$4"
  local body="$5"
  local has_required="$6"
  local success_pattern="$7"
  local fail_pattern="$8"

  local cmd="curl -sS -m ${TIMEOUT_SEC} -X ${method} '${url}'"
  if [[ -n "$body" ]]; then
    cmd="${cmd} --data-raw '${body}'"
  fi

  if [[ "$MODE" == "dry-run" ]]; then
    append_row "$test_id" "$flow" "DRY_RUN" "-" "\`${cmd}\`"
    return 0
  fi

  if [[ "$has_required" != "yes" ]]; then
    append_row "$test_id" "$flow" "SKIPPED_MISSING_FIXTURE" "-" "required fixture values missing"
    return 0
  fi

  local body_file="$OUT_DIR/${test_id}-${TIMESTAMP}.body.txt"
  local http_code
  set +e
  http_code=$(eval "$cmd -o '$body_file' -w '%{http_code}'")
  local rc=$?
  set -e

  if [[ $rc -ne 0 ]]; then
    append_row "$test_id" "$flow" "FAIL_EXEC" "-" "\`${cmd}\`"
    return 0
  fi

  if [[ "$http_code" =~ ^[23] ]]; then
    if [[ -n "$fail_pattern" ]] && rg -qi "$fail_pattern" "$body_file"; then
      append_row "$test_id" "$flow" "FAIL_CONTRACT" "$http_code" "\`$(basename "$body_file")\`"
      return 0
    fi
    if [[ -n "$success_pattern" ]] && ! rg -qi "$success_pattern" "$body_file"; then
      append_row "$test_id" "$flow" "FAIL_CONTRACT" "$http_code" "\`$(basename "$body_file")\`"
      return 0
    fi
    append_row "$test_id" "$flow" "PASS_CONTRACT" "$http_code" "\`$(basename "$body_file")\`"
  else
    append_row "$test_id" "$flow" "FAIL_HTTP" "$http_code" "\`$(basename "$body_file")\`"
  fi
}

build_required_flag() {
  local flag="yes"
  for v in "$@"; do
    if [[ -z "$v" ]]; then
      flag="no"
      break
    fi
  done
  echo "$flag"
}

LAUNCH_URL="${BASE_URL}/cwstartgamev2.do?bankId=${BANK_ID}&gameId=${GAME_ID}&mode=${GAME_MODE}&token=${TOKEN}&lang=${LANG_CODE}"
LAUNCH_REQUIRED=$(build_required_flag "$BANK_ID" "$GAME_ID" "$TOKEN")
run_case "P0-LA-01" "Launch" "GET" "$LAUNCH_URL" "" "$LAUNCH_REQUIRED" "template\\.jsp|sid=|web_socket_url" "bank is incorrect|casino is incorrect|invalid parameters|http error|404 not found|exception"

LAUNCH_NEG_URL="${BASE_URL}/cwstartgamev2.do?bankId=${NEG_BANK_ID}&gameId=${NEG_GAME_ID}&mode=${GAME_MODE}&token=${NEG_TOKEN}&lang=${LANG_CODE}"
run_case "P0-LA-02" "LaunchInvalidParams" "GET" "$LAUNCH_NEG_URL" "" "yes" "bank is incorrect|casino is incorrect|invalid parameters|error" "template\\.jsp|sid=|web_socket_url"

WAGER_URL="${BASE_URL}/bscheck.do?bankId=${BANK_ID}&gameId=${GAME_ID}&userId=${USER_ID}&currency=${CURRENCY}&amount=${WAGER_AMOUNT}&extBonusId=${EXT_BONUS_ID}&hash=${BONUS_HASH}"
WAGER_REQUIRED=$(build_required_flag "$BANK_ID" "$GAME_ID" "$USER_ID" "$CURRENCY" "$WAGER_AMOUNT" "$EXT_BONUS_ID" "$BONUS_HASH")
run_case "P0-WA-01" "Wager" "GET" "$WAGER_URL" "" "$WAGER_REQUIRED" "<code>0</code>|errorCode\\s*=\\s*\"0\"" "Invalid parameters|<code>610</code>|errorCode\\s*=\\s*\"610\"|Bank is incorrect"

WAGER_NEG_URL="${BASE_URL}/bscheck.do?bankId=${NEG_BANK_ID}&gameId=${NEG_GAME_ID}&userId=invalid_user&currency=USD&amount=0.1&extBonusId=1&hash=deadbeef"
run_case "P0-WA-00" "WagerInvalidParams" "GET" "$WAGER_NEG_URL" "" "yes" "invalid parameters|<code>610</code>|errorCode\\s*=\\s*\"610\"|bank is incorrect|casino is incorrect" "<code>0</code>|errorCode\\s*=\\s*\"0\""

SETTLE_URL="${BASE_URL}/bsaward.do?bankId=${BANK_ID}&gameId=${GAME_ID}&userId=${USER_ID}&currency=${CURRENCY}&amount=${SETTLE_AMOUNT}&extBonusId=${EXT_BONUS_ID}&hash=${BONUS_HASH}"
SETTLE_REQUIRED=$(build_required_flag "$BANK_ID" "$GAME_ID" "$USER_ID" "$CURRENCY" "$SETTLE_AMOUNT" "$EXT_BONUS_ID" "$BONUS_HASH")
run_case "P0-SE-01" "Settle" "GET" "$SETTLE_URL" "" "$SETTLE_REQUIRED" "<code>0</code>|errorCode\\s*=\\s*\"0\"" "Invalid parameters|<code>610</code>|errorCode\\s*=\\s*\"610\"|Bank is incorrect"

SETTLE_NEG_URL="${BASE_URL}/bsaward.do?bankId=${NEG_BANK_ID}&gameId=${NEG_GAME_ID}&userId=invalid_user&currency=USD&amount=0.1&extBonusId=1&hash=deadbeef"
run_case "P0-SE-00" "SettleInvalidParams" "GET" "$SETTLE_NEG_URL" "" "yes" "invalid parameters|<code>610</code>|errorCode\\s*=\\s*\"610\"|bank is incorrect|casino is incorrect" "<code>0</code>|errorCode\\s*=\\s*\"0\""

echo "Report generated: $REPORT_FILE"
