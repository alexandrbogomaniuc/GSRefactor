#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/cluster-hosts.sh
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

BANK_ID="6275"
GAME_ID="838"
TRANSPORT="host"
MULTIPLAYER_BASE_URL="$(cluster_hosts_http_url MULTIPLAYER_SERVICE_EXTERNAL_HOST MULTIPLAYER_SERVICE_EXTERNAL_PORT 127.0.0.1 18079)"
RUN_SYNC_CANARY="false"
READINESS_MULTIPLAYER_HOST="$(cluster_hosts_get MULTIPLAYER_SERVICE_EXTERNAL_HOST 127.0.0.1)"
READINESS_MULTIPLAYER_PORT="$(cluster_hosts_get MULTIPLAYER_SERVICE_EXTERNAL_PORT 18079)"
READINESS_GS_HOST="$(cluster_hosts_get GS_EXTERNAL_HOST 127.0.0.1)"
READINESS_GS_PORT="$(cluster_hosts_get GS_EXTERNAL_PORT 18081)"
CHECK_DOCKER="true"
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase6/multiplayer"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID                   Default: ${BANK_ID}
  --game-id ID                   Default: ${GAME_ID}
  --transport MODE               host|docker (default: ${TRANSPORT})
  --multiplayer-base-url URL     Default: ${MULTIPLAYER_BASE_URL}
  --run-sync-canary BOOL         true|false (default: ${RUN_SYNC_CANARY})
  --readiness-multiplayer-host H Default: ${READINESS_MULTIPLAYER_HOST}
  --readiness-multiplayer-port P Default: ${READINESS_MULTIPLAYER_PORT}
  --readiness-gs-host H          Default: ${READINESS_GS_HOST}
  --readiness-gs-port P          Default: ${READINESS_GS_PORT}
  --check-docker BOOL            true|false (default: ${CHECK_DOCKER})
  --out-dir DIR                  Default: ${OUT_DIR}
  -h, --help                     Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bank-id)
      BANK_ID="$2"; shift 2 ;;
    --game-id)
      GAME_ID="$2"; shift 2 ;;
    --transport)
      TRANSPORT="$2"; shift 2 ;;
    --multiplayer-base-url)
      MULTIPLAYER_BASE_URL="$2"; shift 2 ;;
    --run-sync-canary)
      RUN_SYNC_CANARY="$2"; shift 2 ;;
    --readiness-multiplayer-host)
      READINESS_MULTIPLAYER_HOST="$2"; shift 2 ;;
    --readiness-multiplayer-port)
      READINESS_MULTIPLAYER_PORT="$2"; shift 2 ;;
    --readiness-gs-host)
      READINESS_GS_HOST="$2"; shift 2 ;;
    --readiness-gs-port)
      READINESS_GS_PORT="$2"; shift 2 ;;
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
report_file="${OUT_DIR}/phase6-multiplayer-runtime-evidence-${ts}.md"
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
policy_out="${work_dir}/policy.out"

readiness_status="$(run_and_capture "${readiness_out}" \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase6-multiplayer-runtime-readiness-check.sh \
  --multiplayer-host "${READINESS_MULTIPLAYER_HOST}" --multiplayer-port "${READINESS_MULTIPLAYER_PORT}" \
  --gs-host "${READINESS_GS_HOST}" --gs-port "${READINESS_GS_PORT}" \
  --check-docker "${CHECK_DOCKER}")"

policy_status="SKIPPED"
canary_status="SKIPPED"
if [[ "${readiness_status}" == "PASS" ]]; then
  policy_status="$(run_and_capture "${policy_out}" \
    /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase6-multiplayer-routing-policy-probe.sh \
    --bank-id "${BANK_ID}" --game-id "${GAME_ID}" \
    --transport "${TRANSPORT}" \
    --multiplayer-base-url "${MULTIPLAYER_BASE_URL}")"

  if [[ "${RUN_SYNC_CANARY}" == "true" ]]; then
    canary_status="$(run_and_capture "${canary_out}" \
      /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase6-multiplayer-canary-probe.sh \
      --bank-id "${BANK_ID}" --game-id "${GAME_ID}" \
      --transport "${TRANSPORT}" \
      --multiplayer-base-url "${MULTIPLAYER_BASE_URL}")"
  fi
fi

{
  echo "# Phase 6 Multiplayer Runtime Evidence (${ts} UTC)"
  echo
  echo "- bankId: ${BANK_ID}"
  echo "- gameId: ${GAME_ID}"
  echo "- transport: ${TRANSPORT}"
  echo "- multiplayerBaseUrl: ${MULTIPLAYER_BASE_URL}"
  echo "- runSyncCanary: ${RUN_SYNC_CANARY}"
  echo "- readiness_check: ${readiness_status}"
  echo "- multiplayer_routing_policy_probe: ${policy_status}"
  echo "- multiplayer_canary_probe: ${canary_status}"
  echo
  echo "## Readiness Output"
  echo '```text'
  sed -n '1,220p' "${readiness_out}"
  echo '```'
  echo
  echo "## Routing Policy Probe Output"
  echo '```text'
  if [[ -s "${policy_out}" ]]; then
    sed -n '1,260p' "${policy_out}"
  else
    echo "Routing policy probe not executed because readiness check failed."
  fi
  echo '```'
  echo
  echo "## Canary Output"
  echo '```text'
  if [[ -s "${canary_out}" ]]; then
    sed -n '1,260p' "${canary_out}"
  else
    if [[ "${readiness_status}" != "PASS" ]]; then
      echo "Canary probe not executed because readiness check failed."
    elif [[ "${RUN_SYNC_CANARY}" != "true" ]]; then
      echo "Canary probe not executed because --run-sync-canary=false (default safe mode)."
    else
      echo "Canary probe did not produce output."
    fi
  fi
  echo '```'
} > "${report_file}"

echo "report=${report_file}"

if [[ "${readiness_status}" != "PASS" || "${policy_status}" != "PASS" ]]; then
  exit 2
fi

if [[ "${RUN_SYNC_CANARY}" == "true" && "${canary_status}" != "PASS" ]]; then
  exit 2
fi
