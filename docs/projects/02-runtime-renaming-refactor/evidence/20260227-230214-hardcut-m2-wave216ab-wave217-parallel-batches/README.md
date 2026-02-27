# Evidence: Hard-Cut M2 Wave 216A/216B + 217

## Scope
- W216A: 10 planned declarations
- W216B: 10 planned declarations
- W217: bounded rewires + validation

## Planning Artifacts
- `target-batchA.txt`
- `target-batchB.txt`
- `rewires-batchA-all.txt`
- `rewires-batchB-all.txt`
- `overlap-metrics.txt`
- `package-checks.txt`
- `package-checks-after.txt`

## Validation Runs
- batchA rerun1-2: `STEP03` compile drift
- batchA rerun3-9: `STEP06` compile drift
- batchA rerun10: canonical fast gate
- batchB rerun1: canonical fast gate
- full matrix rerun1: canonical (`STEP07=web-gs`)

## Canonical Evidence
- Fast gate batchA canonical: `fast-gate-status-batchA-rerun10.txt`
- Fast gate batchB canonical: `fast-gate-status-batchB-rerun1.txt`
- Full matrix canonical: `validation-status-rerun1.txt`
- PRE status: `prewarm-status-rerun1.txt`
- STEP09 retry log: `STEP09-rerun1-retry1.log`

## Canonical Result
- fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- retry1: `STEP09 FAIL (rc=2)`
