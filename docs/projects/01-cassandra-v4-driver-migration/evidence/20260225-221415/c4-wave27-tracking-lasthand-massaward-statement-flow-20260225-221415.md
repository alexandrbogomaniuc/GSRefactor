# CASS-V4 Wave 27: tracking + lasthand + mass-award statement-flow decoupling

## Timestamp (UTC)
2026-02-25T22:15:06Z

## Scope
Converted typed querybuilder declarations to generic `Statement` flow in:
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraTrackingInfoPersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraLasthandPersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraMassAwardPersister.java`

## What changed
- Replaced typed `Select` / `Insert` / `Delete` variables with `Statement` declarations in tracking/lasthand paths.
- Reworked tracking query creation to avoid `Select.Selection` typed temporary variable.
- Converted delayed-mass-award index insert in mass-award persister to `Statement` query declaration.
- Kept query predicates, table usage, limits, and write-time logic unchanged.

## Validation evidence
- Common persisters install (pass):
  - `c4-wave27-build-common-persisters-20260225-221415.txt`
- Cache tests (pass, 63 tests):
  - `c4-wave27-unit-tests-20260225-221415.txt`
- Web GS package (pass):
  - `c4-wave27-build-web-gs-20260225-221415.txt`
- MP subset package (pass):
  - `c4-wave27-build-mp-stack-20260225-221415.txt`

## Inventory delta
- Snapshot file:
  - `phase7-cassandra-driver-inventory-20260225-221506.txt`
- GS driver3 import lines: `434 -> 430` (delta `-4`)
- MP driver3 import lines: unchanged `151`
- Driver4 import lines: unchanged `0`

## Risk assessment
- Low risk: no schema/table/data flow changes; only query declaration typing and assembly style were modified.
