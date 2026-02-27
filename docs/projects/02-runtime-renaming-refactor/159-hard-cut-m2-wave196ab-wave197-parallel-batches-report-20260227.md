# Hard-Cut M2 Wave 196A/196B + Wave 197 Report

Date (UTC): 2026-02-27
Wave group: 196A + 196B + 197
Scope: declaration-first migration in `websocket` + `gs.managers.payment.wallet.v3` with bounded importer/class-string rewires.

## Batch Breakdown
- `W196A`: 7 declaration migrations (`IWebSocketSessionsController`, `IWebSocketSessionsListener`, `SessionWrapper`, `WebSocketImpl`, `WebSocketMessageCallback`, `WebSocketServletImpl`, `WebSocketSessionsController`).
- `W196B`: 4 declaration migrations (`AuthActionType`, `CommonWalletAuthResult`, `GTBetsCommonWalletAuthResult`, `ICommonWalletClient`).
- `W197`: integration and validation.

## Stabilization
- Parallel-mode intent executed as `1 worker + main` fallback due agent thread-cap while preserving strict non-overlap ownership.
- No source rollback required.
- Applied targeted Java importer rewires, bounded `web.xml` class-string update for `WebSocketServletImpl`, and one bounded Java FQCN rewire in wallet-v4 interface extension.
- No blind/global replacement performed.
- Kept unrelated local runtime config change (`web-gs/src/main/resources/cluster-hosts.properties`) outside migration commit scope.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260227-125224-hardcut-m2-wave196ab-wave197-parallel-batches/`
- Fast gate:
  - rerun1: `STEP01-08 PASS`, `STEP09 FAIL` (`startgame` alias returns `HTTP 502`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`, launch alias `HTTP 502`)

## Outcome Metrics
- Scoped declaration migrations retained: `11`.
- Scoped bounded rewires retained: `34`.
- Global tracked declarations/files remaining: `1154` (baseline `2277`, reduced `1123`).
- Hard-cut burndown completion: `49.319280%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `31.164910%`
  - Core total (01+02): `65.582455%`
  - Entire portfolio: `82.791227%`
- ETA refresh: ~`47.7h` (~`5.97` workdays).
