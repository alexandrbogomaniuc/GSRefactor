#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REFACTOR_DIR="$(cd "$SCRIPT_DIR/../docker/refactor" && pwd)"

(
  cd "$REFACTOR_DIR"
  docker compose -p refactor --env-file .env down
)
