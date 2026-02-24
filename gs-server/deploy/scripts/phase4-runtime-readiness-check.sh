#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/cluster-hosts.sh
source "${SCRIPT_DIR}/lib/cluster-hosts.sh"

PROTOCOL_HOST="$(cluster_hosts_get PROTOCOL_ADAPTER_EXTERNAL_HOST 127.0.0.1)"
PROTOCOL_PORT="$(cluster_hosts_get PROTOCOL_ADAPTER_EXTERNAL_PORT 18078)"
GS_HOST="$(cluster_hosts_get GS_EXTERNAL_HOST 127.0.0.1)"
GS_PORT="$(cluster_hosts_get GS_EXTERNAL_PORT 18081)"
CHECK_DOCKER="true"
TRANSPORT="host"
PROTOCOL_CONTAINER="refactor-protocol-adapter-1"
GS_CONTAINER="refactor-gs-1"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --protocol-host HOST   Default: ${PROTOCOL_HOST}
  --protocol-port PORT   Default: ${PROTOCOL_PORT}
  --gs-host HOST         Default: ${GS_HOST}
  --gs-port PORT         Default: ${GS_PORT}
  --check-docker BOOL    true|false (default: ${CHECK_DOCKER})
  --transport MODE       host|docker (default: ${TRANSPORT})
  --protocol-container N Default: ${PROTOCOL_CONTAINER}
  --gs-container N       Default: ${GS_CONTAINER}
  -h, --help             Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --protocol-host)
      PROTOCOL_HOST="$2"; shift 2 ;;
    --protocol-port)
      PROTOCOL_PORT="$2"; shift 2 ;;
    --gs-host)
      GS_HOST="$2"; shift 2 ;;
    --gs-port)
      GS_PORT="$2"; shift 2 ;;
    --check-docker)
      CHECK_DOCKER="$2"; shift 2 ;;
    --transport)
      TRANSPORT="$2"; shift 2 ;;
    --protocol-container)
      PROTOCOL_CONTAINER="$2"; shift 2 ;;
    --gs-container)
      GS_CONTAINER="$2"; shift 2 ;;
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

check_http_in_container() {
  local container="$1"
  local cmd="$2"
  docker exec "${container}" sh -lc "${cmd}" >/dev/null
}

status=0

echo "Phase 4 Runtime Readiness"
echo "  transport: ${TRANSPORT}"
if [[ "${TRANSPORT}" == "docker" ]]; then
  echo "  protocol-container: ${PROTOCOL_CONTAINER}"
  echo "  gs-container:       ${GS_CONTAINER}"
  if check_http_in_container "${PROTOCOL_CONTAINER}" "wget -qO- 'http://127.0.0.1:18078/api/v1/protocol/events?limit=1'"; then
    echo "PASS protocol endpoint reachable (container-local)"
  else
    echo "FAIL protocol endpoint unreachable (container-local ${PROTOCOL_CONTAINER}:18078)"
    status=1
  fi
  if check_http_in_container "${GS_CONTAINER}" "curl -sS -o /dev/null 'http://127.0.0.1:8080/support/configPortal.jsp'"; then
    echo "PASS gs endpoint reachable (container-local)"
  else
    echo "FAIL gs endpoint unreachable (container-local ${GS_CONTAINER}:8080)"
    status=1
  fi
else
  echo "  protocol: ${PROTOCOL_HOST}:${PROTOCOL_PORT}"
  echo "  gs:       ${GS_HOST}:${GS_PORT}"
  if check_tcp "${PROTOCOL_HOST}" "${PROTOCOL_PORT}"; then
    echo "PASS protocol endpoint reachable"
  else
    echo "FAIL protocol endpoint unreachable (${PROTOCOL_HOST}:${PROTOCOL_PORT})"
    status=1
  fi
  if check_tcp "${GS_HOST}" "${GS_PORT}"; then
    echo "PASS gs endpoint reachable"
  else
    echo "FAIL gs endpoint unreachable (${GS_HOST}:${GS_PORT})"
    status=1
  fi
fi

if [[ "${CHECK_DOCKER}" == "true" ]]; then
  if command -v docker >/dev/null 2>&1; then
    if docker ps >/dev/null; then
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
  echo "NOT_READY: fix failed checks before running phase4 evidence pack"
fi

exit ${status}
