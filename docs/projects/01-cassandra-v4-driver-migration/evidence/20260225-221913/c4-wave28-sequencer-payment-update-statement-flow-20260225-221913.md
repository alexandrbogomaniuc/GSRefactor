# CASS-V4 Wave 28: Sequencer + Payment update statement-flow cleanup

## Scope
Converted remaining low-risk typed `Update` declarations to generic `Statement` flow in:
- `CassandraSequencerPersister`
- `CassandraIntSequencerPersister`
- `CassandraPaymentTransactionPersister`

## What changed
- `CassandraSequencerPersister`
  - Replaced `Update` local variables with fluent `Statement` chains in CAS update paths.
- `CassandraIntSequencerPersister`
  - Replaced `Update` local variables with fluent `Statement` chains in CAS update paths.
- `CassandraPaymentTransactionPersister`
  - Removed `Update`-typed method contract and local variables.
  - Refactored update construction into `Statement` return path:
    - `getUpdateStatement(PaymentTransaction, String, ByteBuffer, String)`
    - `resolveExtIdKey(...)` helper
  - Preserved existing bucket lookup, extId behavior, and serialized/json persistence logic.

## Compile iteration
- First `common-persisters` build failed because `Update.Where` supports one `where(...)` followed by `and(...)`.
- Fixed by switching chained clauses to `where(...).and(...).and(...)`.
- Rerun succeeded.

## Validation
- PASS: `mvn -DskipTests install` (`common-persisters`) after compile fix rerun.
- PASS: `mvn test` (`cache`) — `63` tests, `0` failures, `0` errors.
- PASS: `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` (`web-gs`).
- PASS: `mvn -pl core-interfaces,core,persistance -am -DskipTests package` (`mp-server`).

## Inventory delta
- GS `driver3_import_lines`: `430 -> 427` (`-3`)
- MP `driver3_import_lines`: `151` (no change)
- Driver4 import lines remain `0`.

## Evidence files
- `c4-wave28-build-common-persisters-20260225-221913.txt` (first failed attempt)
- `c4-wave28-build-common-persisters-20260225-221913-rerun.txt` (passing rerun)
- `c4-wave28-unit-tests-20260225-221913.txt`
- `c4-wave28-build-web-gs-20260225-221913.txt`
- `c4-wave28-build-mp-stack-20260225-221913.txt`
- `phase7-cassandra-driver-inventory-20260225-222026.txt`
