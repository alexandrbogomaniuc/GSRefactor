# Evidence: Hard-Cut M2 Wave 228A + 228B + 229

## Scope
- W228A: 20 declaration migrations (`mp-server/thrift-api` thrift classes).
- W228B: 12 declaration migrations (`mp-server/thrift-api` thrift classes).
- W229: integration and canonical validation.

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
- Explorer selected strict-disjoint, declaration-only batches with zero rewires.
- Worker B required one in-file FQCN namespace alignment in owned `TBot.java` (`TBotState` reference update) as part of declaration migration.
- No additional compile stabilization required after worker edits.
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
