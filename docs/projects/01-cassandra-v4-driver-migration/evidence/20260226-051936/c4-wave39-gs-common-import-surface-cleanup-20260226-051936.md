# CASS-V4 Wave 39 (GS common-persister import-surface cleanup)

## Scope
Reduced driver3 import surface in high-density GS common-persister hotspots by converting direct `Statement` / `ResultSet` / `QueryBuilder` imports to fully-qualified usage where safe.

### Changed files
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraTrackingInfoPersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraFrBonusArchivePersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraBonusArchivePersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/AbstractDistributedConfigEntryPersister.java`

## Validation
All required checks passed:
- `mvn -DskipTests install` in `gs-server/promo/persisters`
- `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
- `mvn test` in `gs-server/cassandra-cache/cache`
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
- `mvn -pl core-interfaces,core,persistance -am -DskipTests package` in `mp-server`

## Inventory delta
- GS driver3 import lines: `396 -> 383` (`-13`)
- MP driver3 import lines: `60 -> 60` (no change)
- Combined GS+MP: `456 -> 443` (`-13`)

## Completion snapshot (import burn-down metric)
- GS-only: `21.52%` (`488 -> 383`)
- MP-only: `60.26%` (`151 -> 60`)
- Combined GS+MP: `30.67%` (`639 -> 443`)

## Notes
This wave started shifting the burn-down focus back to GS after MP-heavy waves and preserved runtime behavior.
