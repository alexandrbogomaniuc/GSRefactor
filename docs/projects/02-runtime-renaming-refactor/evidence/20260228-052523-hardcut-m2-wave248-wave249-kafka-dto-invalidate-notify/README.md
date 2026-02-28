# W248 + W249 Evidence

## Scope
- Batch A declarations (`target-batchA.txt`): common-gs kafka invalidate/notify/refresh request DTO package migrations.
- Bounded rewires: in-service handlers + `RemoteCallHelper` import alignment only.

## Overlap Proof
- `overlap-metrics.txt`
  - decl overlap: 0
  - rewire overlap: 0
  - cross overlaps: 0

## Validation
- Fast gate batchA rerun1: `STEP06 FAIL` (`rc=1`) due moved DTOs losing same-package visibility to `KafkaRequest`.
- Fast gate batchA rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`).
- Full matrix rerun1: `PRE01-03 PASS`, `STEP06 FAIL` (`rc=1`).
- Full matrix rerun2 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 FAIL (`rc=2`).

## Stabilization Notes
- Added minimal compatibility import in moved DTO declarations: `import com.dgphoenix.casino.kafka.dto.KafkaRequest;`
- No blind/global replacement was used.
- Smoke failure remains the known external `/startgame` HTTP 502 blocker.
