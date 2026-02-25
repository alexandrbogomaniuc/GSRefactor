#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./lib/cluster-hosts.sh
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"
ROOT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"

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
OUT_DIR="${ROOT_DIR}/../docs/phase6/multiplayer"
POLICY_EXPECT_BANK_MP_ENABLED="false"
POLICY_EXPECT_NON_MP_REASON="non_multiplayer_game"
POLICY_EXPECT_NON_MP_ROUTE="false"
POLICY_EXPECT_MP_REASON="bank_multiplayer_disabled"
POLICY_EXPECT_MP_ROUTE="false"
policy_expect_bank_mp_enabled_set="false"
policy_expect_mp_reason_set="false"
policy_expect_mp_route_set="false"

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
  --policy-expect-bank-mp-enabled BOOL  true|false (default: ${POLICY_EXPECT_BANK_MP_ENABLED})
  --policy-expect-non-mp-reason VALUE   Default: ${POLICY_EXPECT_NON_MP_REASON}
  --policy-expect-non-mp-route BOOL     true|false (default: ${POLICY_EXPECT_NON_MP_ROUTE})
  --policy-expect-mp-reason VALUE       Default: ${POLICY_EXPECT_MP_REASON}
  --policy-expect-mp-route BOOL         true|false (default: ${POLICY_EXPECT_MP_ROUTE})
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
    --policy-expect-bank-mp-enabled)
      POLICY_EXPECT_BANK_MP_ENABLED="$2"; policy_expect_bank_mp_enabled_set="true"; shift 2 ;;
    --policy-expect-non-mp-reason)
      POLICY_EXPECT_NON_MP_REASON="$2"; shift 2 ;;
    --policy-expect-non-mp-route)
      POLICY_EXPECT_NON_MP_ROUTE="$2"; shift 2 ;;
    --policy-expect-mp-reason)
      POLICY_EXPECT_MP_REASON="$2"; policy_expect_mp_reason_set="true"; shift 2 ;;
    --policy-expect-mp-route)
      POLICY_EXPECT_MP_ROUTE="$2"; policy_expect_mp_route_set="true"; shift 2 ;;
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

for expected_bool in "${CHECK_DOCKER}" "${POLICY_EXPECT_BANK_MP_ENABLED}" "${POLICY_EXPECT_NON_MP_ROUTE}" "${POLICY_EXPECT_MP_ROUTE}"; do
  if [[ "${expected_bool}" != "true" && "${expected_bool}" != "false" ]]; then
    echo "Invalid boolean value: ${expected_bool}. Use true|false." >&2
    exit 1
  fi
done

# In sync-canary mode, default expectations should represent an eligible multiplayer route.
if [[ "${RUN_SYNC_CANARY}" == "true" ]]; then
  if [[ "${policy_expect_bank_mp_enabled_set}" != "true" ]]; then
    POLICY_EXPECT_BANK_MP_ENABLED="true"
  fi
  if [[ "${policy_expect_mp_route_set}" != "true" ]]; then
    POLICY_EXPECT_MP_ROUTE="true"
  fi
  if [[ "${policy_expect_mp_reason_set}" != "true" ]]; then
    POLICY_EXPECT_MP_REASON="eligible"
  fi
fi

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
  "${SCRIPT_DIR}/phase6-multiplayer-runtime-readiness-check.sh" \
  --multiplayer-host "${READINESS_MULTIPLAYER_HOST}" --multiplayer-port "${READINESS_MULTIPLAYER_PORT}" \
  --gs-host "${READINESS_GS_HOST}" --gs-port "${READINESS_GS_PORT}" \
  --check-docker "${CHECK_DOCKER}")"

policy_status="SKIPPED"
canary_status="SKIPPED"
if [[ "${readiness_status}" == "PASS" ]]; then
  policy_status="$(run_and_capture "${policy_out}" \
    "${SCRIPT_DIR}/phase6-multiplayer-routing-policy-probe.sh" \
    --bank-id "${BANK_ID}" --game-id "${GAME_ID}" \
    --transport "${TRANSPORT}" \
    --multiplayer-base-url "${MULTIPLAYER_BASE_URL}" \
    --expect-bank-mp-enabled "${POLICY_EXPECT_BANK_MP_ENABLED}" \
    --expect-non-mp-reason "${POLICY_EXPECT_NON_MP_REASON}" \
    --expect-non-mp-route "${POLICY_EXPECT_NON_MP_ROUTE}" \
    --expect-mp-reason "${POLICY_EXPECT_MP_REASON}" \
    --expect-mp-route "${POLICY_EXPECT_MP_ROUTE}")"

  if [[ "${RUN_SYNC_CANARY}" == "true" ]]; then
    canary_status="$(run_and_capture "${canary_out}" \
      "${SCRIPT_DIR}/phase6-multiplayer-canary-probe.sh" \
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
  echo "- policyExpectBankMpEnabled: ${POLICY_EXPECT_BANK_MP_ENABLED}"
  echo "- policyExpectNonMpRoute: ${POLICY_EXPECT_NON_MP_ROUTE}"
  echo "- policyExpectNonMpReason: ${POLICY_EXPECT_NON_MP_REASON}"
  echo "- policyExpectMpRoute: ${POLICY_EXPECT_MP_ROUTE}"
  echo "- policyExpectMpReason: ${POLICY_EXPECT_MP_REASON}"
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
