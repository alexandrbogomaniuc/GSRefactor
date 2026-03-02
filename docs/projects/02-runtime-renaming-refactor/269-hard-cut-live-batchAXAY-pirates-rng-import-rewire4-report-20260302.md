# 269 - Hard-cut live batchAX+AY (pirates RNG import normalization)

## Scope
Continuation stabilization wave after batchAV+AW, targeting `web` first-fail in pirates lanes:
- `pirates-math` `ShotCalculator` unresolved legacy `RNG` import.
- adjacent `pirates` model RNG imports to maintain forward first-fail progression.

## What was executed
- Batch AX (`1` rewire):
  - `ShotCalculator` import `RNG` `com.dgphoenix -> com.abs`.
- Batch AY (`3` rewires):
  - `GameMap`, `GameRoom`, `PlayGameState` imports `RNG` `com.dgphoenix -> com.abs`.
- Total retained rewires: `4` import-only bindings across `4` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -pl games/common-games -am -DskipTests compile`: PASS
- `mvn -pl web -am -DskipTests compile`: FAIL
  - first-fail remains in `pirates-math/ShotCalculator`, now advanced from RNG-missing to generic boundary mismatch:
    - `GameTools.getRandomPair` expects `List<com.abs...Triple<...>>`
    - call site still supplies `List<com.dgphoenix...Triple<...>>`
- `mvn -pl games/clashofthegods -am -DskipTests compile`: PASS

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP01` (`mvn -DskipTests install`)
- `fast_gate_batchB`: FAIL `STEP01` (`mvn -DskipTests install`)
- `prewarm`: FAIL `PRE01` (`mvn -DskipTests install`)
- `validation`: FAIL `PRE01` (`mvn -DskipTests install`)
- `STEP09 retry1`: SKIP
- Canonical failure remains environment/infrastructure in this sandbox due Maven external dependency resolution constraints (`repo.maven.apache.org` / DNS), not batch-local compile semantics.

## Measured movement
- Cleared legacy RNG import lane in targeted pirates files (`4/4` rewires retained).
- `web` first-fail moved deeper to tighter Triple namespace/type-boundary mismatch in `ShotCalculator`, confirming forward stabilization movement.
- `common-games` and `clashofthegods` consumer gates remain PASS.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-171440-hardcut-live-batchAXAY-pirates-rng-import-rewire4/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-dg-rng-imports.txt`
  - `post-scan-abs-rng-imports.txt`
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
- Remaining stabilization/import-normalization ETA: `~0.1-0.6h` (`~0.01-0.08` workdays), centered on:
  - `pirates-math` `Triple`/`Pair` namespace boundary alignment against `GameTools`.
