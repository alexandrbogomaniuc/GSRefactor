# W246A/W246B + W247 Evidence

## Scope
- Batch A declarations (`target-batchA.txt`): common-gs kafka bot-config request DTO package migrations.
- Batch B declarations (`target-batchB.txt`): common-gs kafka bot-config/private-room response DTO package migrations.

## Overlap Proof
- `overlap-metrics.txt`
  - decl overlap: 0
  - rewire overlap: 0
  - cross overlaps: 0

## Validation
- Fast gate batchA rerun1: STEP01-08 PASS, STEP09 FAIL (`rc=2`) (`fast-gate-runner-batchA-rerun1.log`).
- Fast gate batchB rerun1: STEP01-08 PASS, STEP09 FAIL (`rc=2`) (`fast-gate-runner-batchB-rerun1.log`).
- Full matrix rerun1: PRE01-03 PASS, STEP01-08 PASS, STEP09 FAIL (`rc=2`), retry1 FAIL (`rc=2`) (`validation-runner-rerun1.log`, `STEP09-rerun1-retry1.log`).

## Stabilization Notes
- No blind/global replacement was used.
- Kept rewires bounded to direct imports and wildcard-consumer alignment in `KafkaRequestMultiPlayer`/`BattlegroundService`.
- Smoke failure remains the known external `/startgame` HTTP 502 blocker.
