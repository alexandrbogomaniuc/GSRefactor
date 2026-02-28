# Hard-Cut M2 Wave 252 + Wave 253 Report

Date (UTC): 2026-02-28
Wave group: 252 + 253
Scope: declaration-first migration of common-gs Kafka request DTO suite with bounded import rewires.

## Batch Breakdown
- `W252`: `11` declaration migrations retained (`AddMQReservedNicknamesRequest`, `CheckBuyInRequest`, `CloseFRBonusAndSessionRequest`, `CloseGameSessionRequest`, `GetBalanceRequest`, `GetCrashGamesSettingsRequest`, `GetDetailedPlayerInfo2Request`, `InvitePlayersToPrivateRoomRequest`, `LeaveMultiPlayerLobbyRequest`, `PingRequest`, `SessionTouchRequest`).
- `W253`: integration validation.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning stayed thread-limited; strict ownership-safe fallback was executed on main.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Bounded rewires only (no blind/global replacement):
  - declaration package moves `com.dgphoenix -> com.abs` for selected DTOs.
  - direct import rewires in matching handlers plus kafka messaging services for `PingRequest`.
  - compile compatibility retained through explicit `KafkaRequest` import in moved DTO declarations and `BGPlayerDto` import in `InvitePlayersToPrivateRoomRequest`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-055222-hardcut-m2-wave252-wave253-kafka-dto-request-suite/`
- Fast gate batchA:
  - rerun1 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from prior checkpoint:
  - `com.dgphoenix -> com.abs`: `11`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+11`
- Global tracked declarations/files remaining: `624` (baseline `2277`, reduced `1653`).
- Hard-cut burndown completion: `72.595520%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `45.873215%`
  - Core total (01+02): `72.936608%`
  - Entire portfolio: `86.468304%`
- ETA refresh: ~`25.8h` (~`3.23` workdays).
