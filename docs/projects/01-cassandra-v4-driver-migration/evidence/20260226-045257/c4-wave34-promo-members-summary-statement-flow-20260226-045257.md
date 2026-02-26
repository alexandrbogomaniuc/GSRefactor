# CASS-V4 Wave 34: Promo members/summary-feed cleanup

## Scope
Converted typed querybuilder declarations to generic execution flow in:
- `CassandraPromoCampaignMembersPersister`
- `CassandraSummaryTournamentPromoFeedPersister`

## What changed
- Removed querybuilder `Insert` / `Select` / `Update` typed declarations in these classes.
- Switched to direct execute-chain statements and generic statement signatures where safe.
- Kept one `Select` usage in `getPromoCampaignMembersSafely` as fully-qualified type because `setFetchSize(...)` and `where(...)` chaining requires select-specific API.

## Iteration note
- Initial promo-persisters compile failed (`CassandraPromoCampaignMembersPersister` line 174) because `Statement` type does not expose `where(...)`.
- Fixed by using fully-qualified `com.datastax.driver.core.querybuilder.Select` for that specific variable.
- Rerun passed.

## Validation (final)
- PASS: `mvn -DskipTests install` (`promo/persisters`, rerun)
- PASS: `mvn -DskipTests install` (`common-persisters`)
- PASS: `mvn test` (`cache`) — `63` tests, `0` failures, `0` errors.
- PASS: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` (`web-gs`).
- PASS: `mvn -pl core-interfaces,core,persistance -am -DskipTests package` (`mp-server`).

## Inventory delta
- GS `driver3_import_lines`: `404 -> 399` (`-5`)
- MP `driver3_import_lines`: `151` (no change)
- Driver4 import lines remain `0`.

## Completion snapshot (import burn-down metric)
- GS-only: `18.24%` (`488 -> 399`)
- GS+MP combined: `13.93%` (`639 -> 550`)

## Evidence files
- `c4-wave34-build-promo-persisters-20260226-045257.txt` (initial fail)
- `c4-wave34-build-promo-persisters-20260226-045257-rerun.txt` (pass)
- `c4-wave34-build-common-persisters-20260226-045257.txt`
- `c4-wave34-unit-tests-20260226-045257.txt`
- `c4-wave34-build-web-gs-20260226-045257.txt`
- `c4-wave34-build-mp-stack-20260226-045257.txt`
- `phase7-cassandra-driver-inventory-20260226-045438.txt`
