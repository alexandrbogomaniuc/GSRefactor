# Hard-Cut M2 Wave 262 + Wave 263 Report

Date (UTC): 2026-02-28
Wave group: 262 + 263
Scope: declaration-first migration of final common exception declarations `WalletException` and `FRBException` with bounded consumer rewires.

## Batch Breakdown
- `W262`: `2` declaration migrations retained (`WalletException`, `FRBException`).
- `W263`: integration and validation.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning stayed thread-limited (`agent thread limit reached`); strict ownership-safe fallback was executed on main.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Bounded rewires only (no blind/global replacement):
  - declaration package moves `com.dgphoenix -> com.abs` for `WalletException` and `FRBException`.
  - direct import rewires in wallet clients/managers/interfaces, MQ handlers, tournament handlers, FRB managers/clients, and web actions.
  - explicit `com.abs` imports added for wildcard exception-import users that reference moved symbols.
  - compile compatibility retained via imports of unmigrated base classes (`CommonException`, `BonusException`) in moved declarations.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-070700-hardcut-m2-wave262-wave263-common-exceptions-wallet-frb/`
- Fast gate batchA:
  - rerun1: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `2`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+2`
- Global tracked declarations/files remaining: `587` (baseline `2277`, reduced `1690`).
- Hard-cut burndown completion: `74.220465%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `46.587160%`
  - Core total (01+02): `73.293580%`
  - Entire portfolio: `86.646790%`
- ETA refresh: ~`24.2h` (~`3.03` workdays).
