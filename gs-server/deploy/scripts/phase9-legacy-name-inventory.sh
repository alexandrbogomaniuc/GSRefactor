#!/usr/bin/env bash
set -euo pipefail

ROOT="/Users/alexb/Documents/Dev/Dev_new/gs-server"
OUT_DIR="/Users/alexb/Documents/Dev/Dev_new/docs/phase9"
TIMESTAMP="$(date -u +%Y%m%d-%H%M%S)"
OUT_FILE="${OUT_DIR}/legacy-name-inventory-gs-${TIMESTAMP}.md"

mkdir -p "${OUT_DIR}"

declare -a PATTERNS=(
  "betsoft"
  "nucleus"
  "maxquest"
  "mq"
  "maxduel"
  "discreetgaming"
  "betsoft gaming"
  "com\\.dgphoenix"
  "dgphoenix"
)

{
  echo "# Phase 9 Legacy Naming Inventory (GS Scope)"
  echo
  echo "- Timestamp (UTC): $(date -u +"%Y-%m-%d %H:%M:%S")"
  echo "- Root scanned: ${ROOT}"
  echo
  echo "| Pattern | Match Count |"
  echo "|---|---:|"
} > "${OUT_FILE}"

for pattern in "${PATTERNS[@]}"; do
  count=$(rg -i --no-messages --hidden --glob '!.git' --glob '!target' --glob '!node_modules' --glob '!*.class' --glob '!*.jar' --glob '!*.log' -o "${pattern}" "${ROOT}" | wc -l | tr -d ' ')
  echo "| \`${pattern}\` | ${count} |" >> "${OUT_FILE}"
done

{
  echo
  echo "## Top File Hits"
  echo
  echo "| File | Hits |"
  echo "|---|---:|"
} >> "${OUT_FILE}"

set +o pipefail
rg -i --no-messages --hidden --glob '!.git' --glob '!target' --glob '!node_modules' --glob '!*.class' --glob '!*.jar' --glob '!*.log' -n \
  "betsoft|nucleus|maxquest|mq|maxduel|discreetgaming|betsoft gaming|com\\.dgphoenix|dgphoenix" "${ROOT}" \
  | awk -F: '{print $1}' \
  | sort | uniq -c | sort -nr | head -n 50 \
  | awk '{count=$1; $1=""; sub(/^ /,""); print "| `" $0 "` | " count " |"}' >> "${OUT_FILE}"
set -o pipefail

echo "Legacy inventory report: ${OUT_FILE}"
