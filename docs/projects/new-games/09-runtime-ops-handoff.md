# Runtime Ops Handoff

Last updated: 2026-02-13

This runbook is for repeatable local/runtime deployment and verification of New Games route `00010`.

## Scripts
- Build bundle artifact:
  - `/Users/alexb/Documents/Dev/new-games-server/scripts/build-gs-runtime-bundle.sh`
- Deploy to GS runtime:
  - `/Users/alexb/Documents/Dev/new-games-server/scripts/deploy-gs-runtime.sh`
- Run end-to-end verification:
  - `/Users/alexb/Documents/Dev/new-games-server/scripts/runtime-e2e.sh`
- Snapshot runtime health/status:
  - `/Users/alexb/Documents/Dev/new-games-server/scripts/runtime-status.sh`

## Fast Path (single machine)
1. `cd /Users/alexb/Documents/Dev/new-games-server`
2. `npm run runtime:deploy-gs`
3. `npm run runtime:e2e`
4. `npm run runtime:status`

## M4 Evidence Pack
- Generate one-command performance/stability evidence:
  - `cd /Users/alexb/Documents/Dev/new-games-server`
  - `npm run runtime:proof-pack`
- Generated report location:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/new-games/evidence/m4-proof-pack-<UTC>.md`

## Bundle-Based Deploy (recommended for handoff)
1. Build artifact on integration machine:
   - `cd /Users/alexb/Documents/Dev/new-games-server`
   - `npm run runtime:build-bundle`
2. Deploy from artifact (no local GS source compile required):
   - `CLASS_BUNDLE=/absolute/path/to/newgames-gs-runtime-<timestamp>.tar.gz npm run runtime:deploy-gs`
3. Verify:
   - `npm run runtime:e2e`
   - `npm run runtime:status`

## Expected Healthy Signals
- Deploy script:
  - `GS internal endpoint is ready (GET -> 405 as expected).`
  - `Launch route check passed (302).`
- E2E script:
  - `E2E OK`
  - `GSValidateStatus=200`
  - `Open/Place/Collect/History` all with status `200`
- Status script (default non-mutating mode):
  - `GSInternalValidateGetStatus=405`
  - `LaunchStatus=skipped (set PROBE_LAUNCH=1 to execute launch check)`
  - `NGSHealthStatus=200`
  - latest `gameplay_transactions` rows show paired `bet`/`win` with same `external_transaction_id`.
- Optional route probe:
  - `PROBE_LAUNCH=1 npm run runtime:status` should return `LaunchStatus=302`.
  - do not run in parallel with `runtime:e2e` (launch probe creates a new SID).

## Key Environment Overrides
- `CLASS_BUNDLE`: deploy prebuilt artifact instead of compiling sources.
- `RESTART_GS=0`: skip GS container restart.
- `GS_ENDPOINT_BASE`, `GS_CONTAINER`, `LAUNCH_URL`: adjust target routing.
- `NGS_BASE_URL`, `AUTO_START_NGS`: control NGS process behavior in E2E/status scripts.
- `PROBE_LAUNCH=1`: include launch URL probe in `runtime:status`.

## Rollback
- Deploy script stores replaced classes backup in:
  - `/tmp/gs-runtime-class-backup-<timestamp>/`
- Restore manually by copying backup classes back into:
  - `/Users/alexb/Documents/Dev/Doker/runtime-gs/webapps/gs/ROOT/WEB-INF/classes/`
- Restart GS container and rerun `npm run runtime:status`.
