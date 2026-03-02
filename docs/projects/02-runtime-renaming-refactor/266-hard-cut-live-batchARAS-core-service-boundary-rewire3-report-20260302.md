# 266 - Hard-cut live batchAR+AS (core service contract boundary normalization)

## Scope
Continuation stabilization wave after batchAP+AQ, targeting the `web` first-fail service-contract lane:
- `CommonException` throws boundary alignment for room/lobby services.
- `Pair` generic contract alignment between `BigQueryClientService` and `IAnalyticsDBClientService`.

## What was executed
- Batch AR (`2` rewires):
  - `AbstractRoomInfoService` import `CommonException` `com.dgphoenix -> com.abs`.
  - `LobbySessionService` import `CommonException` `com.dgphoenix -> com.abs`.
- Batch AS (`1` rewire):
  - `BigQueryClientService` import `Pair` `com.dgphoenix -> com.abs`.
- Total retained rewires: `3` import/signature-boundary bindings across `3` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -pl games/common-games -am -DskipTests compile`: PASS
- `mvn -pl web -am -DskipTests compile`: FAIL
  - first-fail narrowed to `BGPrivateRoomInfoService` mixed exception namespace boundary:
    - catches `com.dgphoenix...CommonException` while called method now throws `com.abs...CommonException`.
- `mvn -pl games/clashofthegods -am -DskipTests compile`: PASS

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP01` (`mvn -DskipTests install`)
- `fast_gate_batchB`: FAIL `STEP01` (`mvn -DskipTests install`)
- `prewarm`: FAIL `PRE01` (`mvn -DskipTests install`)
- `validation`: FAIL `PRE01` (`mvn -DskipTests install`)
- `STEP09 retry1`: SKIP
- Canonical failure remains environment/infrastructure in this sandbox due Maven external dependency resolution constraints (`repo.maven.apache.org` / DNS), not batch-local compile semantics.

## Measured movement
- Cleared prior `AbstractRoomInfoService`/`IRoomInfoService`, `LobbySessionService`/`ILobbySessionService`, and `BigQueryClientService`/`IAnalyticsDBClientService` mismatch lane from `web` first-fail.
- Post-wave `web` first-fail reduced to a narrower single-file cleanup lane in `BGPrivateRoomInfoService`.
- `common-games` and `clashofthegods` consumer gates are both PASS in this wave.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-164028-hardcut-live-batchARAS-core-service-boundary-rewire3/`
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
- Remaining stabilization/import-normalization ETA: `~0.5-2h` (`~0.06-0.25` workdays), centered on:
  - residual `CommonException` namespace catches/throws normalization in `mp-server/core` service inheritors.
