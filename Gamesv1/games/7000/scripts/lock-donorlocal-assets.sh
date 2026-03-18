#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEST_PATH="$ROOT_DIR/../../GameseDonors/ChickenGame/assets/_donor_raw_local"

DEST_MANIFEST="$DEST_PATH/runtime/manifest.json"
if [[ -L "$DEST_PATH" ]]; then
  echo "[7000] donorlocal destination must be a real folder (symlink detected): $DEST_PATH" >&2
  exit 1
fi

if [[ ! -f "$DEST_MANIFEST" ]]; then
  echo "[7000] donorlocal destination manifest was not created: $DEST_MANIFEST" >&2
  exit 1
fi

echo "[7000] donorlocal asset source locked (validate-only)"
echo "destination: $DEST_PATH"
echo "manifest: $DEST_MANIFEST"
