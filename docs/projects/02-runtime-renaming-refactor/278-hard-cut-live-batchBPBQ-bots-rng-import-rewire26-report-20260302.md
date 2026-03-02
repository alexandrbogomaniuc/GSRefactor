# 278 - Hard-cut live batchBP+BQ (bots RNG import normalization)

## Scope
Continuation stabilization wave after batchBN+BO, targeting current head fail lane in `mp-server/bots`:
- unresolved legacy `com.dgphoenix.casino.common.util.RNG` imports across bot infrastructure and game-specific strategies.

## What was executed
- Batch BP (`13` rewires): bots infra/shared strategy RNG import normalization.
- Batch BQ (`13` rewires): bots game-specific strategy RNG import normalization.
- Total retained rewires: `26` import-only bindings across `26` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -f mp-server/pom.xml -pl bots -am -DskipTests compile`: FAIL
- `mvn -DskipTests -pl web -am compile` in `mp-server`: FAIL
  - first-fail moved off unresolved RNG imports and narrowed to bots dependency lane:
    - `mp-server/bots/src/main/java/com/betsoft/casino/bots/model/RicochetBullet.java` static import failure for `com.dgphoenix.casino.common.util.string.DateTimeUtils`.
- `mvn -DskipTests install` in `mp-server/games/clashofthegods`: FAIL in known test-compile lane.
- Harmonized cotg compile gate (`mvn -DskipTests -pl games/clashofthegods -am compile`): PASS.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains the known smoke-stage external/runtime lane, not local BP/BQ import-only rewires.

## Measured movement
- Cleared the bots RNG unresolved import frontier (26 rewires completed).
- Advanced first-fail into a narrower bots string-utils dependency lane (`RicochetBullet` static `DateTimeUtils` import), confirming downstream progression.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-210858-hardcut-live-batchBPBQ-bots-rng-import-rewire26/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-dg-imports.txt`
  - `post-scan-abs-imports.txt`
  - `post-scan-dg-rng-global.txt`
  - `fast-gate-common-games.log`
  - `fast-gate-bots.log`
  - `fast-gate-bots-first-fail.txt`
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
- Remaining stabilization/import-normalization ETA: `~0.50-4.00h` (`~0.06-0.50` workdays), centered on bots string-utils dependency alignment (`RicochetBullet` `DateTimeUtils` static import) and any immediate downstream boundary cleanup.
