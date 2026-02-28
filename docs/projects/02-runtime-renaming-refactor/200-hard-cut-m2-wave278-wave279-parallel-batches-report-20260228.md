# Hard-Cut M2 Wave 278 + Wave 279 Report

Date (UTC): 2026-02-28
Wave group: 278 + 279
Scope: declaration-first migration for low-risk `common/web` + `common/web/bonus` + `common/web/statistics` declarations with bounded compatibility rewires.

## Batch Breakdown
- `W278` (Batch A): `5` declaration migrations retained.
- `W279` (Batch B): `5` declaration migrations retained.
- Total retained declaration migrations: `10`.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning remained thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- Exploratory kafka-dto leaf attempt was deferred due cross-module duplicate-FQCN collisions (`common-gs` vs `mp-server/kafka` under identical `com.abs` DTO names).
- Added bounded compile/runtime compatibility rewires required for canonical validation:
  - `RemoteCallHelper` explicit `com.abs` DTO imports (`SendPromoNotificationsRequest`, `UpdateStubBalanceByExternalUserIdRequest`, `SendPlayerTournamentStateChangedRequest`, `SendBalanceUpdatedRequest`) to avoid wildcard resolution drift.
  - web-gs JSP `HostConfiguration` import alignment to `com.abs` in:
    - `error_pages/error.jsp`
    - `free/mp/template.jsp`
    - `real/mp/template.jsp`
    - `support/bankReleaseReport.jsp`
    - `tools/bankProperties.jsp`
    - `tools/setStubAccountBalance.jsp`

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-100144-hardcut-m2-wave278-wave279-common-web-bonus-stats/`
- Fast gate batchA:
  - rerun4: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun4: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun4: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `10`
  - bounded rewires/stabilization regressions (`com.abs -> com.dgphoenix` declarations): `0`
  - net tracked declaration delta: `+10`
- Global tracked declarations/files remaining: `517` (baseline `2277`, reduced `1760`).
- Hard-cut burndown completion: `77.294686%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `47.534293%`
  - Core total (01+02): `73.767147%`
  - Entire portfolio: `86.883574%`
- ETA refresh: ~`21.2h` (~`2.65` workdays).
