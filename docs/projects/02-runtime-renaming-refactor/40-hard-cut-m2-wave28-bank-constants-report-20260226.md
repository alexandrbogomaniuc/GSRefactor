# Hard-Cut M2 Wave 28 Report (BankConstants)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W28-bank-constants`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.cache.data.bank.BankConstants` -> `com.abs.casino.common.cache.data.bank.BankConstants`

Wave touched 6 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-111555-hardcut-m2-wave28-bank-constants`

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
- Runtime logic risk: low.
- Constant class migration with direct import rewrites in Java and support JSPs.

## Next wave proposal
- M2 Wave 29: continue with next low-fanout candidate (`SessionConstants` with common-gs boundary assessment, or FRB interface pair).
