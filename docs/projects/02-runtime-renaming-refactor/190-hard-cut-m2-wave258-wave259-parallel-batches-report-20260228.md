# Hard-Cut M2 Wave 258 + Wave 259 Report

Date (UTC): 2026-02-28
Wave group: 258 + 259
Scope: declaration-first migration of common exception package declarations with bounded consumer rewires.

## Batch Breakdown
- `W258`: `10` declaration migrations retained (`AccountLockedException`, `AssignServerException`, `CurrencyMismatchException`, `CurrencyNotFoundException`, `GameAlreadyStartedException`, `GameSessionNotStartedException`, `InvalidCurrencyRateException`, `MismatchSessionException`, `ServerNotFoundException`, `UnknownCurrencyException`).
- `W259`: integration validation.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning stayed thread-limited (`agent thread limit reached`); strict ownership-safe fallback was executed on main.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Bounded rewires only (no blind/global replacement):
  - declaration package moves `com.dgphoenix -> com.abs` for selected exception declarations.
  - direct import rewires in `DomainSession`, `SessionHelper`, `CurrencyCache`, `CurrencyManager`, `AbstractPlayerSessionManager`, `CurrencyManagerTest`, `GameHistoryListAction`, `GameHistoryServlet`.
  - compile compatibility retained via explicit imports of unmigrated base classes in moved exceptions (`CommonException`, `AccountException`, `ObjectNotFoundException`).
- Full-matrix rerun2 stalled in `STEP09-retry1`; rerun3 added timeout-bounded `STEP09` execution to keep evidence deterministic without changing application code.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-063702-hardcut-m2-wave258-wave259-common-exceptions/`
- Fast gate batchA:
  - rerun1: `STEP01-05 PASS`, `STEP06 FAIL`
  - rerun2: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
  - rerun3 (canonical): `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP06 FAIL`
  - rerun2: stalled during `STEP09-retry1`
  - rerun3 (canonical): `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `10`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+10`
- Global tracked declarations/files remaining: `599` (baseline `2277`, reduced `1678`).
- Hard-cut burndown completion: `73.693456%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `46.424793%`
  - Core total (01+02): `73.212396%`
  - Entire portfolio: `86.606198%`
- ETA refresh: ~`24.7h` (~`3.09` workdays).
