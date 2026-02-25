# CASS-V4 Wave 26: distributed-config + http-call + expired-bonus statement-flow cleanup

## Timestamp (UTC)
2026-02-25T22:11:02Z

## Scope
Converted remaining typed querybuilder declarations in:
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/AbstractDistributedConfigEntryPersister.java` (select path)
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraHttpCallInfoPersister.java` (select path)
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraExpiredBonusTrackerInfoPersister.java`

## What changed
- Replaced typed `Select` declarations with generic `Statement` in distributed-config and http-call query paths.
- Replaced typed `Insert` declaration with `Statement` in expired-bonus tracker persist path.
- Kept typed `Insert` builder in `CassandraHttpCallInfoPersister#persist` because the method mutates insert values across optional branches and remains compile-safe as-is.

## Validation evidence
- Common persisters install (pass):
  - `c4-wave26-build-common-persisters-20260225-221010.txt`
- Cache tests (pass, 63 tests):
  - `c4-wave26-unit-tests-20260225-221010.txt`
- Web GS package (pass):
  - `c4-wave26-build-web-gs-20260225-221010.txt`
- MP subset package (pass):
  - `c4-wave26-build-mp-stack-20260225-221010.txt`

## Inventory result
- Snapshot file:
  - `phase7-cassandra-driver-inventory-20260225-221102.txt`
- GS driver3 import lines: `434 -> 434` (no net change)
- MP driver3 import lines: unchanged `151`
- Driver4 import lines: unchanged `0`

## Why inventory is unchanged
- This wave replaced some typed driver3 imports with other driver3 imports (`Statement`) in the same files, improving API-shape neutrality but not total driver3 import count.

## Risk assessment
- Low risk: no schema/table/logic changes; only variable typing and query declaration style were adjusted.
