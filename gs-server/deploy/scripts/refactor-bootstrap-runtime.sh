#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
DEV_NEW_ROOT="$(cd "$DEPLOY_DIR/../.." && pwd)"

RUNTIME_BASE="${RUNTIME_BASE:-$DEV_NEW_ROOT/Doker/runtime-gs}"
RUNTIME_ROOT="$RUNTIME_BASE/webapps/gs/ROOT"
DEFAULT_CONFIGS_DIR="$RUNTIME_BASE/default-configs"
LEGACY_CLIENT_DIR="${LEGACY_CLIENT_DIR:-$DEV_NEW_ROOT/legacy-games-client}"
LEGACY_HTML5_GAMES="${LEGACY_HTML5_GAMES:-dragonstone}"
MP_TARGET_DIR="${LEGACY_MP_TARGET_DIR:-$DEV_NEW_ROOT/mp-server/web/target}"

AUTO_BUILD_GS="${AUTO_BUILD_GS:-1}"
AUTO_BUILD_MP="${AUTO_BUILD_MP:-1}"
AUTO_BUILD_HTML5="${AUTO_BUILD_HTML5:-1}"
MAVEN_CMD="${MAVEN_CMD:-mvn}"
NPM_CMD="${NPM_CMD:-npm}"

log() { printf '[refactor-bootstrap] %s\n' "$*"; }
die() { printf '[refactor-bootstrap] ERROR: %s\n' "$*" >&2; exit 1; }
run() { log "$*"; "$@"; }
require_cmd() { command -v "$1" >/dev/null 2>&1 || die "Missing command: $1"; }
require_path() { [[ -e "$1" ]] || die "Missing required path: $1"; }

ensure_base_dirs() {
  mkdir -p \
    "$RUNTIME_BASE/logs/gs" \
    "$RUNTIME_BASE/logs/nginx" \
    "$RUNTIME_BASE/logs/cassandra" \
    "$RUNTIME_BASE/logs/cassandra-refactor" \
    "$RUNTIME_BASE/webapps/gs" \
    "$RUNTIME_BASE/static/info" \
    "$RUNTIME_BASE/static/jackpots" \
    "$RUNTIME_BASE/static/winners" \
    "$DEFAULT_CONFIGS_DIR"
}

build_gs_root_war_if_needed() {
  local root_war="$DEV_NEW_ROOT/gs-server/game-server/web-gs/target/ROOT.war"
  if [[ -f "$root_war" ]]; then
    return
  fi
  [[ "$AUTO_BUILD_GS" == "1" ]] || die "Missing GS ROOT.war and AUTO_BUILD_GS=0: $root_war"
  require_cmd "$MAVEN_CMD"
  require_cmd java
  log "Building GS ROOT.war from gs-server (this may take a while)"
  (
    cd "$DEV_NEW_ROOT/gs-server"
    "$MAVEN_CMD" -s "$DEV_NEW_ROOT/gs-server/game-server/build/build-settings.xml" \
      -DskipTests -pl game-server/web-gs -am package
  )
  [[ -f "$root_war" ]] || die "GS build completed but ROOT.war not found: $root_war"
}

seed_gs_runtime_root() {
  local root_war="$DEV_NEW_ROOT/gs-server/game-server/web-gs/target/ROOT.war"
  if [[ -d "$RUNTIME_ROOT/WEB-INF" ]]; then
    log "GS runtime root already present: $RUNTIME_ROOT"
    return
  fi
  build_gs_root_war_if_needed
  require_cmd unzip
  mkdir -p "$RUNTIME_ROOT"
  log "Extracting ROOT.war into runtime root"
  unzip -oq "$root_war" -d "$RUNTIME_ROOT"
}

seed_default_configs() {
  local src="$DEV_NEW_ROOT/gs-server/game-server/config/local-machine"
  require_path "$src"
  if [[ -n "$(find "$DEFAULT_CONFIGS_DIR" -mindepth 1 -maxdepth 1 2>/dev/null)" ]]; then
    log "Default configs already present: $DEFAULT_CONFIGS_DIR"
    return
  fi
  require_cmd rsync
  log "Copying default configs from gs-server local-machine config"
  rsync -a "$src"/ "$DEFAULT_CONFIGS_DIR"/
}

build_mp_artifact_if_needed() {
  if [[ -d "$MP_TARGET_DIR/web-mp-casino" ]]; then
    return
  fi
  [[ "$AUTO_BUILD_MP" == "1" ]] || die "Missing MP target and AUTO_BUILD_MP=0: $MP_TARGET_DIR/web-mp-casino"
  require_cmd "$MAVEN_CMD"
  require_cmd java
  log "Building MP web artifact into mp-server/web/target (this may take a while)"
  (
    cd "$DEV_NEW_ROOT/mp-server"
    "$MAVEN_CMD" -s config/settings.xml -DskipTests -Dmaven.test.skip=true -pl web -am package
  )
  [[ -d "$MP_TARGET_DIR/web-mp-casino" ]] || die "MP build completed but target dir not found: $MP_TARGET_DIR/web-mp-casino"
}

build_legacy_html5_game() {
  local game="$1"
  local base="$LEGACY_CLIENT_DIR/$game"
  require_path "$base"
  require_cmd "$NPM_CMD"

  if [[ -d "$LEGACY_CLIENT_DIR/common/PIXI" ]]; then
    log "Installing common/PIXI dependencies (build-time)"
    (cd "$LEGACY_CLIENT_DIR/common/PIXI" && "$NPM_CMD" install)
  fi
  if [[ -d "$base/shared" ]]; then
    log "Installing shared dependencies for $game"
    (cd "$base/shared" && "$NPM_CMD" install)
  fi
  if [[ -d "$base/lobby" ]]; then
    log "Installing/building lobby for $game"
    (cd "$base/lobby" && "$NPM_CMD" install && "$NPM_CMD" run build)
  fi
  if [[ -d "$base/game" ]]; then
    log "Installing/building game for $game"
    (cd "$base/game" && "$NPM_CMD" install && "$NPM_CMD" run build)
  fi
}

sync_common_runtime_assets() {
  local dst="$RUNTIME_ROOT/html5pc/actiongames/common"
  mkdir -p "$dst"
  require_cmd rsync
  # Runtime mostly needs assets + PIXI sources/build scripts; exclude node_modules.
  rsync -a --delete \
    --exclude 'node_modules/' \
    --exclude '.git/' \
    "$LEGACY_CLIENT_DIR/common/" "$dst/"
}

sync_game_runtime_assets() {
  local game="$1"
  local src="$LEGACY_CLIENT_DIR/$game"
  local dst="$RUNTIME_ROOT/html5pc/actiongames/$game"
  mkdir -p "$dst"
  require_cmd rsync
  # Copy game sources/build outputs but avoid node_modules and bulky authoring directories.
  rsync -a --delete \
    --exclude 'node_modules/' \
    --exclude '.git/' \
    --exclude '.svn/' \
    --exclude '_sources/' \
    --exclude 'asset_sources/' \
    --exclude 'ansible/' \
    "$src/" "$dst/"
}

seed_html5pc_assets() {
  [[ "$AUTO_BUILD_HTML5" == "1" ]] || { log "Skipping html5pc bootstrap (AUTO_BUILD_HTML5=0)"; return; }
  require_path "$LEGACY_CLIENT_DIR"
  local need_any=0
  for game in ${LEGACY_HTML5_GAMES}; do
    [[ -f "$RUNTIME_ROOT/html5pc/actiongames/$game/game/game.js" ]] || need_any=1
    [[ -f "$RUNTIME_ROOT/html5pc/actiongames/$game/lobby/game.js" ]] || need_any=1
  done
  [[ -f "$RUNTIME_ROOT/html5pc/actiongames/common/assets/version.json" ]] || need_any=1
  if [[ "$need_any" != "1" ]]; then
    log "html5pc runtime assets already present for games: $LEGACY_HTML5_GAMES"
    return
  fi

  sync_common_runtime_assets
  for game in ${LEGACY_HTML5_GAMES}; do
    build_legacy_html5_game "$game"
    sync_game_runtime_assets "$game"
  done
}

main() {
  ensure_base_dirs
  seed_gs_runtime_root
  seed_default_configs
  build_mp_artifact_if_needed
  seed_html5pc_assets
  log "Bootstrap complete."
}

main "$@"
