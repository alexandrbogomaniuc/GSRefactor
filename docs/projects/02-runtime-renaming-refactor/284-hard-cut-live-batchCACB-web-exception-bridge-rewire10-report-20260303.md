# 284 - Hard-cut live batchCA+CB (web exception bridge and buy-in catch tightening)

## Scope
Continuation stabilization wave after batchBZ, targeting the localized web exception-boundary lane and adjacent buy-in catch-path consistency:
- CA: `SitInHandler` + `RoomServiceFactory` + `AbstractStartGameUrlHandler` exception boundary bridge.
- CB: buy-in exception handling tightening across game/lobby handlers.

## What was executed
- Batch CA (`4` targeted rewires):
  - wrapped `SitInHandler` battleground buy-in call with explicit `BuyInFailedException` -> `com.abs...CommonException` bridge,
  - added `RoomServiceFactory#getRoomAbs(...)` compatibility bridge to translate legacy `com.dgphoenix...CommonException` to `com.abs...CommonException`,
  - rewired `SitInHandler` and `AbstractStartGameUrlHandler` room lookup/start calls to `getRoomAbs(...)`.
- Batch CB (`6` targeted rewires):
  - converted broad `catch (Exception)` + `instanceof BuyInFailedException` blocks to explicit `catch (BuyInFailedException)` first, then generic fallback,
  - preserved existing error-code mapping and fallback behavior in `BuyInHandler`, `ReBuyHandler`, `LobbyReBuyHandler`, `AbstractCrashBetHandler`, and `PurchaseWeaponLootBoxHandler`.
- Total retained rewires: `10` across `8` files.
- No blind global replace and no business-flow rewrites.

## Validation status
### Targeted mp fast gates
- `mvn -DskipTests install` in `mp-server/games/common-games`: PASS
- `mvn -f mp-server/pom.xml -pl bots -am -DskipTests compile`: PASS
- `mvn -DskipTests -pl web -am compile` in `mp-server`: FAIL
  - first-fail moved off `SitInHandler` to localized `SitOutHandler` exception boundary points:
    - `SitOutHandler.java:[92,23]` legacy catch no longer thrown,
    - `SitOutHandler.java:[57,43]` unreported `com.abs...CommonException`.
- `mvn -DskipTests install` in `mp-server/games/clashofthegods`: FAIL in known test-compile lane.

### Full canonical matrix (pre-push)
- `fast_gate_batchA`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `fast_gate_batchB`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `prewarm`: PASS
- `validation`: FAIL `STEP09` (`node gs-server/deploy/scripts/refactor-onboard.mjs smoke`)
- `STEP09 retry1`: FAIL `rc=2`
- Canonical profile remains the known smoke-stage external/runtime lane, not local CA/CB rewires.

## Measured movement
- Cleared remaining `SitInHandler` compile fail-head from batchBZ.
- Advanced web fail frontier into the next localized file (`SitOutHandler`) for the next bounded pass.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260303-014918-hardcut-live-batchCACB-web-exception-bridge/`
  - `pre-commit-git-status.txt`
  - `changed-files.txt`
  - `diff-integrated.patch`
  - `rewire-count.txt`
  - `post-scan-targeted-exception-boundaries.txt`
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
- Remaining stabilization/import-normalization ETA: `~0.02-0.75h` (`~0.00-0.09` workdays), centered on localized `SitOutHandler` exception-boundary harmonization.
