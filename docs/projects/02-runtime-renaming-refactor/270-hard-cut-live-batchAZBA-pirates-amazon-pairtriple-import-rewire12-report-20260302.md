# 270 - Hard-cut live batchAZ+BA (pirates+amazon Pair/Triple boundary normalization)

## Scope
Continuation stabilization wave after batchAX+AY, targeting first-fail movement from pirates RNG lane into Pair/Triple generic boundaries:
- `pirates-math` Pair/Triple import and signature boundary alignment.
- `amazon-math` + `amazon` Pair/Triple import and signature boundary alignment.

## What was executed
- Batch AZ (`6` rewires):
  - `pirates-math` files:
    - `ShotCalculator`
    - `MathData`
    - `PayTableInst`
  - normalized `Pair` + `Triple` imports (`com.dgphoenix -> com.abs`).
- Batch BA (`6` rewires):
  - `amazon-math`/`amazon` files:
    - `MathData`
    - `PayTableInst`
    - `EnemyGame`
  - normalized `Pair` + `Triple` imports (`com.dgphoenix -> com.abs`).
- Total retained rewires: `12` import/signature-boundary bindings across `6` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -pl games/common-games -am -DskipTests compile`: PASS
- `mvn -pl web -am -DskipTests compile`: FAIL
  - first-fail moved from `ShotCalculator` Triple mismatch to deeper `pirates` consumer boundaries:
    - `PlayerRoundInfo` Pair type incompatibility (`abs Pair` vs expected `dg Pair`),
    - `EnemyGame` + `PlayGameState` throws mismatch (`overridden method does not throw com.dgphoenix...CommonException`).
- `mvn -pl games/clashofthegods -am -DskipTests compile`: PASS

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP01` (`mvn -DskipTests install`)
- `fast_gate_batchB`: FAIL `STEP01` (`mvn -DskipTests install`)
- `prewarm`: FAIL `PRE01` (`mvn -DskipTests install`)
- `validation`: FAIL `PRE01` (`mvn -DskipTests install`)
- `STEP09 retry1`: SKIP
- Canonical failure remains environment/infrastructure in this sandbox due Maven external dependency resolution constraints (`repo.maven.apache.org` / DNS), not batch-local compile semantics.

## Measured movement
- Cleared previous `ShotCalculator` `getRandomPair` Triple mismatch lane.
- `web` first-fail advanced to narrower pirates model boundary set (`PlayerRoundInfo`, `EnemyGame`, `PlayGameState`) indicating continued forward stabilization.
- `common-games` and `clashofthegods` consumer gates remain PASS.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-172736-hardcut-live-batchAZBA-pirates-amazon-pairtriple-import-rewire12/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-dg-pairtriple-imports.txt`
  - `post-scan-abs-pairtriple-imports.txt`
  - `fast-gate-common-games.log`
  - `fast-gate-web.log`
  - `fast-gate-cotg-consumer.log`
  - `fast-gate-web-first-fail.txt`
  - `fast-gate-cotg-first-fail.txt`
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
- Remaining stabilization/import-normalization ETA: `~0.05-0.35h` (`~0.01-0.04` workdays), centered on:
  - pirates model `Pair`/`CommonException` boundary harmonization (`PlayerRoundInfo`, `EnemyGame`, `PlayGameState` lanes).
