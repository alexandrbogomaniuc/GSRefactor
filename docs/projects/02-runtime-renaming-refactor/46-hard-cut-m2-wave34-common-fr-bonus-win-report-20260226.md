# Hard-Cut M2 Wave 34 Report (CommonFRBonusWin)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W34-common-fr-bonus-win`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.cache.data.payment.bonus.CommonFRBonusWin` -> `com.abs.casino.common.cache.data.payment.bonus.CommonFRBonusWin`

Wave touched 4 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-115923-hardcut-m2-wave34-common-fr-bonus-win`

## Key migration result
- Remaining legacy refs for this scope: `0`
- New `com.abs` refs for this scope: `4`

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
- Single class migration with bounded import rewrites and full matrix pass.

## Next wave proposal
- M2 Wave 35: continue payment bonus family with next low-fanout model declaration migration.
