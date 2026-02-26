# Hard-Cut M2 Wave 88 Report (SuccessResponse)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W88-success-response`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.response.SuccessResponse` -> `com.abs.casino.actions.api.response.SuccessResponse`

Runtime/config updates:
- `APIResponseBuilder` now imports `com.abs...SuccessResponse` for cross-package compatibility.
- `SuccessResponse()` constructor visibility changed `protected` -> `public` so legacy-package `APIResponseBuilder` can instantiate it after package split.

Wave touched 2 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/SuccessResponse.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/APIResponseBuilder.java`

## Evidence
Primary passing evidence:
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-175510-hardcut-m2-wave88-success-response-rerun`

Initial failed attempt (captured and resolved):
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-175344-hardcut-m2-wave88-success-response`
- failure was compile error in `web-gs`: constructor access level after package migration.

## Key migration result
- Pre-scan legacy refs for wave scope: `1`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `2`

## Validation summary
Passing checks:
- `common` install
- `common-wallet` test
- `sb-utils` test
- `promo/persisters` install
- `cassandra-cache/common-persisters` install
- `cassandra-cache/cache` test
- `web-gs` package (with explicit `-Dcluster.properties=common.properties`)
- `mp-server core-interfaces/core/persistance` package
- `refactor-onboard.mjs smoke`

## Risk assessment
- Runtime logic risk: low.
- Access-level fix is narrow and required for compatibility while `APIResponseBuilder` remains in legacy package.

## Next wave proposal
- M2 Wave 89: migrate `Response` or `APIResponseBuilder` package with bounded dependent updates.
