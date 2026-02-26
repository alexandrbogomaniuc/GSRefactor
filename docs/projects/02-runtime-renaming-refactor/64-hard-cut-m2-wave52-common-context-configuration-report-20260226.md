# Hard-Cut M2 Wave 52 Report (CommonContextConfiguration)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W52-common-context-configuration`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.config.CommonContextConfiguration` -> `com.abs.casino.common.config.CommonContextConfiguration`

Wave touched 5 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/CommonContextConfiguration.java`
- `gs-server/common-wallet/src/test/java/com/dgphoenix/casino/payment/wallet/commonwalletmanger/CommonWalletManagerTest.java`
- `gs-server/support/archiver/src/main/java/com/dgphoenix/casino/support/Archiver.java`
- `gs-server/support/archiver/src/main/java/com/dgphoenix/casino/support/DsoExport.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/config/WebApplicationContextConfiguration.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-135411-hardcut-m2-wave52-common-context-configuration`

## Key migration result
- Pre-scan legacy refs for wave scope: `5`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `5`

## Validation summary
Passing checks:
- `common` install
- `common-wallet` test
- `sb-utils` test
- `promo/persisters` install
- `cassandra-cache/common-persisters` install
- `cassandra-cache/cache` test
- `web-gs` package (reactor-aligned rebuild with explicit `cluster.properties`)
- `mp-server core-interfaces/core/persistance` package
- `refactor-onboard.mjs smoke`

## Risk assessment
- Runtime logic risk: low.
- Spring context helper configuration namespace move with bounded import rewrites in archiver and web context classes.

## Next wave proposal
- M2 Wave 53: continue with next low-fanout declaration migration.
