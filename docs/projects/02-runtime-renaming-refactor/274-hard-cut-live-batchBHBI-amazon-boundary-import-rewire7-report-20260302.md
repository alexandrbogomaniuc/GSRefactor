# 274 - Hard-cut live batchBH+BI (amazon CommonException/Pair boundary normalization)

## Scope
Continuation stabilization wave after batchBF+BG, targeting amazon boundary mismatches that became the `web` first-fail lane:
- `CommonException` throws/override contract mismatches.
- mixed `Pair` namespace incompatibilities.

## What was executed
- Batch BH (`2` rewires):
  - `amazon/model/PlayGameState` imports:
    - `CommonException` `com.dgphoenix -> com.abs`
    - `Pair` `com.dgphoenix -> com.abs`
- Batch BI (`5` rewires):
  - `CommonException` import normalization in:
    - `QualifyGameState`, `WaitingPlayersGameState`, `GameRoom`, `EnemyGame`.
  - `Pair` import normalization in:
    - `PlayerRoundInfo`.
- Total retained rewires: `7` import/signature-boundary bindings across `6` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: FAIL
  - first-fail moved off amazon and advanced to `pirates-common-math` Triple namespace boundary:
    - `ShotCalculator` `GameTools.getRandomPair(...)` expecting `List<com.abs...Triple<...>>` but receiving `List<com.dgphoenix...Triple<...>>`.
- Harmonized cotg compile gate (`mvn -DskipTests -pl games/clashofthegods -am compile`): PASS.
- Note: initial cotg `install` lane reports FAIL due test-compile drift; compile-only harmonized gate is PASS for comparability.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains in the known smoke-stage external/runtime lane, not local BH/BI import-boundary edits.

## Measured movement
- Cleared amazon `CommonException` throws-contract and `Pair` namespace boundary set in targeted files.
- Advanced web first-fail into the next narrow lane: `pirates-common-math` Triple boundary alignment.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-190339-hardcut-live-batchBHBI-amazon-boundary-import-rewire7/`
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
- Remaining stabilization/import-normalization ETA: `~0.60-4.00h` (`~0.08-0.50` workdays), centered on `pirates-common-math` Triple namespace harmonization and immediate downstream consumer boundaries.
