# Hard-Cut M2 Wave 286 + Wave 287 Report

Date (UTC): 2026-02-28
Wave group: 286 + 287
Scope: declaration-first migration for low-risk wallet loggable/persister surfaces with bounded compatibility rewires.

## Batch Breakdown
- `W286` (Batch A): `5` declaration migrations retained (`IWalletPersister`, `ILoggableResponseCode`, `ILoggableContainer`, `ILoggableCWClient`, `SimpleLoggableContainer`).
- `W287` (Batch B): `5` declaration migrations retained (`WalletPersister`, `WalletAlertStatus`, `CWMType`, `CommonWalletStatusResult`, `CommonWalletWagerResult`).
- Total retained declaration migrations: `10`.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning remained thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- `rerun1` failed at `STEP01` because moved wallet status/wager results surfaced wildcard-import same-package drift in legacy wallet interfaces/clients.
- `rerun2` failed at `STEP02` due mixed loggable interface package types (`com.dgphoenix` vs `com.abs`) in `common-wallet` v2/v4 clients.
- `rerun3` failed at `STEP06` due missing explicit compatibility import for moved `AccountLockedException` in `GameServer`.
- `rerun4` failed at `STEP07` due JSP import drift for moved `FRBWinOperationStatus`.
- Applied bounded compatibility import alignments and reran to canonical status on `rerun5`.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-111506-hardcut-m2-wave286-wave287-wallet-loggable-persister/`
- Fast gate batchA:
  - rerun5: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun5: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun5: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `10`
  - bounded rewires/stabilization regressions (`com.abs -> com.dgphoenix` declarations): `0`
  - net tracked declaration delta: `+10`
- Global tracked declarations/files remaining: `492` (baseline `2277`, reduced `1785`).
- Hard-cut burndown completion: `78.392622%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `47.872539%`
  - Core total (01+02): `73.936269%`
  - Entire portfolio: `86.968135%`
- ETA refresh: ~`20.2h` (~`2.53` workdays).
