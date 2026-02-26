# Hard-Cut M2 Wave 18 Report (HistoryInformerItem)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W18-history-informer-item`
Status: `COMPLETE`

## Scope
Migrated namespace:
- from `com.dgphoenix.casino.common.cache.data.HistoryInformerItem`
- to `com.abs.casino.common.cache.data.HistoryInformerItem`

Wave touched 2 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-103007-hardcut-m2-wave18-history-informer-item`

## Key migration result
- Remaining legacy refs for this scope: `0`
- New `com.abs` refs for this scope: `1`

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
- Isolated model + persister import migration in validated module set.

## Next wave proposal
- M2 Wave 19: continue next low-fanout family outside `common-gs` compile boundary.
