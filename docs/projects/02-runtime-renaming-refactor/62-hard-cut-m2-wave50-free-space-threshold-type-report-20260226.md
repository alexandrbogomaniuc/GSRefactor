# Hard-Cut M2 Wave 50 Report (FreeSpaceThresholdType)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W50-free-space-threshold-type`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.config.FreeSpaceThresholdType` -> `com.abs.casino.common.config.FreeSpaceThresholdType`

Wave touched 4 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/FreeSpaceThresholdType.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/MountMonitoringEntry.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/config/GameServerConfigTemplate.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/system/diagnosis/SystemDiagnosisServlet.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-134432-hardcut-m2-wave50-free-space-threshold-type`

## Key migration result
- Pre-scan legacy refs for wave scope: `2`
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
- Enum namespace move with explicit import bridge in common config + diagnosis servlet.

## Next wave proposal
- M2 Wave 51: continue with next low-fanout declaration migration.
