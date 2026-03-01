# Project 02 Hard-Cut Live Batch V Report (Utils/Data/Interfaces 16)

## Timestamp
- 2026-03-01 10:26 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch intent: `16` declarations
- Retained declaration moves: `16`
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-102556-hardcut-live-batchV-utils-data-interfaces16`

## Retained Declaration Moves (`com.dgphoenix -> com.abs`)
1. `GameGroup`
2. `GameType`
3. `GameVariableType`
4. `IBaseGameInfo`
5. `TObject`
6. `ExecutorUtils`
7. `CWError`
8. `RNG`
9. `Coin`
10. `Limit`
11. `GameMode`
12. `WalletOperationStatus`
13. `IWallet`
14. `ClientType`
15. `ApplicationContextHelper`
16. `CommonExecutorService`

## Bounded Compatibility Rewires
- Package-only declaration migration for retained low-fanout utility/data/interface targets.
- Explicit import rewires in moved declarations:
  - `IBaseGameInfo`: `Identifiable -> com.abs.casino.common.cache.Identifiable`
  - `IWallet`: `IDistributedCacheEntry -> com.abs.casino.common.cache.IDistributedCacheEntry`
  - `IWallet`: `IWalletOperation -> com.abs.casino.gs.managers.payment.wallet.IWalletOperation`
- No blind/global replace.

## Validation Evidence
- Focused fast-gate module summary:
  - `common`: `FAIL` (`rc=1`)
  - `common-wallet`: `FAIL` (`rc=1`)
  - `sb-utils`: `FAIL` (`rc=1`)
  - `common-gs`: `FAIL` (`rc=1`)
  - `common-promo`: `FAIL` (`rc=1`)
- Canonical runner (`run-rerun1.sh`) summary:
  - `fast_gate_batchA`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `fast_gate_batchB`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `prewarm`: `FAIL` at `PRE01` (`mvn -DskipTests install`)
  - `validation`: `FAIL` at `PRE01` (`mvn -DskipTests install`)
  - `step09_retry1`: `FAIL` (`rc=SKIP`)

## Counts and Metrics
- Baseline tracked declarations/files: `2277`
- Pre-batch remaining: `73`
- Post-batch remaining: `57`
- Reduced total: `2220`
- Batch reduction: `16`
- Burndown: `97.496706%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `53.754184%`
- Core total (01+02): `76.877092%`
- Entire portfolio: `88.438546%`

## ETA Refresh
- Remaining declarations: `57`
- ETA: `~2.4h` (`~0.30` workdays)
