# W252 + W253 Evidence

## Scope
- Batch A declarations (`target-batchA.txt`): common-gs kafka request DTO package migrations.
- Bounded rewires: direct import updates in corresponding handlers and kafka message services only.

## Overlap Proof
- `overlap-metrics.txt`
  - decl overlap: 0
  - rewire overlap: 0
  - cross overlaps: 0

## Validation
- Fast gate batchA rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`).
- Full matrix rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 FAIL (`rc=2`).

## Stabilization Notes
- Added minimal compatibility imports in moved DTO declarations:
  - `import com.dgphoenix.casino.kafka.dto.KafkaRequest;`
  - `import com.dgphoenix.casino.kafka.dto.BGPlayerDto;` (for `InvitePlayersToPrivateRoomRequest`)
- No blind/global replacement was used.
- Smoke failure remains the known external `/startgame` HTTP 502 blocker.
