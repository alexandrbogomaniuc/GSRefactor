# Project 02 Hard-Cut Live Batch T Report (Data/Enums 10)

## Timestamp
- 2026-03-01 10:15 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch intent: `10` declarations
- Retained declaration moves: `10`
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-101427-hardcut-live-batchT-data-enums10`

## Retained Declaration Moves (`com.dgphoenix -> com.abs`)
1. `BonusType`
2. `BonusGameMode`
3. `WalletOperationType`
4. `FRBonusNotification`
5. `FRBWinOperation`
6. `FRBonusWin`
7. `LasthandInfo`
8. `IdObject`
9. `IDistributedConfigEntry`
10. `PromoWinInfo`

## Bounded Compatibility Rewires
- Package-only declaration migration for retained low-fanout data/enum targets.
- No blind/global replace.

## Validation Evidence
- Focused fast-gate module summary:
  - `common`: `FAIL` (`rc=1`, known mixed duplicate-class drift profile)
  - `common-wallet`: `FAIL` (`rc=1`, existing mixed import drift)
  - `sb-utils`: `PASS` (`rc=0`)
  - `common-gs`: `FAIL` (`rc=1`, pre-existing mixed import drift)
  - `common-promo`: `FAIL` (`rc=1`, pre-existing mixed promo/util drift)
- Canonical runner (`run-rerun1.sh`) summary:
  - `fast_gate_batchA`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `fast_gate_batchB`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `prewarm`: `FAIL` at `PRE03` (`mvn -DskipTests install`)
  - `validation`: `FAIL` at `PRE03` (`mvn -DskipTests install`)
  - `step09_retry1`: `SKIP`

## Counts and Metrics
- Baseline tracked declarations/files: `2277`
- Pre-batch remaining: `96`
- Post-batch remaining: `86`
- Reduced total: `2191`
- Batch reduction: `10`
- Burndown: `96.223101%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `53.362446%`
- Core total (01+02): `76.681223%`
- Entire portfolio: `88.340611%`

## ETA Refresh
- Remaining declarations: `86`
- ETA: `~3.6h` (`~0.45` workdays)
