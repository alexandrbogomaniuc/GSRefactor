# Evidence: Hard-Cut M2 Wave 222A + 222B + 223

## Scope
- W222A: 12 declaration migrations (`game-server/common-gs/kafka/handler`).
- W222B: 12 declaration migrations (`common-promo`).
- W223: bounded integration stabilization + canonical validation.

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
- Fast gate batchA rerun1 failed at `STEP04` after moving promo declarations because moved files lost same-package visibility to legacy promo types.
- Applied minimal import-only stabilization in moved files:
  - added `import com.dgphoenix.casino.common.promo.*;` in moved `W222B` promo declarations.
  - added explicit `import com.dgphoenix.casino.kafka.handler.KafkaOuterRequestHandler;` in moved `W222A` handlers.
- No blind/global replacement performed.

## Validation Runs
- Fast gate batchA:
  - rerun1: `STEP04 FAIL`
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
