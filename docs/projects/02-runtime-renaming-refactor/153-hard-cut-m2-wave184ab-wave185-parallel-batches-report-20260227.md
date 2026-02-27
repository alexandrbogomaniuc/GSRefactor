# Hard-Cut M2 Wave 184A/184B + Wave 185 Report

Date (UTC): 2026-02-27
Wave group: 184A + 184B + 185
Scope: declaration-first migration in `common-gs` (`cache` + `promo.tournaments`) with bounded importer rewires.

## Batch Breakdown
- `W184A`: 6 declaration migrations (`CachesHolder`, `GameList`, `GameListInfo`, `GameListKey`, `HttpSessionCache`, `PingSessionCache`).
- `W184B`: 5 declaration migrations (`ErrorCodes`, `Placeholder`, `TournamentLeaderboard`, `TournamentLeaderboardBuilder`, `TournamentManager`).
- `W185`: integration and validation.

## Stabilization
- No source rollback required.
- Initial bounded rewire map was extended after compile surfaced additional direct static-import dependencies in tournament websocket handlers.
- Applied targeted import/static-import rewires only (no blind/global replacement).
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) outside migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-104909-hardcut-m2-wave184ab-wave185-parallel-batches/`
- Fast gate:
  - rerun4: steps `1-8 PASS`, step `9 FAIL` (`startgame` alias returns `HTTP 502`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`)

## Outcome Metrics
- Scoped declaration migrations retained: `11`.
- Scoped bounded rewires retained: `21`.
- Global tracked declarations/files remaining: `1233` (baseline `2277`, reduced `1044`).
- Hard-cut burndown completion: `45.849802%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `30.731225%`
  - Core total (01+02): `65.365613%`
  - Entire portfolio: `82.682806%`
- ETA refresh: ~`51.0h` (~`6.38` workdays).
