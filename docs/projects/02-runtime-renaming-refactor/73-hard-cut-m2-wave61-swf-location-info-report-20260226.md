# Hard-Cut M2 Wave 61 Report (SwfLocationInfo)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W61-swf-location-info`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.SwfLocationInfo` -> `com.abs.casino.common.games.SwfLocationInfo`

Wave touched 4 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/SwfLocationInfo.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/IStartGameHelper.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/NewTranslationGameHelper.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/filters/StartGameServletFilter.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-145155-hardcut-m2-wave61-swf-location-info`

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
- Runtime logic risk: low.
- Package migration isolated to start-game SWF location model and direct import sites.

## Next wave proposal
- M2 Wave 62: migrate next low-fanout declaration in `com.dgphoenix.casino.common.games` (`CdnCheckResult`).
