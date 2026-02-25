# CASS-V4 Wave 12 - Medium Hotspots Statement Flow

## Timestamp
- 2026-02-25 21:24 UTC

## Scope
- Continue staged migration by converting medium-complexity persisters that still used typed `Select`/`Update` variables.
- Keep runtime logic unchanged and validate against the same four-check matrix.

## Code Changes
- Switched typed querybuilder variable usage to generic `Statement` flow in:
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraFRBonusWinPersister.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraExtendedAccountInfoPersister.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraCallStatisticsPersister.java`

## Validation
- PASS: `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
  - Evidence: `c4-wave12-build-common-persisters-20260225-212401.txt`
- PASS: `mvn -q -Dtest=KeySpaceManagerTest,CassandraPersistenceManagerTest,ClusterConfigDeserializationTest,KeyspaceConfigurationFactoryTest test` in `gs-server/cassandra-cache/cache`
  - Evidence: `c4-wave12-unit-tests-20260225-212401.txt`
- PASS: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
  - Evidence: `c4-wave12-build-web-gs-20260225-212401.txt`
- PASS: `mvn -DskipTests -pl core-interfaces,core,persistance -am package` in `mp-server`
  - Evidence: `c4-wave12-build-mp-stack-20260225-212401.txt`

## Result
- Wave 12 completed without regressions in targeted builds/tests.
- Additional medium complexity persistence paths now use driver-neutral statement typing.

## Next Target
- Continue with sequencer and payment transaction persister hotspots, then refresh inventory delta to track cumulative reduction.
