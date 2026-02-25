# CASS-V4 Wave 25: delayed-mass-award + round-session statement-flow decoupling

## Timestamp (UTC)
2026-02-25T22:07:49Z

## Scope
Converted typed querybuilder variable usage to generic `Statement` flow in:
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraDelayedMassAwardPersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraDelayedMassAwardHistoryPersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraRoundGameSessionPersister.java`

## What changed
- Replaced typed `Select` / `Insert` variables with `Statement` query flow.
- Inlined where-clause chains into query construction where applicable.
- Kept existing filters, columns, and write-time behavior unchanged.

## Validation evidence
- Common persisters install (pass):
  - `c4-wave25-build-common-persisters-20260225-220704.txt`
- Cache tests (pass, 63 tests):
  - `c4-wave25-unit-tests-20260225-220704.txt`
- Web GS package (pass):
  - `c4-wave25-build-web-gs-20260225-220704.txt`
- MP subset package (pass):
  - `c4-wave25-build-mp-stack-20260225-220704.txt`

## Inventory delta
- Snapshot file:
  - `phase7-cassandra-driver-inventory-20260225-220750.txt`
- GS driver3 import lines: `436 -> 434` (delta `-2`)
- MP driver3 import lines: unchanged `151`
- Driver4 import lines: unchanged `0`

## Risk assessment
- Low risk: schema/table definitions and business logic stayed intact; only query variable typing and assembly style changed.
