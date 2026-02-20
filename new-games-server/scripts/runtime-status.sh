#!/usr/bin/env bash

set -euo pipefail

DEV_ROOT="${DEV_ROOT:-/Users/alexb/Documents/Dev}"
GS_CONTAINER="${GS_CONTAINER:-gp3-gs-1}"
NGS_BASE_URL="${NGS_BASE_URL:-http://localhost:6400}"
GS_ENDPOINT_BASE="${GS_ENDPOINT_BASE:-http://localhost:81}"
LAUNCH_URL="${LAUNCH_URL:-http://localhost/cwstartgamev2.do?bankId=6274&gameId=00010&mode=real&token=bav_game_session_001&lang=en}"
PROBE_LAUNCH="${PROBE_LAUNCH:-0}"
CASINO_DB_CONTAINER="${CASINO_DB_CONTAINER:-casino_mysql}"
CASINO_DB_NAME="${CASINO_DB_NAME:-apidb}"
CASINO_DB_USER="${CASINO_DB_USER:-root}"
CASINO_DB_PASS="${CASINO_DB_PASS:-Moldova1981!}"

required_commands=(curl docker)
for cmd in "${required_commands[@]}"; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Missing required command: $cmd" >&2
    exit 1
  fi
done

echo "TimestampUTC=$(date -u +%Y-%m-%dT%H:%M:%SZ)"

echo "Containers:"
docker ps --format '{{.Names}}|{{.Status}}' | rg '^(gp3-|casino_)' || true

gs_validate_status="$(curl -s -o /dev/null -w '%{http_code}' "$GS_ENDPOINT_BASE/gs-internal/newgames/v1/session/validate" || true)"
echo "GSInternalValidateGetStatus=${gs_validate_status:-0}"

if [[ "$PROBE_LAUNCH" == "1" ]]; then
  echo "LaunchProbeWarning=enabled (creates new SID; avoid running concurrently with runtime:e2e)"
  launch_headers="$(curl -sS -D - -o /dev/null "$LAUNCH_URL" || true)"
  launch_status="$(printf '%s\n' "$launch_headers" | awk 'NR==1{print $2}')"
  launch_location="$(printf '%s\n' "$launch_headers" | awk 'BEGIN{IGNORECASE=1}/^Location:/{print $2}' | tr -d '\r')"
  echo "LaunchStatus=${launch_status:-0}"
  if [[ -n "$launch_location" ]]; then
    echo "LaunchLocation=$launch_location"
  fi
else
  echo "LaunchStatus=skipped (set PROBE_LAUNCH=1 to execute launch check)"
fi

ngs_health_status="$(curl -s -o /dev/null -w '%{http_code}' "$NGS_BASE_URL/healthz" || true)"
echo "NGSHealthStatus=${ngs_health_status:-0}"

if docker ps --format '{{.Names}}' | rg -qx "$CASINO_DB_CONTAINER"; then
  echo "LatestGameplayTransactions:"
  docker exec "$CASINO_DB_CONTAINER" mysql -u"$CASINO_DB_USER" -p"$CASINO_DB_PASS" -D "$CASINO_DB_NAME" -N -e \
    "SELECT CONCAT(transaction_id,'|',transaction_type,'|',amount,'|',IFNULL(external_transaction_id,''),'|',DATE_FORMAT(transaction_date,'%Y-%m-%d %H:%i:%s')) FROM gameplay_transactions ORDER BY transaction_id DESC LIMIT 5;" 2>/dev/null || true
else
  echo "LatestGameplayTransactions=skipped (DB container not running)"
fi
