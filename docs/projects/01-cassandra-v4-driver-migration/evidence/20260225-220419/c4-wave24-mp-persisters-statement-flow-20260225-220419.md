# CASS-V4 Wave 24: MP persisters statement-flow decoupling

## Timestamp (UTC)
2026-02-25T22:05:07Z

## Scope
Converted typed querybuilder variable usage to generic `Statement` flow in:
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/mp/BattlegroundPrivateRoomSettingsPersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/mp/LeaderboardResultPersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/mp/MQReservedNicknamePersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/mp/RoundKPIInfoPersister.java`

## What changed
- Replaced typed `Select` / `Insert` variable declarations with `Statement` flow.
- Preserved query predicates, limits, and `allowFiltering` behavior in nickname lookup.
- Left persistence and deserialization logic unchanged.

## Validation evidence
- Common persisters install (pass):
  - `c4-wave24-build-common-persisters-20260225-220419.txt`
- Cache tests (pass, 63 tests):
  - `c4-wave24-unit-tests-20260225-220419.txt`
- Web GS package (pass):
  - `c4-wave24-build-web-gs-20260225-220419.txt`
- MP subset package (pass):
  - `c4-wave24-build-mp-stack-20260225-220419.txt`

## Inventory delta
- Snapshot file:
  - `phase7-cassandra-driver-inventory-20260225-220509.txt`
- GS driver3 import lines: `440 -> 436` (delta `-4`)
- MP driver3 import lines: unchanged `151`
- Driver4 import lines: unchanged `0`

## Risk assessment
- Low risk: no schema/table changes and no business-logic changes; only query variable typing/assembly style was adjusted.
