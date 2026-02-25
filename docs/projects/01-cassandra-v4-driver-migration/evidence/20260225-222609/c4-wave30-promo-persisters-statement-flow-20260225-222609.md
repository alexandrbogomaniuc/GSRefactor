# CASS-V4 Wave 30: Promo persister statement-flow cleanup

## Scope
Converted typed querybuilder declarations to generic `Statement` flow in:
- `CassandraTournamentFeedHistoryPersister`
- `CassandraSummaryFeedTransformerPersister`
- `CassandraPlayerAliasPersister`

## What changed
- Removed direct `Insert` / `Select` imports in the three classes.
- Switched local query variables to `Statement` while preserving existing clauses, limits, and persistence payload behavior.

## Validation
- PASS: `mvn -DskipTests install` (`promo/persisters`)
- PASS: `mvn -DskipTests install` (`common-persisters`)
- PASS: `mvn test` (`cache`) — `63` tests, `0` failures, `0` errors.
- PASS: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` (`web-gs`).
- PASS: `mvn -pl core-interfaces,core,persistance -am -DskipTests package` (`mp-server`).

## Inventory delta
- GS `driver3_import_lines`: `424 -> 421` (`-3`)
- MP `driver3_import_lines`: `151` (no change)
- Driver4 import lines remain `0`.

## Evidence files
- `c4-wave30-build-promo-persisters-20260225-222609.txt`
- `c4-wave30-build-common-persisters-20260225-222609.txt`
- `c4-wave30-unit-tests-20260225-222609.txt`
- `c4-wave30-build-web-gs-20260225-222609.txt`
- `c4-wave30-build-mp-stack-20260225-222609.txt`
- `phase7-cassandra-driver-inventory-20260225-222720.txt`
