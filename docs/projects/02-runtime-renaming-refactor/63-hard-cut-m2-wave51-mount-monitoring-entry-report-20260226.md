# Hard-Cut M2 Wave 51 Report (MountMonitoringEntry)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W51-mount-monitoring-entry`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.config.MountMonitoringEntry` -> `com.abs.casino.common.config.MountMonitoringEntry`

Wave touched 4 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/MountMonitoringEntry.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/GameServerConfigTemplate.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/system/configuration/GameServerConfiguration.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/system/diagnosis/SystemDiagnosisServlet.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-134842-hardcut-m2-wave51-mount-monitoring-entry`

## Key migration result
- Pre-scan legacy refs for wave scope: `3`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `4`

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
- Config entry model namespace move with explicit import rewrites in common-gs and web-gs.

## Next wave proposal
- M2 Wave 52: continue with next low-fanout declaration migration.
