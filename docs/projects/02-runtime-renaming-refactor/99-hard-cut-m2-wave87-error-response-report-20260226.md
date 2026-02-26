# Hard-Cut M2 Wave 87 Report (ErrorResponse)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W87-error-response`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.response.ErrorResponse` -> `com.abs.casino.actions.api.response.ErrorResponse`

Runtime/config updates:
- `APIResponseBuilder` now imports `com.abs...ErrorResponse` for cross-package compatibility.

Wave touched 2 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/ErrorResponse.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/APIResponseBuilder.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-173552-hardcut-m2-wave87-error-response`

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
- API response object creation path is unchanged; only package namespace and import bridge were updated.

## Next wave proposal
- M2 Wave 88: migrate next low-fanout class in `actions/api/response` package.
