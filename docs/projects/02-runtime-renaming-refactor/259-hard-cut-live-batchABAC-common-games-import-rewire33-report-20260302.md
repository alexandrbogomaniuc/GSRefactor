# 259 - Hard-cut live batchAB+AC (mp common-games import rewire)

## Scope
Continuation wave focused on the first-failing mp module (`mp-common-games`) after batchAA, using two non-overlapping parallel import-only batches.

## What was executed
- Batch AB (`5` files): `15` import rewires.
- Batch AC (`11` files): `18` import rewires.
- Total retained rewires: `33` declarations across `16` files.
- Targeted symbol families moved to `com.abs`:
  - `RNG`, `Pair`, `Triple`, `StringUtils`, `CommonException`, `StatisticsManager`, `TransactionErrorCodes`, `ILongIdGenerator`.
- No logic changes; import-only rewiring.

## Validation status
### Targeted mp fast gates
- `mvn -pl games/common-games -am -DskipTests compile`: FAIL (`mp-common-games` first-fail)
- `mvn -pl web -am -DskipTests compile`: FAIL (`mp-common-games` first-fail)
- `mvn -pl games/clashofthegods -am -DskipTests compile`: FAIL (`mp-common-games` first-fail)
- Current first-fail signature now points to remaining residual legacy imports (example: `ShamanTrajectoryGenerator` unresolved `com.dgphoenix...RNG`).

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`

## Measured movement
- `mp-server/games/common-games` legacy imports (`^import com.dgphoenix`) reduced:
  - before wave (`HEAD`): `88`
  - after wave (working tree): `55`
  - delta: `-33`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-142034-hardcut-live-batchABAC-common-games-import-rewire33/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `fast-gate-common-games.log`
  - `fast-gate-web.log`
  - `fast-gate-cotg-consumer.log`
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
- Remaining stabilization/import-normalization ETA: `~9-14h` (`~1.1-1.75` workdays), with immediate focus on the remaining `55` `common-games` legacy imports before broader mp-module sweep.
