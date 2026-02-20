#!/usr/bin/env bash
set -euo pipefail

SESSION_CONTAINER="refactor-session-service-1"
EVENT_ID=""
REASON="manual-replay"

usage() {
  cat <<USAGE
Usage: $(basename "$0") --event-id EVENT_ID [options]

Options:
  --event-id ID              Required DLQ event id
  --reason TEXT              Replay reason (default: ${REASON})
  --session-container NAME   Session-service container (default: ${SESSION_CONTAINER})
  -h, --help                 Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --event-id)
      EVENT_ID="$2"; shift 2 ;;
    --reason)
      REASON="$2"; shift 2 ;;
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

if [[ -z "${EVENT_ID}" ]]; then
  echo "--event-id is required" >&2
  usage
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "docker command is required" >&2
  exit 1
fi

escaped_reason="${REASON// /%20}"

echo "Requeueing DLQ event: ${EVENT_ID}"
docker exec "${SESSION_CONTAINER}" sh -lc \
  "wget -qO- --post-data='' 'http://127.0.0.1:18073/api/v1/outbox/${EVENT_ID}/requeue?reason=${escaped_reason}'"

echo
echo "DLQ queue after replay:"
docker exec "${SESSION_CONTAINER}" sh -lc "wget -qO- 'http://127.0.0.1:18073/api/v1/outbox?status=DLQ'"
