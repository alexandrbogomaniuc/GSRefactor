# Hard-Cut M2 Wave 32 Report (FRBonusNotificationStatus)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W32-fr-bonus-notification-status`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.cache.data.payment.frb.FRBonusNotificationStatus` -> `com.abs.casino.common.cache.data.payment.frb.FRBonusNotificationStatus`

Wave touched 5 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-114542-hardcut-m2-wave32-fr-bonus-notification-status`

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
- Runtime logic risk: medium-low.
- Enum migration with bounded dependent import rewrites and full matrix pass.

## Next wave proposal
- M2 Wave 33: migrate `FRBWinOperationStatus` with staged import updates and full matrix rerun.
