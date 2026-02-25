#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GS_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
REPO_ROOT="$(cd "${GS_ROOT}/.." && pwd)"
MP_ROOT="${REPO_ROOT}/mp-server"
NEW_GAMES_ROOT="${REPO_ROOT}/new-games-server"
OUT_DIR="${REPO_ROOT}/docs/phase7/cassandra"
TS="$(date -u '+%Y%m%d-%H%M%S')"
OUT_FILE=""

usage() {
  cat <<USAGE
Usage: $(basename "$0") [options]

Options:
  --repo-root DIR Repository root that contains gs-server/mp-server (default: ${REPO_ROOT})
  --gs-root DIR   GS root (default: ${GS_ROOT})
  --mp-root DIR   MP root (default: ${MP_ROOT})
  --new-games-root DIR  New games root (default: ${NEW_GAMES_ROOT})
  --out-dir DIR   Output dir (default: ${OUT_DIR})
  -h, --help      Show help
USAGE
}

recompute_defaults_from_repo_root() {
  GS_ROOT="${REPO_ROOT}/gs-server"
  MP_ROOT="${REPO_ROOT}/mp-server"
  NEW_GAMES_ROOT="${REPO_ROOT}/new-games-server"
  OUT_DIR="${REPO_ROOT}/docs/phase7/cassandra"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo-root)
      REPO_ROOT="$2"
      recompute_defaults_from_repo_root
      shift 2 ;;
    --gs-root)
      GS_ROOT="$2"; shift 2 ;;
    --mp-root)
      MP_ROOT="$2"; shift 2 ;;
    --new-games-root)
      NEW_GAMES_ROOT="$2"; shift 2 ;;
    --out-dir)
      OUT_DIR="$2"; shift 2 ;;
    -h|--help)
      usage; exit 0 ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1 ;;
  esac
done

OUT_FILE="${OUT_DIR}/phase7-cassandra-driver-inventory-${TS}.txt"
mkdir -p "$OUT_DIR"

scan_dependency_declarations() {
  local root="$1"
  local label="$2"
  echo "== ${label}: Cassandra dependency declarations =="
  if [[ ! -d "$root" ]]; then
    echo "missing_root=${root}"
    echo
    return
  fi

  rg -n --glob "pom.xml" \
    "cassandra-driver-core|cassandra-driver-mapping|java-driver-core|java-driver-query-builder|java-driver-mapper-runtime|spring-data-cassandra|cassandra\\.driver\\.version|datastax\\.java\\.driver" \
    "$root" -S || true
  echo
}

scan_import_inventory() {
  local root="$1"
  local label="$2"
  echo "== ${label}: Java import inventory =="
  if [[ ! -d "$root" ]]; then
    echo "missing_root=${root}"
    echo
    return
  fi

  local v3_count
  local v4_count
  set +o pipefail
  v3_count="$(rg -n --glob "*.java" "import com\\.datastax\\.driver\\.core\\." "$root" | wc -l | tr -d ' ')"
  v4_count="$(rg -n --glob "*.java" "import com\\.datastax\\.oss\\.driver\\.api\\." "$root" | wc -l | tr -d ' ')"
  set -o pipefail
  echo "driver3_import_lines=${v3_count}"
  echo "driver4_import_lines=${v4_count}"
  echo

  echo "-- top files with driver3 imports --"
  rg -n --glob "*.java" "import com\\.datastax\\.driver\\.core\\." "$root" \
    | cut -d: -f1 \
    | sort | uniq -c | sort -nr | head -n 30 || true
  echo

  echo "-- driver3 type usage by import --"
  rg -N --no-filename --glob "*.java" -o "import com\\.datastax\\.driver\\.core\\.[A-Za-z0-9_]+" "$root" \
    | sed -E 's/^import com\.datastax\.driver\.core\.//' \
    | sort | uniq -c | sort -nr | head -n 40 || true
  echo

  echo "-- driver4 type usage by import --"
  rg -N --no-filename --glob "*.java" -o "import com\\.datastax\\.oss\\.driver\\.api\\.[A-Za-z0-9_\\.]+[A-Za-z0-9_]+" "$root" \
    | sed -E 's/^import com\.datastax\.oss\.driver\.api\.//' \
    | sort | uniq -c | sort -nr | head -n 40 || true
  echo
}

{
  echo "timestamp_utc=$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
  echo "script=${SCRIPT_DIR}/$(basename "$0")"
  echo "repo_root=${REPO_ROOT}"
  echo "gs_root=${GS_ROOT}"
  echo "mp_root=${MP_ROOT}"
  echo "new_games_root=${NEW_GAMES_ROOT}"
  echo

  scan_dependency_declarations "$GS_ROOT" "GS"
  scan_dependency_declarations "$MP_ROOT" "MP"
  scan_dependency_declarations "$NEW_GAMES_ROOT" "NEW_GAMES"

  scan_import_inventory "$GS_ROOT" "GS"
  scan_import_inventory "$MP_ROOT" "MP"
  scan_import_inventory "$NEW_GAMES_ROOT" "NEW_GAMES"
} > "$OUT_FILE"

echo "driver_inventory=${OUT_FILE}"
