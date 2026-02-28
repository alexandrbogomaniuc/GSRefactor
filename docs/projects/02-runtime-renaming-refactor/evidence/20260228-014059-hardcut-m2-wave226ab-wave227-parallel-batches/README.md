# Evidence: Hard-Cut M2 Wave 226A + 226B + 227

## Scope
- W226A: 14 declaration migrations (`mp-server/thrift-api` thrift classes) + 1 bounded rewire (`MQThriftService`).
- W226B: 14 declaration migrations (`mp-server/thrift-api` thrift classes) + 1 bounded rewire (`GameServerThriftService`).
- W227: bounded integration + canonical validation.

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
- Refined planning selected strict-disjoint low-rewire batches (`1` rewire per batch, no `web-gs` rewires).
- No additional compile stabilization was required after worker execution.
- Worker-scoped compatibility rewires were minimal and bounded to the two owned thrift service files.
- No blind/global replacement performed.

## Validation Runs
- Fast gate batchA:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
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
