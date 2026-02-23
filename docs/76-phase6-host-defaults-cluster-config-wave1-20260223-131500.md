# Phase 6 Host Defaults From Cluster Config (Wave 1)

## What was done
- Added shared helper `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/lib/cluster-hosts.sh` for reading `cluster-hosts.properties` with safe fallbacks.
- Updated Phase 6 multiplayer scripts to load default host/port values from cluster config instead of hardcoded literals:
  - `phase6-multiplayer-routing-policy-probe.sh`
  - `phase6-multiplayer-canary-probe.sh`
  - `phase6-multiplayer-runtime-readiness-check.sh`
  - `phase6-multiplayer-runtime-evidence-pack.sh`
- Added external endpoint keys for host-mode tooling:
  - `MULTIPLAYER_SERVICE_EXTERNAL_HOST`
  - `MULTIPLAYER_SERVICE_EXTERNAL_PORT`
  - `GS_EXTERNAL_HOST`
  - `GS_EXTERNAL_PORT`
- Synced cluster-host config to portal classpath resource via `sync-cluster-hosts.sh`, making the new keys visible in the GS support/config portal pages.

## Why this matters (high level)
- This reduces hidden environment assumptions in test/evidence tooling.
- Operators can change refactor host endpoints in one place (`cluster-hosts.properties`) and reuse the same scripts without editing commands.
- It preserves backward compatibility because all scripts still fall back to the previous default values if keys are missing.

## Validation
- `bash -n` passed for helper and all modified Phase 6 scripts.
- `--help` passed for modified Phase 6 scripts.
- `sync-cluster-hosts.sh` passed and updated portal resource copy.
- `phase5-6-local-verification-suite.sh` passed after changes.
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-131357.md`
  - summary: PASS=14, FAIL=0, SKIP=0

## Scope note
- This is a staged migration wave (Phase 6 scripts first). Remaining Phase 4/5/7 scripts will be moved to cluster-config-backed defaults in follow-up waves.
