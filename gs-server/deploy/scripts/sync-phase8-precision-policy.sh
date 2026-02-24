#!/usr/bin/env bash
set -euo pipefail

ROOT="/Users/alexb/Documents/Dev/Dev_new"
SRC="${ROOT}/gs-server/deploy/config/phase8-precision-policy.json"
DST="${ROOT}/gs-server/game-server/web-gs/src/main/resources/phase8-precision-policy.json"

usage() {
  cat <<USAGE
Usage: $(basename "$0")

Syncs Phase 8 precision policy JSON from deploy config into the GS classpath resource copy.
USAGE
}

if [[ ${1:-} == "-h" || ${1:-} == "--help" ]]; then
  usage
  exit 0
fi
if [[ $# -gt 0 ]]; then
  echo "Unknown argument: $1" >&2
  usage
  exit 1
fi

mkdir -p "$(dirname "${DST}")"
cp "${SRC}" "${DST}"
if cmp -s "${SRC}" "${DST}"; then
  echo "synced phase8 precision policy -> ${DST}"
else
  echo "sync verification failed" >&2
  exit 2
fi
