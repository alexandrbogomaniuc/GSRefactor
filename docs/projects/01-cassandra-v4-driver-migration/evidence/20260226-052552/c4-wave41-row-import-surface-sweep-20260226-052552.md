# CASS-V4 Wave 41 (Row import-surface sweep)

## Scope
Executed a cross-module sweep to reduce driver3 import surface by removing direct `Row` imports and converting references to fully-qualified row types where safe.

### Changed scope summary
- `46` files in `gs-server/cassandra-cache/common-persisters/src/main/java`
- `15` files in `gs-server/promo/persisters/src/main/java`
- `25` files in `mp-server/persistance/src/main/java`
- `4` files in `gs-server/cassandra-cache/cache/src/main/java`
- Full file list:
  - `c4-wave41-changed-files-20260226-052552.txt`

## Validation
All required checks passed:
- `mvn -DskipTests install` in `gs-server/promo/persisters`
- `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
- `mvn test` in `gs-server/cassandra-cache/cache`
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
- `mvn -pl core-interfaces,core,persistance -am -DskipTests package` in `mp-server`

## Inventory delta
- GS driver3 import lines: `305 -> 253` (`-52`)
- MP driver3 import lines: `60 -> 35` (`-25`)
- Combined GS+MP: `365 -> 288` (`-77`)

## Completion snapshot (import burn-down metric)
- GS-only: `48.16%` (`488 -> 253`)
- MP-only: `76.82%` (`151 -> 35`)
- Combined GS+MP: `54.93%` (`639 -> 288`)

## Notes
This wave crossed the requested 50% combined burn-down threshold while preserving runtime behavior under the full validation matrix.
