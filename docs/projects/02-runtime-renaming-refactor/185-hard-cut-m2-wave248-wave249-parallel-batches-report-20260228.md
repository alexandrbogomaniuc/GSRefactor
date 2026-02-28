# Hard-Cut M2 Wave 248 + Wave 249 Report

Date (UTC): 2026-02-28
Wave group: 248 + 249
Scope: declaration-first migration of common-gs Kafka invalidate/notify/refresh request DTOs with bounded import rewires.

## Batch Breakdown
- `W248`: `11` declaration migrations retained (`Invalidate*Request`, `RefreshConfigRequest`, `NotifyPromoCampaign*Request`, `NotifySessionClosedRequest`).
- `W249`: integration validation.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning stayed thread-limited; strict ownership-safe fallback was executed on main.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Bounded rewires only (no blind/global replacement):
  - moved DTO package declarations `com.dgphoenix -> com.abs`.
  - import rewires in in-service handlers and `RemoteCallHelper` for moved DTOs.
  - compile stabilization: added explicit `KafkaRequest` compatibility import in moved DTO declarations.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-052523-hardcut-m2-wave248-wave249-kafka-dto-invalidate-notify/`
- Fast gate batchA:
  - rerun1: `STEP06 FAIL` (`rc=1`, missing `KafkaRequest` import after package move)
  - rerun2 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP06 FAIL` (`rc=1`)
  - rerun2 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from prior checkpoint:
  - `com.dgphoenix -> com.abs`: `11`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+11`
- Global tracked declarations/files remaining: `646` (baseline `2277`, reduced `1631`).
- Hard-cut burndown completion: `71.629337%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `45.262679%`
  - Core total (01+02): `72.631340%`
  - Entire portfolio: `86.315670%`
- ETA refresh: ~`26.8h` (~`3.36` workdays).
