#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/dev/null
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

WALLET_HOST="$(cluster_hosts_get WALLET_ADAPTER_EXTERNAL_HOST 127.0.0.1)"
WALLET_PORT="$(cluster_hosts_get WALLET_ADAPTER_EXTERNAL_PORT 18075)"
GS_HOST="$(cluster_hosts_get GS_EXTERNAL_HOST 127.0.0.1)"
GS_PORT="$(cluster_hosts_get GS_EXTERNAL_PORT 18081)"
CHECK_DOCKER="true"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --wallet-host HOST   Default: ${WALLET_HOST}
  --wallet-port PORT   Default: ${WALLET_PORT}
  --gs-host HOST       Default: ${GS_HOST}
  --gs-port PORT       Default: ${GS_PORT}
  --check-docker BOOL  true|false (default: ${CHECK_DOCKER})
  -h, --help           Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --wallet-host)
      WALLET_HOST="$2"; shift 2 ;;
    --wallet-port)
      WALLET_PORT="$2"; shift 2 ;;
    --gs-host)
      GS_HOST="$2"; shift 2 ;;
    --gs-port)
      GS_PORT="$2"; shift 2 ;;
    --check-docker)
      CHECK_DOCKER="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

check_tcp() {
  local host="$1"
  local port="$2"
  if command -v nc >/dev/null 2>&1; then
    nc -z -w 1 "$host" "$port" >/dev/null 2>&1
    return $?
  fi
  timeout 1 bash -c "</dev/tcp/${host}/${port}" >/dev/null 2>&1
}

status=0

echo "Phase 5 Wallet Runtime Readiness"
echo "  wallet-adapter: ${WALLET_HOST}:${WALLET_PORT}"
echo "  gs:             ${GS_HOST}:${GS_PORT}"

if check_tcp "${WALLET_HOST}" "${WALLET_PORT}"; then
  echo "PASS wallet-adapter endpoint reachable"
else
  echo "FAIL wallet-adapter endpoint unreachable (${WALLET_HOST}:${WALLET_PORT})"
  status=1
fi

if check_tcp "${GS_HOST}" "${GS_PORT}"; then
  echo "PASS gs endpoint reachable"
else
  echo "FAIL gs endpoint unreachable (${GS_HOST}:${GS_PORT})"
  status=1
fi

if [[ "${CHECK_DOCKER}" == "true" ]]; then
  if command -v docker >/dev/null 2>&1; then
    if docker ps >/dev/null 2>&1; then
      echo "PASS docker socket accessible"
    else
      echo "FAIL docker socket not accessible"
      status=1
    fi
  else
    echo "FAIL docker command not available"
    status=1
  fi
fi

if [[ ${status} -eq 0 ]]; then
  echo "READY: runtime checks passed"
else
  echo "NOT_READY: fix failed checks before running phase5 wallet canary probe"
fi

exit ${status}
