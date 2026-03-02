# 277 - Hard-cut live batchBN+BO (dragonstone boundary normalization)

## Scope
Continuation stabilization wave after batchBL+BM, targeting current head fail lanes in dragonstone families:
- `Pair` namespace mismatches in `PlayerRoundInfo` / `PlayGameState` / `GameMap`.
- `CommonException` throws-contract drift in room/state/game files for `dragonstone` and `bg_dragonstone`.

## What was executed
- Batch BN (`9` rewires):
  - `dragonstone`: normalized `Pair` imports in `PlayerRoundInfo`, `PlayGameState`, `GameMap`.
  - `dragonstone`: normalized `CommonException` imports in `PlayGameState`, `GameRoom`, `QualifyGameState`, `WaitingPlayersGameState`, `RoomSeats`, `EnemyGame`.
- Batch BO (`10` rewires):
  - `bg_dragonstone`: normalized `Pair` imports in `PlayerRoundInfo`, `PlayGameState`, `GameMap`.
  - `bg_dragonstone`: normalized `CommonException` imports in `PlayGameState`, `GameRoom`, `QualifyGameState`, `WaitingPlayersGameState`, `PrivateBTGWaitingGameState`, `RoomSeats`, `EnemyGame`.
- Total retained rewires: `19` import/signature-boundary bindings across `17` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: FAIL
  - first-fail moved off `dragonstone` boundary lane and advanced into `mp-server/bots` unresolved legacy RNG imports (`com.dgphoenix.casino.common.util.RNG`).
- `mvn -DskipTests install` in `mp-server/games/clashofthegods`: FAIL in known test-compile lane.
- Harmonized cotg compile gate (`mvn -DskipTests -pl games/clashofthegods -am compile`): PASS.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains the known smoke-stage external/runtime lane, not local BN/BO import-boundary edits.

## Measured movement
- Cleared the `dragonstone` + `bg_dragonstone` Pair/CommonException boundary head lane.
- Advanced first-fail downstream into `bots` RNG migration lane, confirming monotonic progression.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-202723-hardcut-live-batchBNBO-dragonstone-boundary-import-rewire19/`
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
- Remaining stabilization/import-normalization ETA: `~0.70-5.50h` (`~0.09-0.69` workdays), centered on `mp-server/bots` RNG import harmonization and any dependent boundary cleanup.
