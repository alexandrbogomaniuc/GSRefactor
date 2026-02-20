#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CORE_FILE="${CM_CORE_FILE:-${CM_USERS_FILE:-${ROOT_DIR}/data/cm-core.json}}"
MIRROR_FILE="${CM_MIRROR_FILE:-${ROOT_DIR}/data/cm-mirror.json}"
LEGACY_FILE="${ROOT_DIR}/data/users.json"

rm -f "${CORE_FILE}" "${MIRROR_FILE}" "${LEGACY_FILE}"
echo "Removed ${CORE_FILE}"
echo "Removed ${MIRROR_FILE}"
echo "Removed ${LEGACY_FILE} (legacy)"
echo "Next start will bootstrap default root/root again."
