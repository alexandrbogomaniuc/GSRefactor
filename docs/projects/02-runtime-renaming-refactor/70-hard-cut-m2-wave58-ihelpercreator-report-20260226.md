# Hard-Cut M2 Wave 58 Report (IHelperCreator)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W58-ihelpercreator`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.IHelperCreator` -> `com.abs.casino.common.games.IHelperCreator`

Wave touched 3 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/IHelperCreator.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/StartGameHelpers.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServer.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-143128-hardcut-m2-wave58-ihelpercreator`

## Key migration result
- Pre-scan legacy refs for wave scope: `1`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `3`

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
- Runtime logic risk: low to moderate.
- Interface namespace migration plus bounded import rewrites in `StartGameHelpers` and `GameServer`.

## Next wave proposal
- M2 Wave 59: continue with next low-fanout package declaration migration.
