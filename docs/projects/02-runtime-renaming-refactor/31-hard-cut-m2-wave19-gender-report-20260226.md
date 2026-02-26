# Hard-Cut M2 Wave 19 Report (Gender)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W19-gender`
Status: `COMPLETE`

## Scope
Migrated namespace:
- from `com.dgphoenix.casino.common.cache.data.account.Gender`
- to `com.abs.casino.common.cache.data.account.Gender`

Wave touched 2 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-103303-hardcut-m2-wave19-gender`

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
- Isolated enum + single dependent import migration.

## Next wave proposal
- M2 Wave 20: continue next low-fanout family outside `common-gs` compile boundary.
