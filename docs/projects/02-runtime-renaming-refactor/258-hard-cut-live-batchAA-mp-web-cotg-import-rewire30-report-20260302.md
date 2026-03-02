# 258 - Hard-cut live batchAA (mp-web + clashofthegods import rewire)

## Scope
Post-completion hard-cut stabilization wave focused on low-risk `mp-server` import rewires from `com.dgphoenix` to `com.abs` in non-overlapping batches.

## What was executed
- Explorer carved two non-overlapping safe batches; two workers applied them in parallel.
- Batch A (`mp-server/web`): `13` import rewires.
  - Classes: `CommonException`, `StringUtils`, `Pair`, `ApplicationContextHelper`, `ExecutorUtils`.
- Batch B (`mp-server/games/clashofthegods-math`): `13` import rewires.
  - Classes: `Pair`, `Triple`.
- Integrated boundary drift fix in `mp-server/core-interfaces`:
  - `4` `RNG` import rewires required to unblock immediate compile drift in dependency chain.
- Net retained rewires: `30` declarations across `24` Java files (import-only; no logic changes).

## Validation status
### Targeted fast gates (mp-server)
- `mvn -pl web -am -DskipTests compile`: FAIL (`MP Common Games 1.0.0-SNAPSHOT` first-fail).
- `mvn -pl games/clashofthegods-math -am -DskipTests compile`: FAIL (`MP Common Games 1.0.0-SNAPSHOT` first-fail).
- `mvn -pl games/clashofthegods -am -DskipTests compile`: FAIL (`MP Common Games 1.0.0-SNAPSHOT` first-fail).
- Key first-fail diagnostics:
  - `cannot find symbol: class RNG`
  - `package com.dgphoenix.casino.common.mp does not exist`
  - `cannot find symbol: class ILongIdGenerator`
  - `cannot find symbol: variable TransactionErrorCodes`

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`

## Blocker profile
- Canonical chain remains recovered through compile/build lanes (`PRE01-03 PASS`, `STEP01-08 PASS`).
- Current canonical blocker remains external smoke lane at `STEP09` (`rc=2`).
- mp-targeted fast gates are currently blocked by unresolved residual legacy imports/symbols in `mp-server/games/common-games` and adjacent modules.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-135824-hardcut-live-batchAA-mp-web-cotg-import-rewire30/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `fast-gate-batchA-mp-web.log`
  - `fast-gate-batchB-cotg-math.log`
  - `fast-gate-batchB-consumer-cotg.log`
  - `fast-gate-status.txt`
  - `run-rerun1.sh`
  - `fast-gate-status-batchA-rerun1.txt`
  - `fast-gate-status-batchB-rerun1.txt`
  - `prewarm-status-rerun1.txt`
  - `validation-status-rerun1.txt`
  - `validation-summary-rerun1.txt`

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
- Remaining stabilization/import-normalization ETA: `~12-18h` (`~1.5-2.25` workdays), centered on mp fast-gate residual legacy import cleanup + known external STEP09 smoke condition.
