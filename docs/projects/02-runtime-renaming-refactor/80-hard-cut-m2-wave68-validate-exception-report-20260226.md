# Hard-Cut M2 Wave 68 Report (ValidateException)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W68-validate-exception`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.actions.api.ValidateException` -> `com.abs.casino.actions.api.ValidateException`

Wave touched 1 file:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/ValidateException.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-153108-hardcut-m2-wave68-validate-exception`

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
- Class is currently not referenced by call sites in repository-wide symbol search.

## Next wave proposal
- M2 Wave 69: continue with next low-fanout web-gs API declaration in `com.dgphoenix.casino.actions.api`.
