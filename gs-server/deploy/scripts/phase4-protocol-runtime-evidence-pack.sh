#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/cluster-hosts.sh
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

BANK_ID="6275"
BASE_URL="$(cluster_hosts_http_url PROTOCOL_ADAPTER_EXTERNAL_HOST PROTOCOL_ADAPTER_EXTERNAL_PORT 127.0.0.1 18078)"
GS_BASE_URL="$(cluster_hosts_http_url GS_EXTERNAL_HOST GS_EXTERNAL_PORT 127.0.0.1 18081)"
TRANSPORT="host"
SESSION_ID=""
RUN_SECURITY_PROBE="false"
SECURITY_PROBE_REQUIRE_SECRET="false"
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase4/protocol"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID       Default: ${BANK_ID}
  --base-url URL     Default: ${BASE_URL}
  --gs-base-url URL  Default: ${GS_BASE_URL}
  --transport MODE   host|docker (default: ${TRANSPORT})
  --session-id SID   Optional (used by wallet probe)
  --run-security-probe BOOL     true|false (default: ${RUN_SECURITY_PROBE})
  --security-require-secret B   true|false (default: ${SECURITY_PROBE_REQUIRE_SECRET})
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
    --run-security-probe)
      RUN_SECURITY_PROBE="$2"; shift 2 ;;
    --security-require-secret)
      SECURITY_PROBE_REQUIRE_SECRET="$2"; shift 2 ;;
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
report_file="${OUT_DIR}/phase4-protocol-runtime-evidence-${ts}.md"
work_dir="$(mktemp -d)"
trap 'rm -rf "${work_dir}"' EXIT

run_and_capture() {
  local name="$1"
  local out_file="$2"
  shift 2
  if "$@" >"${out_file}" 2>&1; then
    echo "PASS"
  else
    echo "FAIL"
  fi
}

parity_out="${work_dir}/parity.out"
wallet_out="${work_dir}/wallet.out"
security_out="${work_dir}/security.out"

parity_status="$(run_and_capture parity "${parity_out}" \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-json-xml-parity-check.sh \
  --bank-id "${BANK_ID}" --base-url "${BASE_URL}")"

if [[ -n "${SESSION_ID}" ]]; then
  wallet_status="$(run_and_capture wallet "${wallet_out}" \
    /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-wallet-canary-probe.sh \
    --bank-id "${BANK_ID}" --session-id "${SESSION_ID}" --transport "${TRANSPORT}" \
    --gs-base-url "${GS_BASE_URL}" --protocol-base-url "${BASE_URL}")"
else
  wallet_status="$(run_and_capture wallet "${wallet_out}" \
    /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-wallet-canary-probe.sh \
    --bank-id "${BANK_ID}" --transport "${TRANSPORT}" \
    --gs-base-url "${GS_BASE_URL}" --protocol-base-url "${BASE_URL}")"
fi

security_status="SKIPPED"
if [[ "${RUN_SECURITY_PROBE}" == "true" ]]; then
  security_status="$(run_and_capture security "${security_out}" \
    /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-json-security-canary-probe.sh \
    --bank-id "${BANK_ID}" --base-url "${BASE_URL}" \
    --require-secret "${SECURITY_PROBE_REQUIRE_SECRET}")"
fi

{
  echo "# Phase 4 Protocol Runtime Evidence (${ts} UTC)"
  echo
  echo "- bankId: ${BANK_ID}"
  echo "- transport: ${TRANSPORT}"
  echo "- protocolBaseUrl: ${BASE_URL}"
  echo "- gsBaseUrl: ${GS_BASE_URL}"
  echo "- parity_check: ${parity_status}"
  echo "- wallet_shadow_probe: ${wallet_status}"
  echo "- json_security_probe: ${security_status}"
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

if [[ "${parity_status}" != "PASS" || "${wallet_status}" != "PASS" ]]; then
  exit 2
fi

if [[ "${RUN_SECURITY_PROBE}" == "true" && "${security_status}" != "PASS" ]]; then
  exit 2
fi
