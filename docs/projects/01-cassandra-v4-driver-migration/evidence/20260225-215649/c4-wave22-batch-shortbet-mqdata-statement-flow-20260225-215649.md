# CASS-V4 Wave 22: batch-status + short-bet + mq-data statement-flow decoupling

## Timestamp (UTC)
2026-02-25T21:57:38Z

## Scope
Converted additional typed querybuilder usage to generic `Statement` flow in:
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraBatchOperationStatusPersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraShortBetInfoPersister.java` (select/query paths)
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/mp/MQDataPersister.java`

## What changed
- Replaced typed `Select` / `Insert` variable declarations with `Statement` in batch-status and mq-data persisters.
- Replaced typed `Select`/`Select.Where` flow with `Statement` query chains in short-bet read paths.
- Kept `Insert` usage in short-bet write paths where TTL `using(...)` handling remains on insert builders.

## Validation evidence
- Common persisters install (pass):
  - `c4-wave22-build-common-persisters-20260225-215649.txt`
- Cache tests (pass, 63 tests):
  - `c4-wave22-unit-tests-20260225-215649.txt`
- Web GS package (pass):
  - `c4-wave22-build-web-gs-20260225-215649.txt`
- MP subset package (pass):
  - `c4-wave22-build-mp-stack-20260225-215649.txt`

## Inventory delta
- Snapshot file:
  - `phase7-cassandra-driver-inventory-20260225-215738.txt`
- GS driver3 import lines: `445 -> 442` (delta `-3`)
- MP driver3 import lines: unchanged `151`
- Driver4 import lines: unchanged `0`

## Risk assessment
- Low risk: no schema/table changes and no filter logic changes; only query variable typing/assembly style was adjusted.
