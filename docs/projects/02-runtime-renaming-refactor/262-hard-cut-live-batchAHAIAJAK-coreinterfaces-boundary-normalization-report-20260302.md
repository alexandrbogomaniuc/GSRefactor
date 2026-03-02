# 262 - Hard-cut live batchAH+AI+AJ+AK (core-interfaces boundary normalization)

## Scope
Continuation stabilization wave after batchAF+AG, targeting interface-boundary namespace mismatches in `mp-server/core-interfaces` plus one local testmodel import-collision fix in `common-games`.

## What was executed
- Batch AH (`2` files): `CommonException` import normalization in `IGameState`, `ITimedEvent`.
- Batch AI (`2` files): `CommonException` import normalization in `IRoom`, `ISeat`.
- Batch AJ (`3` files): `Pair` import normalization in `IPlayerRoundInfo`, `IShootResult`, `IAnalyticsDBClientService`.
- Batch AK (`5` files): `CommonException` import normalization in `ISocketService`, `IGame`, `IActionGameSeat`, `IRoomInfoService`, `ILobbySessionService`.
- Additional bounded fix (`1` file): removed conflicting `com.abs...ICurrency` import from `StubCurrency` to resolve dual-import collision against `com.betsoft.casino.mp.model.ICurrency`.
- Net retained normalization impacts: `49` across `13` files (import/signature-boundary only, no behavior logic changes).

## Validation status
### Targeted mp fast gates
- `mvn -pl games/common-games -am -DskipTests compile`: PASS
- `mvn -pl web -am -DskipTests compile`: FAIL (first-fail moved to `bg_maxblastchampions` legacy imports: `RNG` and `DateTimeUtils` in `com.dgphoenix` namespaces)
- `mvn -pl games/clashofthegods -am -DskipTests compile`: FAIL (first-fail at `clashofthegods-math` `Pair`/`Triple` mixed-namespace type-boundary mismatches)

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`

## Measured movement
- `core-interfaces` legacy `CommonException` imports reduced:
  - before wave: `16`
  - after wave: `7`
  - delta: `-9`
- `core-interfaces` legacy `Pair` imports reduced:
  - before wave: `6`
  - after wave: `3`
  - delta: `-3`
- `common-games` first-fail module status:
  - before wave: compile FAIL
  - after wave: compile PASS

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260302-153306-hardcut-live-batchAHAIAJAK-coreinterfaces-boundary-normalization/`
  - `diff-integrated.patch`
  - `changed-files.txt`
  - `rewire-count.txt`
  - `post-scan-coreinterfaces-dg-commonexception-imports.txt`
  - `post-scan-coreinterfaces-dg-pair-imports.txt`
  - `post-scan-coreinterfaces-abs-commonexception-imports.txt`
  - `post-scan-coreinterfaces-abs-pair-imports.txt`
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
- Remaining stabilization/import-normalization ETA: `~3-6h` (`~0.38-0.75` workdays), centered on `bg_maxblastchampions` legacy utility imports and `clashofthegods-math` Pair/Triple boundary normalization.
