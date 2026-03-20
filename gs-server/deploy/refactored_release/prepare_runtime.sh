#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GS_SERVER_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

RUNTIME_DIR="${RUNTIME_DIR:-$SCRIPT_DIR/runtime}"
WEBGS_TARGET_DIR="${WEBGS_TARGET_DIR:-$GS_SERVER_DIR/game-server/web-gs/target}"
ROOT_WAR_INPUT="${ROOT_WAR_INPUT:-$WEBGS_TARGET_DIR/ROOT.war}"
CONFIG_SRC="${CONFIG_SRC:-$GS_SERVER_DIR/game-server/config/local-machine}"
STATIC_HTML5_SOURCE="${STATIC_HTML5_SOURCE:-}"
MP_LOBBY_WS_HOST="${MP_LOBBY_WS_HOST:-127.0.0.1:8080}"
CASSANDRA_HOST_ALIAS="${CASSANDRA_HOST_ALIAS:-fullstack-cassandra}"
ZOOKEEPER_HOST_ALIAS="${ZOOKEEPER_HOST_ALIAS:-fullstack-zookeeper}"
KAFKA_HOST_ALIAS="${KAFKA_HOST_ALIAS:-fullstack-kafka}"

require_file() {
  local path="$1"
  if [ ! -e "$path" ]; then
    echo "missing required path: $path" >&2
    exit 1
  fi
}

require_file "$ROOT_WAR_INPUT"
require_file "$CONFIG_SRC"

rm -rf "$RUNTIME_DIR"
mkdir -p "$RUNTIME_DIR/exploded" "$RUNTIME_DIR"

unzip -qo "$ROOT_WAR_INPUT" -d "$RUNTIME_DIR/exploded"
cp -R "$CONFIG_SRC" "$RUNTIME_DIR/export_localmachine"
mkdir -p "$RUNTIME_DIR/static_html5"

python3 - "$RUNTIME_DIR/exploded/WEB-INF/classes" "$RUNTIME_DIR/export_localmachine" "$MP_LOBBY_WS_HOST" "$CASSANDRA_HOST_ALIAS" "$ZOOKEEPER_HOST_ALIAS" "$KAFKA_HOST_ALIAS" <<'PY'
from pathlib import Path
import re
import sys

classes_dir = Path(sys.argv[1])
export_dir = Path(sys.argv[2])
mp_lobby_ws_host = sys.argv[3]
cassandra_host = sys.argv[4]
zk_host = sys.argv[5]
kafka_host = sys.argv[6]

for name in [
    "ClusterConfig.xml",
    "SCClusterConfig.xml",
    "BigStorageClusterConfig.xml",
    "cluster-hosts.properties",
    "common.properties",
]:
    path = classes_dir / name
    if not path.exists():
        continue
    text = path.read_text(errors="ignore")
    text = text.replace(
        "c1.gsmp.lan:9042,c2.gsmp.lan:9042,c3.gsmp.lan:9042",
        f"{cassandra_host}:9042,{cassandra_host}:9042,{cassandra_host}:9042",
    )
    text = text.replace(
        "c1.gsmp.lan:7199,c2.gsmp.lan:7199,c3.gsmp.lan:7199",
        f"{cassandra_host}:7199,{cassandra_host}:7199,{cassandra_host}:7199",
    )
    for host in ("c1.gsmp.lan", "c2.gsmp.lan", "c3.gsmp.lan"):
        text = text.replace(host, cassandra_host)
    text = text.replace("zookeeper1.gsmp.lan:2181", f"{zk_host}:2181")
    text = text.replace("zookeeper:2181", f"{zk_host}:2181")
    text = text.replace("kafka1.gsmp.lan:9092", f"{kafka_host}:9092")
    text = text.replace("kafka:9092", f"{kafka_host}:9092")
    path.write_text(text)

pattern = re.compile(
    r"(<string>MP_LOBBY_WS_HOST</string>\s*<string>)([^<]+)(</string>)",
    re.MULTILINE,
)
for path in export_dir.glob("*.xml"):
    text = path.read_text(errors="ignore")
    updated = pattern.sub(rf"\g<1>{mp_lobby_ws_host}\3", text)
    path.write_text(updated)
PY

(
  cd "$RUNTIME_DIR/exploded"
  jar -cf "$RUNTIME_DIR/ROOT.patched.war" .
)

rm -rf "$RUNTIME_DIR/exploded"

if [ -n "$STATIC_HTML5_SOURCE" ]; then
  require_file "$STATIC_HTML5_SOURCE"
  rm -rf "$RUNTIME_DIR/static_html5"
  ln -s "$STATIC_HTML5_SOURCE" "$RUNTIME_DIR/static_html5"
fi

cat <<EOF
Prepared runtime bundle:
  ROOT_WAR=$RUNTIME_DIR/ROOT.patched.war
  EXPORT_BUNDLE=$RUNTIME_DIR/export_localmachine
  STATIC_HTML5=$RUNTIME_DIR/static_html5
EOF
