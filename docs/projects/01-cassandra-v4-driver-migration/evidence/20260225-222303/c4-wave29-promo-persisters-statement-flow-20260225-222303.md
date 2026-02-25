# CASS-V4 Wave 29: Promo persister statement-flow cleanup

## Scope
Converted typed querybuilder declarations to generic `Statement` flow in three promo persisters:
- `CassandraPromoFeedPersister`
- `CassandraTournamentIconPersister`
- `CassandraSupportedPromoPlatformsPersister`

## What changed
- Removed direct `Insert` / `Select` imports from the three classes.
- Switched local query variables from typed querybuilder classes to `Statement` while preserving predicates, limits, and payload columns.
- Kept behavior unchanged (same table names, keys, and execution paths).

## Validation
- PASS: `mvn -DskipTests install` (`promo/persisters`)
- PASS: `mvn -DskipTests install` (`common-persisters`)
- PASS: `mvn test` (`cache`) — `63` tests, `0` failures, `0` errors.
- PASS: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` (`web-gs`).
- PASS: `mvn -pl core-interfaces,core,persistance -am -DskipTests package` (`mp-server`).

## Inventory delta
- GS `driver3_import_lines`: `427 -> 424` (`-3`)
- MP `driver3_import_lines`: `151` (no change)
- Driver4 import lines remain `0`.

## Evidence files
- `c4-wave29-build-promo-persisters-20260225-222303.txt`
- `c4-wave29-build-common-persisters-20260225-222303.txt`
- `c4-wave29-unit-tests-20260225-222303.txt`
- `c4-wave29-build-web-gs-20260225-222303.txt`
- `c4-wave29-build-mp-stack-20260225-222303.txt`
- `phase7-cassandra-driver-inventory-20260225-222410.txt`
