#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEST_PATH="$ROOT_DIR/../../GameseDonors/ChickenGame/assets/_donor_raw_local"

DEST_MANIFEST="$DEST_PATH/runtime/manifest.json"
EXPECTED_MANIFEST_SHA256="${DONORLOCAL_APPROVED_MANIFEST_SHA256:-b6c0fe4c6677df0926f0e3eb572b9f44b0117f060a7422e082c4bff569830188}"
UNAPPROVED_SLOT_HUNT_PATH="$DEST_PATH/runtime/slot_hunt_latest"
if [[ -L "$DEST_PATH" ]]; then
  echo "[7000] donorlocal destination must be a real folder (symlink detected): $DEST_PATH" >&2
  exit 1
fi

if [[ ! -f "$DEST_MANIFEST" ]]; then
  echo "[7000] donorlocal destination manifest was not created: $DEST_MANIFEST" >&2
  exit 1
fi

if [[ -d "$UNAPPROVED_SLOT_HUNT_PATH" ]]; then
  echo "[7000] donorlocal destination includes disallowed runtime payload: $UNAPPROVED_SLOT_HUNT_PATH" >&2
  echo "[7000] restore approved assets before launching donorlocal benchmark." >&2
  exit 1
fi

ACTUAL_MANIFEST_SHA256="$(shasum -a 256 "$DEST_MANIFEST" | awk '{print $1}')"
if [[ "$ACTUAL_MANIFEST_SHA256" != "$EXPECTED_MANIFEST_SHA256" ]]; then
  echo "[7000] donorlocal manifest hash mismatch." >&2
  echo "[7000] expected: $EXPECTED_MANIFEST_SHA256" >&2
  echo "[7000] actual:   $ACTUAL_MANIFEST_SHA256" >&2
  echo "[7000] refusing to launch with non-approved assets." >&2
  exit 1
fi

echo "[7000] donorlocal asset source locked (validate-only)"
echo "destination: $DEST_PATH"
echo "manifest: $DEST_MANIFEST"
echo "manifest_sha256: $ACTUAL_MANIFEST_SHA256"
