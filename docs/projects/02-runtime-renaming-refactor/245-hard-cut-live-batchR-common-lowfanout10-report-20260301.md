# Project 02 Hard-Cut Live Batch R Report (Common Low-Fanout 10)

## Timestamp
- 2026-03-01 10:09 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch intent: `10` declarations
- Retained declaration moves: `10`
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-100754-hardcut-live-batchR-common-lowfanout10`

## Retained Declaration Moves (`com.dgphoenix -> com.abs`)
1. `PeriodicReportsCache`
2. `IAccountManager`
3. `DomainSessionFactory`
4. `OperationStatisticsCache`
5. `VersionedDistributedCacheEntry`
6. `IStartGameProcessor`
7. `ICloseGameProcessor`
8. `ServerConfigsTemplateCache`
9. `LimitsCache`
10. `CoinsCache`

## Bounded Compatibility Rewires
- Package-only declaration migration for retained low-fanout targets.
- Added explicit legacy imports in moved `VersionedDistributedCacheEntry` for `IDistributedCacheEntry` and `Identifiable`.
- No blind/global replace.

## Validation Evidence
- Focused fast-gate module summary:
  - `common`: `FAIL` (`rc=1`, known mixed duplicate-class drift profile)
  - `common-wallet`: `FAIL` (`rc=1`, existing mixed import drift)
  - `sb-utils`: `PASS` (`rc=0`)
  - `common-promo`: `FAIL` (`rc=1`, pre-existing mixed promo/util drift)
  - `common-gs`: `FAIL` (`rc=1`, pre-existing mixed import drift)
- Canonical runner (`run-rerun1.sh`) summary:
  - `fast_gate_batchA`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `fast_gate_batchB`: `FAIL` at `STEP01` (`mvn -DskipTests install`)
  - `prewarm`: `FAIL` at `PRE03` (`mvn -DskipTests install`)
  - `validation`: `FAIL` at `PRE03` (`mvn -DskipTests install`)
  - `step09_retry1`: `SKIP`

## Counts and Metrics
- Baseline tracked declarations/files: `2277`
- Pre-batch remaining: `116`
- Post-batch remaining: `106`
- Reduced total: `2171`
- Batch reduction: `10`
- Burndown: `95.344751%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `53.092282%`
- Core total (01+02): `76.546141%`
- Entire portfolio: `88.273071%`

## ETA Refresh
- Remaining declarations: `106`
- ETA: `~4.4h` (`~0.55` workdays)
