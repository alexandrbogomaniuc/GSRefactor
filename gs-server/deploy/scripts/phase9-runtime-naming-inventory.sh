#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"
OUTPUT_DIR="${OUTPUT_DIR:-$PROJECT_ROOT/docs/phase9/runtime-naming-cleanup/evidence}"
TS="$(date -u +%Y%m%d-%H%M%S)"

mkdir -p "$OUTPUT_DIR"

CLASS_OUT="$OUTPUT_DIR/${TS}-class_refs.txt"
MQ_OUT="$OUTPUT_DIR/${TS}-mq_refs.txt"
MAP_OUT="$OUTPUT_DIR/${TS}-phase9_map_refs.txt"

TARGETS=("$PROJECT_ROOT/gs-server")
if [ -d "$PROJECT_ROOT/mp-server" ]; then
  TARGETS+=("$PROJECT_ROOT/mp-server")
fi

COMMON_GLOBS=(
  --glob '**/*.java'
  --glob '**/*.xml'
  --glob '**/*.jsp'
  --glob '**/*.properties'
  --glob '**/*.json'
  --glob '**/*.sh'
  --glob '**/*.mjs'
  --glob '!**/target/**'
  --glob '!**/node_modules/**'
  --glob '!**/docs/**'
)

rg -n --no-heading -S \
  'Class\.forName\(|com\.dgphoenix|com\.abs' \
  "${COMMON_GLOBS[@]}" \
  "${TARGETS[@]}" > "$CLASS_OUT" || true

rg -n --no-heading -S \
  'MQ_[A-Z0-9_]+|DISABLE_MQ|/MQ_|mqbase\.com' \
  "${COMMON_GLOBS[@]}" \
  "${TARGETS[@]}" > "$MQ_OUT" || true

rg -n --no-heading -S \
  'phase9-abs-compatibility-map|reviewOnly|requiresWrapper|BLOCKED_ENV_INFRA_NAMES_AND_MQ_TOKEN' \
  --glob '**/*.json' \
  --glob '**/*.sh' \
  --glob '**/*.mjs' \
  --glob '!**/target/**' \
  "$PROJECT_ROOT/gs-server/deploy" > "$MAP_OUT" || true

CLASS_COUNT="$(wc -l < "$CLASS_OUT" | tr -d ' ')"
MQ_COUNT="$(wc -l < "$MQ_OUT" | tr -d ' ')"
MAP_COUNT="$(wc -l < "$MAP_OUT" | tr -d ' ')"

cat <<EOF
Phase9 runtime naming inventory generated:
  class refs: $CLASS_OUT ($CLASS_COUNT lines)
  mq refs:    $MQ_OUT ($MQ_COUNT lines)
  map refs:   $MAP_OUT ($MAP_COUNT lines)
EOF

