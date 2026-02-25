#!/usr/bin/env bash

set -euo pipefail

DEV_ROOT="${DEV_ROOT:-/Users/alexb/Documents/Dev}"
NGS_DIR="${NGS_DIR:-$DEV_ROOT/new-games-server}"
NGS_BASE_URL="${NGS_BASE_URL:-http://localhost:6400}"
AUTO_START_NGS="${AUTO_START_NGS:-1}"
REPORT_DIR="${REPORT_DIR:-$DEV_ROOT/docs/projects/new-games/evidence}"
ARTIFACTS_DIR="${ARTIFACTS_DIR:-$NGS_DIR/artifacts/perf}"
TARGET_BETS_PER_SEC="${TARGET_BETS_PER_SEC:-100}"
TARGET_PLACEBET_P95_MS="${TARGET_PLACEBET_P95_MS:-250}"
TARGET_COLLECT_P95_MS="${TARGET_COLLECT_P95_MS:-300}"

required_commands=(npm jq curl date awk rg)
for cmd in "${required_commands[@]}"; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Missing required command: $cmd" >&2
    exit 1
  fi
done

if [[ ! -d "$NGS_DIR" ]]; then
  echo "Missing NGS directory: $NGS_DIR" >&2
  exit 1
fi

mkdir -p "$REPORT_DIR" "$ARTIFACTS_DIR"

timestamp_utc="$(date -u +%Y%m%d-%H%M%S)"
created_utc="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
perf_json_file="$ARTIFACTS_DIR/perf-proof-$timestamp_utc.json"
perf_err_file="$ARTIFACTS_DIR/perf-proof-$timestamp_utc.stderr.txt"
status_txt_file="$ARTIFACTS_DIR/status-proof-$timestamp_utc.txt"
e2e_txt_file="$ARTIFACTS_DIR/e2e-proof-$timestamp_utc.txt"
report_file="$REPORT_DIR/m4-proof-pack-$timestamp_utc.md"

ngs_pid=""
cleanup() {
  if [[ -n "$ngs_pid" ]]; then
    kill "$ngs_pid" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

health_url="$NGS_BASE_URL/healthz"
if ! curl -fsS "$health_url" >/dev/null 2>&1; then
  if [[ "$AUTO_START_NGS" != "1" ]]; then
    echo "NGS is not reachable at $NGS_BASE_URL and AUTO_START_NGS=$AUTO_START_NGS" >&2
    exit 1
  fi

  (
    cd "$NGS_DIR"
    npm run --silent dev >/tmp/new-games-proof-pack.log 2>&1
  ) &
  ngs_pid="$!"

  ready="0"
  for _ in $(seq 1 80); do
    if curl -fsS "$health_url" >/dev/null 2>&1; then
      ready="1"
      break
    fi
    sleep 0.5
  done

  if [[ "$ready" != "1" ]]; then
    echo "NGS did not become ready at $health_url" >&2
    tail -n 80 /tmp/new-games-proof-pack.log >&2 || true
    exit 1
  fi
fi

overall_result="PASS"

cd "$NGS_DIR"

if ! npm run --silent runtime:e2e >"$e2e_txt_file" 2>&1; then
  overall_result="FAIL"
fi

if ! npm run --silent runtime:status >"$status_txt_file" 2>&1; then
  overall_result="FAIL"
fi

if ! npm run --silent perf:smoke >"$perf_json_file" 2>"$perf_err_file"; then
  overall_result="FAIL"
fi

if ! jq -e . "$perf_json_file" >/dev/null 2>&1; then
  overall_result="FAIL"
  cat > "$report_file" <<REPORT
# M4 Proof Pack

Created UTC: $created_utc
Result: FAIL

Reason:
- perf output is not valid JSON ($perf_json_file).

Artifacts:
- E2E: $e2e_txt_file
- Status: $status_txt_file
- Perf JSON: $perf_json_file
- Perf stderr: $perf_err_file
REPORT
  echo "ProofPackReport=$report_file"
  echo "ProofPackResult=FAIL"
  exit 1
fi

bets_per_sec="$(jq -r '.totals.achievedBetsPerSec // 0' "$perf_json_file")"
place_p95="$(jq -r '.placebet.p95 // 0' "$perf_json_file")"
collect_p95="$(jq -r '.collect.p95 // 0' "$perf_json_file")"

pass_bps="$(awk -v a="$bets_per_sec" -v b="$TARGET_BETS_PER_SEC" 'BEGIN{print (a+0>=b+0)?"PASS":"FAIL"}')"
pass_place_p95="$(awk -v a="$place_p95" -v b="$TARGET_PLACEBET_P95_MS" 'BEGIN{print (a+0<=b+0)?"PASS":"FAIL"}')"
pass_collect_p95="$(awk -v a="$collect_p95" -v b="$TARGET_COLLECT_P95_MS" 'BEGIN{print (a+0<=b+0)?"PASS":"FAIL"}')"

if ! rg -q "E2E OK" "$e2e_txt_file"; then
  overall_result="FAIL"
fi
if [[ "$pass_bps" != "PASS" || "$pass_place_p95" != "PASS" || "$pass_collect_p95" != "PASS" ]]; then
  overall_result="FAIL"
fi

cat > "$report_file" <<REPORT
# M4 Proof Pack

Created UTC: $created_utc
Overall Result: $overall_result

## Target Checks
- Bets/sec >= $TARGET_BETS_PER_SEC: $pass_bps (actual: $bets_per_sec)
- placebet p95 <= $TARGET_PLACEBET_P95_MS ms: $pass_place_p95 (actual: $place_p95 ms)
- collect p95 <= $TARGET_COLLECT_P95_MS ms: $pass_collect_p95 (actual: $collect_p95 ms)
- Runtime E2E chain: $(if rg -q "E2E OK" "$e2e_txt_file"; then echo PASS; else echo FAIL; fi)

## Evidence Files
- E2E output: $e2e_txt_file
- Runtime status output: $status_txt_file
- Perf JSON: $perf_json_file
- Perf stderr: $perf_err_file

## Perf JSON Snapshot
\`\`\`json
$(cat "$perf_json_file")
\`\`\`
REPORT

echo "ProofPackReport=$report_file"
echo "ProofPackPerfJson=$perf_json_file"
echo "ProofPackPerfStderr=$perf_err_file"
echo "ProofPackStatus=$status_txt_file"
echo "ProofPackE2E=$e2e_txt_file"
echo "ProofPackResult=$overall_result"

if [[ "$overall_result" != "PASS" ]]; then
  exit 1
fi
