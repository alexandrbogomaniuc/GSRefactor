# Phase 5 Bonus/History Host Defaults From Cluster Config (Wave 2)

## What was done
- Updated Phase 5 Bonus/FRB and History canary/readiness/evidence scripts to load host-mode default endpoints from `cluster-hosts.properties` using the shared helper (`deploy/scripts/lib/cluster-hosts.sh`).
- Added external endpoint keys for Phase 5 services:
  - `BONUS_FRB_SERVICE_EXTERNAL_HOST`
  - `BONUS_FRB_SERVICE_EXTERNAL_PORT`
  - `HISTORY_SERVICE_EXTERNAL_HOST`
  - `HISTORY_SERVICE_EXTERNAL_PORT`
- Synced updated cluster config into the GS classpath resource so the config portal/support pages display the same values.

## Why this matters (high level)
- Operators can change Phase 5 refactor host endpoints in one place and reuse the same runtime evidence commands.
- It reduces environment drift while preserving backward compatibility (CLI flags still override defaults).

## Validation
- `sync-cluster-hosts.sh` passed.
- `bash -n` passed for all modified Phase 5 bonus/history scripts.
- `--help` passed for modified Phase 5 evidence-pack scripts.
- `phase5-6-local-verification-suite.sh` passed after changes.
  - report: `/Users/alexb/Documents/Dev/Dev_new/docs/quality/local-verification/phase5-6-local-verification-20260223-132527.md`
  - summary: PASS=14, FAIL=0, SKIP=0

## Scope note
- This is the next staged wave only for Phase 5 Bonus/History. Wallet/GamePlay/Phase4/Phase7 script default migration remains for follow-up waves.
