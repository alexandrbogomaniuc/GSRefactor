# W232A/W232B + W233 Evidence

## Scope
- Batch A declarations (`target-batchA.txt`): 10 kafka/dto files.
- Batch B declarations (`target-batchB.txt`): 10 kafka/dto files.
- Retained rewires:
  - Batch A: 0
  - Batch B: 0

## Overlap Proof
- `overlap-metrics.txt`
  - decl overlap: 0
  - rewire overlap: 0
  - cross overlaps: 0

## Validation
- Fast gate batchA rerun1: STEP01-08 PASS, STEP09 FAIL (`rc=2`) (`fast-gate-runner-batchA-rerun1.log`)
- Fast gate batchB rerun1: STEP01-08 PASS, STEP09 FAIL (`rc=2`) (`fast-gate-runner-batchB-rerun1.log`)
- Full matrix rerun1: PRE01-03 PASS, STEP01-08 PASS, STEP09 FAIL (`rc=2`), retry1 FAIL (`rc=2`) (`validation-runner-rerun1.log`)

## Stabilization Notes
- Declaration-first execution required no additional compile stabilization.
- No blind/global replacement performed.
- Pre-existing unrelated local files were preserved outside commit scope.
