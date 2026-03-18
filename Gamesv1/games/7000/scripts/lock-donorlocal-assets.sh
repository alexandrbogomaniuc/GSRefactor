#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LINK_PATH="$ROOT_DIR/../../GameseDonors/ChickenGame/assets/_donor_raw_local"
LOCKED_TARGET="/Users/alexb/Documents/Dev/GSRefactor_phase1a_20260305-1323/Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local"
LOCKED_MANIFEST="$LOCKED_TARGET/runtime/manifest.json"

if [[ ! -f "$LOCKED_MANIFEST" ]]; then
  echo "[7000] locked donorlocal manifest is missing: $LOCKED_MANIFEST" >&2
  exit 1
fi

ln -sfn "$LOCKED_TARGET" "$LINK_PATH"

RESOLVED_MANIFEST_DIR="$(cd "$(dirname "$LINK_PATH/runtime/manifest.json")" && pwd -P)"
RESOLVED_MANIFEST="$RESOLVED_MANIFEST_DIR/manifest.json"

echo "[7000] donorlocal asset source locked"
echo "link: $LINK_PATH"
echo "target: $LOCKED_TARGET"
echo "manifest: $RESOLVED_MANIFEST"
