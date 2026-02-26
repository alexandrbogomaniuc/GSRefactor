# Hard-Cut M2 Wave 54 Report (HostConfiguration)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W54-host-configuration`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.config.HostConfiguration` -> `com.abs.casino.common.config.HostConfiguration`

Wave touched 8 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/HostConfiguration.java`
- `gs-server/common/src/test/java/com/dgphoenix/casino/common/config/HostConfigurationTest.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/cache/data/game/BaseGameInfo.java`
- `gs-server/promo/core/src/main/java/com/dgphoenix/casino/promo/PromoCampaignManager.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/SharedGameServerComponentsConfiguration.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServerComponentsConfiguration.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/game/BaseStartGameAction.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/BaseStartGameAction.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-140514-hardcut-m2-wave54-host-configuration`

## Key migration result
- Pre-scan legacy refs for wave scope: `7`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `8`

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

Validation notes:
- `promo/core` was installed before matrix rerun so `PromoCampaignManager` constructor signature aligns with migrated `HostConfiguration` package.
- MP module selector is `persistance` (repo naming), used explicitly in step 8.

## Risk assessment
- Runtime logic risk: low.
- This wave is constructor/type wiring and import alignment for one config class across GS and promo wiring paths.

## Next wave proposal
- M2 Wave 55: continue with next low-fanout package declaration migration.
