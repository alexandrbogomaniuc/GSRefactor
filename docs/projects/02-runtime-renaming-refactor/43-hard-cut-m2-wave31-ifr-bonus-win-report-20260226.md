# Hard-Cut M2 Wave 31 Report (IFRBonusWin)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W31-ifr-bonus-win`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.cache.data.payment.frb.IFRBonusWin` -> `com.abs.casino.common.cache.data.payment.frb.IFRBonusWin`

Wave touched 6 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-113605-hardcut-m2-wave31-ifr-bonus-win`

## Key migration result
- Remaining legacy refs for this scope: `0`
- New `com.abs` refs for this scope: `5`

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
- Interface migration with bounded dependent import rewrites and full matrix pass.

## Next wave proposal
- M2 Wave 32: continue FRB payment interface family with pre-scan for cross-module fanout.
