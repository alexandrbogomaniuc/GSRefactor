# Hard-Cut M2 Wave 276 + Wave 277 Report

Date (UTC): 2026-02-28
Wave group: 276 + 277
Scope: declaration-first migration for low-risk `utils` config/logkit declarations with bounded compatibility rewires.

## Batch Breakdown
- `W276` (Batch A): `3` declaration migrations retained.
- `W277` (Batch B): bounded import/consumer rewires + validation.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning remained thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- Deferred from initial batch plan due high fanout/type-drift risk:
  - `ILoadBalancer`, `ILockManager`, `LockingInfo`, `CommonExecutorService`, `NtpTimeProvider`, `Coin`, `Limit`.
- Added bounded compile-compat rewires required to re-establish canonical matrix after module clean/incremental drift:
  - `LockingInfo` compatibility import to `com.abs` `LocalLockInfo`.
  - `CommonExecutorService`/`NtpTimeProvider` compatibility imports to moved `UtilsApplicationContextHelper` (`com.abs`).
  - `NtpTimeProvider` compatibility import to moved `NtpWrapper` (`com.abs`).
  - `JsonResult` and `JsonResultForLeaderboardUrls` compatibility imports to moved `ResultType` (`com.abs`).
  - `BaseGameCache`, `CurrencyCache`, `BankInfoCache`, `AbstractLazyLoadingExportableCache` compatibility imports to moved cache interfaces (`com.abs`).
  - `web-gs` currency exception imports (`UnknownCurrencyException`, `InvalidCurrencyRateException`, `CurrencyMismatchException`) to `com.abs`.
  - JSP compile compatibility import for moved `RoundFinishedHelper` (`editGameForm.jsp`).

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-091631-hardcut-m2-wave276-wave277-utils-core-lock-ntp-logkit-banktypes/`
- Fast gate batchA:
  - rerun6: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun6: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun6: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `3`
  - bounded rewires/stabilization regressions (`com.abs -> com.dgphoenix` declarations): `0`
  - net tracked declaration delta: `+3`
- Global tracked declarations/files remaining: `527` (baseline `2277`, reduced `1750`).
- Hard-cut burndown completion: `76.855512%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `47.398994%`
  - Core total (01+02): `73.699497%`
  - Entire portfolio: `86.849748%`
- ETA refresh: ~`21.6h` (~`2.70` workdays).
