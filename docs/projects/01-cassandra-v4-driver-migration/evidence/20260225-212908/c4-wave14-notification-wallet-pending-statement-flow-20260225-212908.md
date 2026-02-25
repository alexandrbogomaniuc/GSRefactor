# CASS-V4 Wave 14 - Notification/Wallet/Pending Statement Flow

## Timestamp
- 2026-02-25 21:30 UTC

## Scope
- Continue staged query-typing decoupling in additional `common-persisters` runtime classes.

## Code Changes
- Converted typed querybuilder variables to generic `Statement` flow in:
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraNotificationPersister.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraPendingDataArchivePersister.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraWalletOperationInfoPersister.java`

## Build Fix During Wave
- First compile run failed after removing imports because `CassandraWalletOperationInfoPersister` still uses `QueryBuilder.delete()/in(...)` in `delete(long... walletOperationIds)`.
- Fixed by restoring `QueryBuilder` import while keeping `Insert`/`Select` typed variables removed.

## Validation
- PASS: `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
  - Evidence: `c4-wave14-build-common-persisters-20260225-212908.txt`
- PASS: `mvn -q -Dtest=KeySpaceManagerTest,CassandraPersistenceManagerTest,ClusterConfigDeserializationTest,KeyspaceConfigurationFactoryTest test` in `gs-server/cassandra-cache/cache`
  - Evidence: `c4-wave14-unit-tests-20260225-212908.txt`
- PASS: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
  - Evidence: `c4-wave14-build-web-gs-20260225-212908.txt`
- PASS: `mvn -DskipTests -pl core-interfaces,core,persistance -am package` in `mp-server`
  - Evidence: `c4-wave14-build-mp-stack-20260225-212908.txt`

## Result
- Wave 14 completed with successful builds/tests after a single compile-fix iteration.
- More persistence paths now use driver-neutral statement typing without behavior change.

## Next Target
- Continue on remaining high-density typed-query hotspots (`Sequencer`, `PaymentTransaction`, `HistoryInformer`) and then run the next inventory checkpoint.
