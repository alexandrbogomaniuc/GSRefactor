# W244A/W244B + W245 Evidence

## Scope
- Batch A declarations (`target-batchA.txt`): common-gs kafka round/payment DTO package migrations.
- Batch B declarations (`target-batchB.txt`): common-gs kafka sit-in/sit-out DTO package migrations.

## Overlap Proof
- `overlap-metrics.txt`
  - decl overlap: 0
  - rewire overlap: 0
  - cross overlaps: 0

## Validation
- Fast gate batchA rerun1: STEP01-05 PASS, STEP06 FAIL (`cluster.properties` unresolved in runner shell).
- Fast gate batchA rerun2 (canonical): STEP01-08 PASS, STEP09 FAIL (`rc=2`) (`fast-gate-runner-batchA-rerun2.log`).
- Fast gate batchB rerun1: STEP01-05 PASS, STEP06 FAIL (duplicate FQCN drift on `SitOutRequest2`).
- Fast gate batchB rerun2 (canonical): STEP01-08 PASS, STEP09 FAIL (`rc=2`) (`fast-gate-runner-batchB-rerun2.log`).
- Full matrix rerun1 (canonical): PRE01-03 PASS, STEP01-08 PASS, STEP09 FAIL (`rc=2`), retry1 FAIL (`rc=2`) (`validation-runner-rerun1.log`, `STEP09-rerun1-retry1.log`).

## Stabilization Notes
- No blind/global replacement was used.
- Added bounded STEP06/STEP07 runner property fix: `-Dcluster.properties=local/local-machine.properties`.
- Resolved compile drift by reverting one conflicting declaration (`SitOutRequest2`) to legacy package due duplicate FQCN with mp-server kafka DTO.
- Smoke failure remains the known external `/startgame` HTTP 502 blocker.
