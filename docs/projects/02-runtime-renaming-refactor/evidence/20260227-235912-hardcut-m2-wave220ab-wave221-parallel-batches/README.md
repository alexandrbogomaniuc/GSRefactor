# Evidence: Hard-Cut M2 Wave 220A + 220B + 221

## Scope
- W220A: 10 declaration migrations (`game-server/common-gs/kafka/handler`).
- W220B: 10 declaration migrations (`game-server/common-gs/kafka/handler`).
- W221: bounded integration rewires + canonical validation.

## Planning Artifacts
- `target-batchA.txt`
- `target-batchB.txt`
- `rewires-batchA-all.txt`
- `rewires-batchB-all.txt`
- `overlap-metrics.txt`
- `rationale.txt`
- `package-checks.txt`
- `package-checks-after.txt`

## Stabilization Notes
- Planned rewires were empty for both batches.
- Fast gate batchA rerun1 failed at `STEP06` (`KafkaOuterRequestHandler` unresolved in moved handlers).
- Applied minimal import-only stabilization in moved handlers:
  - added explicit `import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;`
  - no declaration rollback, no global replace.

## Validation Runs
- Fast gate batchA:
  - rerun1: `STEP06 FAIL`
  - rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- Fast gate batchB:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
  - retry1: `STEP09 FAIL (rc=2)`

## Canonical Result
- fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- retry1: `STEP09 FAIL (rc=2)`
