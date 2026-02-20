#!/usr/bin/env bash
set -euo pipefail

SESSION_CONTAINER="refactor-session-service-1"
LIMIT=20

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --limit N                 Rows per list (default: ${LIMIT})
  --session-container NAME  Session-service container (default: ${SESSION_CONTAINER})
  -h, --help                Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --limit)
      LIMIT="$2"; shift 2 ;;
    --session-container)
      SESSION_CONTAINER="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

if ! command -v docker >/dev/null 2>&1; then
  echo "docker command is required" >&2
  exit 1
fi

docker exec "${SESSION_CONTAINER}" sh -lc \
  "wget -qO- 'http://127.0.0.1:18073/api/v1/outbox/replay-report?limit=${LIMIT}'"
