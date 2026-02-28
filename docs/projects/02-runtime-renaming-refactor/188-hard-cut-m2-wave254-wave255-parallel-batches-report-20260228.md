# Hard-Cut M2 Wave 254 + Wave 255 Report

Date (UTC): 2026-02-28
Wave group: 254 + 255
Scope: declaration-first migration of common-gs Kafka round/private-room DTO package declarations with bounded consumer rewires.

## Batch Breakdown
- `W254`: `11` declaration migrations retained (`RefundBuyInRequest`, `RemoveMQReservedNicknamesRequest`, `StartNewRoundForManyPlayersRequest`, `StartNewRoundForManyPlayersResponseDto`, `StartNewRoundRequest`, `UpdateCurrencyRatesRequestResponse`, `UpdatePlayersStatusInPrivateRoomRequest`, `LongResponseDto`, `NotifyPrivateRoomWasDeactivatedRequest`, `PushOnlineRoomsPlayersRequest`, `RemoteUnlockRequest`).
- `W255`: integration validation.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning stayed thread-limited; strict ownership-safe fallback was executed on main.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Bounded rewires only (no blind/global replacement):
  - declaration package moves `com.dgphoenix -> com.abs` for selected DTOs.
  - direct import rewires in matching handlers and `RemoteUnlocker`.
  - compile compatibility retained through explicit imports of unmigrated DTO dependencies (`KafkaRequest`, `BasicKafkaResponse`, `RoundPlayerDto`, `StartNewRoundResponseDto`, `CurrencyRateDto`, `BGUpdatePrivateRoomRequest`, `RMSRoomDto`).

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-060140-hardcut-m2-wave254-wave255-kafka-dto-round-private-room/`
- Fast gate batchA:
  - rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun2 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from prior checkpoint:
  - `com.dgphoenix -> com.abs`: `11`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+11`
- Global tracked declarations/files remaining: `613` (baseline `2277`, reduced `1664`).
- Hard-cut burndown completion: `73.078612%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `46.178481%`
  - Core total (01+02): `73.089241%`
  - Entire portfolio: `86.544621%`
- ETA refresh: ~`25.3h` (~`3.16` workdays).
