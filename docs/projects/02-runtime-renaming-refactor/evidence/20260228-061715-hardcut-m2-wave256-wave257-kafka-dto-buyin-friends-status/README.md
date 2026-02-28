# W256 + W257 Evidence

## Scope
- Batch A declarations (`target-batchA.txt`): common-gs kafka buy-in/friends/status DTO package migrations.
- Bounded rewires: direct import updates in matching handlers, MQ/MP service surfaces, and DTO files importing `RoundInfoResultDto`.

## Overlap Proof
- `overlap-metrics.txt`
  - decl overlap: 0
  - rewire overlap: 0
  - cross overlaps: 0

## Validation
- Fast gate batchA rerun1: `STEP01-05 PASS`, `STEP06 FAIL` (compile drift).
- Fast gate batchA rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`).
- Full matrix rerun1: `PRE01-03 PASS`, `STEP06 FAIL`.
- Full matrix rerun2 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 FAIL (`rc=2`).

## Stabilization Notes
- Added minimal compatibility imports in moved DTO declarations for unmigrated DTO dependencies:
  - `KafkaRequest`, `BasicKafkaResponse`, `BGFriendDto`, `CrashGameSettingDto`, `BattlegroundRoundInfoDto`, `FRBonusDto`, `CashBonusDto`, `TournamentInfoDto`, `BattlegroundInfoDto`.
- No blind/global replacement was used.
- Smoke failure remains the known external `/startgame` HTTP 502 blocker.
