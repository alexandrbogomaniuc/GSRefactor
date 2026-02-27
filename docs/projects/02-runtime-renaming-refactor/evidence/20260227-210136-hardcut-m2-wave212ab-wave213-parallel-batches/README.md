# Evidence: Hard-Cut M2 Wave 212A/212B + 213

## Scope
- W212A: 10 declarations
- W212B: 10 declarations
- W213: bounded rewires + validation

## Planning Artifacts
- `target-batchA.txt`
- `target-batchB.txt`
- `rewires-batchA-all.txt`
- `rewires-batchB-all.txt`
- `overlap-metrics.txt`
- `package-checks.txt`

## Validation Runs
- Initial runs:
  - `fast-gate-runner-batchA-rerun1.log`
  - `fast-gate-runner-batchB-rerun1.log`
  - `validation-runner-rerun1.log`
- Stabilization reruns:
  - `fast-gate-runner-batchA-rerun2.log` ... `fast-gate-runner-batchA-rerun5.log`
  - `fast-gate-runner-batchB-rerun2.log` ... `fast-gate-runner-batchB-rerun5.log`
  - `validation-runner-rerun2.log` ... `validation-runner-rerun5.log`
  - `stabilization-prewarm-rerun2.log` ... `stabilization-prewarm-rerun5.log`

## Canonical Evidence
- Fast gate batchA canonical: `fast-gate-status-batchA-rerun5.txt`
- Fast gate batchB canonical: `fast-gate-status-batchB-rerun5.txt`
- Full matrix canonical: `validation-status-rerun5.txt`
- Full-matrix STEP09 retry: `STEP09-rerun5-retry1.log`

## Canonical Result
- fast gate batchA: `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- fast gate batchB: `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- full matrix: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL (rc=2)`
- retry1: `STEP09 FAIL (rc=2)`
