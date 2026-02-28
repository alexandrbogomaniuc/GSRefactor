# W242A/W242B + W243 Evidence (stabilized)

## Scope
- Batch A declarations (`target-batchA.txt`): mp-server kafka dto/handler package migrations.
- Batch B declarations (`target-batchB.txt`): mp-server bots/web rewires plus bounded common-gs stabilization required after local cache invalidation.

## Overlap Proof
- `overlap-metrics.txt`
  - decl overlap: 0
  - rewire overlap: 0
  - cross overlaps: 0

## Validation
- Fast gate batchA rerun1: STEP01-08 PASS, STEP09 FAIL (`rc=2`) (`fast-gate-runner-batchA-rerun1.log`)
- Fast gate batchB rerun1: STEP01-08 PASS, STEP09 FAIL (`rc=2`) (`fast-gate-runner-batchB-rerun1.log`)
- Full matrix rerun1: PRE01-03 PASS, STEP01-08 PASS, STEP09 FAIL (`rc=2`), retry1 FAIL (`rc=2`) (`validation-runner-rerun1.log`, `STEP09-rerun1-retry1.log`)

## Stabilization Notes
- No blind/global replacement was used.
- Common-gs compile stabilization was constrained to files in current dirty scope to recover STEP06 after cache invalidation.
- Smoke failure remains the known external `/startgame` HTTP 502 blocker.
