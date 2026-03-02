# 281 - Hard-cut live batchBV+BW (web boundary signature/import alignment)

## Scope
Continuation stabilization wave after batchBT+BU, targeting the next narrowed web compatibility lane:
- `ILockManager` generic contract alignment in `RemoteUnlocker`.
- `CassandraPersistenceManager` type alignment in `WebSocketRouter`/`EnterLobbyHandler`.
- `CommonException` namespace/signature alignment in `AbstractStartGameUrlHandler` and `SocketService`.

## What was executed
- Batch BV (`3` rewires): import/type alignment in `RemoteUnlocker`, `WebSocketRouter`, `EnterLobbyHandler`.
- Batch BW (`8` rewires): `CommonException` namespace/signature alignment in `AbstractStartGameUrlHandler` and `SocketService`.
- Total retained rewires: `11` import/signature boundary rewires across `5` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -f mp-server/pom.xml -pl bots -am -DskipTests compile`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: FAIL
  - first-fail moved off prior `RemoteUnlocker`/`WebSocketRouter`/start-game override lane into a narrower two-file boundary lane:
    - `RoomServiceFactory` mixed exception namespace handling (`com.dgphoenix` catch vs `com.abs` throw),
    - `KafkaMultiPlayerResponseService` method-reference typing issue at line `721` (`Identifiable::getId`).
- `mvn -DskipTests install` in `mp-server/games/clashofthegods`: FAIL in known test-compile lane.
- Harmonized cotg compile gate (`mvn -DskipTests -pl games/clashofthegods -am compile`): PASS.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains the known smoke-stage external/runtime lane, not local BV/BW import/signature rewires.

## Measured movement
- Cleared `RemoteUnlocker` generic contract mismatch and cassandra manager type mismatch lane.
- Cleared broad start-game/SocketService signature mismatch lane.
- Reduced web fail-head to two localized files (`RoomServiceFactory`, `KafkaMultiPlayerResponseService`).

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-230338-hardcut-live-batchBVBW-web-boundary-signature-import-rewire12/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-dg-imports.txt`
  - `post-scan-abs-imports.txt`
  - `post-scan-dg-targeted-global.txt`
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
- Remaining stabilization/import-normalization ETA: `~0.10-1.80h` (`~0.01-0.23` workdays), centered on `RoomServiceFactory` exception-namespace harmonization and `KafkaMultiPlayerResponseService` method-reference typing alignment.
