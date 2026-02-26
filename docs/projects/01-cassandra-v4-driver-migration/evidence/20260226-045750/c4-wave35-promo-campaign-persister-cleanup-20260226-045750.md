# CASS-V4 Wave 35: Promo campaign persister cleanup

## Scope
Converted typed querybuilder declarations in:
- `CassandraPromoCampaignPersister`

## What changed
- Removed typed `Insert` / `Delete` / `Select` import usage.
- Switched batch writes/deletes to direct `batch.add(...)` query-chain calls.
- Kept one select-specific type as fully-qualified `com.datastax.driver.core.querybuilder.Select` for branch-dependent select assembly.
- Preserved campaign persistence/deletion semantics and status-based selection logic.

## Validation
- PASS: `mvn -DskipTests install` (`promo/persisters`)
- PASS: `mvn -DskipTests install` (`common-persisters`)
- PASS: `mvn test` (`cache`) — `63` tests, `0` failures, `0` errors.
- PASS: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` (`web-gs`).
- PASS: `mvn -pl core-interfaces,core,persistance -am -DskipTests package` (`mp-server`).

## Inventory delta
- GS `driver3_import_lines`: `399 -> 396` (`-3`)
- MP `driver3_import_lines`: `151` (no change)
- Driver4 import lines remain `0`.

## Completion snapshot (import burn-down metric)
- GS-only: `18.85%` (`488 -> 396`)
- GS+MP combined: `14.40%` (`639 -> 547`)

## Evidence files
- `c4-wave35-build-promo-persisters-20260226-045750.txt`
- `c4-wave35-build-common-persisters-20260226-045750.txt`
- `c4-wave35-unit-tests-20260226-045750.txt`
- `c4-wave35-build-web-gs-20260226-045750.txt`
- `c4-wave35-build-mp-stack-20260226-045750.txt`
- `phase7-cassandra-driver-inventory-20260226-045859.txt`
