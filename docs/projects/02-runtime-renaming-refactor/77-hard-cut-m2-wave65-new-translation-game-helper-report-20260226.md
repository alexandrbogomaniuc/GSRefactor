# Hard-Cut M2 Wave 65 Report (NewTranslationGameHelper)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W65-new-translation-game-helper`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.NewTranslationGameHelper` -> `com.abs.casino.common.games.NewTranslationGameHelper`

Wave touched 2 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/NewTranslationGameHelper.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServer.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-151308-hardcut-m2-wave65-new-translation-game-helper`

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
- Migration is bounded to helper implementation namespace and one GS constructor-path import.

## Next wave proposal
- M2 Wave 66: migrate `AbstractStartGameHelper` to continue closing remaining `common.games` namespace declarations.
