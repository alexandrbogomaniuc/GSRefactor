# CASS-V4 Wave 10 - Common Persisters Querybuilder Type Decoupling

## Timestamp
- 2026-02-25 21:18 UTC

## Scope
- Continue incremental Cassandra v4 migration by removing direct typed querybuilder coupling in `common-persisters` hotspots.
- Keep runtime behavior unchanged while reducing direct driver3 API surface.

## Code Changes
- Refactored query construction flow from typed querybuilder classes (`Insert`, `Select`, `Update`) to generic `Statement` usage in:
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraBonusArchivePersister.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraFrBonusArchivePersister.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraCurrentPlayerSessionStatePersister.java`
- Fixed compile blocker in:
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraTransactionDataPersister.java`
- Compile blocker details:
  - Removed dependency on missing `KeyspaceConfiguration.PROTOCOL_VERSION`.
  - Added local serialization constant:
    - `SERIALIZE_PROTOCOL_VERSION = ProtocolVersion.NEWEST_SUPPORTED`
  - Switched serialization call sites to the local constant.

## Validation
- PASS: `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
  - Evidence: `c4-wave10-build-common-persisters-20260225-203312.txt`
- PASS: `mvn -q -Dtest=KeySpaceManagerTest,CassandraPersistenceManagerTest,ClusterConfigDeserializationTest,KeyspaceConfigurationFactoryTest test` in `gs-server/cassandra-cache/cache`
  - Evidence: `c4-wave10-unit-tests-20260225-203312.txt`
- PASS: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
  - Evidence: `c4-wave10-build-web-gs-20260225-203312.txt`
- PASS: `mvn -DskipTests -pl core-interfaces,core,persistance -am package` in `mp-server`
  - Evidence: `c4-wave10-build-mp-stack-20260225-203312.txt`

## Result
- Wave 10 is complete and validated with cross-module build/test checks.
- `common-persisters` query hotspots are further decoupled from typed driver3 querybuilder imports.

## Next Target
- Continue the next common-persisters hotspot wave and refresh driver inventory delta to quantify reduction.
