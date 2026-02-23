#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/cluster-hosts.sh
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

HISTORY_HOST="$(cluster_hosts_get HISTORY_SERVICE_EXTERNAL_HOST 127.0.0.1)"
HISTORY_PORT="$(cluster_hosts_get HISTORY_SERVICE_EXTERNAL_PORT 18077)"
GS_HOST="$(cluster_hosts_get GS_EXTERNAL_HOST 127.0.0.1)"
GS_PORT="$(cluster_hosts_get GS_EXTERNAL_PORT 18081)"
CHECK_DOCKER="true"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --history-host HOST  Default: ${HISTORY_HOST}
  --history-port PORT  Default: ${HISTORY_PORT}
  --gs-host HOST       Default: ${GS_HOST}
  --gs-port PORT       Default: ${GS_PORT}
  --check-docker BOOL  true|false (default: ${CHECK_DOCKER})
  -h, --help           Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --history-host)
      HISTORY_HOST="$2"; shift 2 ;;
    --history-port)
      HISTORY_PORT="$2"; shift 2 ;;
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

echo "Phase 5 History Runtime Readiness"
echo "  history-service: ${HISTORY_HOST}:${HISTORY_PORT}"
echo "  gs:              ${GS_HOST}:${GS_PORT}"

if check_tcp "${HISTORY_HOST}" "${HISTORY_PORT}"; then
  echo "PASS history-service endpoint reachable"
else
  echo "FAIL history-service endpoint unreachable (${HISTORY_HOST}:${HISTORY_PORT})"
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
  echo "NOT_READY: fix failed checks before running phase5 history canary probe"
fi

exit ${status}
