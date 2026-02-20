#!/usr/bin/env bash

set -euo pipefail

DEV_ROOT="${DEV_ROOT:-/Users/alexb/Documents/Dev}"
GS_RUNTIME_ROOT="${GS_RUNTIME_ROOT:-$DEV_ROOT/Doker/runtime-gs/webapps/gs/ROOT}"
GS_RUNTIME_CLASSES="${GS_RUNTIME_ROOT}/WEB-INF/classes"
GS_SOURCE_ROOT="${GS_SOURCE_ROOT:-$DEV_ROOT/mq-gs-clean-version/game-server/web-gs/src/main/java}"
GS_SOURCE_WEB_XML="${GS_SOURCE_WEB_XML:-$DEV_ROOT/mq-gs-clean-version/game-server/web-gs/src/main/webapp/WEB-INF/web.xml}"
SERVLET_API_JAR="${SERVLET_API_JAR:-$DEV_ROOT/.m2repo/javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.jar}"
OUTPUT_DIR="${OUTPUT_DIR:-$PWD/artifacts/gs-runtime}"
OUTPUT_NAME="${OUTPUT_NAME:-newgames-gs-runtime-$(date -u +%Y%m%d-%H%M%S)}"

required_commands=(javac tar sha256sum)
for cmd in "${required_commands[@]}"; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Missing required command: $cmd" >&2
    exit 1
  fi
done

required_paths=(
  "$GS_RUNTIME_CLASSES"
  "$GS_SOURCE_ROOT"
  "$GS_SOURCE_WEB_XML"
  "$SERVLET_API_JAR"
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

build_dir="$(mktemp -d /tmp/gs-newgames-bundle-build-XXXXXX)"
bundle_dir="$(mktemp -d /tmp/gs-newgames-bundle-root-XXXXXX)"
cleanup() {
  rm -rf "$build_dir" "$bundle_dir"
}
trap cleanup EXIT

classpath="${GS_RUNTIME_CLASSES}:${SERVLET_API_JAR}"
for jar in "$GS_RUNTIME_ROOT"/WEB-INF/lib/*.jar; do
  classpath="${classpath}:$jar"
done

mkdir -p "$build_dir/classes"
javac -cp "$classpath" -d "$build_dir/classes" "${java_sources[@]}"

mkdir -p "$bundle_dir/classes"
cp -R "$build_dir/classes/com" "$bundle_dir/classes/com"
cp "$GS_SOURCE_WEB_XML" "$bundle_dir/web.xml"

source_rev="unknown"
if command -v git >/dev/null 2>&1; then
  source_rev="$(git -C "$DEV_ROOT/mq-gs-clean-version" rev-parse --short HEAD 2>/dev/null || printf 'unknown')"
fi

created_utc="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
cat > "$bundle_dir/manifest.json" <<MANIFEST
{
  "artifact": "newgames-gs-runtime-bundle",
  "createdUtc": "${created_utc}",
  "sourceRevision": "${source_rev}",
  "entries": [
    "classes/com/dgphoenix/casino/actions/enter/game/BaseStartGameAction.class",
    "classes/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameAction.class",
    "classes/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameForm.class",
    "classes/com/dgphoenix/casino/web/api/newgames/NewGamesInternalApiServlet.class",
    "classes/com/dgphoenix/casino/web/api/newgames/NewGamesInternalApiServlet\\$*.class",
    "web.xml"
  ]
}
MANIFEST

mkdir -p "$OUTPUT_DIR"
out_tar="$OUTPUT_DIR/${OUTPUT_NAME}.tar.gz"
(
  cd "$bundle_dir"
  tar -czf "$out_tar" .
)
sha256sum "$out_tar" > "$out_tar.sha256"

printf 'Bundle=%s\n' "$out_tar"
printf 'SHA256=%s\n' "$out_tar.sha256"
printf 'CreatedUtc=%s\n' "$created_utc"
