# 276 - Hard-cut live batchBL+BM (revengeofra boundary normalization)

## Scope
Continuation stabilization wave after batchBJ+BK, targeting the current fail frontier in `games/revengeofra`:
- `PlayerRoundInfo` `Pair` namespace mismatch.
- `GameRoom` / `PlayGameState` / `WaitingPlayersGameState` / `QualifyGameState` throws-contract drift on `CommonException`.
- residual `Pair`/`Triple` boundary drift in `revengeofra` model + test support files.

## What was executed
- Batch BL (`7` rewires):
  - `PlayerRoundInfo`: `Pair` import normalization (`com.dgphoenix -> com.abs`).
  - `GameRoom`: `CommonException` import normalization.
  - `PlayGameState`: `CommonException` + `Pair` + `Triple` import normalization.
  - `WaitingPlayersGameState` + `QualifyGameState`: `CommonException` import normalization.
- Batch BM (`4` rewires):
  - `GameMap`: `Pair` import normalization.
  - `RoomSeats`: `CommonException` import normalization.
  - `TestWrongWeapons`: `Pair` import normalization and stale legacy `Triple` import removal.
- Total retained rewires: `11` import/signature-boundary bindings across `8` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: FAIL
  - first-fail moved off `revengeofra` and advanced to `dragonstone` boundary lane:
    - `PlayerRoundInfo` Pair namespace incompatibilities.
    - `PlayGameState`/`GameRoom` throws-contract mismatches (override signatures still expecting `com.dgphoenix...CommonException` in downstream lane).
- `mvn -DskipTests install` in `mp-server/games/clashofthegods`: FAIL in test-compile lane (known baseline test drift).
- Harmonized cotg compile gate (`mvn -DskipTests -pl games/clashofthegods -am compile`): PASS.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains the known smoke-stage external/runtime lane, not local import-boundary edits.

## Measured movement
- Cleared the `revengeofra` fail-head boundary lane that emerged after batchBJ+BK.
- Advanced first-fail into the next downstream family (`dragonstone`) with the same boundary class (`Pair` + `CommonException` throws-contract), confirming monotonic hard-cut progression.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-195147-hardcut-live-batchBLBM-revengeofra-boundary-import-rewire11/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-dg-imports.txt`
  - `post-scan-abs-imports.txt`
  - `fast-gate-common-games.log`
  - `fast-gate-web.log`
  - `fast-gate-web-first-fail.txt`
  - `fast-gate-cotg-consumer.log`
  - `fast-gate-cotg-first-fail.txt`
  - `fast-gate-cotg-consumer-compile.log`
  - `fast-gate-cotg-consumer-compile-first-fail.txt`
  - `fast-gate-status.txt`
  - `fast-gate-status-harmonized.txt`
  - `run-validation.sh`
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
- Remaining stabilization/import-normalization ETA: `~0.90-6.50h` (`~0.11-0.81` workdays), centered on `dragonstone` `Pair` + `CommonException` boundary harmonization now visible as the new head fail lane.
