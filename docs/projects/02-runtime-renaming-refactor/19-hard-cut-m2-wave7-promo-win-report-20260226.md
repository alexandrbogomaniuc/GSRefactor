# Hard-Cut M2 Wave 7 Report (Promo Win)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W7-promo-win`
Status: `COMPLETE`

## Scope
Migrated namespace:
- from `com.dgphoenix.casino.promo.win`
- to `com.abs.casino.promo.win`

Wave touched 2 files in promo persisters path.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-094953-hardcut-m2-wave7-promo-win`

## Key migration result
- Remaining legacy refs for this family: `0`
- New `com.abs` refs for this family: `2`

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
- Change is strictly package/import migration in promo persister domain.

## Next wave proposal
- M2 Wave 8: continue with another low-fanout family outside blocked common-gs boundary.
