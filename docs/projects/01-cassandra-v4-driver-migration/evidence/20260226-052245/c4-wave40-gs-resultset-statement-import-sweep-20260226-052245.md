# CASS-V4 Wave 40 (GS ResultSet/Statement import sweep)

## Scope
Executed a broad GS sweep to reduce driver3 import surface by removing direct `ResultSet` and `Statement` imports and switching to fully-qualified references where needed.

### Changed scope summary
- `48` files under `gs-server/cassandra-cache/common-persisters/src/main/java`
- `14` files under `gs-server/promo/persisters/src/main/java`
- Full list captured in:
  - `c4-wave40-changed-files-20260226-052245.txt`

## Validation
All required checks passed:
- `mvn -DskipTests install` in `gs-server/promo/persisters`
- `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
- `mvn test` in `gs-server/cassandra-cache/cache`
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
- `mvn -pl core-interfaces,core,persistance -am -DskipTests package` in `mp-server`

## Inventory delta
- GS driver3 import lines: `383 -> 305` (`-78`)
- MP driver3 import lines: `60 -> 60` (no change)
- Combined GS+MP: `443 -> 365` (`-78`)

## Completion snapshot (import burn-down metric)
- GS-only: `37.50%` (`488 -> 305`)
- MP-only: `60.26%` (`151 -> 60`)
- Combined GS+MP: `42.88%` (`639 -> 365`)

## Notes
This wave is import-surface focused and preserved runtime behavior while materially accelerating combined burn-down.
