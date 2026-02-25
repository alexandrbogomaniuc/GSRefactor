#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
DEV_NEW_ROOT="$(cd "$DEPLOY_DIR/../.." && pwd)"
WORKSPACE_ROOT="$(cd "$DEV_NEW_ROOT/.." && pwd)"

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
PRIVATE_M2_REPO_DIR="${PRIVATE_M2_REPO_DIR:-$WORKSPACE_ROOT/.m2repo}"
PRIVATE_M2_BULK_SYNC="${PRIVATE_M2_BULK_SYNC:-1}"
PRIVATE_M2_BULK_REL_PATHS="${PRIVATE_M2_BULK_REL_PATHS:-com/dgphoenix}"
PRIVATE_UTILS_COORDS="${PRIVATE_UTILS_COORDS:-com.dgphoenix.casino:utils-restricted:1.1.0}"
PRIVATE_MP_SEED_COORDS="${PRIVATE_MP_SEED_COORDS:-com.dgphoenix.casino:gsn-casino-project:1.0.0-SNAPSHOT:pom com.dgphoenix.casino:gsn-cassandra-cache:1.0-SNAPSHOT:pom com.dgphoenix.casino:rng:2.0 com.dgphoenix.casino:gsn-common:1.0-SNAPSHOT com.dgphoenix.casino:gsn-utils-restricted:1.0-SNAPSHOT com.dgphoenix.casino:gsn-cache-restricted:1.0-SNAPSHOT com.dgphoenix.casino.tools:kryo-validator:2.9.1-SNAPSHOT com.dgphoenix.casino.tools:annotations:1.1.0 com.dgphoenix.casino:utils-restricted:1.1.0}"

log() { printf '[refactor-bootstrap] %s\n' "$*"; }
die() { printf '[refactor-bootstrap] ERROR: %s\n' "$*" >&2; exit 1; }
run() { log "$*"; "$@"; }
require_cmd() { command -v "$1" >/dev/null 2>&1 || die "Missing command: $1"; }
require_path() { [[ -e "$1" ]] || die "Missing required path: $1"; }
has_cmd() { command -v "$1" >/dev/null 2>&1; }

maven_local_repo_dir() {
  local candidate="${MAVEN_LOCAL_REPO:-$HOME/.m2/repository}"
  printf '%s\n' "$candidate"
}

coord_to_repo_path() {
  local coords="$1"
  local group artifact version packaging
  IFS=':' read -r group artifact version packaging <<<"$coords"
  packaging="${packaging:-jar}"
  local group_path
  group_path="$(printf '%s' "$group" | tr '.' '/')"
  printf '%s/%s/%s/%s' "$group_path" "$artifact" "$version" "$artifact-$version"
}

copy_local_m2_artifact() {
  local coords="$1"
  local group artifact version packaging
  IFS=':' read -r group artifact version packaging <<<"$coords"
  packaging="${packaging:-jar}"
  [[ -n "${group:-}" && -n "${artifact:-}" && -n "${version:-}" ]] || \
    die "Artifact coordinates must be groupId:artifactId:version[:packaging] (got: $coords)"

  local local_repo
  local_repo="$(maven_local_repo_dir)"
  local base_rel
  base_rel="$(coord_to_repo_path "$coords")"
  local local_artifact="$local_repo/${base_rel}.${packaging}"
  local local_pom="$local_repo/${base_rel}.pom"

  local source_artifact="" source_pom=""
  local source_base="$PRIVATE_M2_REPO_DIR/${base_rel%/*}"
  if [[ -f "$source_base/$(basename "${base_rel}.${packaging}")" && -f "$source_base/$(basename "${base_rel}.pom")" ]]; then
    source_artifact="$source_base/$(basename "${base_rel}.${packaging}")"
    source_pom="$source_base/$(basename "${base_rel}.pom")"
  fi

  if [[ -z "$source_artifact" ]]; then
    while IFS= read -r artifact_path; do
      local dir_path pom_path
      dir_path="$(dirname "$artifact_path")"
      pom_path="$dir_path/${artifact}-${version}.pom"
      if [[ -f "$pom_path" ]]; then
        source_artifact="$artifact_path"
        source_pom="$pom_path"
        break
      fi
    done < <(find "$WORKSPACE_ROOT" -type f -path "*/${artifact}/${version}/${artifact}-${version}.${packaging}" 2>/dev/null | sort)
  fi

  if [[ -z "$source_artifact" || -z "$source_pom" ]]; then
    log "Private artifact not found locally for $coords (looked in $PRIVATE_M2_REPO_DIR and workspace caches)"
    return
  fi

  if [[ -f "$local_artifact" && -f "$local_pom" ]] && cmp -s "$local_artifact" "$source_artifact" && cmp -s "$local_pom" "$source_pom"; then
    log "Private Maven artifact already installed: $coords"
    return
  fi

  log "Seeding local Maven cache from workspace copy: $coords"
  mkdir -p "$(dirname "$local_artifact")"
  cp -f "$source_artifact" "$local_artifact"
  cp -f "$source_pom" "$local_pom"
}

bulk_seed_private_m2_cache() {
  [[ "$PRIVATE_M2_BULK_SYNC" == "1" ]] || return
  local local_repo rel src dst
  local_repo="$(maven_local_repo_dir)"
  for rel in $PRIVATE_M2_BULK_REL_PATHS; do
    src="$PRIVATE_M2_REPO_DIR/$rel"
    dst="$local_repo/$rel"
    [[ -d "$src" ]] || continue
    mkdir -p "$dst"
    if has_cmd rsync; then
      run rsync -a "$src/" "$dst/"
    else
      log "rsync not found; using cp -R for local Maven bulk seed: $rel"
      cp -R "$src/." "$dst/"
    fi
  done
}

ensure_private_mp_seed_artifacts() {
  bulk_seed_private_m2_cache
  local coords
  for coords in $PRIVATE_MP_SEED_COORDS; do
    copy_local_m2_artifact "$coords"
  done
}

copy_tree_contents() {
  local src="$1"
  local dst="$2"
  mkdir -p "$dst"
  if has_cmd rsync; then
    run rsync -a "${src}/" "${dst}/"
  else
    log "rsync not found; using cp -R fallback for ${src} -> ${dst}"
    rm -rf "${dst:?}/"*
    cp -R "${src}/." "${dst}/"
  fi
}

sync_tree_filtered() {
  local src="$1"
  local dst="$2"
  shift 2
  mkdir -p "$dst"
  if has_cmd rsync; then
    run rsync -a --delete "$@" "${src}/" "${dst}/"
  else
    log "rsync not found; using cp -R fallback (filtered sync not available) for ${src} -> ${dst}"
    rm -rf "${dst:?}/"*
    cp -R "${src}/." "${dst}/"
    rm -rf "${dst}/node_modules" "${dst}/.git" "${dst}/.svn" "${dst}/_sources" "${dst}/asset_sources" "${dst}/ansible" || true
  fi
}

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
  log "Copying default configs from gs-server local-machine config"
  copy_tree_contents "$src" "$DEFAULT_CONFIGS_DIR"
}

build_mp_artifact_if_needed() {
  if [[ -d "$MP_TARGET_DIR/web-mp-casino" ]]; then
    return
  fi
  [[ "$AUTO_BUILD_MP" == "1" ]] || die "Missing MP target and AUTO_BUILD_MP=0: $MP_TARGET_DIR/web-mp-casino"
  require_cmd "$MAVEN_CMD"
  require_cmd java
  ensure_private_mp_seed_artifacts
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
  # Runtime mostly needs assets + PIXI sources/build scripts; exclude node_modules.
  sync_tree_filtered "$LEGACY_CLIENT_DIR/common" "$dst" \
    --exclude 'node_modules/' \
    --exclude '.git/'
}

sync_game_runtime_assets() {
  local game="$1"
  local src="$LEGACY_CLIENT_DIR/$game"
  local dst="$RUNTIME_ROOT/html5pc/actiongames/$game"
  mkdir -p "$dst"
  # Copy game sources/build outputs but avoid node_modules and bulky authoring directories.
  sync_tree_filtered "$src" "$dst" \
    --exclude 'node_modules/' \
    --exclude '.git/' \
    --exclude '.svn/' \
    --exclude '_sources/' \
    --exclude 'asset_sources/' \
    --exclude 'ansible/'
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
