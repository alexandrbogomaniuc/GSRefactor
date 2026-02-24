#!/usr/bin/env bash

set -euo pipefail

DEV_ROOT="${DEV_ROOT:-/Users/alexb/Documents/Dev}"
GS_CONTAINER="${GS_CONTAINER:-gp3-gs-1}"
GS_ENDPOINT_BASE="${GS_ENDPOINT_BASE:-http://localhost:81}"
GS_RUNTIME_ROOT="${GS_RUNTIME_ROOT:-$DEV_ROOT/Doker/runtime-gs/webapps/gs/ROOT}"
GS_RUNTIME_CLASSES="${GS_RUNTIME_ROOT}/WEB-INF/classes"
GS_RUNTIME_WEB_XML="${GS_RUNTIME_ROOT}/WEB-INF/web.xml"
GS_SOURCE_ROOT="${GS_SOURCE_ROOT:-$DEV_ROOT/mq-gs-clean-version/game-server/web-gs/src/main/java}"
GS_SOURCE_WEB_XML="${GS_SOURCE_WEB_XML:-$DEV_ROOT/mq-gs-clean-version/game-server/web-gs/src/main/webapp/WEB-INF/web.xml}"
SERVLET_API_JAR="${SERVLET_API_JAR:-$DEV_ROOT/.m2repo/javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.jar}"
RESTART_GS="${RESTART_GS:-1}"
SYNC_RUNTIME_WEB_XML="${SYNC_RUNTIME_WEB_XML:-1}"
WAIT_SECONDS="${WAIT_SECONDS:-240}"
CLASS_BUNDLE="${CLASS_BUNDLE:-}"
LAUNCH_URL="${LAUNCH_URL:-http://localhost/startgame?bankId=6274&gameId=00010&mode=real&token=bav_game_session_001&lang=en}"

required_commands=(rsync curl docker)
for cmd in "${required_commands[@]}"; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Missing required command: $cmd" >&2
    exit 1
  fi
done

if [[ -n "$CLASS_BUNDLE" ]] && ! command -v tar >/dev/null 2>&1; then
  echo "Missing required command: tar" >&2
  exit 1
fi

required_paths=(
  "$GS_RUNTIME_CLASSES"
  "$GS_RUNTIME_WEB_XML"
  "$GS_SOURCE_ROOT"
  "$GS_SOURCE_WEB_XML"
)
for path in "${required_paths[@]}"; do
  if [[ ! -e "$path" ]]; then
    echo "Missing required path: $path" >&2
    exit 1
  fi
done

java_sources=(
  "$GS_SOURCE_ROOT/com/dgphoenix/casino/actions/enter/game/BaseStartGameAction.java"
  "$GS_SOURCE_ROOT/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameAction.java"
  "$GS_SOURCE_ROOT/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameForm.java"
  "$GS_SOURCE_ROOT/com/dgphoenix/casino/web/api/newgames/NewGamesInternalApiServlet.java"
)

for src in "${java_sources[@]}"; do
  if [[ ! -f "$src" ]]; then
    echo "Missing Java source: $src" >&2
    exit 1
  fi
done

work_dir="$(mktemp -d /tmp/gs-newgames-deploy-XXXXXX)"
cleanup() {
  rm -rf "$work_dir"
}
trap cleanup EXIT

compiled_classes_root=""
bundle_web_xml=""
if [[ -n "$CLASS_BUNDLE" ]]; then
  if [[ ! -f "$CLASS_BUNDLE" ]]; then
    echo "CLASS_BUNDLE does not exist: $CLASS_BUNDLE" >&2
    exit 1
  fi

  tar -xzf "$CLASS_BUNDLE" -C "$work_dir"
  if [[ ! -d "$work_dir/classes/com" ]]; then
    echo "CLASS_BUNDLE is invalid. Missing classes/com content." >&2
    exit 1
  fi

  compiled_classes_root="$work_dir/classes"
  if [[ -f "$work_dir/web.xml" ]]; then
    bundle_web_xml="$work_dir/web.xml"
  fi
  echo "Using prebuilt class bundle: $CLASS_BUNDLE"
else
  if [[ ! -f "$SERVLET_API_JAR" ]]; then
    echo "Missing required path: $SERVLET_API_JAR" >&2
    exit 1
  fi
  if ! command -v javac >/dev/null 2>&1; then
    echo "Missing required command: javac" >&2
    exit 1
  fi

  mkdir -p "$work_dir/classes"

  classpath="${GS_RUNTIME_CLASSES}:${SERVLET_API_JAR}"
  for jar in "$GS_RUNTIME_ROOT"/WEB-INF/lib/*.jar; do
    classpath="${classpath}:$jar"
  done

  javac -cp "$classpath" -d "$work_dir/classes" "${java_sources[@]}"
  compiled_classes_root="$work_dir/classes"
  echo "Compiled classes from source."
fi

if ! rg -q "/gs-internal/newgames/v1/\*" "$GS_RUNTIME_WEB_XML"; then
  if [[ "$SYNC_RUNTIME_WEB_XML" == "1" ]]; then
    timestamp="$(date +%Y%m%d_%H%M%S)"
    backup_web_xml="/tmp/gs-runtime-webxml-${timestamp}.bak"
    cp "$GS_RUNTIME_WEB_XML" "$backup_web_xml"
    if [[ -n "$bundle_web_xml" ]]; then
      cp "$bundle_web_xml" "$GS_RUNTIME_WEB_XML"
      echo "Runtime web.xml synced from bundle (backup: $backup_web_xml)"
    else
      cp "$GS_SOURCE_WEB_XML" "$GS_RUNTIME_WEB_XML"
      echo "Runtime web.xml synced from source (backup: $backup_web_xml)"
    fi
  else
    echo "Runtime web.xml does not contain New Games servlet mapping." >&2
    echo "Set SYNC_RUNTIME_WEB_XML=1 or patch $GS_RUNTIME_WEB_XML manually." >&2
    exit 1
  fi
fi

timestamp="$(date +%Y%m%d_%H%M%S)"
backup_dir="/tmp/gs-runtime-class-backup-${timestamp}"
mkdir -p "$backup_dir"
shopt -s nullglob
backup_targets=(
  "$GS_RUNTIME_CLASSES/com/dgphoenix/casino/actions/enter/game/BaseStartGameAction.class"
  "$GS_RUNTIME_CLASSES/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameAction.class"
  "$GS_RUNTIME_CLASSES/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameForm.class"
  "$GS_RUNTIME_CLASSES/com/dgphoenix/casino/web/api/newgames/NewGamesInternalApiServlet"*.class
)
for target in "${backup_targets[@]}"; do
  if [[ -e "$target" ]]; then
    rel_path="${target#${GS_RUNTIME_CLASSES}/}"
    mkdir -p "$backup_dir/$(dirname "$rel_path")"
    cp -a "$target" "$backup_dir/$rel_path"
  fi
done
shopt -u nullglob

rsync -a "$compiled_classes_root/com/" "$GS_RUNTIME_CLASSES/com/"
echo "Copied classes to runtime. Backup: $backup_dir"

if [[ "$RESTART_GS" == "1" ]]; then
  docker restart "$GS_CONTAINER" >/dev/null
  echo "Restarted container: $GS_CONTAINER"
else
  echo "Skipped GS restart (RESTART_GS=$RESTART_GS)"
fi

deadline=$((SECONDS + WAIT_SECONDS))
while true; do
  http_code="$(curl -s -o /dev/null -w '%{http_code}' "$GS_ENDPOINT_BASE/gs-internal/newgames/v1/session/validate" || true)"
  if [[ "$http_code" == "405" ]]; then
    break
  fi
  if (( SECONDS >= deadline )); then
    echo "Timed out waiting for GS internal endpoint readiness (last status=$http_code)." >&2
    exit 1
  fi
  sleep 2
done
echo "GS internal endpoint is ready (GET -> 405 as expected)."

launch_headers="$(curl -sS -D - -o /dev/null "$LAUNCH_URL")"
launch_status="$(printf '%s\n' "$launch_headers" | awk 'NR==1{print $2}')"
launch_location="$(printf '%s\n' "$launch_headers" | awk 'BEGIN{IGNORECASE=1}/^Location:/{print $2}' | tr -d '\r')"
if [[ "$launch_status" != "302" ]]; then
  echo "Launch URL did not return 302. Status=$launch_status" >&2
  exit 1
fi

echo "Launch route check passed (302)."
echo "Location: $launch_location"
echo "Done."
