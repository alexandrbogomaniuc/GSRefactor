# 285 - Hard-cut live batchCD+CE (sit-out exception boundary harmonization)

## Scope
Continuation stabilization wave after batchCA+CB, targeting the localized sit-out exception boundary lane.
- CD: `SitOutHandler` compile-head exception boundary fix.
- CE: sit-out wrapper hardening in room/kafka task paths.

## What was executed
- Batch CD (`1` rewire):
  - in `SitOutHandler`, changed inner catch around `room.processSitOut(...)` to explicit `catch (com.abs.casino.common.exception.CommonException e)` while retaining outer legacy `com.dgphoenix...CommonException` catch for `getRoomWithCheck(...)` boundary.
- Batch CE (`4` rewires):
  - added explicit first `catch (com.abs.casino.common.exception.CommonException e)` before generic `catch (Exception e)` in sit-out wrapper paths:
    - `RoomServiceFactory#sitOutFromMultiNodePrivateRoom`
    - `SitOutTask#run`
    - `KafkaMultiPlayerResponseService#sitOutFromMultiNodePrivateRoom`
    - `KafkaMultiPlayerResponseService#sitOutTask`
- Total retained rewires: `5` across `4` files.
- No control-flow changes and no global replacement.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -f mp-server/pom.xml -pl bots -am -DskipTests compile`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: FAIL
  - first-fail moved off `SitOutHandler` and is now localized in `EnterLobbyHandler`:
    - `EnterLobbyHandler.java:[1976,81]` unreported `com.abs...CommonException`
    - `EnterLobbyHandler.java:[1985,62]` unreported `com.abs...CommonException`
- `mvn -DskipTests install` in `mp-server/games/clashofthegods`: FAIL in known test-compile lane.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains the known smoke-stage external/runtime lane, not local CD/CE rewires.

## Measured movement
- Cleared the `SitOutHandler` compile-head lane.
- Advanced web fail frontier to localized `EnterLobbyHandler` exception boundary points.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260303-022647-hardcut-live-batchCDCE-sitout-exception-boundary-rewire5/`
  - `pre-commit-git-status.txt`
  - `changed-files.txt`
  - `diff-integrated.patch`
  - `rewire-count.txt`
  - `post-scan-targeted-sitout-boundaries.txt`
  - `fast-gate-common-games.log`
  - `fast-gate-bots.log`
  - `fast-gate-bots-first-fail.txt`
  - `fast-gate-web.log`
  - `fast-gate-web-first-fail.txt`
  - `fast-gate-cotg-consumer.log`
  - `fast-gate-cotg-first-fail.txt`
  - `fast-gate-status.txt`
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
- Remaining stabilization/import-normalization ETA: `~0.01-0.60h` (`~0.00-0.08` workdays), centered on localized `EnterLobbyHandler` exception-boundary harmonization.
