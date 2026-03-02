# 265 - Hard-cut live batchAP+AQ (core/web RNG + GameTools boundary normalization)

## Scope
Continuation stabilization wave after batchAN+AO, targeting residual `mp-server/core`/`mp-server/web` legacy `RNG` import lanes and the `GameTools` Pair/Triple boundary drift impacting `clashofthegods`.

## What was executed
- Batch AP (`11` rewires): import normalization from `com.dgphoenix` to `com.abs` for `RNG` call lanes in:
  - `mp-server/core` service/model files and
  - `mp-server/web` bot/kafka/service files.
- Batch AQ (`7` rewires): boundary normalization for:
  - `core-interfaces` `GameTools` Pair/Triple imports,
  - `clashofthegods` test imports (`RNG`, `Pair`, `Triple`).
- Total retained rewires: `18` import/signature-boundary bindings across `16` files.
- No behavior logic changes.

## Validation status
### Targeted mp fast gates
- `mvn -pl games/common-games -am -DskipTests compile`: PASS
- `mvn -pl web -am -DskipTests compile`: FAIL (first-fail moved to `mp-server/core` interface-boundary mismatch lane:
  - `AbstractRoomInfoService` / `IRoomInfoService` `remove(long)` throws mismatch (`CommonException` namespace drift),
  - `LobbySessionService` / `ILobbySessionService` throws mismatch,
  - `BigQueryClientService` / `IAnalyticsDBClientService` `Pair` generic boundary mismatch and erasure/override clash)
- `mvn -pl games/clashofthegods -am -DskipTests compile`: PASS

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP01` (`mvn -DskipTests install`)
- `fast_gate_batchB`: FAIL `STEP01` (`mvn -DskipTests install`)
- `prewarm`: FAIL `PRE01` (`mvn -DskipTests install`)
- `validation`: FAIL `PRE01` (`mvn -DskipTests install`)
- `STEP09 retry1`: SKIP
- Canonical failure remains environment/infrastructure in this sandbox due Maven external dependency resolution constraints (`repo.maven.apache.org` / DNS), not batch-local compile semantics.

## Measured movement
- AP/AQ targeted legacy import lanes are fully normalized in touched ownership paths:
  - `mp-server/core` + `mp-server/web` targeted `RNG` import rewires applied (`11`),
  - `GameTools` + cotg test boundary imports rewired (`7`).
- `clashofthegods` targeted consumer gate recovered to PASS in this wave.
- `web` first-fail moved off residual `RNG` import lane to narrower interface/throws/generic boundary mismatches centered on service contracts.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-162733-hardcut-live-batchAPAQ-core-web-gametools-import-rewire18/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-coreweb-dg-rng-imports.txt`
  - `post-scan-coreweb-abs-rng-imports.txt`
  - `post-scan-gametools-tests-dg-imports.txt`
  - `post-scan-gametools-tests-abs-imports.txt`
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
- Remaining stabilization/import-normalization ETA: `~1-3h` (`~0.13-0.38` workdays), centered on:
  - service interface throws alignment (`IRoomInfoService`, `ILobbySessionService` lanes),
  - `IAnalyticsDBClientService` vs `BigQueryClientService` `Pair` generic boundary normalization.
