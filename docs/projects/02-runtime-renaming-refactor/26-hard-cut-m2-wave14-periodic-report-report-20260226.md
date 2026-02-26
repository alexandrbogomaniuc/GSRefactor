# Hard-Cut M2 Wave 14 Report (PeriodicReportInfo)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W14-periodic-report`
Status: `COMPLETE`

## Scope
Migrated namespace:
- from `com.dgphoenix.casino.common.cache.data.report.PeriodicReportInfo`
- to `com.abs.casino.common.cache.data.report.PeriodicReportInfo`

Wave touched 2 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-100936-hardcut-m2-wave14-periodic-report`

## Key migration result
- Remaining legacy refs for this scope: `0`
- New `com.abs` refs for this scope: `2`

## Validation summary
Passing checks:
- `common` install
- `common-wallet` test
- `sb-utils` test
- `promo/persisters` install
- `cassandra-cache/common-persisters` install
- `cassandra-cache/cache` test
- `web-gs` package
- `mp-server core-interfaces/core/persistance` package
- `refactor-onboard.mjs smoke`

## Risk assessment
- Runtime logic risk: low.
- Isolated data-model + cache import update with full green matrix.

## Next wave proposal
- M2 Wave 15: continue next low-fanout family outside common-gs boundary.
