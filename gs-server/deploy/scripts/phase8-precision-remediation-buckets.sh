#!/usr/bin/env bash
set -euo pipefail

ROOT="/Users/alexb/Documents/Dev/Dev_new"
GS_ROOT="${ROOT}/gs-server"
OUT_DIR="${ROOT}/docs/phase8/precision"
TS="$(date -u '+%Y%m%d-%H%M%S')"
OUT_FILE="${OUT_DIR}/phase8-precision-remediation-buckets-${TS}.md"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --root DIR      Project root (default: ${ROOT})
  --gs-root DIR   GS root (default: ${GS_ROOT})
  --out-dir DIR   Output dir (default: ${OUT_DIR})
  -h, --help      Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --root)
      ROOT="$2"; shift 2 ;;
    --gs-root)
      GS_ROOT="$2"; shift 2 ;;
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

command -v rg >/dev/null 2>&1 || { echo "Missing required command: rg" >&2; exit 1; }
mkdir -p "${OUT_DIR}"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

scan_bucket() {
  local key="$1" pattern="$2" desc="$3" globs="$4"
  local out="${TMP_DIR}/${key}.txt"
  local status=0
  set -f
  # shellcheck disable=SC2086
  rg -n -S \
    --glob '!**/node_modules/**' \
    --glob '!**/target/**' \
    --glob '!**/build/**' \
    --glob '!**/*.min.js' \
    --glob '!**/*.min.css' \
    ${globs} \
    "$pattern" \
    "${GS_ROOT}/game-server" "${GS_ROOT}/refactor-services" > "$out" || status=$?
  set +f
  if [[ $status -ne 0 && $status -ne 1 ]]; then
    echo "rg failed for bucket ${key} (status=${status})" >&2
    exit "$status"
  fi
  printf '%s\n' "$desc" > "${TMP_DIR}/${key}.desc"
}

scan_bucket_union() {
  local key="$1" desc="$2" globs="$3"
  shift 3
  local out="${TMP_DIR}/${key}.txt"
  : > "$out"
  printf '%s\n' "$desc" > "${TMP_DIR}/${key}.desc"
  local idx=0
  for pattern in "$@"; do
    idx=$((idx + 1))
    scan_bucket "${key}__part${idx}" "$pattern" "$desc" "$globs"
    if [[ -s "${TMP_DIR}/${key}__part${idx}.txt" ]]; then
      cat "${TMP_DIR}/${key}__part${idx}.txt" >> "$out"
    fi
  done
  if [[ -s "$out" ]]; then
    sort -u "$out" -o "$out"
  fi
}

count_file() {
  local f="$1"
  [[ -s "$f" ]] && wc -l < "$f" | tr -d ' ' || echo 0
}

top_files() {
  local in="$1" max="${2:-12}"
  if [[ ! -s "$in" ]]; then
    echo "(none)"
    return
  fi
  cut -d: -f1 "$in" | sort | uniq -c | sort -nr | head -n "$max" | sed -E 's/^ +//'
}

samples() {
  local in="$1" max="${2:-25}"
  if [[ ! -s "$in" ]]; then
    echo "(none)"
    return
  fi
  sed -n "1,${max}p" "$in" | sed -E 's/[[:space:]]+$//'
}

scan_bucket_union \
  "wave1_reporting_stats" \
  'Wave 1 candidate (lower-risk first): reporting/display cent conversions and score rounding (explicitly excludes core session/update paths)' \
  "-g *.java" \
  'Math\.round\(Double\.parseDouble\(player\.getScore\(\)\) \* 100\)' \
  'DigitFormatter\.doubleToMoney\(coin\.getValue\(\) / 100\.0d\)' \
  'return \(double\) Math\.round\(d \* 100\) / 100'

scan_bucket \
  "wave2_settings_coin_rules" \
  '(0\\.01|lowest possible: 0\\.01|/ *100\\.0|KEY_DEFAULTNUMLINES.*100|default 100 .*cents)' \
  'Wave 2 candidate: game settings / dynamic coin derivation / FRB coin fallback assumptions' \
  "-g *.java"

scan_bucket \
  "wave3_config_templates" \
  '(<minValue>|<value>100</value>|0\\.01)' \
  'Wave 3 candidate: config/template defaults and bank/game coin value constraints (requires bank-by-bank compatibility review)' \
  "-g *.xml"

scan_bucket \
  "wave4_core_financial_paths" \
  '(Double\\.parseDouble|Float\\.parseFloat|\\bfloat\\b|\\bdouble\\b|BigDecimal|setScale|RoundingMode)' \
  'Wave 4 candidate (highest risk): core financial and settlement precision/rounding paths' \
  "-g *.java"

TOTAL_W1=$(count_file "${TMP_DIR}/wave1_reporting_stats.txt")
TOTAL_W2=$(count_file "${TMP_DIR}/wave2_settings_coin_rules.txt")
TOTAL_W3=$(count_file "${TMP_DIR}/wave3_config_templates.txt")
TOTAL_W4=$(count_file "${TMP_DIR}/wave4_core_financial_paths.txt")

{
  echo "# Phase 8 Precision Remediation Buckets and Wave Plan (${TS} UTC)"
  echo
  echo '- scope: GS-only (`game-server` + `refactor-services`)'
  echo "- purpose: convert raw Phase 8 audit findings into safe remediation waves with rollback-friendly sequencing"
  echo "- compatibility rule: no protocol or runtime contract changes during these waves"
  echo
  echo "## Recommended Wave Order (safe-first)"
  echo "1. Wave 1: reporting/statistics cent-based assumptions (non-financial outputs first)"
  echo "2. Wave 2: game settings / dynamic coin derivation / FRB fallback precision assumptions"
  echo "3. Wave 3: config/template coin minima and bank/game defaults (with canary bank validation)"
  echo "4. Wave 4: core financial settlement and wallet/gameplay precision paths (highest risk; idempotency/parity gated)"
  echo
  echo "## Bucket Counts"
  echo "- wave1_reporting_stats: ${TOTAL_W1}"
  echo "- wave2_settings_coin_rules: ${TOTAL_W2}"
  echo "- wave3_config_templates: ${TOTAL_W3}"
  echo "- wave4_core_financial_paths: ${TOTAL_W4}"
  echo
  echo "## Acceptance Gates Per Wave"
  echo "- Add/extend deterministic precision vectors before code changes in the target bucket."
  echo '- Run `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-6-local-verification-suite.sh` after each batch.'
  echo "- Preserve backward-compatible request/response and bank routing behavior."
  echo '- Canary on selected banks (start with bank `6275` where applicable).'
  echo "- No cutover of financial paths without parity evidence and rollback path."
  echo
  for key in wave1_reporting_stats wave2_settings_coin_rules wave3_config_templates wave4_core_financial_paths; do
    echo "## ${key}"
    echo "- description: $(cat "${TMP_DIR}/${key}.desc")"
    echo "- hits: $(count_file "${TMP_DIR}/${key}.txt")"
    echo "- top files:"
    echo '```text'
    top_files "${TMP_DIR}/${key}.txt" 12
    echo '```'
    echo "- sample matches:"
    echo '```text'
    samples "${TMP_DIR}/${key}.txt" 25
    echo '```'
    echo
  done

  echo "## Immediate Next Actions"
  echo "1. Add bucket-specific vector smoke for Wave 1 reporting/statistics conversions (cent vs thousandth formatting/parsing boundaries)."
  echo '2. Inventory exact call sites in `MQServiceHandler` and `GameSettingsManager` for behavior-preserving wrappers/adapters.'
  echo "3. Define config compatibility rules for bank/game/currency minima before changing template values."
  echo "4. Prepare parity assertions for wallet/gameplay amounts before any Wave 4 code edits."
} > "${OUT_FILE}"

echo "phase8_bucket_report=${OUT_FILE}"
