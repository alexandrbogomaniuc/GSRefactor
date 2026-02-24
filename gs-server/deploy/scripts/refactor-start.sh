#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
DEV_NEW_ROOT="$(cd "$DEPLOY_DIR/../.." && pwd)"
REFACTOR_DIR="$DEPLOY_DIR/docker/refactor"

COMPOSE_FILE="$REFACTOR_DIR/docker-compose.yml"
SYNC_CLUSTER_HOSTS="$SCRIPT_DIR/sync-cluster-hosts.sh"
BOOTSTRAP_RUNTIME="$SCRIPT_DIR/refactor-bootstrap-runtime.sh"

LEGACY_MP_TARGET_DIR="${LEGACY_MP_TARGET_DIR:-$DEV_NEW_ROOT/mp-server/web/target}"
BUILD_IMAGES="${BUILD_IMAGES:-0}"
AUTO_BOOTSTRAP_RUNTIME="${AUTO_BOOTSTRAP_RUNTIME:-1}"

log() { printf '[refactor-start] %s\n' "$*"; }
die() { printf '[refactor-start] ERROR: %s\n' "$*" >&2; exit 1; }
run() { log "$*"; "$@"; }

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || die "Missing command: $1"
}

require_path() {
  local path="$1"
  [[ -e "$path" ]] || die "Missing required path: $path"
}

check_runtime_seed_paths() {
  local base="$DEV_NEW_ROOT/Doker/runtime-gs"
  require_path "$base"
  require_path "$base/webapps/gs"
  require_path "$base/static"
  require_path "$base/default-configs"
  mkdir -p "$base/logs/gs" "$base/logs/nginx" "$base/logs/cassandra" "$base/logs/cassandra-refactor"
}

check_legacy_mp_artifacts() {
  require_path "$LEGACY_MP_TARGET_DIR"
  require_path "$LEGACY_MP_TARGET_DIR/web-mp-casino"
}

maybe_bootstrap_runtime() {
  if [[ "$AUTO_BOOTSTRAP_RUNTIME" != "1" ]]; then
    return
  fi
  local needs_bootstrap=0
  [[ -d "$DEV_NEW_ROOT/Doker/runtime-gs/webapps/gs/ROOT/WEB-INF" ]] || needs_bootstrap=1
  [[ -d "$DEV_NEW_ROOT/Doker/runtime-gs/webapps/gs/ROOT/html5pc/actiongames/dragonstone/game" ]] || needs_bootstrap=1
  [[ -d "$LEGACY_MP_TARGET_DIR/web-mp-casino" ]] || needs_bootstrap=1
  if [[ "$needs_bootstrap" == "1" ]]; then
    require_path "$BOOTSTRAP_RUNTIME"
    run "$BOOTSTRAP_RUNTIME"
  fi
}

preflight() {
  require_cmd docker
  require_cmd curl
  maybe_bootstrap_runtime
  docker info >/dev/null 2>&1 || die "Docker daemon is not reachable"
  require_path "$COMPOSE_FILE"
  require_path "$SYNC_CLUSTER_HOSTS"
  check_runtime_seed_paths
  check_legacy_mp_artifacts
}

start_stack() {
  run "$SYNC_CLUSTER_HOSTS"
  export LEGACY_MP_TARGET_DIR

  (
    cd "$REFACTOR_DIR"
    if [[ "$BUILD_IMAGES" == "1" ]]; then
      run docker compose -p refactor --env-file .env build gs mp static
    fi
    run docker compose -p refactor --env-file .env up -d c1-refactor zookeeper redis
    run docker compose -p refactor --env-file .env up -d kafka
    run docker compose -p refactor --env-file .env up -d \
      config-service session-service gameplay-orchestrator wallet-adapter \
      bonus-frb-service history-service multiplayer-service protocol-adapter \
      mp gs static
  )
}

post_checks() {
  log "Quick health checks"
  curl -fsS http://127.0.0.1:18072/health >/dev/null && log "config-service ok" || log "config-service check failed"
  curl -fsS http://127.0.0.1:18081 >/dev/null && log "gs http ok" || log "gs http check failed"
  log "Launch alias URL (refactor static facade): http://127.0.0.1:18080/startgame?bankId=6275&subCasinoId=507&gameId=838&mode=real&token=bav_game_session_001&lang=en"
}

preflight
start_stack
post_checks
