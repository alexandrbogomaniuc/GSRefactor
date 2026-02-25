# CASS-V4 Wave 21: host-cdn + country-restriction + player-game-settings statement-flow decoupling

## Timestamp (UTC)
2026-02-25T21:54:45Z

## Scope
Converted typed querybuilder variable usage to generic `Statement` flow in:
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraHostCdnPersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraCountryRestrictionPersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraPlayerGameSettingsPersister.java`

## What changed
- Replaced typed `Select` / `Insert` / `Delete` variables with `Statement`-typed query construction.
- Removed mutable `query.where()` mutation style where possible and built predicates inline using chained `where(...).and(...)` construction.
- Kept existing table names, filters, and execution labels unchanged.

## Validation evidence
- Common persisters install (pass):
  - `c4-wave21-build-common-persisters-20260225-215352.txt`
- Cache tests (pass, 63 tests):
  - `c4-wave21-unit-tests-20260225-215352.txt`
- Web GS package (pass):
  - `c4-wave21-build-web-gs-20260225-215352.txt`
- MP subset package (pass):
  - `c4-wave21-build-mp-stack-20260225-215352.txt`

## Inventory delta
- Snapshot file:
  - `phase7-cassandra-driver-inventory-20260225-215445.txt`
- GS driver3 import lines: `451 -> 445` (delta `-6`)
- MP driver3 import lines: unchanged `151`
- Driver4 import lines: unchanged `0`

## Risk assessment
- Low risk: statement typing and query assembly style changed, but query predicates and persistence payload fields were preserved.
