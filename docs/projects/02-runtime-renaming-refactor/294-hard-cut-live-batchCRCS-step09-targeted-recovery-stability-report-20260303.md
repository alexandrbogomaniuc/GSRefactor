# Hard-cut Live Batch CR+CS - STEP09 Targeted Recovery Stability

- Timestamp (UTC): 2026-03-03 07:05-07:30
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Scope: Stabilize STEP09 runtime lane by fixing MP startup regression and making smoke recovery targeted + restart-aware.
- Batch size: `20` targeted edits.

## Summary
Batch `CR+CS` addressed a concrete regression and reduced recovery blast radius.

- `CR` (compose/start hardening):
  - fixed `mp` startup command syntax regression (`sh: Syntax error: "&&" unexpected`).
  - made MP bootstrap copy retry race-safe for mutable `/legacy-mp-target` content.
  - lowered `c1-refactor` heap profile (`384M/96M`) and `kafka` heap opts (`128m/384m`) to reduce churn pressure.
  - added core-service stability window checks in `refactor-start.sh` before continuing startup.
- `CS` (smoke targeted recovery + diagnostics):
  - infra diagnostics now include core-service `status` + `restartCount` and explicit `restarting` unhealthy signal.
  - recovery target list now uses unhealthy core services only, always adding `gs/static` (instead of blind full-core restart).
  - preserved `rc=3` semantics for unresolved infra lane.

## Files Changed
- `gs-server/deploy/docker/refactor/docker-compose.yml`
- `gs-server/deploy/scripts/refactor-start.sh`
- `gs-server/deploy/scripts/refactor-onboard.mjs`
- `gs-server/deploy/docker/refactor/README.md`

## Validation
Evidence directory:
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260303-070507-hardcut-live-batchCRCS-step09-targeted-recovery-stability/`

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
- `step09_retry1 PASS rc=0`

Key movement:
- `STEP09` retry moved from infra-fail (`rc=3`) to pass (`rc=0`) within the same run by targeted recovery path.
- Retry log shows first-pass alias/support/direct failures, diagnostics, targeted `gs/static` recovery, then full launch alias pass.

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
- Residual runtime closure (`STEP09` first-pass stability / repeated-run consistency): `~0.25-3.00h` (`~0.03-0.38` workdays).
