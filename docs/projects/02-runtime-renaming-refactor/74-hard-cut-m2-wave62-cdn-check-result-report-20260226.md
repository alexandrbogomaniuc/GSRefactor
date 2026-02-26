# Hard-Cut M2 Wave 62 Report (CdnCheckResult)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W62-cdn-check-result`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.CdnCheckResult` -> `com.abs.casino.common.games.CdnCheckResult`

Wave touched 5 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/CdnCheckResult.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/ICassandraHostCdnPersister.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/NewTranslationGameHelper.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraHostCdnPersister.java`
- `gs-server/game-server/web-gs/src/main/webapp/cdn/info.jsp`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-145636-hardcut-m2-wave62-cdn-check-result`

## Key migration result
- Pre-scan legacy refs for wave scope: `3`
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
- Includes JSP + shared-model import rewires in CDN-check path; behavior validated by full matrix and smoke.

## Next wave proposal
- M2 Wave 63: migrate next low-fanout declaration in `com.dgphoenix.casino.common.games` (`IStartGameHelper` or `StartGameHelpers`).
