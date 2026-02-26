# CASS-V4 Wave 31: Promo statistics/config statement-flow cleanup

## Scope
Converted typed querybuilder declarations to generic `Statement` flow in:
- `CassandraMaxBalanceTournamentPersister`
- `CassandraPromoWinPersister`
- `CassandraBattlegroundConfigPersister`
- `CassandraPromoCampaignStatisticsPersister`

## What changed
- Removed direct typed `Insert` / `Select` / `Update` / `Delete` imports in these classes.
- Switched local query variables to `Statement` with fluent chained query construction.
- Preserved existing predicates, limits, update-set clauses, and serialization behavior.

## Validation
- PASS: `mvn -DskipTests install` (`promo/persisters`)
- PASS: `mvn -DskipTests install` (`common-persisters`)
- PASS: `mvn test` (`cache`) — `63` tests, `0` failures, `0` errors.
- PASS: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` (`web-gs`).
- PASS: `mvn -pl core-interfaces,core,persistance -am -DskipTests package` (`mp-server`).

## Inventory delta
- GS `driver3_import_lines`: `421 -> 415` (`-6`)
- MP `driver3_import_lines`: `151` (no change)
- Driver4 import lines remain `0`.

## Evidence files
- `c4-wave31-build-promo-persisters-20260226-043647.txt`
- `c4-wave31-build-common-persisters-20260226-043647.txt`
- `c4-wave31-unit-tests-20260226-043647.txt`
- `c4-wave31-build-web-gs-20260226-043647.txt`
- `c4-wave31-build-mp-stack-20260226-043647.txt`
- `phase7-cassandra-driver-inventory-20260226-043751.txt`
