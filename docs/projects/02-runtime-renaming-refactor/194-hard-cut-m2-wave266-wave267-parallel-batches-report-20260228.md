# Hard-Cut M2 Wave 266 + Wave 267 Report

Date (UTC): 2026-02-28
Wave group: 266 + 267
Scope: declaration-first migration of low-risk `sb-utils/common/exception` declarations with bounded consumer rewires.

## Batch Breakdown
- `W266` (Batch A): `11` declaration migrations retained.
- `W267` (Batch B): bounded import rewires + validation.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning stayed thread-limited (`agent thread limit reached`); strict ownership-safe fallback was executed on main.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Stabilization reruns:
  - `rerun1`: `STEP03` compile failure (moved exceptions extending unmigrated `CommonException` without explicit import).
  - `rerun2`: canonical profile restored after adding minimal compatibility imports for `CommonException` in moved declarations.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-073605-hardcut-m2-wave266-wave267-sbutils-common-exception-lowrisk/`
- Fast gate batchA:
  - rerun2: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun2: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun2: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `11`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+11`
- Global tracked declarations/files remaining: `565` (baseline `2277`, reduced `1712`).
- Hard-cut burndown completion: `75.186649%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `46.884832%`
  - Core total (01+02): `73.442416%`
  - Entire portfolio: `86.721208%`
- ETA refresh: ~`23.3h` (~`2.91` workdays).
