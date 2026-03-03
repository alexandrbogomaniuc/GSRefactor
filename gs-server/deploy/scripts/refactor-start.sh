#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
DEV_NEW_ROOT="$(cd "$DEPLOY_DIR/../.." && pwd)"
REFACTOR_DIR="$DEPLOY_DIR/docker/refactor"

COMPOSE_FILE="$REFACTOR_DIR/docker-compose.yml"
SYNC_CLUSTER_HOSTS="$SCRIPT_DIR/sync-cluster-hosts.sh"
BOOTSTRAP_RUNTIME="$SCRIPT_DIR/refactor-bootstrap-runtime.sh"
CLUSTER_HOSTS_LIB="$SCRIPT_DIR/lib/cluster-hosts.sh"

LEGACY_MP_TARGET_DIR="${LEGACY_MP_TARGET_DIR:-$DEV_NEW_ROOT/mp-server/web/target}"
SUPPORT_SRC_ROOT="${SUPPORT_SRC_ROOT:-$DEV_NEW_ROOT/gs-server/game-server/web-gs/src/main/webapp/support}"
RUNTIME_SUPPORT_ROOT="${RUNTIME_SUPPORT_ROOT:-$DEV_NEW_ROOT/Doker/runtime-gs/webapps/gs/ROOT/support}"
BUILD_IMAGES="${BUILD_IMAGES:-0}"
AUTO_BOOTSTRAP_RUNTIME="${AUTO_BOOTSTRAP_RUNTIME:-1}"

if [[ -f "$CLUSTER_HOSTS_LIB" ]]; then
  # shellcheck source=./lib/cluster-hosts.sh
  source "$CLUSTER_HOSTS_LIB"
fi

cluster_cfg_or_default() {
  local key="$1"
  local fallback="$2"
  if command -v cluster_hosts_get >/dev/null 2>&1; then
    cluster_hosts_get "$key" "$fallback"
  else
    printf '%s\n' "$fallback"
  fi
}

LAUNCH_BASE_URL="${LAUNCH_BASE_URL:-$(cluster_cfg_or_default LAUNCH_BASE_URL "http://127.0.0.1:18080/startgame")}"
LAUNCH_BANK_ID="${LAUNCH_BANK_ID:-$(cluster_cfg_or_default LAUNCH_BANK_ID "6275")}"
LAUNCH_SUBCASINO_ID="${LAUNCH_SUBCASINO_ID:-$(cluster_cfg_or_default LAUNCH_SUBCASINO_ID "507")}"
LAUNCH_GAME_ID="${LAUNCH_GAME_ID:-$(cluster_cfg_or_default LAUNCH_GAME_ID "838")}"
LAUNCH_MODE="${LAUNCH_MODE:-$(cluster_cfg_or_default LAUNCH_MODE "real")}"
LAUNCH_TOKEN="${LAUNCH_TOKEN:-$(cluster_cfg_or_default LAUNCH_TOKEN "bav_game_session_001")}"
LAUNCH_LANG="${LAUNCH_LANG:-$(cluster_cfg_or_default LAUNCH_LANG "en")}"
SECONDARY_LAUNCH_BANK_ID="${SECONDARY_LAUNCH_BANK_ID:-$(cluster_cfg_or_default SECONDARY_LAUNCH_BANK_ID "")}"
SECONDARY_LAUNCH_SUBCASINO_ID="${SECONDARY_LAUNCH_SUBCASINO_ID:-$(cluster_cfg_or_default SECONDARY_LAUNCH_SUBCASINO_ID "")}"
SECONDARY_LAUNCH_GAME_ID="${SECONDARY_LAUNCH_GAME_ID:-$(cluster_cfg_or_default SECONDARY_LAUNCH_GAME_ID "$LAUNCH_GAME_ID")}"
SECONDARY_LAUNCH_MODE="${SECONDARY_LAUNCH_MODE:-$(cluster_cfg_or_default SECONDARY_LAUNCH_MODE "$LAUNCH_MODE")}"
SECONDARY_LAUNCH_TOKEN="${SECONDARY_LAUNCH_TOKEN:-$(cluster_cfg_or_default SECONDARY_LAUNCH_TOKEN "$LAUNCH_TOKEN")}"
SECONDARY_LAUNCH_LANG="${SECONDARY_LAUNCH_LANG:-$(cluster_cfg_or_default SECONDARY_LAUNCH_LANG "$LAUNCH_LANG")}"
CONFIG_SERVICE_PORT="${CONFIG_SERVICE_PORT:-$(cluster_cfg_or_default CONFIG_SERVICE_PORT "18072")}"
SESSION_SERVICE_PORT="${SESSION_SERVICE_PORT:-$(cluster_cfg_or_default SESSION_SERVICE_PORT "18073")}"
GAMEPLAY_ORCHESTRATOR_PORT="${GAMEPLAY_ORCHESTRATOR_PORT:-$(cluster_cfg_or_default GAMEPLAY_ORCHESTRATOR_PORT "18074")}"
WALLET_ADAPTER_PORT="${WALLET_ADAPTER_PORT:-$(cluster_cfg_or_default WALLET_ADAPTER_PORT "18075")}"
PROTOCOL_ADAPTER_PORT="${PROTOCOL_ADAPTER_PORT:-$(cluster_cfg_or_default PROTOCOL_ADAPTER_PORT "18078")}"
GS_EXTERNAL_HOST="${GS_EXTERNAL_HOST:-$(cluster_cfg_or_default GS_EXTERNAL_HOST "127.0.0.1")}"
GS_EXTERNAL_PORT="${GS_EXTERNAL_PORT:-$(cluster_cfg_or_default GS_EXTERNAL_PORT "18081")}"
STATIC_EXTERNAL_HOST="${STATIC_EXTERNAL_HOST:-$(cluster_cfg_or_default STATIC_EXTERNAL_HOST "127.0.0.1")}"
STATIC_EXTERNAL_PORT="${STATIC_EXTERNAL_PORT:-$(cluster_cfg_or_default STATIC_EXTERNAL_PORT "18080")}"
REFACTOR_READY_RETRIES="${REFACTOR_READY_RETRIES:-30}"
REFACTOR_READY_DELAY_SEC="${REFACTOR_READY_DELAY_SEC:-2}"
REFACTOR_STRICT_READINESS="${REFACTOR_STRICT_READINESS:-1}"
REFACTOR_CORE_STABILITY_CHECKS="${REFACTOR_CORE_STABILITY_CHECKS:-3}"
REFACTOR_CORE_STABILITY_DELAY_SEC="${REFACTOR_CORE_STABILITY_DELAY_SEC:-3}"
REFACTOR_EDGE_STABILITY_CHECKS="${REFACTOR_EDGE_STABILITY_CHECKS:-3}"
REFACTOR_EDGE_STABILITY_DELAY_SEC="${REFACTOR_EDGE_STABILITY_DELAY_SEC:-3}"
REFACTOR_WARM_ALIAS_RETRIES="${REFACTOR_WARM_ALIAS_RETRIES:-3}"
REFACTOR_WARM_ALIAS_DELAY_SEC="${REFACTOR_WARM_ALIAS_DELAY_SEC:-2}"

log() { printf '[refactor-start] %s\n' "$*"; }
die() { printf '[refactor-start] ERROR: %s\n' "$*" >&2; exit 1; }
run() { log "$*"; "$@"; }
is_truthy() {
  case "${1:-}" in
    1|true|TRUE|yes|YES|on|ON) return 0 ;;
    *) return 1 ;;
  esac
}
usage() {
  cat <<'EOF'
Usage: refactor-start.sh [up|preflight|smoke]

Commands:
  up         Run preflight checks, start the refactor stack, and print quick checks (default)
  preflight  Validate local prerequisites and runtime seed artifacts without starting containers
  smoke      Run quick HTTP checks against a running refactor stack
EOF
}

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
    run bash "$BOOTSTRAP_RUNTIME"
  fi
}

preflight() {
  require_cmd docker
  require_cmd curl
  docker info >/dev/null 2>&1 || die "Docker daemon is not reachable"
  require_path "$COMPOSE_FILE"
  require_path "$SYNC_CLUSTER_HOSTS"
  if [[ "$AUTO_BOOTSTRAP_RUNTIME" == "1" ]]; then
    require_path "$BOOTSTRAP_RUNTIME"
    log "Preflight ok: runtime assets will be auto-bootstrapped during 'up' if missing"
  else
    check_runtime_seed_paths
    check_legacy_mp_artifacts
    log "Preflight ok: using preseeded runtime assets (AUTO_BOOTSTRAP_RUNTIME=0)"
  fi
}

sync_modernization_support_assets() {
  mkdir -p "$RUNTIME_SUPPORT_ROOT" "$RUNTIME_SUPPORT_ROOT/data"

  local support_files=(
    modernizationProgress.html
    modernizationRunbook.jsp
    modernizationDocs.jsp
    phase8DiscrepancyViewer.html
  )
  local support_data_files=(
    modernization-checklist.json
    session-outbox-health.json
    audit-requirements-status.json
    audit-scope-summary.json
  )
  local f
  for f in "${support_files[@]}"; do
    if [[ -f "$SUPPORT_SRC_ROOT/$f" ]]; then
      cp -f "$SUPPORT_SRC_ROOT/$f" "$RUNTIME_SUPPORT_ROOT/$f"
    fi
  done
  for f in "${support_data_files[@]}"; do
    if [[ -f "$SUPPORT_SRC_ROOT/data/$f" ]]; then
      cp -f "$SUPPORT_SRC_ROOT/data/$f" "$RUNTIME_SUPPORT_ROOT/data/$f"
    fi
  done
}

ensure_runtime_assets() {
  maybe_bootstrap_runtime
  check_runtime_seed_paths
  check_legacy_mp_artifacts
  sync_modernization_support_assets
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
      mp
  )
}

service_running() {
  local service="$1"
  local cid
  cid="$(cd "$REFACTOR_DIR" && docker compose -p refactor --env-file .env ps -q "$service" 2>/dev/null | head -n1 || true)"
  [[ -n "$cid" ]] || return 1
  [[ "$(docker inspect -f '{{.State.Running}}' "$cid" 2>/dev/null || echo false)" == "true" ]]
}

service_restart_count() {
  local service="$1"
  local cid
  cid="$(cd "$REFACTOR_DIR" && docker compose -p refactor --env-file .env ps -q "$service" 2>/dev/null | head -n1 || true)"
  [[ -n "$cid" ]] || { printf 'n/a\n'; return 0; }
  docker inspect -f '{{.RestartCount}}' "$cid" 2>/dev/null || printf 'n/a\n'
}

verify_core_services_stable() {
  local core_services=("$@")
  local checks="${REFACTOR_CORE_STABILITY_CHECKS}"
  local delay="${REFACTOR_CORE_STABILITY_DELAY_SEC}"
  local check_idx=1
  local service
  local unstable=()

  while (( check_idx <= checks )); do
    unstable=()
    for service in "${core_services[@]}"; do
      if ! service_running "$service"; then
        unstable+=("$service")
      fi
    done
    if (( ${#unstable[@]} > 0 )); then
      die "core services unstable during stability check ${check_idx}/${checks}: ${unstable[*]}"
    fi
    if (( check_idx < checks )); then
      sleep "$delay"
    fi
    ((check_idx++))
  done

  for service in "${core_services[@]}"; do
    log "core service stable: ${service} (restartCount=$(service_restart_count "$service"))"
  done
}

verify_core_services_running() {
  local core_services=(c1-refactor zookeeper kafka mp)
  local down=()
  local service

  for service in "${core_services[@]}"; do
    if ! service_running "$service"; then
      down+=("$service")
    fi
  done

  if (( ${#down[@]} == 0 )); then
    log "core services are running: ${core_services[*]}"
    return 0
  fi

  log "warn: core services not running: ${down[*]} (attempting one recovery up)"
  (
    cd "$REFACTOR_DIR"
    run docker compose -p refactor --env-file .env up -d "${down[@]}"
  )
  sleep 4

  local still_down=()
  for service in "${down[@]}"; do
    if ! service_running "$service"; then
      still_down+=("$service")
    fi
  done

  if (( ${#still_down[@]} > 0 )); then
    die "core services failed to recover: ${still_down[*]}"
  fi
  log "core services recovered: ${down[*]}"
  verify_core_services_stable "${core_services[@]}"
}

start_edge_services() {
  (
    cd "$REFACTOR_DIR"
    run docker compose -p refactor --env-file .env up -d gs static
  )
}

verify_edge_services_stable() {
  local edge_services=(gs static)
  local checks="${REFACTOR_EDGE_STABILITY_CHECKS}"
  local delay="${REFACTOR_EDGE_STABILITY_DELAY_SEC}"
  local check_idx=1
  local attempted_recovery=0
  local service
  local down=()

  while (( check_idx <= checks )); do
    down=()
    for service in "${edge_services[@]}"; do
      if ! service_running "$service"; then
        down+=("$service")
      fi
    done

    if (( ${#down[@]} > 0 )); then
      if (( attempted_recovery == 0 )); then
        log "warn: edge services not running: ${down[*]} (attempting one recovery up)"
        (
          cd "$REFACTOR_DIR"
          run docker compose -p refactor --env-file .env up -d "${down[@]}"
        )
        attempted_recovery=1
        sleep 4
        continue
      fi
      die "edge services unstable during stability check ${check_idx}/${checks}: ${down[*]}"
    fi

    if (( check_idx < checks )); then
      sleep "$delay"
    fi
    ((check_idx++))
  done

  for service in "${edge_services[@]}"; do
    log "edge service stable: ${service} (restartCount=$(service_restart_count "$service"))"
  done
}

build_launch_url() {
  local bank_id="$1"
  local subcasino_id="$2"
  local game_id="$3"
  local mode="$4"
  local token="$5"
  local lang="$6"
  printf '%s?bankId=%s&subCasinoId=%s&gameId=%s&mode=%s&token=%s&lang=%s' \
    "$LAUNCH_BASE_URL" "$bank_id" "$subcasino_id" "$game_id" "$mode" "$token" "$lang"
}

wait_for_health() {
  local name="$1"
  local url="$2"
  local retries="${3:-$REFACTOR_READY_RETRIES}"
  local delay="${4:-$REFACTOR_READY_DELAY_SEC}"
  local attempt=1
  while (( attempt <= retries )); do
    if curl -fsS --max-time 3 "$url" >/dev/null 2>&1; then
      log "ready: ${name} (${url})"
      return 0
    fi
    if (( attempt < retries )); then
      sleep "$delay"
    fi
    ((attempt++))
  done
  log "warn: ${name} not ready after ${retries} attempts (${url})"
  return 1
}

wait_for_refactor_readiness() {
  local strict="${REFACTOR_STRICT_READINESS}"
  local readiness_failures=()

  log "Waiting for refactor service readiness"
  wait_for_health "gs" "http://${GS_EXTERNAL_HOST}:${GS_EXTERNAL_PORT}/support/bankSelectAction.do?bankId=${LAUNCH_BANK_ID}" || readiness_failures+=("gs")
  wait_for_health "static" "http://${STATIC_EXTERNAL_HOST}:${STATIC_EXTERNAL_PORT}/html5pc/actiongames/dragonstone/lobby/version.json" || readiness_failures+=("static")
  wait_for_health "config-service" "http://127.0.0.1:${CONFIG_SERVICE_PORT}/health" || readiness_failures+=("config-service")
  wait_for_health "session-service" "http://127.0.0.1:${SESSION_SERVICE_PORT}/health" || readiness_failures+=("session-service")
  wait_for_health "gameplay-orchestrator" "http://127.0.0.1:${GAMEPLAY_ORCHESTRATOR_PORT}/health" || readiness_failures+=("gameplay-orchestrator")
  wait_for_health "wallet-adapter" "http://127.0.0.1:${WALLET_ADAPTER_PORT}/health" || readiness_failures+=("wallet-adapter")
  wait_for_health "protocol-adapter" "http://127.0.0.1:${PROTOCOL_ADAPTER_PORT}/health" || readiness_failures+=("protocol-adapter")

  if (( ${#readiness_failures[@]} > 0 )); then
    if is_truthy "$strict"; then
      die "strict readiness failed for: ${readiness_failures[*]}"
    fi
    log "warn: non-strict readiness mode; continuing with failed checks: ${readiness_failures[*]}"
  fi
}

warm_launch_alias_probe() {
  local launch_url
  local retries="${REFACTOR_WARM_ALIAS_RETRIES}"
  local delay="${REFACTOR_WARM_ALIAS_DELAY_SEC}"
  local attempt=1
  launch_url="$(build_launch_url "$LAUNCH_BANK_ID" "$LAUNCH_SUBCASINO_ID" "$LAUNCH_GAME_ID" "$LAUNCH_MODE" "$LAUNCH_TOKEN" "$LAUNCH_LANG")"
  while (( attempt <= retries )); do
    if curl -fsS --max-time 8 "$launch_url" >/dev/null 2>&1; then
      log "warm alias probe ok attempt ${attempt}/${retries} (${launch_url})"
      return 0
    fi
    if (( attempt < retries )); then
      sleep "$delay"
    fi
    ((attempt++))
  done
  log "warn: warm alias probe failed after ${retries} attempts (${launch_url})"
  return 1
}

post_checks() {
  local launch_url
  launch_url="$(build_launch_url "$LAUNCH_BANK_ID" "$LAUNCH_SUBCASINO_ID" "$LAUNCH_GAME_ID" "$LAUNCH_MODE" "$LAUNCH_TOKEN" "$LAUNCH_LANG")"
  log "Quick health checks"
  curl -fsS "http://127.0.0.1:18080/html5pc/actiongames/dragonstone/lobby/version.json" >/dev/null && log "static facade ok" || log "static facade check failed"
  curl -fsS "http://127.0.0.1:${CONFIG_SERVICE_PORT}/health" >/dev/null && log "config-service ok" || log "config-service check failed"
  curl -fsS "http://127.0.0.1:18081/support/bankSelectAction.do?bankId=${LAUNCH_BANK_ID}" >/dev/null && log "gs support route ok" || log "gs support route warn (diagnostic)"
  log "Launch alias URL (refactor static facade): $launch_url"
  if [[ -n "$SECONDARY_LAUNCH_BANK_ID" && -n "$SECONDARY_LAUNCH_SUBCASINO_ID" ]]; then
    local secondary_launch_url
    secondary_launch_url="$(build_launch_url "$SECONDARY_LAUNCH_BANK_ID" "$SECONDARY_LAUNCH_SUBCASINO_ID" "$SECONDARY_LAUNCH_GAME_ID" "$SECONDARY_LAUNCH_MODE" "$SECONDARY_LAUNCH_TOKEN" "$SECONDARY_LAUNCH_LANG")"
    log "Secondary launch URL: $secondary_launch_url"
  fi
}

ACTION="${1:-up}"

case "$ACTION" in
  up)
    preflight
    ensure_runtime_assets
    start_stack
    verify_core_services_running
    start_edge_services
    verify_edge_services_stable
    wait_for_refactor_readiness
    if ! warm_launch_alias_probe; then
      if is_truthy "$REFACTOR_STRICT_READINESS"; then
        die "strict readiness failed: warm alias probe"
      fi
      log "warn: non-strict readiness mode; continuing despite warm alias probe failure"
    fi
    post_checks
    ;;
  preflight)
    preflight
    log "Preflight passed."
    ;;
  smoke)
    post_checks
    ;;
  -h|--help|help)
    usage
    ;;
  *)
    usage >&2
    die "Unknown command: $ACTION"
    ;;
esac
