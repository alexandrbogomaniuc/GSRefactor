#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"

ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
ALLOW_MISSING_RUNTIME="false"
GS_CONTAINER="refactor-gs-1"
LOG_DIR="${ROOT}/Doker/runtime-gs/logs/gs"
POLICY_FILE="${ROOT}/gs-server/deploy/config/phase8-precision-policy.json"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Checks readiness for Phase 8 non-prod precision canary execution on the refactor GS stack.
It does not enable precision flags or restart containers.

Options:
  --allow-missing-runtime B   true|false (default: ${ALLOW_MISSING_RUNTIME})
  --gs-container NAME         Default: ${GS_CONTAINER}
  --log-dir DIR               Default: ${LOG_DIR}
  -h, --help                  Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --allow-missing-runtime) ALLOW_MISSING_RUNTIME="$2"; shift 2 ;;
    --gs-container) GS_CONTAINER="$2"; shift 2 ;;
    --log-dir) LOG_DIR="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

status="READY"
notes=()
dockersvc_names=""
docker_access_denied="false"

if ! dockersvc_names=$(docker ps --format '{{.Names}}' 2>/dev/null); then
  docker_access_denied="true"
  if [[ "${ALLOW_MISSING_RUNTIME}" == "true" ]]; then
    status="READY_OFFLINE_ONLY"
    notes+=("docker_api_unavailable")
  else
    echo "status=NOT_READY"
    echo "reason=docker_api_unavailable"
    exit 2
  fi
fi

if [[ "${docker_access_denied}" != "true" ]] && ! grep -Fxq "${GS_CONTAINER}" <<< "${dockersvc_names}"; then
  if [[ "${ALLOW_MISSING_RUNTIME}" == "true" ]]; then
    status="READY_OFFLINE_ONLY"
    notes+=("gs_container_missing=${GS_CONTAINER}")
  else
    echo "status=NOT_READY"
    echo "reason=gs_container_missing:${GS_CONTAINER}"
    exit 2
  fi
fi

if [[ ! -d "${LOG_DIR}" ]]; then
  if [[ "${ALLOW_MISSING_RUNTIME}" == "true" ]]; then
    status="READY_OFFLINE_ONLY"
    notes+=("log_dir_missing=${LOG_DIR}")
  else
    echo "status=NOT_READY"
    echo "reason=log_dir_missing:${LOG_DIR}"
    exit 2
  fi
fi

precision_log_count=0
if [[ -d "${LOG_DIR}" ]]; then
  precision_log_count=$({ rg -n "phase8-precision-dual-calc" "${LOG_DIR}" -g '*.log' 2>/dev/null || true; } | wc -l | tr -d ' ')
fi

matrix_line="$(${REPO_ROOT}/gs-server/deploy/scripts/phase8-precision-verification-matrix.sh --out-dir "${ROOT}/docs/phase8/precision")"
matrix_report="${matrix_line#report=}"
blocking_count="$( { rg -n '^- blockingCategories: ' "${matrix_report}" || true; } | sed -E 's/.*: ([0-9]+)/\1/' | head -n1 )"
remaining_blockers="$( { rg -n '^- .*-> resolve before Phase 8 closure$' "${matrix_report}" || true; } | sed -E 's/^- //' | paste -sd ';' - )"

echo "status=${status}"
echo "gs_container=${GS_CONTAINER}"
echo "log_dir=${LOG_DIR}"
echo "precision_dual_calc_log_lines=${precision_log_count}"
echo "policy_file=${POLICY_FILE}"
echo "matrix_report=${matrix_report}"
echo "matrix_blocking_count=${blocking_count:-unknown}"
echo "matrix_remaining_blockers=${remaining_blockers:-none}"
echo "canary_flags_hint=-Dabs.gs.phase8.precision.dualCalc.compare=true -Dabs.gs.phase8.precision.scaleReady.apply=true -Dabs.gs.phase8.precision.scaleReady.minorUnitScale=3"
if [[ ${#notes[@]} -gt 0 ]]; then
  printf 'notes=%s\n' "$(IFS=';'; echo "${notes[*]}")"
fi
