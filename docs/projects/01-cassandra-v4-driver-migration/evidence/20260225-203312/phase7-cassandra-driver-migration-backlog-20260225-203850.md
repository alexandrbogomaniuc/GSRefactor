# Cassandra Driver Migration Backlog

timestamp_utc: 2026-02-25T20:38:50Z
repo_root: /Users/alexb/Documents/Dev/Dev_new

## Summary
- Java files importing driver3 API: **163**
- GS files: **128**
- MP files: **35**

## Module Hotspots (by path group)
- 69 files: `gs-server/cassandra-cache/common-persisters`
- 35 files: `mp-server/persistance`
- 27 files: `gs-server/cassandra-cache/cache`
- 17 files: `gs-server/promo/persisters`
- 15 files: `gs-server/game-server`

## Top Files By Driver3 Import Lines
- 8 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraTrackingInfoPersister.java`
- 8 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraFrBonusArchivePersister.java`
- 8 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraCurrentPlayerSessionStatePersister.java`
- 8 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraBonusArchivePersister.java`
- 8 lines: `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/persist/engine/AbstractCassandraPersister.java`
- 7 lines: `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/WeaponsPersister.java`
- 7 lines: `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/PlayerQuestsPersister.java`
- 7 lines: `gs-server/promo/persisters/src/main/java/com/dgphoenix/casino/promo/persisters/CassandraUnsendedPromoWinInfoPersister.java`
- 7 lines: `gs-server/promo/persisters/src/main/java/com/dgphoenix/casino/promo/persisters/CassandraPromoCampaignStatisticsPersister.java`
- 7 lines: `gs-server/promo/persisters/src/main/java/com/dgphoenix/casino/promo/persisters/CassandraPromoCampaignPersister.java`
- 7 lines: `gs-server/promo/persisters/src/main/java/com/dgphoenix/casino/promo/persisters/CassandraBattlegroundConfigPersister.java`
- 7 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraPlayerGameSettingsPersister.java`
- 7 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraHostCdnPersister.java`
- 7 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraCountryRestrictionPersister.java`
- 7 lines: `gs-server/cassandra-cache/cache/src/test/java/com/dgphoenix/casino/cassandra/persist/engine/TableDefinitionTest.java`
- 6 lines: `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/SequencerPersister.java`
- 6 lines: `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/RoundResultNotificationPersister.java`
- 6 lines: `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/GameRoomSnapshotPersister.java`
- 6 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraWalletOperationInfoPersister.java`
- 6 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraSequencerPersister.java`
- 6 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraRoundGameSessionPersister.java`
- 6 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraPendingDataArchivePersister.java`
- 6 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraNotificationPersister.java`
- 6 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraMassAwardPersister.java`
- 6 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraIntSequencerPersister.java`
- 6 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraHistoryTokenPersister.java`
- 6 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraHistoryInformerItemPersister.java`
- 6 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraBatchOperationStatusPersister.java`
- 6 lines: `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/AbstractDistributedConfigEntryPersister.java`
- 6 lines: `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/persist/engine/TableDefinition.java`
- 6 lines: `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/SchemaCreator.java`
- 6 lines: `gs-server/cassandra-cache/cache/src/main/java/com/dgphoenix/casino/cassandra/AbstractLockManager.java`
- 5 lines: `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/TournamentSessionPersister.java`
- 5 lines: `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/SocketClientInfoPersister.java`
- 5 lines: `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/SocketClientCountPersister.java`
- 5 lines: `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/ServerConfigPersister.java`
- 5 lines: `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/RoomTemplatePersister.java`
- 5 lines: `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/RoomPlayerInfoPersister.java`
- 5 lines: `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/ReservedNicknamePersister.java`
- 5 lines: `mp-server/persistance/src/main/java/com/betsoft/casino/mp/data/persister/PlayerStatsPersister.java`

## Driver3 Type Usage (import-based)
- 243: `querybuilder`
- 112: `DataType`
- 90: `Row`
- 72: `ResultSet`
- 30: `schemabuilder`
- 21: `Session`
- 20: `Statement`
- 11: `ConsistencyLevel`
- 5: `exceptions`
- 5: `TableMetadata`
- 5: `Host`
- 3: `ColumnDefinitions`
- 2: `Metrics`
- 1: `TypeCodec`
- 1: `TimestampGenerator`
- 1: `KeyspaceMetadata`
- 1: `ExecutionInfo`
- 1: `CodecRegistry`

## Recommended Migration Order (Wave 2+)
- 1) `gs-server/cassandra-cache/cache`
- 2) `gs-server/cassandra-cache/common-persisters`
- 3) `mp-server/persistance`
- 4) `gs-server/promo/persisters`
- 5) remaining low-count modules

## API Mapping Starters (driver3 -> driver4)
- `Session` -> `CqlSession`
- `ResultSet` -> `com.datastax.oss.driver.api.core.cql.ResultSet`
- `Row` -> `com.datastax.oss.driver.api.core.cql.Row`
- `Statement` -> `SimpleStatement` / `BoundStatement`
- `ConsistencyLevel` -> `DefaultConsistencyLevel`
- `querybuilder` -> `com.datastax.oss.driver.api.querybuilder`
- `DataType` -> `com.datastax.oss.driver.api.core.type.DataTypes`
