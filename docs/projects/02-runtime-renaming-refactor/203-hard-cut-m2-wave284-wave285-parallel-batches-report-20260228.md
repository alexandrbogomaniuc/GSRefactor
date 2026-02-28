# Hard-Cut M2 Wave 284 + Wave 285 Report

Date (UTC): 2026-02-28
Wave group: 284 + 285
Scope: declaration-first migration for low-risk `common/cache/data/payment` wallet abstractions with bounded compatibility rewires.

## Batch Breakdown
- `W284` (Batch A): `2` declaration migrations retained (`AbstractWallet`, `AbstractWalletOperation`).
- `W285` (Batch B): `2` declaration migrations retained (`WalletOperationInfo`, `WalletOperationAdditionalProperties`).
- Total retained declaration migrations: `4`.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning remained thread-limited (`agent thread limit reached`); ownership-safe fallback continued on main.
- `rerun1` failed at `STEP01` due moved wallet abstractions losing same-package visibility to unmigrated types (`IWalletOperation`, `WalletOperationType`, `WalletOperationStatus`).
- Applied bounded compatibility imports in moved declarations and reran; `rerun2` reached canonical validation profile.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-105820-hardcut-m2-wave284-wave285-wallet-core-abstractions/`
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
- Global tracked declarations/files remaining: `502` (baseline `2277`, reduced `1775`).
- Hard-cut burndown completion: `77.953448%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `47.737241%`
  - Core total (01+02): `73.868621%`
  - Entire portfolio: `86.934310%`
- ETA refresh: ~`20.6h` (~`2.57` workdays).
