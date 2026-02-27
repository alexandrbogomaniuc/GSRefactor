# Hard-Cut M2 Wave 194A/194B + Wave 195 Report

Date (UTC): 2026-02-27
Wave group: 194A + 194B + 195
Scope: declaration-first migration in `common.client` + `websocket.tournaments` with bounded importer/class-string rewires.

## Batch Breakdown
- `W194A`: 4 declaration migrations (`AbstractLoggableClient`, `IJsonCWClient`, `IJsonRequest`, `LoggableWithResponseCodeClient`).
- `W194B`: 9 declaration migrations (`GsonClassSerializer`, `GsonFactory`, `IMessageHandler`, `ISocketClient`, `TournamentClient`, `TournamentWebSocket`, `TournamentWebSocketMessageListener`, `TournamentWebSocketServlet`, `TournamentWebSocketSessionsController`).
- `W195`: integration and validation.

## Stabilization
- Parallel-mode intent executed as `1 worker + main` fallback due agent thread-cap while preserving strict non-overlap ownership.
- No source rollback required.
- Applied targeted Java importer rewires plus one bounded `web.xml` class-string update for `TournamentWebSocketServlet`.
- No blind/global replacement performed.
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) outside migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-123332-hardcut-m2-wave194ab-wave195-parallel-batches/`
- Fast gate:
  - rerun1: `STEP01-08 PASS`, `STEP09 FAIL` (`startgame` alias returns `HTTP 502`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`)

## Outcome Metrics
- Scoped declaration migrations retained: `13`.
- Scoped bounded rewires retained: `23`.
- Global tracked declarations/files remaining: `1165` (baseline `2277`, reduced `1112`).
- Hard-cut burndown completion: `48.836188%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `31.104523%`
  - Core total (01+02): `65.552261%`
  - Entire portfolio: `82.776131%`
- ETA refresh: ~`48.2h` (~`6.02` workdays).
