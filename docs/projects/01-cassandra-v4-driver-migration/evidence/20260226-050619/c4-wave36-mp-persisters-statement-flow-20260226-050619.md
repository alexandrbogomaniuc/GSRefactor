# CASS-V4 Wave 36 (MP persisters statement-flow cleanup)

## Scope
Refactored MP persistence hotspots to remove typed driver3 querybuilder declarations (`Select`, `Insert`, `Update`, `Delete`, `Truncate`) and convert to direct execute-chain flow.

### Changed files
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/WeaponsPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/PlayerQuestsPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/RoundResultNotificationPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/SequencerPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/GameRoomSnapshotPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/SocketClientInfoPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/SocketClientCountPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/TournamentSessionPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/ServerConfigPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/RoomTemplatePersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/ReservedNicknamePersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/CrashGameStatePersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/PendingOperationPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/BotConfigInfoPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/FriendsPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/MultiNodeSeatPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/RoomPlayerInfoPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/BGPrivateRoomPlayersStatusPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/OnlinePlayerPersister.java`
- `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/BotServiceConfigPersister.java`

## Validation
All required checks passed:
- `mvn -DskipTests install` in `gs-server/promo/persisters`
- `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
- `mvn test` in `gs-server/cassandra-cache/cache`
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
- `mvn -pl core-interfaces,core,persistance -am -DskipTests package` in `mp-server`

## Inventory delta
- GS driver3 import lines: `396 -> 396` (no change)
- MP driver3 import lines: `151 -> 105` (`-46`)
- Combined GS+MP: `547 -> 501` (`-46`)

## Completion snapshot (import burn-down metric)
- GS-only: `18.85%` (`488 -> 396`)
- MP-only: `30.46%` (`151 -> 105`)
- Combined GS+MP: `21.60%` (`639 -> 501`)

## Notes
This wave focused on high-repetition MP persister patterns to accelerate burn-down while preserving runtime behavior.
