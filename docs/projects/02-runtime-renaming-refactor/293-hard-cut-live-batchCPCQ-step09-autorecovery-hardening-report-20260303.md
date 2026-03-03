# Hard-cut Live Batch CP+CQ - STEP09 Auto-Recovery Hardening

- Timestamp (UTC): 2026-03-03 06:29-06:54
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Scope: Runtime-safe STEP09 stabilization via infra survivability tuning + smoke auto-recovery diagnostics.
- Batch size: `20` targeted edits.

## Summary
Batch `CP+CQ` focused on reducing infra-collapse churn and making STEP09 outage causes deterministic within a single smoke run.

- `CP` (compose/start survivability):
  - added `restart: unless-stopped` for `mp`, `c1-refactor`, `zookeeper`.
  - tuned infra heap/JVM defaults (`c1-refactor`, `kafka`, `zookeeper`) to reduce host-memory churn.
  - hardened `mp` boot copy with bounded retries.
  - added `verify_core_services_running()` gate in `refactor-start.sh` before `gs/static` startup.
- `CQ` (smoke diagnostics/recovery):
  - added infra diagnostics (`docker compose ps` + exited-container state/OOM metadata).
  - added bounded auto-recovery knobs in smoke flow (`REFACTOR_SMOKE_AUTORECOVER`, `REFACTOR_SMOKE_RECOVERY_ATTEMPTS`).
  - preserved classification semantics: unresolved infra lane remains `rc=3` with explicit evidence.

## Files Changed
- `gs-server/deploy/docker/refactor/docker-compose.yml`
- `gs-server/deploy/scripts/refactor-start.sh`
- `gs-server/deploy/scripts/refactor-onboard.mjs`
- `gs-server/deploy/docker/refactor/README.md`

## Validation
Evidence directory:
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260303-062945-hardcut-live-batchCPCQ-step09-autorecovery-hardening/`

Fast gate compile results:
- `common_games_compile=0`
- `bots_compile=0`
- `web_compile=0`
- `cotg_compile=0`

Full matrix summary:
- `fast_gate_batchA FAIL STEP09`
- `fast_gate_batchB FAIL STEP09`
- `prewarm PASS`
- `validation FAIL STEP09`
- `step09_retry1 FAIL rc=3`

Observed behavior improvement:
- STEP09 retry now emits compose-state diagnostics and runs bounded recovery attempt inline.
- Failure remains infra lane (`rc=3`), now tied to explicit core instability (`c1-refactor` restart loop with `137` status observed during retry window).

## Metrics
- Baseline tracked declarations/files: `2277`
- Reduced: `2277`
- Remaining: `0`
- Burndown completion: `100.000000%`

Weighted metrics:
- Project 01: `100.000000%`
- Project 02: `54.645725%`
- Core total (01+02): `77.322863%`
- Entire portfolio: `88.661431%`

## ETA Refresh
- Refactor declaration migration: complete (`0` remaining).
- Residual runtime closure (STEP09 core-infra stability, mainly `c1-refactor` restart/137 churn): `~0.50-6.00h` (`~0.06-0.75` workdays).
