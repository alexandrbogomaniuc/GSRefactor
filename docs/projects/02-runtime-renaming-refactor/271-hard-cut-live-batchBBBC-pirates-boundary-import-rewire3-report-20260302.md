# 271 - Hard-cut live batchBB+BC (pirates Pair/CommonException boundary normalization)

## Scope
Continuation stabilization wave after batchAZ+BA, targeting the current pirates model boundary lane:
- `PlayerRoundInfo` `Pair` namespace incompatibility.
- `EnemyGame`/`PlayGameState` `CommonException` throws mismatch against upstream interfaces/base classes.

## What was executed
- Batch BB (`1` rewire):
  - `PlayerRoundInfo` import `Pair` `com.dgphoenix -> com.abs`.
- Batch BC (`2` rewires):
  - `EnemyGame` import `CommonException` `com.dgphoenix -> com.abs`.
  - `PlayGameState` import `CommonException` `com.dgphoenix -> com.abs`.
- Total retained rewires: `3` import/signature-boundary bindings across `3` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -pl games/common-games -am -DskipTests compile`: PASS
- `mvn -pl web -am -DskipTests compile`: FAIL
  - first-fail narrowed to remaining pirates boundaries:
    - `EnemyGame` line 38 unreported `com.dgphoenix...CommonException` (nested/dependent lane still binding old type),
    - `PlayGameState` Pair/Triple namespace incompatibilities at key call sites.
- `mvn -pl games/clashofthegods -am -DskipTests compile`: PASS

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP01` (`mvn -DskipTests install`)
- `fast_gate_batchB`: FAIL `STEP01` (`mvn -DskipTests install`)
- `prewarm`: FAIL `PRE01` (`mvn -DskipTests install`)
- `validation`: FAIL `PRE01` (`mvn -DskipTests install`)
- `STEP09 retry1`: SKIP
- Canonical failure remains environment/infrastructure in this sandbox due Maven external dependency resolution constraints (`repo.maven.apache.org` / DNS), not batch-local compile semantics.

## Measured movement
- Cleared top-level `PlayerRoundInfo` Pair import mismatch lane.
- Reduced pirates throws mismatch surface by rebinding `EnemyGame` and `PlayGameState` to `com.abs` exception import.
- `common-games` and `clashofthegods` consumer gates remain PASS while `web` fails on a tighter residual pirates boundary set.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-180349-hardcut-live-batchBBBC-pirates-boundary-import-rewire3/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-dg-imports.txt`
  - `post-scan-abs-imports.txt`
  - `post-scan-signatures.txt`
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
- Remaining stabilization/import-normalization ETA: `~0.02-0.20h` (`~0.00-0.03` workdays), centered on:
  - residual pirates `PlayGameState` Pair/Triple namespace harmonization and nested `EnemyGame` exception dependency lane.
