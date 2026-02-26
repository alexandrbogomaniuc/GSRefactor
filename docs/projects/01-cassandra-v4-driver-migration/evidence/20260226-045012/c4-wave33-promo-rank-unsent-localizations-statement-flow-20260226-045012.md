# CASS-V4 Wave 33: Promo rank/unsent/localizations cleanup

## Scope
Converted typed querybuilder declarations to generic execution flow in:
- `CassandraTournamentRankPersister`
- `CassandraUnsendedPromoWinInfoPersister`
- `CassandraLocalizationsPersister`

## What changed
- Removed querybuilder `Insert` / `Select` / `Delete` declarations where possible.
- Converted queries to direct chained execute calls.
- Kept `Batch` typed use in localizations where required by `batch.add(...)` semantics.

## Validation
- PASS: `mvn -DskipTests install` (`promo/persisters`)
- PASS: `mvn -DskipTests install` (`common-persisters`)
- PASS: `mvn test` (`cache`) — `63` tests, `0` failures, `0` errors.
- PASS: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` (`web-gs`).
- PASS: `mvn -pl core-interfaces,core,persistance -am -DskipTests package` (`mp-server`).

## Inventory delta
- GS `driver3_import_lines`: `411 -> 404` (`-7`)
- MP `driver3_import_lines`: `151` (no change)
- Driver4 import lines remain `0`.

## Completion snapshot (import burn-down metric)
- GS-only: `17.21%` (`488 -> 404`)
- GS+MP combined: `13.15%` (`639 -> 555`)

## Evidence files
- `c4-wave33-build-promo-persisters-20260226-045012.txt`
- `c4-wave33-build-common-persisters-20260226-045012.txt`
- `c4-wave33-unit-tests-20260226-045012.txt`
- `c4-wave33-build-web-gs-20260226-045012.txt`
- `c4-wave33-build-mp-stack-20260226-045012.txt`
- `phase7-cassandra-driver-inventory-20260226-045122.txt`
