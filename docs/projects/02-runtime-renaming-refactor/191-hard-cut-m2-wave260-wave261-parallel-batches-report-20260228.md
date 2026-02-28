# Hard-Cut M2 Wave 260 + Wave 261 Report

Date (UTC): 2026-02-28
Wave group: 260 + 261
Scope: declaration-first migration of common exception package declarations with bounded consumer rewires.

## Batch Breakdown
- `W260`: `10` declaration migrations retained (`ForeignServerException`, `PaymentTransactionRevokeException`, `PaymentTransactionTimeoutException`, `WebServiceException`, `ConfigurationException`, `ServiceNotAvailableException`, `PlayerHasBetsException`, `NotEnoughMoneyException`, `DBException`, `AccountException`).
- `W261`: integration and validation.

## Stabilization
- Parallel execution target remained `1 explorer + 2 workers + main`, but subagent spawning stayed thread-limited (`agent thread limit reached`); strict ownership-safe fallback was executed on main.
- Overlap checks passed (`decl overlap=0`, `rewire overlap=0`, `cross overlap=0`).
- Bounded rewires only (no blind/global replacement):
  - declaration package moves `com.dgphoenix -> com.abs` for selected exception declarations.
  - direct import rewires in `DomainSession`, `SessionHelper`, `PlayerBetPersister`, `CommonWalletManager`, `AccountManager`, MQ service handler, DB link interfaces/impl, bonus request factory, CW helpers/actions/controllers.
  - compile compatibility retained via explicit imports of unmigrated base class `CommonException` in moved declarations.
- High-fanout `WalletException` and `FRBException` were intentionally deferred to a dedicated wave.

## Validation Evidence
- Evidence folder:
  - `docs/projects/02-runtime-renaming-refactor/evidence/20260228-065619-hardcut-m2-wave260-wave261-common-exceptions-core/`
- Fast gate batchA:
  - rerun1: `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`)
- Full matrix:
  - rerun1: `PRE01-03 PASS`, `STEP01-08 PASS`, `STEP09 FAIL` (`rc=2`), retry1 `STEP09 FAIL` (`rc=2`)

## Outcome Metrics
- Declaration deltas from pre-wave scan:
  - `com.dgphoenix -> com.abs`: `10`
  - `com.abs -> com.dgphoenix` stabilization regressions: `0`
  - net tracked declaration delta: `+10`
- Global tracked declarations/files remaining: `589` (baseline `2277`, reduced `1688`).
- Hard-cut burndown completion: `74.132631%`.
- Weighted completion:
  - Project 01: `100.000000%`
  - Project 02: `46.560099%`
  - Core total (01+02): `73.280049%`
  - Entire portfolio: `86.640024%`
- ETA refresh: ~`24.3h` (~`3.04` workdays).
