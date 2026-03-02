# 260 - Hard-cut live batchAD+AE (common-games import rewire)

## Scope
Continuation stabilization wave focused on remaining legacy imports in `mp-server/games/common-games` after batchAB+AC.

## What was executed
- Batch AD: `16` import rewires across `8` files.
- Batch AE: `17` import rewires across `16` files.
- Total retained rewires: `33` declarations across `24` files.
- Import-only rewires from `com.dgphoenix` to `com.abs` for:
  - `RNG`, `Pair`, `Triple`, `CommonException`, `StringUtils`, `StatisticsManager`.

## Validation status
### Targeted mp fast gates
- `mvn -pl games/common-games -am -DskipTests compile`: FAIL (`mp-common-games` first-fail)
- `mvn -pl web -am -DskipTests compile`: FAIL (`mp-common-games` first-fail)
- `mvn -pl games/clashofthegods -am -DskipTests compile`: FAIL (`mp-common-games` first-fail)
- First-fail signature narrowed to residual hotspots:
  - `StubCurrency` import collision
  - `StubSequencerPersister` unresolved legacy symbol imports

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`

## Measured movement
- `common-games` legacy imports (`^import com.dgphoenix`) reduced:
  - before wave: `55`
  - after wave: `22`
  - delta: `-33`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-143929-hardcut-live-batchADAE-common-games-import-rewire33/`
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
- Remaining stabilization/import-normalization ETA: `~6-10h` (`~0.75-1.25` workdays), centered on the final `22` `common-games` legacy imports and residual testmodel symbol cleanup.
