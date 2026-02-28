# W254 + W255 Evidence

## Scope
- Batch A declarations (`target-batchA.txt`): common-gs kafka round/private-room DTO package migrations.
- Bounded rewires: direct import updates in matching handlers/lock helper only.

## Overlap Proof
- `overlap-metrics.txt`
  - decl overlap: 0
  - rewire overlap: 0
  - cross overlaps: 0

## Validation
- Fast gate batchA rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`).
- Full matrix rerun2 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 FAIL (`rc=2`).

## Stabilization Notes
- Added minimal compatibility imports in moved DTO declarations to retain compile visibility with unmigrated DTO types:
  - `KafkaRequest`, `BasicKafkaResponse`, `RoundPlayerDto`, `StartNewRoundResponseDto`
  - `CurrencyRateDto`, `BGUpdatePrivateRoomRequest`, `RMSRoomDto`
- No blind/global replacement was used.
- Smoke failure remains the known external `/startgame` HTTP 502 blocker.
