# Hard-cut Live Batch CT - STEP09 First-pass Stability

- Timestamp (UTC): 2026-03-03 07:34-07:40
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Scope: Improve STEP09 first-pass reliability by treating recent core-service restarts as unhealthy recovery targets.
- Batch size: `12` targeted edits.

## Summary
Batch `CT` hardened smoke diagnostics and recovery targeting to catch restart loops that appear as transient `running` states.

- `refactor-onboard.mjs`:
  - added `uptimeSeconds` capture from container inspect state.
  - marked `running` services with `restartCount>0` and low uptime (`<=120s`) as `recently restarted` unhealthy signals.
  - included `recently restarted` services in targeted recovery set.
  - introduced adaptive recovery wait (`3s` for `gs/static` only, `10s` when core infra services are included).
  - increased default `REFACTOR_SMOKE_RECOVERY_ATTEMPTS` from `1` to `2`.
- `README.md`:
  - documented new diagnostics fields (`uptimeSeconds`) and `recently restarted` signal semantics.
  - documented adaptive wait and updated default recovery attempts.

## Files Changed
- `gs-server/deploy/scripts/refactor-onboard.mjs`
- `gs-server/deploy/docker/refactor/README.md`

## Validation
Evidence directory:
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260303-073447-hardcut-live-batchCT-step09-firstpass-stability/`

Fast gate compile results:
- `common_games_compile=0`
- `bots_compile=0`
- `web_compile=0`
- `cotg_compile=0`

Full matrix summary:
- `fast_gate_batchA PASS`
- `fast_gate_batchB PASS`
- `prewarm PASS`
- `validation PASS`
- `step09_retry1 SKIP (not needed)`

Key movement:
- `STEP09` now passes on first full validation run (no retry required).
- Diagnostics now explicitly distinguish unstable restart windows from healthy steady-state runtime.

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
- Residual runtime closure (`STEP09` repeated-run consistency / soak): `~0.10-1.50h` (`~0.01-0.19` workdays).
