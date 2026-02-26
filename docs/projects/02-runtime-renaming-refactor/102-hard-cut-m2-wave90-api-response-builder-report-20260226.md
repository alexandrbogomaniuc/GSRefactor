# Hard-Cut M2 Wave 90 Report (APIResponseBuilder)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W90-api-response-builder`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.response.APIResponseBuilder` -> `com.abs.casino.actions.api.response.APIResponseBuilder`

Wave touched 1 file:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/response/APIResponseBuilder.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-180231-hardcut-m2-wave90-api-response-builder`

## Key migration result
- Pre-scan legacy refs for wave scope: `1`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `1`

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
- API response package family is now consistently migrated to `com.abs`.

## Next wave proposal
- M2 Wave 91: migrate next low-fanout declaration outside API response package family.
