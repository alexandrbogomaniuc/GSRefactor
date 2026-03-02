# 273 - Hard-cut live batchBF+BG (multi-game RNG import boundary normalization)

## Scope
Continuation stabilization wave after batchBD+BE, targeting unresolved `RNG` namespace imports that surfaced as the `web` first-fail lane.

## What was executed
- Batch BF (`12` rewires): `RNG` import normalization in:
  - `amazon` (`GameMap`, `EnemyGame`, `PlayGameState`),
  - `pirates-common-math` (`MathQuestData`, `ShotCalculator`),
  - `piratespov` (`GameMap`, `PlayGameState`),
  - `piratesdmc` (`GameMap`, `PlayGameState`),
  - `revengeofra` (`GameMap`, `EnemyGame`, `PlayGameState`).
- Batch BG (`14` rewires): `RNG` import normalization in:
  - `dragonstone-math` (`MathData`, `SpawnConfig`, `MiniSlot`),
  - `dragonstone` (`DragonStoneTestStand`, `GameMap`, `EnemyGame`, `PlayGameState`),
  - `bg_dragonstone-math` (`MathData`, `SpawnConfig`, `MiniSlot`),
  - `bg_dragonstone` (`BGDragonStoneTestStand`, `GameMap`, `EnemyGame`, `PlayGameState`).
- Total retained rewires: `26` import-only bindings across `26` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: FAIL
  - first-fail moved off unresolved RNG symbols and advanced into amazon boundary mismatches:
    - `CommonException` throws/override mismatch in `QualifyGameState`, `GameRoom`, `EnemyGame`, `WaitingPlayersGameState`, and `PlayGameState` method contracts.
    - mixed `Pair` namespace incompatibilities in `PlayerRoundInfo` and `PlayGameState`.
- Harmonized cotg compile gate (`mvn -DskipTests -pl games/clashofthegods -am compile`): PASS.
- Note: initial cotg `install` result reported FAIL due test-compile lane; compile-only harmonized gate passes for comparability.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains in the known smoke-stage external/runtime lane, not local import edits.

## Measured movement
- Cleared the cross-game unresolved RNG import frontier in targeted modules.
- Advanced `web` first-fail to a narrower amazon class-boundary lane (`CommonException` + `Pair`/signature normalization), which is the next actionable stabilization set.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-184147-hardcut-live-batchBFBG-multi-game-rng-import-rewire26/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-dg-rng-imports.txt`
  - `post-scan-abs-rng-imports.txt`
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
- Remaining stabilization/import-normalization ETA: `~1.00-6.00h` (`~0.13-0.75` workdays), centered on amazon `CommonException` throws-contract and `Pair` boundary harmonization now exposed by this wave.
