# Evidence - Hard-Cut M2 Wave 35 (ServerOnlineStatus)

Date (UTC): 2026-02-26
Wave: `W35-server-online-status`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.cache.data.server.ServerOnlineStatus` -> `com.abs.casino.common.cache.data.server.ServerOnlineStatus`

Scope adjustments:
- Updated dependent imports in `LoadBalancerCache`, `NotifyOnServerStatusesUpdatedRequest`, `WatchServersThreadSlave`, and `WatchServersThreadMaster`.

## Validation compatibility corrections (outside target namespace)
Applied minimal type-alignment fixes required to keep `web-gs` packaging green with current hard-cut state:
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/BaseStartGameAction.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/game/BaseStartGameAction.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/transactiondata/BasicTransactionDataStorageHelper.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/bonus/mass/MassAwardBonusManager.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/bonus/restriction/NoAwardRestriction.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/bonus/restriction/PlayerBalanceRestriction.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/dblink/DBLink.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/bonus/FRBonusManager.java`

## Scan result
- legacy refs after wave: 0
- abs refs after wave: 5

## Validation result
- success commands: 9
- failed commands: 0
- detailed logs: `validation-status.txt` and `*.log`
