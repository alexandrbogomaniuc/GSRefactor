#!/usr/bin/env bash
set -euo pipefail

PROTOCOL_HOST="127.0.0.1"
PROTOCOL_PORT="18078"
GS_HOST="127.0.0.1"
GS_PORT="18081"
CHECK_DOCKER="true"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --protocol-host HOST   Default: ${PROTOCOL_HOST}
  --protocol-port PORT   Default: ${PROTOCOL_PORT}
  --gs-host HOST         Default: ${GS_HOST}
  --gs-port PORT         Default: ${GS_PORT}
  --check-docker BOOL    true|false (default: ${CHECK_DOCKER})
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

echo "Phase 4 Runtime Readiness"
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
  echo "NOT_READY: fix failed checks before running phase4 evidence pack"
fi

exit ${status}
