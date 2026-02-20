#!/usr/bin/env bash
set -euo pipefail

SESSION_CONTAINER="refactor-session-service-1"
MAX_NEW=10
MAX_RETRY=2
MAX_DLQ=0
SAMPLE_COUNT=1
SAMPLE_INTERVAL_SECONDS=5

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --max-new N               Threshold for NEW outbox count (default: ${MAX_NEW})
  --max-retry N             Threshold for RETRY outbox count (default: ${MAX_RETRY})
  --max-dlq N               Threshold for DLQ outbox count (default: ${MAX_DLQ})
  --sample-count N          Number of samples for trend check (default: ${SAMPLE_COUNT})
  --sample-interval-sec N   Seconds between samples (default: ${SAMPLE_INTERVAL_SECONDS})
  --session-container NAME  Session-service container (default: ${SESSION_CONTAINER})
  -h, --help                Show this help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --max-new)
      MAX_NEW="$2"; shift 2 ;;
    --max-retry)
      MAX_RETRY="$2"; shift 2 ;;
    --max-dlq)
      MAX_DLQ="$2"; shift 2 ;;
    --sample-count)
      SAMPLE_COUNT="$2"; shift 2 ;;
    --sample-interval-sec)
      SAMPLE_INTERVAL_SECONDS="$2"; shift 2 ;;
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

if ! [[ "${SAMPLE_COUNT}" =~ ^[0-9]+$ ]] || (( SAMPLE_COUNT < 1 )); then
  echo "sample-count must be a positive integer" >&2
  exit 1
fi
if ! [[ "${SAMPLE_INTERVAL_SECONDS}" =~ ^[0-9]+$ ]] || (( SAMPLE_INTERVAL_SECONDS < 0 )); then
  echo "sample-interval-sec must be a non-negative integer" >&2
  exit 1
fi

count_status() {
  local status="$1"
  docker exec "${SESSION_CONTAINER}" sh -lc "wget -qO- 'http://127.0.0.1:18073/api/v1/outbox?status=${status}'" \
    | rg -o '"eventId":"' | wc -l | tr -d ' '
}

new_samples=()
retry_samples=()
dlq_samples=()

for ((i = 1; i <= SAMPLE_COUNT; i += 1)); do
  new_samples+=("$(count_status NEW)")
  retry_samples+=("$(count_status RETRY)")
  dlq_samples+=("$(count_status DLQ)")
  if (( i < SAMPLE_COUNT && SAMPLE_INTERVAL_SECONDS > 0 )); then
    sleep "${SAMPLE_INTERVAL_SECONDS}"
  fi
done

new_count="${new_samples[$((SAMPLE_COUNT - 1))]}"
retry_count="${retry_samples[$((SAMPLE_COUNT - 1))]}"
dlq_count="${dlq_samples[$((SAMPLE_COUNT - 1))]}"

echo "outbox_counts NEW=${new_count} RETRY=${retry_count} DLQ=${dlq_count}"
echo "sampled_new=${new_samples[*]}"
echo "sampled_retry=${retry_samples[*]}"
echo "sampled_dlq=${dlq_samples[*]}"

is_strictly_increasing() {
  local -n sample_ref="$1"
  local n="${#sample_ref[@]}"
  if (( n < 2 )); then
    return 1
  fi
  for ((j = 1; j < n; j += 1)); do
    if (( sample_ref[j] <= sample_ref[j - 1] )); then
      return 1
    fi
  done
  return 0
}

if (( new_count > MAX_NEW )); then
  echo "FAIL: NEW outbox count ${new_count} exceeds threshold ${MAX_NEW}" >&2
  exit 2
fi
if (( retry_count > MAX_RETRY )); then
  echo "FAIL: RETRY outbox count ${retry_count} exceeds threshold ${MAX_RETRY}" >&2
  exit 3
fi
if (( dlq_count > MAX_DLQ )); then
  echo "FAIL: DLQ outbox count ${dlq_count} exceeds threshold ${MAX_DLQ}" >&2
  exit 4
fi

if is_strictly_increasing new_samples; then
  echo "FAIL: NEW outbox count is continuously growing across ${SAMPLE_COUNT} samples" >&2
  exit 5
fi

if is_strictly_increasing retry_samples; then
  echo "FAIL: RETRY outbox count is continuously growing across ${SAMPLE_COUNT} samples" >&2
  exit 6
fi

echo "PASS: outbox counts within thresholds"
