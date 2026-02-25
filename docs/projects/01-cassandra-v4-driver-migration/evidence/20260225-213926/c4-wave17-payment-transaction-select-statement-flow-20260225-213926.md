# CASS-V4 Wave 17 - Payment Transaction Select Statement Flow

## Timestamp
- 2026-02-25 21:40 UTC

## Scope
- Reduce typed querybuilder coupling in the high-density `CassandraPaymentTransactionPersister` without changing conditional update behavior.

## Code Changes
- Updated `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraPaymentTransactionPersister.java`:
  - Converted typed `Select` query variables to generic `Statement` flow in:
    - `getUpdateStatement(...)` read path
    - `getTransaction(...)`
    - `loadAndProcess(...)`
    - `getTransactionIdsByDateRange(...)`
    - `saveExternalTransactionId(...)` lookup path
    - `getTransactionByExtId(...)`
  - Left `Update` conditional-write logic unchanged to avoid behavior risk in LWT/update paths.

## Validation
- PASS: `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
  - Evidence: `c4-wave17-build-common-persisters-20260225-213926.txt`
- PASS: `mvn -q -Dtest=KeySpaceManagerTest,CassandraPersistenceManagerTest,ClusterConfigDeserializationTest,KeyspaceConfigurationFactoryTest test` in `gs-server/cassandra-cache/cache`
  - Evidence: `c4-wave17-unit-tests-20260225-213926.txt`
- PASS: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
  - Evidence: `c4-wave17-build-web-gs-20260225-213926.txt`
- PASS: `mvn -DskipTests -pl core-interfaces,core,persistance -am package` in `mp-server`
  - Evidence: `c4-wave17-build-mp-stack-20260225-213926.txt`

## Result
- Wave 17 completed successfully with no regression in validation checks.
- A major remaining class now has reduced typed querybuilder coupling on read/query paths.

## Next Target
- Run inventory checkpoint now (metric gate) to quantify Waves 14-17 impact, then continue sequencer/tracking/history hotspots.
