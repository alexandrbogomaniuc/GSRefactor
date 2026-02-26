# Hard-Cut M2 Wave 67 Report (InvalidHashException)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W67-invalid-hash-exception`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.InvalidHashException` -> `com.abs.casino.actions.api.InvalidHashException`

Wave touched 1 file:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/InvalidHashException.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-152303-hardcut-m2-wave67-invalid-hash-exception`

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
- Runtime logic risk: very low.
- This class currently has no external call sites in the repository; migration is isolated.

## Next wave proposal
- M2 Wave 68: continue with next low-fanout web-gs API/DTO declaration in `com.dgphoenix.casino.entities.game.requests`.
