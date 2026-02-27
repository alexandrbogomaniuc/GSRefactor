# Hard-Cut M2 Wave 190A/190B + Wave 191 Report

Date (UTC): 2026-02-27
Wave group: 190A + 190B + 191
Scope: declaration-first migration in `gs.managers.game.settings` + `gs.managers.game.engine/event/room` with bounded importer rewires.

## Batch Breakdown
- `W190A`: 7 declaration migrations (`DynamicCoinManager`, `GameSettingsManager`, `GamesLevelContext`, `GamesLevelHelper`, `DynamicCoinManagerTest`, `GameSettingsManagerTest`, `GamesLevelHelperTest`).
- `W190B`: 8 declaration migrations (`AbstractMPGameEngine`, `AbstractSPGameEngine`, `GameEngineManager`, `IGameEngine`, `IGameEventProcessor`, `IGameEvent`, `IRoom`, `RoomManager`).
- `W191`: integration and validation.

## Stabilization
- No source rollback required.
- Corrected one explorer path mismatch during execution (`IGameEventProcessor` is in `common-gs`, not `sb-utils`) before validation.
- Applied targeted importer/static-import rewires and one bounded JSP import rewire.
- No blind/global replacement performed.
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) outside migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-115053-hardcut-m2-wave190ab-wave191-parallel-batches/`
- Fast gate:
  - rerun1: steps `1-8 PASS`, step `9 FAIL` (`startgame` alias returns `HTTP 502`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`)

## Outcome Metrics
- Scoped declaration migrations retained: `15`.
- Scoped bounded rewires retained: `9`.
- Global tracked declarations/files remaining: `1194` (baseline `2277`, reduced `1083`).
- Hard-cut burndown completion: `47.562582%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `30.945322%`
  - Core total (01+02): `65.472661%`
  - Entire portfolio: `82.736331%`
- ETA refresh: ~`49.4h` (~`6.17` workdays).
