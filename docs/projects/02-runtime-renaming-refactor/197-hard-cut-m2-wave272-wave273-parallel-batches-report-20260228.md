# Hard-Cut M2 Wave 272 + Wave 273 Report

Date (UTC): 2026-02-28
Wave group: 272 + 273
Scope: declaration-first migration of low-risk `utils/common` surfaces (`common.util`, `common.cache`, `common.lock`) with bounded consumer rewires.

## Batch Breakdown
- `W272` (Batch A): `11` declaration migrations retained.
- `W273` (Batch B): bounded import/consumer rewires + validation.
- Deferred from initial candidate list: `CommonExecutorService` (kept on `com.dgphoenix` for dedicated follow-up due constructor-type fanout).

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning stayed thread-limited (`agent thread limit reached`); strict ownership-safe fallback was executed on main.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Stabilization reruns:
  - `rerun1`: `STEP01`/`PRE01` failed due moved util declarations losing same-package visibility to unmigrated helpers (`CollectionUtils`, `ExecutorUtils`, `FastByteArrayOutputStream`).
  - `rerun2`: `STEP01` failed due temporary package mismatch against `LocalAccumulatedStatistics`; package alignment restored to `HEAD`.
  - `rerun3`: `STEP06` failed on `CommonExecutorService` type fanout (`com.abs` vs `com.dgphoenix`) in `common-gs` constructor wiring.
  - `rerun4`: after deferring `CommonExecutorService`, canonical profile restored.
  - post-validation residual scan found two legacy JSP imports for `StreamUtils`; patched and revalidated on `rerun5`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-082447-hardcut-m2-wave272-wave273-utils-common-util-cache-lock/`
- Fast gate batchA:
  - rerun5: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun5: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun5: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `11`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+11`
- Global tracked declarations/files remaining: `540` (baseline `2277`, reduced `1737`).
- Hard-cut burndown completion: `76.284585%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `47.223096%`
  - Core total (01+02): `73.611548%`
  - Entire portfolio: `86.805774%`
- ETA refresh: ~`22.1h` (~`2.76` workdays).
