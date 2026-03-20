# CURRENT_STATUS

- current batch label: CASSANDRA-BATCH-B-GS-COMMON-PERSISTERS
- branch: codex/priority3-gs-common-persisters-batchb-20260304
- HEAD before batch: 6c40593d1997e5d4b55d4aa84fd963ce7f2b394d
- HEAD after batch: this commit (reported in push output)
- concise purpose of the batch: execute one additional small cohesive Cassandra migration batch in `gs-server/cassandra-cache/common-persisters` by replacing exact `com.datastax.driver.core.DataType.*` FQCN usage with existing `CassandraDataTypes` helpers while keeping module-scoped compile/test green.

## Exact commands run
- git grep -nE 'com\.datastax\.driver\.core' HEAD -- gs-server/cassandra-cache/common-persisters
- git grep -nE 'cassandra-driver-core|cassandra-driver-mapping|spring-data-cassandra|cassandra\.driver\.version' HEAD -- gs-server/cassandra-cache
- rg -n '\bQueryBuilder\b|\bMappingManager\b|\bMapper\b|\bCluster\b|\bSession\b' gs-server/cassandra-cache/common-persisters --glob '!**/target/**' --glob '!**/.git/**'
- rg -n 'com\.datastax\.driver\.core' gs-server/cassandra-cache/common-persisters --glob '!**/target/**' --glob '!**/.git/**'
- rg -n 'cassandra-driver-core|cassandra-driver-mapping|spring-data-cassandra|cassandra\.driver\.version' gs-server/cassandra-cache --glob '!**/target/**' --glob '!**/.git/**'
- cd /Users/alexb/Documents/Dev/Dev_new/gs-server && mvn -s game-server/build/build-settings.xml -pl cassandra-cache/common-persisters -am -DskipTests compile
- cd /Users/alexb/Documents/Dev/Dev_new/gs-server && mvn -s game-server/build/build-settings.xml -pl cassandra-cache/common-persisters -am test

## RC summary
- GS common-persisters compile rc: 0
- GS common-persisters test rc: 0

## Exact metrics (deduplicated file:line)
- `common-persisters` `com.datastax.driver.core` count before: 1211
- `common-persisters` `com.datastax.driver.core` count after: 1186
- GS Cassandra dependency-coordinate count before (`gs-server/cassandra-cache`): 5
- GS Cassandra dependency-coordinate count after (`gs-server/cassandra-cache`): 5

## Changed files
- gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraBatchOperationStatusPersister.java
- gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraCallIssuesPersister.java
- gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraClientStatisticsPersister.java
- gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraFrbWinOperationPersister.java
- gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraPendingDataArchivePersister.java
- _orchestration_20260303/git_audit/CURRENT_STATUS.md

## Blockers remaining
- Large remaining legacy DataStax usage in `common-persisters` beyond this 5-file cluster, especially high-density files requiring larger batched edits:
  - gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraGameSessionPersister.java
  - gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraTransactionDataPersister.java
  - gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraMetricsPersister.java
- Dependency-coordinate legacy entries remain in GS cassandra-cache lane poms (unchanged in this batch):
  - gs-server/cassandra-cache/cache/pom.xml

## Workspace note
- Untracked local files outside allowed scope were left untouched and not included in this batch.
