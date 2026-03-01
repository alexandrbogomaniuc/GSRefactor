# Project 02 Hard-Cut Live Batch G Report (Common10)

## Timestamp
- 2026-03-01 08:53 UTC

## Scope
- Workspace: `/Users/alexb/Documents/Dev/Dev_new`
- Batch: `common` low-fanout declaration wave (`10` declaration moves)
- Evidence: `docs/projects/02-runtime-renaming-refactor/evidence/20260301-084754-hardcut-live-batchG-common10`

## Declaration Moves (`com.dgphoenix -> com.abs`)
1. `AbstractLazyLoadingExportableCache`
2. `BackgroundImagesCache`
3. `BankPartnerIdCache`
4. `CacheExportProcessor`
5. `CurrencyRateMultiplierLoader`
6. `PromoBonusCache`
7. `SetOfLongsContainer`
8. `BonusMassAwardBonusTemplate`
9. `PromoBonus`
10. `WOStatistics`

## Bounded Stabilization
- Added explicit cross-namespace imports in moved files where same-package visibility no longer applied.
- Added compatibility import in `WOStatisticsContainer` for moved `WOStatistics`.
- No broad/global replacements.

## Validation Evidence
- Fast gate baseline: `gs-server/common` PASS (`fast-gate-common-baseline.log`)
- Fast gate after edits:
  - `gs-server/common` PASS on rerun4 (`fast-gate-common-r4.log`)
  - `gs-server/game-server/common-gs` FAIL due pre-existing persister import drift unrelated to this batch (`fast-gate-common-gs-r2.log`)
- Canonical matrix attempt (copied runner profile):
  - fast gate batchA/batchB: FAIL at `STEP04` (`common-promo`)
  - prewarm: FAIL at `PRE03` (`common-promo`)
  - validation: FAIL at `PRE03` (`common-promo`)
  - details: `validation-summary-rerun1.txt`

## Counts and Metrics
- Baseline tracked declarations/files: `2277`
- Pre-batch remaining: `214`
- Post-batch remaining: `204`
- Reduced total: `2073`
- Batch reduction: `10`
- Burndown: `91.041282%`

## Weighted Completion (current reporting model)
- Project 01: `100.000000%`
- Project 02: `51.769482%`
- Core total (01+02): `75.884741%`
- Entire portfolio: `87.942371%`

## ETA Refresh
- Remaining declarations: `204`
- ETA: `~8.3h` (`~1.04` workdays) at current observed throughput.

## Commit Intent
- Commit only scoped code+evidence+diary/log updates for this batch.
