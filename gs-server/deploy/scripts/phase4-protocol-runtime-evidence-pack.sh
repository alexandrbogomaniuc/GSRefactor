#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
# shellcheck source=/dev/null
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

BANK_ID="6275"
BASE_URL="$(cluster_hosts_http_url PROTOCOL_ADAPTER_EXTERNAL_HOST PROTOCOL_ADAPTER_EXTERNAL_PORT 127.0.0.1 18078)"
GS_BASE_URL="$(cluster_hosts_http_url GS_EXTERNAL_HOST GS_EXTERNAL_PORT 127.0.0.1 18081)"
TRANSPORT="host"
SESSION_ID=""
SUB_CASINO_ID=""
TOKEN="test_user_6275"
RUN_SECURITY_PROBE="false"
SECURITY_PROBE_REQUIRE_SECRET="false"
ALLOW_MISSING_RUNTIME="false"
OUT_DIR="${REPO_ROOT}/docs/phase4/protocol"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID       Default: ${BANK_ID}
  --base-url URL     Default: ${BASE_URL}
  --gs-base-url URL  Default: ${GS_BASE_URL}
  --transport MODE   host|docker (default: ${TRANSPORT})
  --session-id SID   Optional (used by wallet probe)
  --sub-casino-id ID Optional (appended to startgame launch URL for auto session resolution)
  --token TOKEN      Default: ${TOKEN} (used by wallet probe when session-id is omitted)
  --run-security-probe BOOL     true|false (default: ${RUN_SECURITY_PROBE})
  --security-require-secret B   true|false (default: ${SECURITY_PROBE_REQUIRE_SECRET})
  --allow-missing-runtime B     true|false (default: ${ALLOW_MISSING_RUNTIME})
  --out-dir DIR      Default: ${OUT_DIR}
  -h, --help         Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bank-id)
      BANK_ID="$2"; shift 2 ;;
    --base-url)
      BASE_URL="$2"; shift 2 ;;
    --gs-base-url)
      GS_BASE_URL="$2"; shift 2 ;;
    --transport)
      TRANSPORT="$2"; shift 2 ;;
    --session-id)
      SESSION_ID="$2"; shift 2 ;;
    --sub-casino-id)
      SUB_CASINO_ID="$2"; shift 2 ;;
    --token)
      TOKEN="$2"; shift 2 ;;
    --run-security-probe)
      RUN_SECURITY_PROBE="$2"; shift 2 ;;
    --security-require-secret)
      SECURITY_PROBE_REQUIRE_SECRET="$2"; shift 2 ;;
    --allow-missing-runtime)
      ALLOW_MISSING_RUNTIME="$2"; shift 2 ;;
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

if [[ "${ALLOW_MISSING_RUNTIME}" != "true" && "${ALLOW_MISSING_RUNTIME}" != "false" ]]; then
  echo "Invalid --allow-missing-runtime: ${ALLOW_MISSING_RUNTIME}" >&2
  exit 1
fi

mkdir -p "${OUT_DIR}"
ts="$(date -u +%Y%m%d-%H%M%S)"
report_file="${OUT_DIR}/phase4-protocol-runtime-evidence-${ts}.md"
work_dir="$(mktemp -d)"
trap 'rm -rf "${work_dir}"' EXIT

classify_probe_failure() {
  local out_file="$1"
  if [[ "${ALLOW_MISSING_RUNTIME}" == "true" ]] && grep -Eqi \
    'permission denied while trying to connect to the docker API|docker socket not accessible' \
    "${out_file}" 2>/dev/null; then
    echo "SKIP_DOCKER_API_DENIED"
    return 0
  fi
  if [[ "${ALLOW_MISSING_RUNTIME}" == "true" ]] && grep -Eqi \
    'curl: \(7\)|Failed to connect|No such container|could not auto-resolve sessionId|HTTP (GET|POST) failed: .* -> 000|Connection refused' \
    "${out_file}" 2>/dev/null; then
    echo "SKIP_RUNTIME_UNAVAILABLE"
    return 0
  fi
  echo "FAIL"
}

run_and_capture() {
  local name="$1"
  local out_file="$2"
  shift 2
  if "$@" >"${out_file}" 2>&1; then
    echo "PASS"
  else
    classify_probe_failure "${out_file}"
  fi
}

readiness_out="${work_dir}/readiness.out"
parity_out="${work_dir}/parity.out"
wallet_out="${work_dir}/wallet.out"
security_out="${work_dir}/security.out"

readiness_check_docker="false"
if [[ "${TRANSPORT}" == "docker" ]]; then
  readiness_check_docker="true"
fi
readiness_status="$(run_and_capture readiness "${readiness_out}" \
  ${REPO_ROOT}/gs-server/deploy/scripts/phase4-runtime-readiness-check.sh \
  --check-docker "${readiness_check_docker}" \
  --transport "${TRANSPORT}")"

skip_runtime_probes="false"
if [[ "${ALLOW_MISSING_RUNTIME}" == "true" && "${readiness_status}" != "PASS" ]]; then
  skip_runtime_probes="true"
  echo "Runtime probes skipped because readiness_status=${readiness_status} and --allow-missing-runtime=true." > "${parity_out}"
  cp "${parity_out}" "${wallet_out}"
  cp "${parity_out}" "${security_out}"
  parity_status="SKIP_RUNTIME_NOT_READY"
  wallet_status="SKIP_RUNTIME_NOT_READY"
  if [[ "${RUN_SECURITY_PROBE}" == "true" ]]; then
    security_status="SKIP_RUNTIME_NOT_READY"
  else
    security_status="SKIPPED"
  fi
fi

if [[ "${skip_runtime_probes}" != "true" ]]; then
  parity_status="$(run_and_capture parity "${parity_out}" \
    ${REPO_ROOT}/gs-server/deploy/scripts/phase4-json-xml-parity-check.sh \
    --bank-id "${BANK_ID}" --base-url "${BASE_URL}" --transport "${TRANSPORT}")"

  if [[ -n "${SESSION_ID}" ]]; then
    if [[ -n "${SUB_CASINO_ID}" ]]; then
      wallet_status="$(run_and_capture wallet "${wallet_out}" \
        ${REPO_ROOT}/gs-server/deploy/scripts/phase4-protocol-wallet-canary-probe.sh \
        --bank-id "${BANK_ID}" --session-id "${SESSION_ID}" --token "${TOKEN}" --sub-casino-id "${SUB_CASINO_ID}" --transport "${TRANSPORT}" \
        --gs-base-url "${GS_BASE_URL}" --protocol-base-url "${BASE_URL}")"
    else
      wallet_status="$(run_and_capture wallet "${wallet_out}" \
        ${REPO_ROOT}/gs-server/deploy/scripts/phase4-protocol-wallet-canary-probe.sh \
        --bank-id "${BANK_ID}" --session-id "${SESSION_ID}" --token "${TOKEN}" --transport "${TRANSPORT}" \
        --gs-base-url "${GS_BASE_URL}" --protocol-base-url "${BASE_URL}")"
    fi
  else
    if [[ -n "${SUB_CASINO_ID}" ]]; then
      wallet_status="$(run_and_capture wallet "${wallet_out}" \
        ${REPO_ROOT}/gs-server/deploy/scripts/phase4-protocol-wallet-canary-probe.sh \
        --bank-id "${BANK_ID}" --token "${TOKEN}" --sub-casino-id "${SUB_CASINO_ID}" --transport "${TRANSPORT}" \
        --gs-base-url "${GS_BASE_URL}" --protocol-base-url "${BASE_URL}")"
    else
      wallet_status="$(run_and_capture wallet "${wallet_out}" \
        ${REPO_ROOT}/gs-server/deploy/scripts/phase4-protocol-wallet-canary-probe.sh \
        --bank-id "${BANK_ID}" --token "${TOKEN}" --transport "${TRANSPORT}" \
        --gs-base-url "${GS_BASE_URL}" --protocol-base-url "${BASE_URL}")"
    fi
  fi

  security_status="SKIPPED"
  if [[ "${RUN_SECURITY_PROBE}" == "true" ]]; then
    security_status="$(run_and_capture security "${security_out}" \
      ${REPO_ROOT}/gs-server/deploy/scripts/phase4-protocol-json-security-canary-probe.sh \
      --bank-id "${BANK_ID}" --base-url "${BASE_URL}" \
      --require-secret "${SECURITY_PROBE_REQUIRE_SECRET}")"
  fi
fi

{
  echo "# Phase 4 Protocol Runtime Evidence (${ts} UTC)"
  echo
  echo "- bankId: ${BANK_ID}"
  echo "- transport: ${TRANSPORT}"
  echo "- protocolBaseUrl: ${BASE_URL}"
  echo "- gsBaseUrl: ${GS_BASE_URL}"
  echo "- subCasinoId: ${SUB_CASINO_ID:-auto}"
  echo "- token: ${TOKEN}"
  echo "- allowMissingRuntime: ${ALLOW_MISSING_RUNTIME}"
  echo "- runtime_readiness: ${readiness_status}"
  echo "- parity_check: ${parity_status}"
  echo "- wallet_shadow_probe: ${wallet_status}"
  echo "- json_security_probe: ${security_status}"
  if [[ "${skip_runtime_probes}" == "true" ]]; then
    echo "- note: runtime probes skipped because readiness failed and allowMissingRuntime=true"
  fi
  echo
  echo "## Runtime Readiness Output"
  echo '```text'
  sed -n '1,200p' "${readiness_out}"
  echo '```'
  echo
  echo "## Parity Check Output"
  echo '```text'
  sed -n '1,200p' "${parity_out}"
  echo '```'
  echo
  echo "## Wallet Shadow Probe Output"
  echo '```text'
  sed -n '1,220p' "${wallet_out}"
  echo '```'
  echo
  echo "## JSON Security Probe Output"
  echo '```text'
  if [[ -s "${security_out}" ]]; then
    sed -n '1,220p' "${security_out}"
  else
    if [[ "${RUN_SECURITY_PROBE}" != "true" ]]; then
      echo "Security probe not executed because --run-security-probe=false (default safe mode)."
    else
      echo "Security probe did not produce output."
    fi
  fi
  echo '```'
} > "${report_file}"

echo "report=${report_file}"

if [[ "${parity_status}" == "FAIL" || "${wallet_status}" == "FAIL" ]]; then
  exit 2
fi

if [[ "${RUN_SECURITY_PROBE}" == "true" && "${security_status}" == "FAIL" ]]; then
  exit 2
fi
