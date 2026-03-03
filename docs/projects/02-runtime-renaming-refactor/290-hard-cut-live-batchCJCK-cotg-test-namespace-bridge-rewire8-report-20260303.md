# 290 - Hard-cut live batchCJCK (COTG test namespace bridge)

## Scope
Continuation stabilization wave after batchCI, targeting localized `clashofthegods` test-compile namespace/type drift while keeping runtime code untouched.

## What was executed
- Batch CJ+CK (`8` targeted rewires) across `4` files:
  - `SeatStat`: removed duplicate/conflicting `ICurrency` import.
  - `TestWrongWeapons`: normalized `Pair` to legacy `com.dgphoenix` variant expected by the current math API.
  - `TestModel`: switched `LongIdGenerator` import to `com.abs`, bridged test currency object to `com.abs...ICurrency`, and added a local `sitIn(...)` override in the anonymous `StubSocketService` to keep signature compatibility in mixed classpath.
  - `TestMathModel`: aligned exception namespace to `com.abs...CommonException` and bridged test currency object to `com.abs...ICurrency`.
- Total retained rewires: `8` across `4` test files.
- No blind global replacement. No runtime production-path behavior changes.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -f mp-server/pom.xml -pl bots -am -DskipTests compile`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: PASS
- `mvn -DskipTests install` in `mp-server/games/clashofthegods`: PASS

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains the known smoke-stage external/runtime lane, not CJ/CK local test rewires.

## Measured movement
- Cleared the localized COTG test-compile fail-head (all four targeted compile gates are now green).
- Remaining red signal is only the canonical external smoke lane (`STEP09`).

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260303-051911-hardcut-live-batchCJCK-cotg-test-namespace-bridge-rewire8/`
  - `pre-commit-git-status.txt`
  - `changed-files.txt`
  - `diff-integrated.patch`
  - `rewire-count.txt`
  - `post-scan-targeted-cotg-test-boundaries.txt`
  - `fast-gate-common-games.log`
  - `fast-gate-bots.log`
  - `fast-gate-web.log`
  - `fast-gate-cotg-consumer.log`
  - `fast-gate-bots-first-fail.txt`
  - `fast-gate-web-first-fail.txt`
  - `fast-gate-cotg-first-fail.txt`
  - `fast-gate-status.txt`
  - `run-validation.sh`
  - `fast-gate-status-batchA-rerun1.txt`
  - `fast-gate-status-batchB-rerun1.txt`
  - `prewarm-status-rerun1.txt`
  - `validation-status-rerun1.txt`
  - `validation-summary-rerun1.txt`
  - `STEP09-rerun1-retry1.log`

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
- Remaining stabilization/import-normalization ETA: `~0.01-0.20h` (`~0.00-0.03` workdays), dominated by known external `STEP09` smoke lane.
