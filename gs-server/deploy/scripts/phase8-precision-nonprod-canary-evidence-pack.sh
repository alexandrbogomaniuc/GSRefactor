#!/usr/bin/env bash
set -euo pipefail

ROOT="/Users/alexb/Documents/Dev/Dev_new"
OUT_DIR="${ROOT}/docs/phase8/precision"
ALLOW_MISSING_RUNTIME="true"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Creates a Phase 8 non-prod canary readiness/evidence report. This pack is execution-ready scaffolding
for the final runtime canary blocker; it does not restart GS or toggle JVM flags.

Options:
  --out-dir DIR               Default: ${OUT_DIR}
  --allow-missing-runtime B   true|false (default: ${ALLOW_MISSING_RUNTIME})
  -h, --help                  Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --out-dir) OUT_DIR="$2"; shift 2 ;;
    --allow-missing-runtime) ALLOW_MISSING_RUNTIME="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

mkdir -p "${OUT_DIR}"
TS="$(date -u +%Y%m%d-%H%M%S)"
REPORT="${OUT_DIR}/phase8-precision-nonprod-canary-evidence-${TS}.md"
READINESS_OUT="$(${ROOT}/gs-server/deploy/scripts/phase8-precision-nonprod-canary-readiness-check.sh --allow-missing-runtime "${ALLOW_MISSING_RUNTIME}")"

{
  echo "# Phase 8 Non-Prod Precision Canary Evidence Pack (${TS} UTC)"
  echo
  echo "## Scope"
  echo "Execution-ready readiness/evidence scaffold for the final Phase 8 runtime canary blocker."
  echo "No container restart or JVM flag mutation performed by this script."
  echo
  echo "## Readiness Snapshot"
  echo '```text'
  echo "${READINESS_OUT}"
  echo '```'
  echo
  echo "## Next Command (Manual Runtime Canary)"
  echo '```text'
  echo "1) Restart refactor GS with JVM flags: -Dabs.gs.phase8.precision.dualCalc.compare=true -Dabs.gs.phase8.precision.scaleReady.apply=true -Dabs.gs.phase8.precision.scaleReady.minorUnitScale=3"
  echo "2) Execute canary requests for selected scale3 currency/bank profile(s)"
  echo "3) Re-run this evidence pack and confirm precision_dual_calc_log_lines > 0 and matrix_blocking_count reaches 0 only after policy update with captured runtime evidence"
  echo '```'
} > "${REPORT}"

echo "report=${REPORT}"
