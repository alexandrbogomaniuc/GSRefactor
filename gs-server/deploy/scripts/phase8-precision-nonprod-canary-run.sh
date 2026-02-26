#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
COMPOSE_FILE="${ROOT}/gs-server/deploy/docker/refactor/docker-compose.yml"
ENV_FILE="${ROOT}/gs-server/deploy/docker/refactor/.env"
GS_CONTAINER="refactor-gs-1"
BANK_ID="6275"
GAME_ID="838"
MODE="real"
LANG="en"
TOKEN="phase8_canary_6275"
DRY_RUN="false"
RESTORE_DEFAULT="true"
BUILD_GS="true"
WAIT_SECONDS="20"
AUTO_CLOSE_PHASE8="true"
CLOSE_SCRIPT="${ROOT}/gs-server/deploy/scripts/phase8-precision-close-after-canary.sh"
FLAGS='-Dabs.gs.phase8.precision.dualCalc.compare=true -Dabs.gs.phase8.precision.scaleReady.apply=true -Dabs.gs.phase8.precision.scaleReady.minorUnitScale=3 -Dabs.gs.phase8.precision.dualCalc.logEvery=1'

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Runs the Phase 8 non-prod GS precision canary on refactor-gs-1 by restarting GS with JVM flags,
triggering a launch request, and generating an evidence pack. Use --dry-run to print commands only.

Options:
  --compose-file PATH      Default: ${COMPOSE_FILE}
  --env-file PATH          Default: ${ENV_FILE}
  --gs-container NAME      Default: ${GS_CONTAINER}
  --bank-id ID             Default: ${BANK_ID}
  --game-id ID             Default: ${GAME_ID}
  --token TOKEN            Default: ${TOKEN}
  --mode MODE              Default: ${MODE}
  --lang LANG              Default: ${LANG}
  --flags STRING           JVM precision flags (default: Phase 8 canary flags)
  --build-gs B             true|false (default: ${BUILD_GS})
  --restore-default B      true|false (default: ${RESTORE_DEFAULT})
  --wait-seconds N         Default: ${WAIT_SECONDS}
  --auto-close-phase8 B    true|false (default: ${AUTO_CLOSE_PHASE8})
  --close-script PATH      Default: ${CLOSE_SCRIPT}
  --dry-run B              true|false (default: ${DRY_RUN})
  -h, --help               Show help
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --compose-file) COMPOSE_FILE="$2"; shift 2 ;;
    --env-file) ENV_FILE="$2"; shift 2 ;;
    --gs-container) GS_CONTAINER="$2"; shift 2 ;;
    --bank-id) BANK_ID="$2"; shift 2 ;;
    --game-id) GAME_ID="$2"; shift 2 ;;
    --token) TOKEN="$2"; shift 2 ;;
    --mode) MODE="$2"; shift 2 ;;
    --lang) LANG="$2"; shift 2 ;;
    --flags) FLAGS="$2"; shift 2 ;;
    --build-gs) BUILD_GS="$(echo "$2" | tr '[:upper:]' '[:lower:]')"; shift 2 ;;
    --restore-default) RESTORE_DEFAULT="$(echo "$2" | tr '[:upper:]' '[:lower:]')"; shift 2 ;;
    --wait-seconds) WAIT_SECONDS="$2"; shift 2 ;;
    --auto-close-phase8) AUTO_CLOSE_PHASE8="$(echo "$2" | tr '[:upper:]' '[:lower:]')"; shift 2 ;;
    --close-script) CLOSE_SCRIPT="$2"; shift 2 ;;
    --dry-run) DRY_RUN="$(echo "$2" | tr '[:upper:]' '[:lower:]')"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage; exit 1 ;;
  esac
done

run_cmd() {
  if [[ "${DRY_RUN}" == "true" ]]; then
    printf '[DRY-RUN] %s\n' "$*"
    return 0
  fi
  eval "$@"
}

before_count_cmd="(docker logs ${GS_CONTAINER} 2>&1 | rg -c 'phase8-precision-dual-calc') || true"
launch_cmd="docker exec ${GS_CONTAINER} sh -lc \"curl -sS -o /tmp/phase8_canary.body -D /tmp/phase8_canary.hdr -w '%{http_code}' 'http://127.0.0.1:8080/cwstartgamev2.do?bankId=${BANK_ID}&gameId=${GAME_ID}&mode=${MODE}&token=${TOKEN}&lang=${LANG}'\""
recreate_cmd="cd $(dirname "${COMPOSE_FILE}") && GS_JAVA_OPTS='${FLAGS}' docker compose -f '${COMPOSE_FILE}' --env-file '${ENV_FILE}' up -d ${BUILD_GS:+--build} --force-recreate --no-deps gs"
restore_cmd="cd $(dirname "${COMPOSE_FILE}") && GS_JAVA_OPTS='' docker compose -f '${COMPOSE_FILE}' --env-file '${ENV_FILE}' up -d --force-recreate --no-deps gs"

if [[ "${BUILD_GS}" != "true" ]]; then
  recreate_cmd="cd $(dirname "${COMPOSE_FILE}") && GS_JAVA_OPTS='${FLAGS}' docker compose -f '${COMPOSE_FILE}' --env-file '${ENV_FILE}' up -d --force-recreate --no-deps gs"
fi

echo "step=baseline_log_count"
if [[ "${DRY_RUN}" == "true" ]]; then
  printf '[DRY-RUN] %s\n' "before_count=\$( (docker logs ${GS_CONTAINER} 2>&1 | rg -c 'phase8-precision-dual-calc') || true )"
else
  before_count="$( (docker logs "${GS_CONTAINER}" 2>&1 | rg -c 'phase8-precision-dual-calc') || true )"
  echo "before_count=${before_count}"
fi

echo "step=recreate_gs_with_phase8_flags"
run_cmd "${recreate_cmd}"

echo "step=wait_for_gs"
run_cmd "sleep ${WAIT_SECONDS}"

echo "step=inspect_gs_args"
run_cmd "docker inspect ${GS_CONTAINER} --format '{{.Path}} {{join .Args \" \"}}'"

echo "step=trigger_startgame_canary"
run_cmd "${launch_cmd}"
run_cmd "docker exec ${GS_CONTAINER} sh -lc 'head -n 8 /tmp/phase8_canary.hdr; echo ---; head -c 400 /tmp/phase8_canary.body'"

echo "step=evidence_pack"
run_cmd "'${ROOT}/gs-server/deploy/scripts/phase8-precision-nonprod-canary-evidence-pack.sh' --allow-missing-runtime false"

if [[ "${AUTO_CLOSE_PHASE8}" == "true" ]]; then
  echo "step=auto_close_phase8"
  run_cmd "'${CLOSE_SCRIPT}'"
fi

if [[ "${RESTORE_DEFAULT}" == "true" ]]; then
  echo "step=restore_default_gs_flags"
  run_cmd "${restore_cmd}"
  run_cmd "sleep ${WAIT_SECONDS}"
fi

echo "done=true"
