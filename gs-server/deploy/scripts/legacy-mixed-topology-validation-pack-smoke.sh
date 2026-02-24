#!/usr/bin/env bash
set -euo pipefail

TMP="$(mktemp -d)"
trap 'rm -rf "${TMP}"' EXIT
SCRIPT="/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/legacy-mixed-topology-validation-pack.sh"
run="$(bash "${SCRIPT}" --dry-run true --out-dir "${TMP}")"
echo "${run}" | grep -q 'status=DRY_RUN_READY'
report="$(echo "${run}" | awk -F= '/^report=/{print $2}')"
grep -q 'Validation Flow Checklist' "${report}"
echo "LEGACY_MIXED_TOPOLOGY_VALIDATION_PACK_SMOKE_OK"
