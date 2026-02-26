# CASS-V4 Wave 37 (MP residual querybuilder cleanup)

## Scope
Removed remaining typed querybuilder declarations/imports from the next MP persister hotspot set and normalized to direct execute-chain flow.

### Changed files
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/PlayerStatsPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/SpawnConfigPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/PlayerProfilePersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/MapConfigPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/GameConfigPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/ActiveFrbSessionPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/ActiveCashBonusSessionPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/AbstractRoomInfoPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/PlayerNicknamePersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/WeaponsPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/RoundResultNotificationPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/PlayerQuestsPersister.java`

## Validation
All required checks passed:
- `mvn -DskipTests install` in `gs-server/promo/persisters`
- `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
- `mvn test` in `gs-server/cassandra-cache/cache`
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
- `mvn -pl core-interfaces,core,persistance -am -DskipTests package` in `mp-server`

## Inventory delta
- GS driver3 import lines: `396 -> 396` (no change)
- MP driver3 import lines: `105 -> 84` (`-21`)
- Combined GS+MP: `501 -> 480` (`-21`)

## Completion snapshot (import burn-down metric)
- GS-only: `18.85%` (`488 -> 396`)
- MP-only: `44.37%` (`151 -> 84`)
- Combined GS+MP: `24.88%` (`639 -> 480`)

## Notes
This wave removed the remaining querybuilder import usage from MP persisters, including the last querybuilder import in `PlayerNicknamePersister`.
