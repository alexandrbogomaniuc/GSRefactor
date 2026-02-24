#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/cluster-hosts.sh
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

BANK_ID="6275"
GAME_ID="838"
TOKEN="test_user_6275"
SUB_CASINO_ID=""
MODE="real"
LANG="en"
TRANSPORT="host"
GS_BASE_URL="$(cluster_hosts_http_url GS_EXTERNAL_HOST GS_EXTERNAL_PORT 127.0.0.1 18081)"
GAMEPLAY_BASE_URL="$(cluster_hosts_http_url GAMEPLAY_ORCHESTRATOR_EXTERNAL_HOST GAMEPLAY_ORCHESTRATOR_EXTERNAL_PORT 127.0.0.1 18074)"
REQUIRE_REDIS_HIT="false"
READINESS_GAMEPLAY_HOST="$(cluster_hosts_get GAMEPLAY_ORCHESTRATOR_EXTERNAL_HOST 127.0.0.1)"
READINESS_GAMEPLAY_PORT="$(cluster_hosts_get GAMEPLAY_ORCHESTRATOR_EXTERNAL_PORT 18074)"
READINESS_GS_HOST="$(cluster_hosts_get GS_EXTERNAL_HOST 127.0.0.1)"
READINESS_GS_PORT="$(cluster_hosts_get GS_EXTERNAL_PORT 18081)"
READINESS_REDIS_HOST="$(cluster_hosts_get REDIS_EXTERNAL_HOST 127.0.0.1)"
READINESS_REDIS_PORT="$(cluster_hosts_get REDIS_EXTERNAL_PORT 16379)"
CHECK_DOCKER="true"
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase5/gameplay"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID                Default: ${BANK_ID}
  --game-id ID                Default: ${GAME_ID}
  --token TOKEN               Default: ${TOKEN}
  --sub-casino-id ID          Optional (appended to startgame launch URL for auto session resolution)
  --mode MODE                 Default: ${MODE}
  --lang LANG                 Default: ${LANG}
  --transport MODE            host|docker (default: ${TRANSPORT})
  --gs-base-url URL           Default: ${GS_BASE_URL}
  --gameplay-base-url URL     Default: ${GAMEPLAY_BASE_URL}
  --require-redis-hit BOOL    true|false (default: ${REQUIRE_REDIS_HIT})
  --readiness-gameplay-host H Default: ${READINESS_GAMEPLAY_HOST}
  --readiness-gameplay-port P Default: ${READINESS_GAMEPLAY_PORT}
  --readiness-gs-host H       Default: ${READINESS_GS_HOST}
  --readiness-gs-port P       Default: ${READINESS_GS_PORT}
  --readiness-redis-host H    Default: ${READINESS_REDIS_HOST}
  --readiness-redis-port P    Default: ${READINESS_REDIS_PORT}
  --check-docker BOOL         true|false (default: ${CHECK_DOCKER})
  --out-dir DIR               Default: ${OUT_DIR}
  -h, --help                  Show this help
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
    --sub-casino-id)
      SUB_CASINO_ID="$2"; shift 2 ;;
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
    --require-redis-hit)
      REQUIRE_REDIS_HIT="$2"; shift 2 ;;
    --readiness-gameplay-host)
      READINESS_GAMEPLAY_HOST="$2"; shift 2 ;;
    --readiness-gameplay-port)
      READINESS_GAMEPLAY_PORT="$2"; shift 2 ;;
    --readiness-gs-host)
      READINESS_GS_HOST="$2"; shift 2 ;;
    --readiness-gs-port)
      READINESS_GS_PORT="$2"; shift 2 ;;
    --readiness-redis-host)
      READINESS_REDIS_HOST="$2"; shift 2 ;;
    --readiness-redis-port)
      READINESS_REDIS_PORT="$2"; shift 2 ;;
    --check-docker)
      CHECK_DOCKER="$2"; shift 2 ;;
    --out-dir)
      OUT_DIR="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
ts="$(date -u +%Y%m%d-%H%M%S)"
report_file="${OUT_DIR}/phase5-gameplay-runtime-evidence-${ts}.md"
work_dir="$(mktemp -d)"
trap 'rm -rf "${work_dir}"' EXIT

run_and_capture() {
  local out_file="$1"
  shift
  if "$@" >"${out_file}" 2>&1; then
    echo "PASS"
  else
    echo "FAIL"
  fi
}

readiness_out="${work_dir}/readiness.out"
canary_out="${work_dir}/canary.out"

readiness_status="$(run_and_capture "${readiness_out}" \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-runtime-readiness-check.sh \
  --gameplay-host "${READINESS_GAMEPLAY_HOST}" --gameplay-port "${READINESS_GAMEPLAY_PORT}" \
  --gs-host "${READINESS_GS_HOST}" --gs-port "${READINESS_GS_PORT}" \
  --redis-host "${READINESS_REDIS_HOST}" --redis-port "${READINESS_REDIS_PORT}" \
  --check-docker "${CHECK_DOCKER}")"

canary_status="SKIPPED"
if [[ "${readiness_status}" == "PASS" ]]; then
  if [[ -n "${SUB_CASINO_ID}" ]]; then
    canary_status="$(run_and_capture "${canary_out}" \
      /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-gameplay-canary-probe.sh \
      --bank-id "${BANK_ID}" --game-id "${GAME_ID}" --token "${TOKEN}" --sub-casino-id "${SUB_CASINO_ID}" \
      --mode "${MODE}" --lang "${LANG}" \
      --transport "${TRANSPORT}" \
      --gs-base-url "${GS_BASE_URL}" --gameplay-base-url "${GAMEPLAY_BASE_URL}" \
      --require-redis-hit "${REQUIRE_REDIS_HIT}")"
  else
    canary_status="$(run_and_capture "${canary_out}" \
      /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-gameplay-canary-probe.sh \
      --bank-id "${BANK_ID}" --game-id "${GAME_ID}" --token "${TOKEN}" \
      --mode "${MODE}" --lang "${LANG}" \
      --transport "${TRANSPORT}" \
      --gs-base-url "${GS_BASE_URL}" --gameplay-base-url "${GAMEPLAY_BASE_URL}" \
      --require-redis-hit "${REQUIRE_REDIS_HIT}")"
  fi
fi

{
  echo "# Phase 5 Gameplay Runtime Evidence (${ts} UTC)"
  echo
  echo "- bankId: ${BANK_ID}"
  echo "- gameId: ${GAME_ID}"
  echo "- transport: ${TRANSPORT}"
  echo "- gsBaseUrl: ${GS_BASE_URL}"
  echo "- gameplayBaseUrl: ${GAMEPLAY_BASE_URL}"
  echo "- subCasinoId: ${SUB_CASINO_ID:-auto}"
  echo "- requireRedisHit: ${REQUIRE_REDIS_HIT}"
  echo "- readiness_check: ${readiness_status}"
  echo "- gameplay_canary_probe: ${canary_status}"
  echo
  echo "## Readiness Output"
  echo '```text'
  sed -n '1,220p' "${readiness_out}"
  echo '```'
  echo
  echo "## Canary Output"
  echo '```text'
  if [[ -s "${canary_out}" ]]; then
    sed -n '1,260p' "${canary_out}"
  elif [[ "${canary_status}" == "SKIPPED" ]]; then
    echo "Canary probe not executed because readiness check failed."
  else
    echo "Canary probe returned status ${canary_status} but produced no output."
  fi
  echo '```'
} > "${report_file}"

echo "report=${report_file}"

if [[ "${readiness_status}" != "PASS" || "${canary_status}" != "PASS" ]]; then
  exit 2
fi
