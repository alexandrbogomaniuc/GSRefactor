# Hard-Cut M2 Wave 15 Report (MassAwardRestriction)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W15-mass-award-restriction`
Status: `COMPLETE`

## Scope
Migrated namespace:
- from `com.dgphoenix.casino.common.cache.data.bonus.restriction.MassAwardRestriction`
- to `com.abs.casino.common.cache.data.bonus.restriction.MassAwardRestriction`

Wave touched 2 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-101415-hardcut-m2-wave15-mass-award-restriction`

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
- Isolated interface package migration + dependent persister import update.

## Execution note
- A boundary attempt that touched `common-gs`/JSP imports was rolled back because `common-gs` currently has a known baseline compile blocker (`BasicTransactionDataStorageHelper` `PROTOCOL_VERSION`) unrelated to this wave.
- Final wave scope was intentionally constrained to keep the validation matrix green.

## Next wave proposal
- M2 Wave 16: continue next low-fanout family outside current `common-gs` compile boundary.
