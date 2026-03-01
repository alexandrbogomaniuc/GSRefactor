# 257 - Hard-cut live stabilization (STEP08 mp-persistance import normalization)

## Scope
Post-hard-cut stabilization wave to recover canonical `STEP08` by removing mixed namespace references in `mp-server/persistance` and revalidating full canonical flow.

## What was executed
- Ran canonical rerun (`rerun2`) after prior STEP07 repair.
  - Result: blockers shifted to `STEP08` (`mp-server/persistance`) due mixed `com.dgphoenix`/`com.abs` type bounds.
- Applied bounded normalization in:
  - `mp-server/persistance/src/main/java`
  - converted `com.dgphoenix.casino.*` references to `com.abs.casino.*` across `41` Java files.
- Fast gate check:
  - `mp-server/persistance`: `mvn -DskipTests install` PASS.
- Ran full canonical rerun (`rerun3`).

## Validation status (canonical)
- `fast_gate_batchA`: FAIL `STEP09` (smoke) rc=2
- `fast_gate_batchB`: FAIL `STEP09` (smoke) rc=2
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (smoke) rc=2
- `STEP09 retry1`: FAIL rc=2

## Blocker profile
- Current first failing step is now `STEP09` only.
- Failure remains the known external smoke blocker:
  - launch alias `/startgame?...` returns `HTTP 503`.
- Compile/build chain through `STEP01-STEP08` is now recovered.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260301-114628-hardcut-live-stabilization-canonical-rerun-post-step07fix`
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260301-115005-hardcut-live-stabilization-canonical-rerun-post-step08fix`

## Metrics
- Baseline declarations/files: `2277`
- Reduced: `2277`
- Remaining: `0`
- Hard-cut burndown: `100.000000%`
- Project 01: `100.000000%`
- Project 02: `54.645725%`
- Core total (01+02): `77.322863%`
- Entire portfolio: `88.661431%`

## ETA refresh
- Hard-cut declaration refactor ETA: `0.0h` (complete).
- Remaining stabilization ETA: `~8-14h` (`~1.0-1.75` workdays), primarily external STEP09 smoke environment recovery.
