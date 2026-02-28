# Hard-Cut M2 Wave 256 + Wave 257 Report

Date (UTC): 2026-02-28
Wave group: 256 + 257
Scope: declaration-first migration of common-gs Kafka buy-in/friends/status DTO package declarations with bounded consumer rewires.

## Batch Breakdown
- `W256`: `11` declaration migrations retained (`BuyInRequest`, `BuyInResultDto`, `CloseFRBonusResultDto`, `CrashGameSettingsResponseDto`, `DetailedPlayerInfo2Dto`, `GetFriendsRequest`, `GetFriendsResponseDto`, `GetMQDataRequest`, `NotifyOnServerStatusesUpdatedRequest`, `RoundInfoResultDto`, `StringResponseDto`).
- `W257`: integration validation.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning stayed thread-limited (`agent thread limit reached`); strict ownership-safe fallback was executed on main.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Bounded rewires only (no blind/global replacement):
  - declaration package moves `com.dgphoenix -> com.abs` for selected DTOs.
  - direct import rewires in matching handlers, MQ/MP service surfaces, status thread, and DTO request files importing `RoundInfoResultDto`.
  - compile compatibility retained through explicit imports of unmigrated DTO dependencies (`KafkaRequest`, `BasicKafkaResponse`, `BGFriendDto`, `CrashGameSettingDto`, `BattlegroundRoundInfoDto`, `FRBonusDto`, `CashBonusDto`, `TournamentInfoDto`, `BattlegroundInfoDto`).

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-061715-hardcut-m2-wave256-wave257-kafka-dto-buyin-friends-status/`
- Fast gate batchA:
  - rerun1: `STEP01-05 PASS`, `STEP06 FAIL`
  - rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP06 FAIL`
  - rerun2 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `11`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+11`
- Global tracked declarations/files remaining: `609` (baseline `2277`, reduced `1668`).
- Hard-cut burndown completion: `73.254282%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `46.289487%`
  - Core total (01+02): `73.144743%`
  - Entire portfolio: `86.572372%`
- ETA refresh: ~`25.1h` (~`3.14` workdays).
