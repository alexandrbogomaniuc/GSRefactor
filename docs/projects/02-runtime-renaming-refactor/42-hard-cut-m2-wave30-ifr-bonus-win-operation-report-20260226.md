# Hard-Cut M2 Wave 30 Report (IFRBonusWinOperation)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W30-ifr-bonus-win-operation`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.cache.data.payment.frb.IFRBonusWinOperation` -> `com.abs.casino.common.cache.data.payment.frb.IFRBonusWinOperation`

Wave touched 4 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-112847-hardcut-m2-wave30-ifr-bonus-win-operation`

## Key migration result
- Remaining legacy refs for this scope: `0`
- New `com.abs` refs for this scope: `3`

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
- M2 Wave 31: continue next low-fanout FRB interface/status family with explicit dependency pre-scan.
