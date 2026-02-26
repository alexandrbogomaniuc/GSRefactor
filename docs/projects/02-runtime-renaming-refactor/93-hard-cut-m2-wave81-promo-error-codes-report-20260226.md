# Hard-Cut M2 Wave 81 Report (Promo ErrorCodes)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W81-promo-error-codes`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.promo.ErrorCodes` -> `com.abs.casino.actions.api.promo.ErrorCodes`

Runtime/config updates:
- Added explicit import `com.abs.casino.actions.api.promo.ErrorCodes` in `GetTournamentPlayerInfoAction` to keep runtime behavior stable.

Wave touched 2 files:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/promo/ErrorCodes.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/promo/GetTournamentPlayerInfoAction.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-170055-hardcut-m2-wave81-promo-error-codes`

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
- Promo API response code mapping remains unchanged; only package namespace moved.

## Next wave proposal
- M2 Wave 82: migrate next low-fanout promo/api class with bounded references.
