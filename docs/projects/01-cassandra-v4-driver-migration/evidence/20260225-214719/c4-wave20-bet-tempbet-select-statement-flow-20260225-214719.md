# CASS-V4 Wave 20: Bet + TempBet querybuilder statement-flow decoupling

## Timestamp (UTC)
2026-02-25T21:51:12Z

## Scope
Converted remaining typed-select query assembly in:
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraBetPersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraTempBetPersister.java`

## What changed
- Replaced typed `Select`-style where chaining assumptions with direct `QueryBuilder.select(...).from(...).where(...).and(...)` construction assigned to generic `Statement`.
- Removed invalid `query.where()` calls on `Statement` in `CassandraTempBetPersister`.
- Preserved existing query semantics and filter predicates.

## Validation evidence
- Common persisters install (pass):
  - `c4-wave20-build-common-persisters-20260225-214914.txt`
- Cache tests (pass):
  - `c4-wave20-unit-tests-20260225-214914.txt`
- Web GS package (pass, rerun with explicit cluster profile):
  - failed attempt (expected context issue): `c4-wave20-build-web-gs-20260225-214914.txt`
  - passing rerun: `c4-wave20-build-web-gs-20260225-214914-rerun.txt`
- MP subset package (pass):
  - `c4-wave20-build-mp-stack-20260225-214914.txt`

## Notes
- `web-gs` packaging requires explicit cluster properties in this shell context:
  - `-Dcluster.properties=local/local-machine.properties`
- This is a build-context requirement; no production/runtime behavior was changed by this note.

## Inventory delta
- Snapshot file:
  - `phase7-cassandra-driver-inventory-20260225-215112.txt`
- GS driver3 import lines: `453 -> 451` (delta `-2`)
- MP driver3 import lines: unchanged `151`
- Driver4 import lines: unchanged `0`

## Risk assessment
- Low risk: query predicate logic remained unchanged and all validation gates passed after fix.
