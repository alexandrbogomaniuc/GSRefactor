#!/usr/bin/env bash
set -euo pipefail

BANK_ID="6275"
BASE_URL="http://127.0.0.1:18078"
SESSION_ID=""
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase4/protocol"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID       Default: ${BANK_ID}
  --base-url URL     Default: ${BASE_URL}
  --session-id SID   Optional (used by wallet probe)
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
    --session-id)
      SESSION_ID="$2"; shift 2 ;;
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

parity_status="$(run_and_capture parity "${parity_out}" \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-json-xml-parity-check.sh \
  --bank-id "${BANK_ID}" --base-url "${BASE_URL}")"

if [[ -n "${SESSION_ID}" ]]; then
  wallet_status="$(run_and_capture wallet "${wallet_out}" \
    /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-wallet-canary-probe.sh \
    --bank-id "${BANK_ID}" --session-id "${SESSION_ID}")"
else
  wallet_status="$(run_and_capture wallet "${wallet_out}" \
    /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-wallet-canary-probe.sh \
    --bank-id "${BANK_ID}")"
fi

{
  echo "# Phase 4 Protocol Runtime Evidence (${ts} UTC)"
  echo
  echo "- bankId: ${BANK_ID}"
  echo "- parity_check: ${parity_status}"
  echo "- wallet_shadow_probe: ${wallet_status}"
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
} > "${report_file}"

echo "report=${report_file}"

if [[ "${parity_status}" != "PASS" || "${wallet_status}" != "PASS" ]]; then
  exit 2
fi
