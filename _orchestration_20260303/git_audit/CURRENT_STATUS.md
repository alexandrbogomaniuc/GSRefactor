# CURRENT_STATUS

- current batch label: CASSANDRA-BATCH-A-GS-CACHE-PILOT
- branch: codex/phasee-stabilization-20260303
- HEAD before batch: 97c04061bde0bdda90c09539ec0ca13e4aa09176
- HEAD after batch: this commit (reported in push output)
- concise purpose of the batch: execute one small GS Cassandra cache pilot in `common-persisters` by replacing exact legacy DataType FQCN usages with existing `CassandraDataTypes` helpers, without touching web-gs or rename lanes.
- chosen submodule: gs-server/cassandra-cache/common-persisters

## Exact commands run
- git grep -nE 'com\.datastax\.driver\.core|cassandra-driver-core|cassandra-driver-mapping|spring-data-cassandra|cassandra\.driver\.version' HEAD -- gs-server/cassandra-cache/cache gs-server/cassandra-cache/common-persisters gs-server/cassandra-cache
- rg -n '\bQueryBuilder\b|\bMappingManager\b|\bMapper\b|\bCluster\b|\bSession\b' gs-server/cassandra-cache/cache gs-server/cassandra-cache/common-persisters --glob '!**/target/**' --glob '!**/.git/**'
- rg -n 'com\.datastax\.driver\.core|cassandra-driver-core|cassandra-driver-mapping|spring-data-cassandra|cassandra\.driver\.version' gs-server/cassandra-cache/cache gs-server/cassandra-cache/common-persisters gs-server/cassandra-cache --glob '!**/target/**' --glob '!**/.git/**'
- cd /Users/alexb/Documents/Dev/Dev_new/gs-server && mvn -s game-server/build/build-settings.xml -pl cassandra-cache/common-persisters -am -DskipTests compile
- cd /Users/alexb/Documents/Dev/Dev_new/gs-server && mvn -s game-server/build/build-settings.xml -pl cassandra-cache/common-persisters -am test

## RC summary
- GS common-persisters compile rc: 0
- GS common-persisters test rc: 0

## Exact GS Cassandra scan count (allowed scope command output, deduplicated by file:line)
- before: 1784
- after: 1748

## Changed files
- gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraBonusPersister.java
- gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraBonusArchivePersister.java
- gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraFrBonusPersister.java
- gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraFrBonusArchivePersister.java
- gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraWalletOperationInfoPersister.java
- _orchestration_20260303/git_audit/CURRENT_STATUS.md

## Blockers remaining
- Legacy DataStax driver usage remains across both `gs-server/cassandra-cache/cache` and `gs-server/cassandra-cache/common-persisters`; this pilot intentionally migrated only one small 5-file cohesive cluster.
- Some scan output duplication is caused by overlapping input paths in the required post-batch command; acceptance metrics should continue to use file:line deduplicated counts.

## Local audit hygiene note
- Local orchestration outputs are intentionally excluded via `.git/info/exclude`.
