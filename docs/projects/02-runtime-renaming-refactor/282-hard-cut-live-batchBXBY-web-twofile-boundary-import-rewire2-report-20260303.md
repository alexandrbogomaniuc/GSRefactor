# 282 - Hard-cut live batchBX+BY (web two-file boundary alignment)

## Scope
Continuation stabilization wave after batchBV+BW, targeting the narrowed two-file web fail-head:
- `RoomServiceFactory` exception-namespace boundary alignment.
- `KafkaMultiPlayerResponseService` `Identifiable` namespace alignment.

## What was executed
- Batch BX: aligned `RoomServiceFactory` exception boundary handling so generic service-loading paths keep `com.dgphoenix` contract while `roomSaved.start()` catches `com.abs` exception type.
- Batch BY: aligned `KafkaMultiPlayerResponseService` `Identifiable` import to the namespace implemented by `IRoomPlayerInfo`.
- Total retained rewires/boundary alignments: `2` targeted updates across `2` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -f mp-server/pom.xml -pl bots -am -DskipTests compile`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: FAIL
  - first-fail moved off `RoomServiceFactory`/`KafkaMultiPlayerResponseService` lane into a narrower `SitInHandler` exception-namespace lane:
    - `SitInHandler` mixed `com.dgphoenix` catch vs `com.abs` throw contract at lines `573/576/582/846`.
- `mvn -DskipTests install` in `mp-server/games/clashofthegods`: FAIL in known test-compile lane.
- Harmonized cotg compile gate (`mvn -DskipTests -pl games/clashofthegods -am compile`): PASS.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains the known smoke-stage external/runtime lane, not local BX/BY boundary rewires.

## Measured movement
- Cleared `RoomServiceFactory` mixed exception boundary mismatch.
- Cleared `KafkaMultiPlayerResponseService` `Identifiable::getId` namespace mismatch.
- Reduced web fail-head to localized `SitInHandler` exception boundary lane.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-233010-hardcut-live-batchBXBY-web-twofile-boundary-import-rewire2/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-dg-imports.txt`
  - `post-scan-abs-imports.txt`
  - `post-scan-targeted-global.txt`
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
- Remaining stabilization/import-normalization ETA: `~0.05-1.20h` (`~0.01-0.15` workdays), centered on `SitInHandler` exception-namespace boundary harmonization.
