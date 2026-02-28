# Hard-Cut M2 Wave 288 + Wave 289 Report

Date (UTC): 2026-02-28
Wave group: 288 + 289
Scope: declaration-first migration for low-risk wallet helper/external-handler surfaces with bounded compatibility rewires.

## Batch Breakdown
- `W288` (Batch A): `2` declaration migrations retained (`IWalletHelper`, `WalletHelper`).
- `W289` (Batch B): `2` declaration migrations retained (`ExternalTransactionHandler`, `MultiplayerExternalWallettransactionHandler`).
- Total retained declaration migrations: `4`.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning remained thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- `rerun1` failed at `STEP01` because moved `IWalletHelper` lost same-package visibility to unmoved wallet declarations (`CommonWalletOperation`, `CommonGameWallet`, `CommonWallet`, `IWalletOperation`).
- Added bounded compatibility imports in moved declarations and reran.
- `rerun2` reached canonical validation profile.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-113954-hardcut-m2-wave288-wave289-wallet-helper-externalhandlers/`
- Fast gate batchA:
  - rerun2: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Fast gate batchB:
  - rerun2: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun2: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `rc=2`

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `4`
  - bounded rewires/stabilization regressions (`com.abs -> com.dgphoenix` declarations): `0`
  - net tracked declaration delta: `+4`
- Global tracked declarations/files remaining: `488` (baseline `2277`, reduced `1789`).
- Hard-cut burndown completion: `78.568292%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `47.926657%`
  - Core total (01+02): `73.963329%`
  - Entire portfolio: `86.981665%`
- ETA refresh: ~`20.0h` (~`2.50` workdays).
