# Hard-Cut M2 Wave 57 Report (ExternalGameProvider)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W57-external-game-provider`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.ExternalGameProvider` -> `com.abs.casino.common.games.ExternalGameProvider`

Wave touched 1 file:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/ExternalGameProvider.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-142543-hardcut-m2-wave57-external-game-provider`

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
- Single enum namespace migration; no direct import dependents found.

## Next wave proposal
- M2 Wave 58: continue with next low-fanout package declaration migration.
