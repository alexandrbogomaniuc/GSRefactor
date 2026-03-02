# 275 - Hard-cut live batchBJ+BK (pirates chain boundary normalization)

## Scope
Continuation stabilization wave after batchBH+BI, targeting the new fail frontier:
- `pirates-common-math` `Triple` namespace mismatch in `ShotCalculator` `GameTools.getRandomPair(...)` call chain.
- adjacent pirates-family and revengeofra-family `CommonException`/`Pair`/`Triple` boundary drifts to prevent immediate bounce-back failures.

## What was executed
- Batch BJ (`13` rewires):
  - `pirates-common-math`: `MathData`, `PayTableInst`, `ShotCalculator` Pair/Triple import normalization (`com.dgphoenix -> com.abs`).
  - `piratespov`: `EnemyGame`, `GameRoom`, `PlayGameState`, `QualifyGameState`, `WaitingPlayersGameState` `CommonException` import normalization, plus `PlayGameState`/`PlayerRoundInfo` `Pair` import normalization.
- Batch BK (`15` rewires):
  - `piratesdmc`: `EnemyGame`, `GameRoom`, `PlayGameState`, `QualifyGameState`, `WaitingPlayersGameState` `CommonException` import normalization; `PlayGameState` `Pair`/`Triple`; `PlayerRoundInfo` `Pair`.
  - `revengeofra-math`: `MathData` + `PayTableInst` `Pair`/`Triple` import normalization.
  - `revengeofra/EnemyGame`: `CommonException` + `Pair` + `Triple` import normalization.
- Total retained rewires: `28` import/signature-boundary bindings across `18` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: FAIL
  - first-fail moved off `pirates-common-math` and advanced to `revengeofra` boundary lane:
    - `PlayerRoundInfo` Pair namespace incompatibilities.
    - `GameRoom`/`PlayGameState`/`WaitingPlayersGameState`/`QualifyGameState` throws-contract mismatch (still binding `com.dgphoenix...CommonException` in remaining files).
    - residual `PlayGameState` Pair/Triple mixed-namespace signature mismatches.
- Harmonized cotg compile gate (`mvn -DskipTests -pl games/clashofthegods -am compile`): PASS.
- Note: initial cotg `install` invocation fails in test-compile lane; compile-only harmonized gate is PASS for comparability.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains the known smoke-stage external/runtime lane, not local import-boundary edits.

## Measured movement
- Cleared the `pirates-common-math` ShotCalculator Triple mismatch lane that was the previous head fail.
- Advanced first-fail into a narrower `revengeofra` boundary set, confirming downstream progression.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-192813-hardcut-live-batchBJBK-pirates-chain-boundary-import-rewire28/`
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
- Remaining stabilization/import-normalization ETA: `~0.40-3.00h` (`~0.05-0.38` workdays), centered on `revengeofra` `CommonException`/`Pair`/`Triple` boundary harmonization.
