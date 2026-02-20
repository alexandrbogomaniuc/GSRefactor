#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="/Users/alexb/Documents/Dev/Dev_new/gs-server"
VALIDATOR="${ROOT_DIR}/refactor-services/contracts/validators/validate-session-event-stream.js"

KAFKA_CONTAINER="refactor-kafka-1"
BOOTSTRAP_SERVER="kafka:9092"
TOPIC="abs.session.events.v1"
MAX_MESSAGES=50
TIMEOUT_MS=15000

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --topic NAME              Kafka topic (default: ${TOPIC})
  --max-messages N          Max messages to consume (default: ${MAX_MESSAGES})
  --timeout-ms N            Consumer timeout ms (default: ${TIMEOUT_MS})
  --kafka-container NAME    Docker container with kafka-cli (default: ${KAFKA_CONTAINER})
  --bootstrap SERVER        Bootstrap server (default: ${BOOTSTRAP_SERVER})
  -h, --help                Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --topic)
      TOPIC="$2"; shift 2 ;;
    --max-messages)
      MAX_MESSAGES="$2"; shift 2 ;;
    --timeout-ms)
      TIMEOUT_MS="$2"; shift 2 ;;
    --kafka-container)
      KAFKA_CONTAINER="$2"; shift 2 ;;
    --bootstrap)
      BOOTSTRAP_SERVER="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

if [[ ! -x "${VALIDATOR}" ]]; then
  echo "Missing validator: ${VALIDATOR}" >&2
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "docker command is required" >&2
  exit 1
fi

if ! command -v rg >/dev/null 2>&1; then
  echo "rg command is required" >&2
  exit 1
fi

tmp_dir="$(mktemp -d)"
raw_file="${tmp_dir}/raw.log"
json_file="${tmp_dir}/events.jsonl"
trap 'rm -rf "${tmp_dir}"' EXIT

set +e
docker exec "${KAFKA_CONTAINER}" bash -lc \
  "kafka-console-consumer --bootstrap-server ${BOOTSTRAP_SERVER} --topic ${TOPIC} --from-beginning --max-messages ${MAX_MESSAGES} --timeout-ms ${TIMEOUT_MS}" \
  2>&1 | tee "${raw_file}" >/dev/null
consume_exit=${PIPESTATUS[0]}
set -e

rg '^\{' "${raw_file}" > "${json_file}" || true

if [[ ! -s "${json_file}" ]]; then
  echo "FAIL: no JSON messages consumed from topic ${TOPIC}" >&2
  echo "Consumer output:" >&2
  cat "${raw_file}" >&2
  exit 2
fi

node "${VALIDATOR}" "${json_file}"

echo "PASS: contract check topic=${TOPIC} messages=$(wc -l < "${json_file}" | tr -d ' ') consumer_exit=${consume_exit}"
