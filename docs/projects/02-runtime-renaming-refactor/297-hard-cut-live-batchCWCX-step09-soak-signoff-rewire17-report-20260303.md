# Hard-cut Live Batch CW+CX - STEP09 Soak Signoff Automation

- Timestamp (UTC): 2026-03-03 08:06-08:11
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Scope: Close remaining runtime signoff gap by adding explicit soak automation and strict-fail diagnostics.
- Batch size: `17` targeted edits.

## Summary
Integrated `CW+CX` to convert repeated-run closure from manual process to scripted, evidence-producing checks.

- `CW` (`refactor-onboard.mjs`):
  - added `soak` command for repeated smoke execution with artifact output.
  - added soak knobs: `REFACTOR_SOAK_RUNS`, `REFACTOR_SOAK_GAP_MS`, `REFACTOR_SOAK_ARTIFACT_DIR`.
  - soak writes `soak-summary.json` + `soak-summary.txt` with per-run outcomes and final rc (`0/2/3`).
- `CX` (`refactor-start.sh`, `README.md`):
  - added strict-fail diagnostics toggles (`REFACTOR_DIAG_ON_FAIL`, `REFACTOR_DIAG_TAIL_LINES`).
  - added failure diagnostic bundle (core compose ps, nginx error tail, probe commands) before strict readiness failures.
  - added warn-only restart-count surface after successful `up`.
  - documented startup diagnostics and soak usage semantics.

## Files Changed
- `gs-server/deploy/scripts/refactor-onboard.mjs`
- `gs-server/deploy/scripts/refactor-start.sh`
- `gs-server/deploy/docker/refactor/README.md`

## Validation
Evidence directory:
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260303-080606-hardcut-live-batchCWCX-step09-soak-signoff/`

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

Additional soak signoff:
- `soak-run1` (`REFACTOR_SOAK_RUNS=2`, gap `1000ms`) -> `pass_count=2`, `final_rc=0`.
- `soak-run2` (`REFACTOR_SOAK_RUNS=2`, gap `1000ms`) -> `pass_count=2`, `final_rc=0`.

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
- Runtime closure/signoff: `~0.00-0.50h` (`~0.00-0.06` workdays) for optional additional monitoring checkpoint and final acceptance.
