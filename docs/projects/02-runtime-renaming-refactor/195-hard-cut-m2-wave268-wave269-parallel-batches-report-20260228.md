# Hard-Cut M2 Wave 268 + Wave 269 Report

Date (UTC): 2026-02-28
Wave group: 268 + 269
Scope: declaration-first migration of follow-up `sb-utils/common/exception` declarations with bounded consumer rewires.

## Batch Breakdown
- `W268` (Batch A): `7` declaration migrations retained.
- `W269` (Batch B): bounded import rewires + validation.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning stayed thread-limited (`agent thread limit reached`); strict ownership-safe fallback was executed on main.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Stabilization reruns:
  - `rerun1`: fast gates failed at `STEP01` because `STEP01` resolves updated `com.abs` exception imports before the refreshed `sb-utils` artifact install; full matrix failed at `STEP06` due mixed old/new exception types in `GameServer` and `StartGameSessionHelper`.
  - patched `STEP06` drift with explicit moved-type imports (`GameException`, `MaintenanceModeException`, `StartParameters`) in `common-gs`.
  - `rerun2`: canonical profile restored.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-075049-hardcut-m2-wave268-wave269-sbutils-common-exception-followup/`
- Fast gate batchA:
  - rerun2: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun2: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun2: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `7`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+7`
- Global tracked declarations/files remaining: `558` (baseline `2277`, reduced `1719`).
- Hard-cut burndown completion: `75.494071%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `46.979546%`
  - Core total (01+02): `73.489773%`
  - Entire portfolio: `86.744886%`
- ETA refresh: ~`22.9h` (~`2.86` workdays).
