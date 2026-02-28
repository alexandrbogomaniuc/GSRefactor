# Hard-Cut M2 Wave 280 + Wave 281 Report

Date (UTC): 2026-02-28
Wave group: 280 + 281
Scope: declaration-first migration for low-risk `common/web` core declarations with bounded compatibility rewires.

## Batch Breakdown
- `W280` (Batch A): `3` declaration migrations retained (`AbstractLobbyRequest`, `BasicGameServerResponse`, `CommonStatus`).
- `W281` (Batch B): `3` declaration migrations retained (`JsonResult`, `MobileDetector`, `BaseAction`).
- Total retained declaration migrations: `6`.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning remained thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- Added bounded compile/runtime compatibility rewires for moved `common.web` types across `common-gs` and `web-gs` consumers (Java + JSP import alignment), with no declaration rollback.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-102006-hardcut-m2-wave280-wave281-common-web-core/`
- Fast gate batchA:
  - rerun1: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun1: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `6`
  - bounded rewires/stabilization regressions (`com.abs -> com.dgphoenix` declarations): `0`
  - net tracked declaration delta: `+6`
- Global tracked declarations/files remaining: `511` (baseline `2277`, reduced `1766`).
- Hard-cut burndown completion: `77.558191%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `47.615472%`
  - Core total (01+02): `73.807736%`
  - Entire portfolio: `86.903868%`
- ETA refresh: ~`21.0h` (~`2.62` workdays).
