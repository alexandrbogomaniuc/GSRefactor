# Hard-Cut M2 Wave 264 + Wave 265 Report

Date (UTC): 2026-02-28
Wave group: 264 + 265
Scope: declaration-first migration of `common/transactiondata` core declarations (excluding `ITransactionData`) with bounded consumer rewires.

## Batch Breakdown
- `W264` (Batch A): `11` declaration migrations retained.
- `W265` (Batch B): bounded import rewires + validation.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning stayed thread-limited (`agent thread limit reached`); strict ownership-safe fallback was executed on main.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Stabilization reruns:
  - `rerun1`: `STEP01` compile failure (`ITransactionData` missing imports for moved tracking classes).
  - `rerun2`: `STEP01` compile failure (moved declarations missing explicit import of unmigrated `ITransactionData`).
  - `rerun3`: canonical profile restored.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-071951-hardcut-m2-wave264-wave265-common-transactiondata-core/`
- Fast gate batchA:
  - rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun3: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun3: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `11`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+11`
- Global tracked declarations/files remaining: `576` (baseline `2277`, reduced `1701`).
- Hard-cut burndown completion: `74.703557%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `46.735996%`
  - Core total (01+02): `73.367998%`
  - Entire portfolio: `86.683999%`
- ETA refresh: ~`23.7h` (~`2.97` workdays).
