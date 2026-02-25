# CASS-V4 Wave 23: support + currency-rates statement-flow decoupling

## Timestamp (UTC)
2026-02-25T22:01:53Z

## Scope
Converted typed querybuilder variable usage to generic `Statement` flow in:
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraSupportPersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraCurrencyRatesPersister.java`
- `gs-server/cassandra-cache/common-persisters/src/main/java/com/dgphoenix/casino/cassandra/persist/CassandraCurrencyRatesByDatePersister.java`

## What changed
- Replaced typed `Select` / `Insert` variables with `Statement` in support and currency-rates read/write paths.
- Replaced mutable `query.where()` usage with inline where-chain construction where applicable.
- Kept `Insert` typing in the currency-rates-by-date batch loop because `Batch#add` expects `RegularStatement` (compile-safe requirement).

## Validation evidence
- Common persisters install:
  - initial compile fail (captured): `c4-wave23-build-common-persisters-20260225-220044.txt`
  - fixed + rerun pass: `c4-wave23-build-common-persisters-20260225-220044-rerun.txt`
- Cache tests (pass, 63 tests):
  - `c4-wave23-unit-tests-20260225-220044.txt`
- Web GS package (pass):
  - `c4-wave23-build-web-gs-20260225-220044.txt`
- MP subset package (pass):
  - `c4-wave23-build-mp-stack-20260225-220044.txt`

## Compile iteration note
- Failure cause: `Statement` could not be added to querybuilder `Batch` (`RegularStatement` required).
- Fix: restore `Insert` type for per-item batch statements in `CassandraCurrencyRatesByDatePersister#createOrUpdate(long, Set<CurrencyRate>)`.

## Inventory delta
- Snapshot file:
  - `phase7-cassandra-driver-inventory-20260225-220153.txt`
- GS driver3 import lines: `442 -> 440` (delta `-2`)
- MP driver3 import lines: unchanged `151`
- Driver4 import lines: unchanged `0`

## Risk assessment
- Low risk: query filters/tables/data fields were preserved; only variable typing and query assembly style were adjusted.
