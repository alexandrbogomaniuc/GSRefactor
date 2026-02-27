# Evidence: Hard-Cut M2 Wave 214A/214B + 215

## Scope
- W214A: 10 declarations
- W214B: 10 declarations
- W215: bounded rewires + validation

## Planning Artifacts
- `target-batchA.txt`
- `target-batchB.txt`
- `rewires-batchA-all.txt`
- `rewires-batchB-all.txt`
- `overlap-metrics.txt`
- `package-checks.txt`

## Validation Runs
- rerun1: initial execution
- rerun2: STEP01 fix applied, rerun
- rerun3: STEP06 stabilization rerun
- rerun4: non-canonical (runner used `STEP07=gs-api`, discarded)
- rerun5: canonical (`STEP07=web-gs`)

## Canonical Evidence
- Fast gate batchA canonical: `fast-gate-status-batchA-rerun5.txt`
- Fast gate batchB canonical: `fast-gate-status-batchB-rerun5.txt`
- Full matrix canonical: `validation-status-rerun5.txt`
- STEP09 retry log: `STEP09-rerun5-retry1.log`

## Canonical Result
- fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- retry1: `STEP09 FAIL (rc=2)`
