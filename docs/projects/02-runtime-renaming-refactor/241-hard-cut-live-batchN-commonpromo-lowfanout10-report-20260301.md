# Project 02 Hard-Cut Live Batch N Report (Common-Promo Low-Fanout 10)

## Timestamp
- 2026-03-01 09:56 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch intent: `10` declarations
- Retained declaration moves: `10`
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-095513-hardcut-live-batchN-commonpromo-lowfanout10`

## Retained Declaration Moves (`com.dgphoenix -> com.abs`)
1. `MqEndRoundEvent`
2. `MaxPerformanceEventQualifier`
3. `IMaterialPrize`
4. `IParticipantEventQualifier`
5. `IPrizeQualifier`
6. `EnterType`
7. `IParticipantEvent`
8. `RankPrize`
9. `LocalizationTitles`
10. `PromoType`

## Bounded Compatibility Rewires
- Package-only declaration migration for retained low-fanout targets.
- No blind/global replace.
- No high-fanout manager/core class movement in this batch.

## Validation Evidence
- Focused fast-gate module summary:
  - `common-promo`: `FAIL` (`rc=1`, existing mixed promo/util + duplicate-class drift profile)
  - `promo-core`: `FAIL` (`rc=1`, mixed promo symbol drift)
  - `promo-persisters`: `FAIL` (`rc=1`, pre-existing cassandra engine package drift)
  - `common-gs`: `FAIL` (`rc=1`, pre-existing mixed import drift)
  - `common`: `FAIL` (`rc=1`, known duplicate-class drift profile)
- Canonical runner (`run-rerun1.sh`) summary:
  - `fast_gate_batchA`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `fast_gate_batchB`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `prewarm`: `FAIL` at `PRE03` (`mvn -DskipTests install`)
  - `validation`: `FAIL` at `PRE03` (`mvn -DskipTests install`)
  - `step09_retry1`: `SKIP`

## Counts and Metrics
- Baseline tracked declarations/files: `2277`
- Pre-batch remaining: `149`
- Post-batch remaining: `139`
- Reduced total: `2138`
- Batch reduction: `10`
- Burndown: `93.895477%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `52.646511%`
- Core total (01+02): `76.323256%`
- Entire portfolio: `88.161628%`

## ETA Refresh
- Remaining declarations: `139`
- ETA: `~5.7h` (`~0.71` workdays)
