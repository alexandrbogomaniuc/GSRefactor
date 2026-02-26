# Hard-Cut M2 Wave 59 Report (IDelegatedStartGameHelper)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W59-idelegated-start-game-helper`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.IDelegatedStartGameHelper` -> `com.abs.casino.common.games.IDelegatedStartGameHelper`

Wave touched 6 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/IDelegatedStartGameHelper.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/IHelperCreator.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/StartGameHelpers.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/AbstractStartGameHelper.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/NewTranslationGameHelper.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServer.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-143855-hardcut-m2-wave59-idelegated-start-game-helper`

## Key migration result
- Pre-scan legacy refs for wave scope: `2`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `6`

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
- Interface migration with bounded dependent import rewrites in helper pipeline and `GameServer` init path.

## Next wave proposal
- M2 Wave 60: continue with next low-fanout package declaration migration.
