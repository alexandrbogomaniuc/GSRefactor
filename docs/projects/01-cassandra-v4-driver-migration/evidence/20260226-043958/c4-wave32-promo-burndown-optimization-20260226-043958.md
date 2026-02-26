# CASS-V4 Wave 32: Promo query declaration cleanup (optimized)

## Scope
Converted typed querybuilder declarations away from local variables in:
- `CassandraMaxBalanceTournamentPersister`
- `CassandraPromoWinPersister`
- `CassandraBattlegroundConfigPersister`
- `CassandraPromoCampaignStatisticsPersister`

## What changed
- Removed typed `Insert` / `Select` / `Update` / `Delete` local declarations.
- Inlined query execution (direct `execute(...)` chains) to avoid replacing typed imports with `Statement` imports one-for-one.
- Preserved existing predicates, update clauses, limits, and payload serialization behavior.

## Iteration note
- First pass validation was green but inventory stayed flat (`415`) because typed imports were replaced with `Statement` imports.
- Second pass removed local statement types and reran full validation + inventory.

## Validation (final rerun)
- PASS: `mvn -DskipTests install` (`promo/persisters`)
- PASS: `mvn -DskipTests install` (`common-persisters`)
- PASS: `mvn test` (`cache`) — `63` tests, `0` failures, `0` errors.
- PASS: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` (`web-gs`).
- PASS: `mvn -pl core-interfaces,core,persistance -am -DskipTests package` (`mp-server`).

## Inventory delta
- GS `driver3_import_lines`: `415 -> 411` (`-4`)
- MP `driver3_import_lines`: `151` (no change)
- Driver4 import lines remain `0`.

## Completion snapshot (import burn-down metric)
- GS-only: `15.78%` complete (`488 -> 411`)
- GS+MP combined: `12.05%` complete (`639 -> 562`)

## Evidence files
- `c4-wave32-build-promo-persisters-20260226-043958.txt` (initial pass)
- `c4-wave32-build-common-persisters-20260226-043958.txt` (initial pass)
- `c4-wave32-unit-tests-20260226-043958.txt` (initial pass)
- `c4-wave32-build-web-gs-20260226-043958.txt` (initial pass)
- `c4-wave32-build-mp-stack-20260226-043958.txt` (initial pass)
- `phase7-cassandra-driver-inventory-20260226-044058.txt` (initial snapshot)
- `c4-wave32-build-promo-persisters-20260226-043958-rerun.txt` (final)
- `c4-wave32-build-common-persisters-20260226-043958-rerun.txt` (final)
- `c4-wave32-unit-tests-20260226-043958-rerun.txt` (final)
- `c4-wave32-build-web-gs-20260226-043958-rerun.txt` (final)
- `c4-wave32-build-mp-stack-20260226-043958-rerun.txt` (final)
- `phase7-cassandra-driver-inventory-20260226-044314.txt` (final snapshot)
