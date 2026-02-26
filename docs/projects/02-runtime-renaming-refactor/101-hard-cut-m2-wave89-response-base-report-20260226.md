# Hard-Cut M2 Wave 89 Report (Response base class)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W89-response-base`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.response.Response` -> `com.abs.casino.actions.api.response.Response`

Runtime/config updates:
- Updated dependent imports to `com.abs...Response` in:
  - `APIResponseBuilder`
  - `ErrorResponse`
  - `SuccessResponse`

Wave touched 4 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/Response.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/APIResponseBuilder.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/ErrorResponse.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/SuccessResponse.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-175902-hardcut-m2-wave89-response-base`

## Key migration result
- Pre-scan legacy refs for wave scope: `3`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `4`

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
- This wave removes cross-package inheritance drift and keeps serializer response hierarchy consistent.

## Next wave proposal
- M2 Wave 90: migrate `APIResponseBuilder` package declaration with bounded call-site imports.
