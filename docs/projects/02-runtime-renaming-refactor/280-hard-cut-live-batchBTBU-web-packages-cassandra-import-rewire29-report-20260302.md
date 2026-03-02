# 280 - Hard-cut live batchBT+BU (web fail-head package/cassandra import normalization)

## Scope
Continuation stabilization wave after batchBR+BS, targeting the narrowed web fail-head:
- `com.dgphoenix.casino.common.mp.*`
- `com.dgphoenix.casino.kafka.dto.*`
- `com.dgphoenix.casino.cassandra.IRemoteUnlocker`
- adjacent uniform `CassandraPersistenceManager` import drift in web handlers/services.

## What was executed
- Batch BT (`10` rewires): direct web fail-head import normalization in `SocketService`, `EnterLobbyHandler`, `RemoteUnlocker`, `WebSocketRouter`.
- Batch BU (`19` rewires): non-overlap cassandra import normalization (`CassandraPersistenceManager`) across web config/controllers/handlers/services/socket classes.
- Total retained rewires: `29` import-only bindings across `23` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -f mp-server/pom.xml -pl bots -am -DskipTests compile`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: FAIL
  - first-fail moved off unresolved package-not-found imports and narrowed into deeper type/signature boundary lane:
    - abstract override signature mismatches (`GetStartGameUrlHandler` / battleground start handlers),
    - `RemoteUnlocker` interface generic type mismatch (`ILockManager` namespace),
    - `CassandraPersistenceManager` cross-namespace type incompatibilities in `WebSocketRouter`/`EnterLobbyHandler` call sites,
    - `KafkaMultiPlayerResponseService` method reference mismatch at line 721.
- `mvn -DskipTests install` in `mp-server/games/clashofthegods`: FAIL in known test-compile lane.
- Harmonized cotg compile gate (`mvn -DskipTests -pl games/clashofthegods -am compile`): PASS.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains the known smoke-stage external/runtime lane, not local BT/BU import-only rewires.

## Measured movement
- Cleared the prior web unresolved package frontier (`common.mp` / `kafka.dto` / `IRemoteUnlocker` missing imports).
- Surfaced and narrowed downstream web type-compatibility/signature lane for next bounded boundary harmonization wave.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-222311-hardcut-live-batchBTBU-web-packages-cassandra-import-rewire29/`
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
- Remaining stabilization/import-normalization ETA: `~0.20-2.50h` (`~0.03-0.31` workdays), centered on web boundary harmonization (type/signature compatibility in start-game handlers, `RemoteUnlocker`, and cassandra manager call sites).
