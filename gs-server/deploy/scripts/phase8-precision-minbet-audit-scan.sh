#!/usr/bin/env bash
set -euo pipefail

GS_ROOT="/Users/alexb/Documents/Dev/Dev_new/gs-server"
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase8/precision"
TS="$(date -u '+%Y%m%d-%H%M%S')"
OUT_FILE="${OUT_DIR}/phase8-precision-minbet-audit-${TS}.md"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --gs-root DIR   GS root to scan (default: ${GS_ROOT})
  --out-dir DIR   Output dir (default: ${OUT_DIR})
  -h, --help      Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
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
tmp_dir="$(mktemp -d)"
trap 'rm -rf "${tmp_dir}"' EXIT

targets=(
  "${GS_ROOT}/game-server"
  "${GS_ROOT}/refactor-services"
)

scan_to_file() {
  local pattern="$1"
  local out="$2"
  shift 2
  rg -n -S \
    --glob '!**/node_modules/**' \
    --glob '!**/target/**' \
    --glob '!**/build/**' \
    --glob '!**/*.min.js' \
    --glob '!**/*.min.css' \
    --glob '!**/jquery/**' \
    --glob '!**/bootstrap/**' \
    "$pattern" "${targets[@]}" "$@" > "${out}" || true
}

scan_to_file '(?i)(\bminBet\b|\bminimum\b|\bmin bet\b|\ballowedMin\b|\blimit_min\b|\bbetPerLine\b|\blineBet\b|\bcoins?\b)' "${tmp_dir}/min_keywords.txt" \
  -g '*.java' -g '*.js' -g '*.jsp' -g '*.xml' -g '*.properties' -g '*.yaml' -g '*.yml'
scan_to_file '\b0\.01\b|\b0\.1\b|\b1\.00\b|\b100\b' "${tmp_dir}/decimal_literals.txt" -g '*.java' -g '*.js' -g '*.jsp' -g '*.xml' -g '*.properties'
scan_to_file '(BigDecimal|setScale|ROUND_HALF|RoundingMode|Math\.round|Double\.parseDouble|Float\.parseFloat|new BigDecimal\()' "${tmp_dir}/rounding_apis.txt" -g '*.java' -g '*.js'
scan_to_file '(?i)(currency|denom|denomination|coin|betPerLine|lines|lineBet|totalBet)' "${tmp_dir}/bet_model_terms.txt" -g '*.java' -g '*.js' -g '*.jsp'
scan_to_file '\b(float|double)\b' "${tmp_dir}/float_double_types.txt" -g '*.java'

count_lines() {
  if [[ -s "$1" ]]; then
    wc -l < "$1" | tr -d ' '
  else
    echo 0
  fi
}

min_count="$(count_lines "${tmp_dir}/min_keywords.txt")"
literal_count="$(count_lines "${tmp_dir}/decimal_literals.txt")"
rounding_count="$(count_lines "${tmp_dir}/rounding_apis.txt")"
bet_terms_count="$(count_lines "${tmp_dir}/bet_model_terms.txt")"
float_double_count="$(count_lines "${tmp_dir}/float_double_types.txt")"

{
  echo "# Phase 8 Precision / Min-Bet Audit Scan (${TS} UTC)"
  echo
  echo "- scope: GS-only scan (\`game-server\` + \`refactor-services\`)"
  echo "- target precision objective: support \`0.001\` where business allows"
  echo "- focus: hardcoded minimum assumptions, decimal handling, rounding, and line-based bet calculations"
  echo
  echo "## Summary Counts"
  echo "- min/line keyword hits: ${min_count}"
  echo "- numeric literal hits (0.01/0.1/1.00/100): ${literal_count}"
  echo "- rounding/decimal API hits: ${rounding_count}"
  echo "- bet model term hits: ${bet_terms_count}"
  echo "- Java float/double declarations: ${float_double_count}"
  echo
  echo "## Priority Follow-up Buckets"
  echo "1. Hardcoded minimums (\`0.01\`, line-based total min assumptions)."
  echo "2. Decimal arithmetic and rounding logic (\`BigDecimal\`, \`setScale\`, \`Math.round\`)."
  echo "3. Float/double use in money-adjacent code (precision risk)."
  echo "4. Bet model line/coin/denomination calculations requiring end-to-end precision review."
  echo
  echo "## Min/Line Keywords (sample)"
  echo '```text'
  sed -n '1,120p' "${tmp_dir}/min_keywords.txt" | sed -E 's/[[:space:]]+$//'
  echo '```'
  echo
  echo "## Numeric Literals (sample)"
  echo '```text'
  sed -n '1,120p' "${tmp_dir}/decimal_literals.txt" | sed -E 's/[[:space:]]+$//'
  echo '```'
  echo
  echo "## Rounding/Decimal APIs (sample)"
  echo '```text'
  sed -n '1,120p' "${tmp_dir}/rounding_apis.txt" | sed -E 's/[[:space:]]+$//'
  echo '```'
  echo
  echo "## Bet Model Terms (sample)"
  echo '```text'
  sed -n '1,120p' "${tmp_dir}/bet_model_terms.txt" | sed -E 's/[[:space:]]+$//'
  echo '```'
  echo
  echo "## Java float/double Declarations (sample)"
  echo '```text'
  sed -n '1,120p' "${tmp_dir}/float_double_types.txt" | sed -E 's/[[:space:]]+$//'
  echo '```'
} > "${OUT_FILE}"

echo "precision_audit_report=${OUT_FILE}"
