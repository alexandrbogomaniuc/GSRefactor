# W230A/W230B + W231 Evidence

## Scope
- Batch A declarations (`target-batchA.txt`): 11 thrift-api files.
- Batch B declarations (`target-batchB.txt`): 11 kafka/dto files.
- Retained rewires:
  - Batch A: 2 (`rewires-batchA-all.txt`)
  - Batch B: 0 (`rewires-batchB-all.txt`)

## Overlap Proof
- `overlap-metrics.txt`
  - decl overlap: 0
  - rewire overlap: 0
  - cross overlaps: 0

## Validation
- Fast gate batchA rerun4: STEP01-08 PASS, STEP09 FAIL (`rc=2`) (`fast-gate-runner-batchA-rerun4.log`)
- Fast gate batchB rerun4: STEP01-08 PASS, STEP09 FAIL (`rc=2`) (`fast-gate-runner-batchB-rerun4.log`)
- Full matrix rerun1: PRE01-03 PASS, STEP01-08 PASS, STEP09 FAIL (`rc=2`), retry1 FAIL (`rc=2`) (`validation-runner-rerun1.log`)

## Stabilization Notes
- Attempted external rewire set for batchB was discarded after causing unnecessary compile drift in `common-gs`; retained declaration-only batchB plan.
- No blind/global replacement performed.
- Pre-existing unrelated local files were preserved outside commit scope.
