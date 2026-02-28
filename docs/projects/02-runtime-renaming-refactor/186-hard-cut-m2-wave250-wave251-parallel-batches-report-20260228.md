# Hard-Cut M2 Wave 250 + Wave 251 Report

Date (UTC): 2026-02-28
Wave group: 250 + 251
Scope: declaration-first migration of common-gs Kafka status/server-info DTOs with bounded import rewires.

## Batch Breakdown
- `W250`: `11` declaration migrations retained (`Send*Request`, `UpdateStubBalanceByExternalUserIdRequest`, `GameServerInfo*`, `GetGameServersInfoRequest`, `GetOnlineStatus*`, `GetExternalAccountIds*`).
- `W251`: integration validation.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning stayed thread-limited; strict ownership-safe fallback was executed on main.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Bounded rewires only (no blind/global replacement):
  - declaration package moves `com.dgphoenix -> com.abs` for selected DTOs.
  - direct import rewires in affected handlers and `RemoteCallHelper`.
  - compile stabilization: explicit compatibility imports for `BGOnlinePlayerDto` and `PromoNotificationType` in moved DTO declarations.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-053950-hardcut-m2-wave250-wave251-kafka-dto-status-server-info/`
- Fast gate batchA:
  - rerun1: `STEP06 FAIL` (`rc=1`)
  - rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP06 FAIL` (`rc=1`)
  - rerun2 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from prior checkpoint:
  - `com.dgphoenix -> com.abs`: `11`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+11`
- Global tracked declarations/files remaining: `635` (baseline `2277`, reduced `1642`).
- Hard-cut burndown completion: `72.112429%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `45.567947%`
  - Core total (01+02): `72.783974%`
  - Entire portfolio: `86.391987%`
- ETA refresh: ~`26.3h` (~`3.29` workdays).
