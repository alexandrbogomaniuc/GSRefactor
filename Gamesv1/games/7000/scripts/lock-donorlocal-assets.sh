#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEST_PATH="$ROOT_DIR/../../GameseDonors/ChickenGame/assets/_donor_raw_local"
SOURCE_PATH="/Users/alexb/Documents/Dev/GSRefactor-beta-local-procedure-live-20260307/Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local"
SOURCE_MANIFEST="$SOURCE_PATH/runtime/manifest.json"

if [[ ! -f "$SOURCE_MANIFEST" ]]; then
  echo "[7000] donorlocal source manifest is missing: $SOURCE_MANIFEST" >&2
  exit 1
fi

if [[ -L "$DEST_PATH" ]]; then
  rm "$DEST_PATH"
fi

mkdir -p "$DEST_PATH"
rsync -a --delete "$SOURCE_PATH/" "$DEST_PATH/"

DEST_MANIFEST="$DEST_PATH/runtime/manifest.json"
if [[ ! -f "$DEST_MANIFEST" ]]; then
  echo "[7000] donorlocal destination manifest was not created: $DEST_MANIFEST" >&2
  exit 1
fi

echo "[7000] donorlocal asset source locked"
echo "source: $SOURCE_PATH"
echo "destination: $DEST_PATH"
echo "manifest: $DEST_MANIFEST"
