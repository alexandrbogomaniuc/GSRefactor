# CASS-V4 Wave 19 - Sequencer Select Statement Flow

## Timestamp
- 2026-02-25 21:44 UTC

## Scope
- Reduce typed querybuilder coupling in sequencer hotspots while preserving existing conditional update behavior.

## Code Changes
- Updated sequencer persisters:
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraSequencerPersister.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraIntSequencerPersister.java`
- Converted typed `Select` query variable in `getCurrentValue(...)` to generic `Statement` flow in both classes.
- Kept `Update` compare-and-set logic (`onlyIf(...)`) unchanged.

## Validation
- PASS: `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
  - Evidence: `c4-wave19-build-common-persisters-20260225-214328.txt`
- PASS: `mvn -q -Dtest=KeySpaceManagerTest,CassandraPersistenceManagerTest,ClusterConfigDeserializationTest,KeyspaceConfigurationFactoryTest test` in `gs-server/cassandra-cache/cache`
  - Evidence: `c4-wave19-unit-tests-20260225-214328.txt`
- PASS: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
  - Evidence: `c4-wave19-build-web-gs-20260225-214328.txt`
- PASS: `mvn -DskipTests -pl core-interfaces,core,persistance -am package` in `mp-server`
  - Evidence: `c4-wave19-build-mp-stack-20260225-214328.txt`

## Result
- Wave 19 completed with green validation and reduced typed `Select` usage in two high-traffic sequencer classes.

## Next Target
- Continue with remaining complex hotspots (`TrackingInfo`, `HistoryInformer`, additional MP persisters), then run next inventory checkpoint after 2-3 waves.
