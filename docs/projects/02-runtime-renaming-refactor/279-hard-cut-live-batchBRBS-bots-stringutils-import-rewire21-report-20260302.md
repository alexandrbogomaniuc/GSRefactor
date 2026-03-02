# 279 - Hard-cut live batchBR+BS (bots/core string-utils import normalization)

## Scope
Continuation stabilization wave after batchBP+BQ, targeting the next narrowed fail lane:
- bots static `DateTimeUtils` dependency in `RicochetBullet`.
- adjacent low-risk `StringUtils` import normalization in bots/core/core-interfaces.

## What was executed
- Batch BR (`10` rewires): bots string-utils import normalization including static `DateTimeUtils` in `RicochetBullet`.
- Batch BS (`11` rewires): core and core-interfaces `StringUtils` import normalization.
- Total retained rewires: `21` import-only bindings across `21` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -f mp-server/pom.xml -pl bots -am -DskipTests compile`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: FAIL
  - first-fail moved off bots string-utils and into web legacy package lane:
    - `com.dgphoenix.casino.common.mp` unresolved imports,
    - `com.dgphoenix.casino.kafka.dto` unresolved imports,
    - `com.dgphoenix.casino.cassandra.IRemoteUnlocker` unresolved imports.
- `mvn -DskipTests install` in `mp-server/games/clashofthegods`: FAIL in known test-compile lane.
- Harmonized cotg compile gate (`mvn -DskipTests -pl games/clashofthegods -am compile`): PASS.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains the known smoke-stage external/runtime lane, not local BR/BS import-only rewires.

## Measured movement
- Cleared the bots string-utils dependency frontier (`RicochetBullet` static `DateTimeUtils` lane).
- Advanced first-fail into a narrowed web package lane (`common.mp` / `kafka.dto` / `IRemoteUnlocker`) for the next batch.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-214657-hardcut-live-batchBRBS-bots-stringutils-import-rewire21/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-dg-imports.txt`
  - `post-scan-abs-imports.txt`
  - `post-scan-dg-stringutils-global.txt`
  - `pre-commit-git-status.txt`
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
- Remaining stabilization/import-normalization ETA: `~0.40-3.50h` (`~0.05-0.44` workdays), centered on web legacy package normalization (`common.mp`, `kafka.dto`, `IRemoteUnlocker`).
