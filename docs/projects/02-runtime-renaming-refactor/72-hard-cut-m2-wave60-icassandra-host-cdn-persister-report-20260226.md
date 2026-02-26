# Hard-Cut M2 Wave 60 Report (ICassandraHostCdnPersister)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W60-icassandra-host-cdn-persister`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.ICassandraHostCdnPersister` -> `com.abs.casino.common.games.ICassandraHostCdnPersister`

Wave touched 4 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/ICassandraHostCdnPersister.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/NewTranslationGameHelper.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/AbstractStartGameHelper.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraHostCdnPersister.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-144503-hardcut-m2-wave60-icassandra-host-cdn-persister`

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
- Interface package migration with bounded import rewrites in helper and persister path.

## Next wave proposal
- M2 Wave 61: migrate next low-fanout declaration in `com.dgphoenix.casino.common.games` (`SwfLocationInfo` or `CdnCheckResult`).
