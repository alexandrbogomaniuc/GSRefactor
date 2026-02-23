#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/cluster-hosts.sh
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

BANK_ID="6275"
TRANSPORT="host"
HISTORY_BASE_URL="$(cluster_hosts_http_url HISTORY_SERVICE_EXTERNAL_HOST HISTORY_SERVICE_EXTERNAL_PORT 127.0.0.1 18077)"
READINESS_HISTORY_HOST="$(cluster_hosts_get HISTORY_SERVICE_EXTERNAL_HOST 127.0.0.1)"
READINESS_HISTORY_PORT="$(cluster_hosts_get HISTORY_SERVICE_EXTERNAL_PORT 18077)"
READINESS_GS_HOST="$(cluster_hosts_get GS_EXTERNAL_HOST 127.0.0.1)"
READINESS_GS_PORT="$(cluster_hosts_get GS_EXTERNAL_PORT 18081)"
CHECK_DOCKER="true"
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase5/history"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --bank-id ID               Default: ${BANK_ID}
  --transport MODE           host|docker (default: ${TRANSPORT})
  --history-base-url URL     Default: ${HISTORY_BASE_URL}
  --readiness-history-host H Default: ${READINESS_HISTORY_HOST}
  --readiness-history-port P Default: ${READINESS_HISTORY_PORT}
  --readiness-gs-host H      Default: ${READINESS_GS_HOST}
  --readiness-gs-port P      Default: ${READINESS_GS_PORT}
  --check-docker BOOL        true|false (default: ${CHECK_DOCKER})
  --out-dir DIR              Default: ${OUT_DIR}
  -h, --help                 Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --bank-id)
      BANK_ID="$2"; shift 2 ;;
    --transport)
      TRANSPORT="$2"; shift 2 ;;
    --history-base-url)
      HISTORY_BASE_URL="$2"; shift 2 ;;
    --readiness-history-host)
      READINESS_HISTORY_HOST="$2"; shift 2 ;;
    --readiness-history-port)
      READINESS_HISTORY_PORT="$2"; shift 2 ;;
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
report_file="${OUT_DIR}/phase5-history-runtime-evidence-${ts}.md"
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
  /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-history-runtime-readiness-check.sh \
  --history-host "${READINESS_HISTORY_HOST}" --history-port "${READINESS_HISTORY_PORT}" \
  --gs-host "${READINESS_GS_HOST}" --gs-port "${READINESS_GS_PORT}" \
  --check-docker "${CHECK_DOCKER}")"

canary_status="SKIPPED"
if [[ "${readiness_status}" == "PASS" ]]; then
  canary_status="$(run_and_capture "${canary_out}" \
    /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-history-canary-probe.sh \
    --bank-id "${BANK_ID}" \
    --transport "${TRANSPORT}" \
    --history-base-url "${HISTORY_BASE_URL}")"
fi

{
  echo "# Phase 5 History Runtime Evidence (${ts} UTC)"
  echo
  echo "- bankId: ${BANK_ID}"
  echo "- transport: ${TRANSPORT}"
  echo "- historyBaseUrl: ${HISTORY_BASE_URL}"
  echo "- readiness_check: ${readiness_status}"
  echo "- history_canary_probe: ${canary_status}"
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
  else
    echo "Canary probe not executed because readiness check failed."
  fi
  echo '```'
} > "${report_file}"

echo "report=${report_file}"

if [[ "${readiness_status}" != "PASS" || "${canary_status}" != "PASS" ]]; then
  exit 2
fi
