# CASS-V4 Wave 11 - Common Persisters Small Hotspots

## Timestamp
- 2026-02-25 21:21 UTC

## Scope
- Continue incremental reduction of driver3 typed querybuilder usage in low-risk `common-persisters` classes.
- Keep behavior unchanged and validate through the same multi-module build/test matrix.

## Code Changes
- Replaced typed query variables (`Insert`/`Select`) with generic `Statement` flow in:
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraBlockedCountriesPersister.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraCurrencyRatesConfigPersister.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraCallIssuesPersister.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraPeriodicTasksPersister.java`
  - `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraExternalGameIdsPersister.java`

## Validation
- PASS: `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
  - Evidence: `c4-wave11-build-common-persisters-20260225-212053.txt`
- PASS: `mvn -q -Dtest=KeySpaceManagerTest,CassandraPersistenceManagerTest,ClusterConfigDeserializationTest,KeyspaceConfigurationFactoryTest test` in `gs-server/cassandra-cache/cache`
  - Evidence: `c4-wave11-unit-tests-20260225-212053.txt`
- PASS: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
  - Evidence: `c4-wave11-build-web-gs-20260225-212053.txt`
- PASS: `mvn -DskipTests -pl core-interfaces,core,persistance -am package` in `mp-server`
  - Evidence: `c4-wave11-build-mp-stack-20260225-212053.txt`

## Result
- Wave 11 completed successfully with no regressions found in targeted tests/builds.
- Additional `common-persisters` query paths now use driver-neutral statement typing.

## Next Target
- Continue with the next medium-complexity hotspots (classes still using typed `Select`/`Update`/`Insert` and nested `Select.Where` patterns), then regenerate inventory delta.
