#!/usr/bin/env bash
set -euo pipefail

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  cat <<'USAGE'
Usage: phase7-cassandra-upgrade-target-rehearsal-smoke.sh

Runs the Phase 7 upgrade-target rehearsal orchestrator in dry-run mode and validates
that it emits a report with the expected readiness markers.
USAGE
  exit 0
fi

ROOT="/Users/alexb/Documents/Dev/Dev_new"
SCRIPT="${ROOT}/gs-server/deploy/scripts/phase7-cassandra-upgrade-target-rehearsal.sh"
OUT_DIR="$(mktemp -d)"
trap 'rm -rf "${OUT_DIR}"' EXIT

out="$("${SCRIPT}" --dry-run true --output-dir "${OUT_DIR}" 2>&1)"
echo "${out}"

report="$(printf '%s\n' "${out}" | awk -F= '/^report=/{print $2}' | tail -n 1)"
if [[ -z "${report}" || ! -f "${report}" ]]; then
  echo "missing report output" >&2
  exit 1
fi

grep -q "Result: DRY_RUN_READY" "${report}" || {
  echo "dry-run report missing result marker" >&2
  exit 1
}

grep -q "phase7-cassandra-target-bootstrap-and-critical-copy.sh" "${report}" || {
  echo "dry-run report missing bootstrap command" >&2
  exit 1
}

echo "PHASE7_TARGET_REHEARSAL_SMOKE_OK"
