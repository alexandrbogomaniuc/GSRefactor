# Hard-Cut M2 Wave 6 Report (Canex Response)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W6-canex-response`
Status: `COMPLETE`

## Scope
Migrated package/import namespace:
- from `com.dgphoenix.casino.common.client.canex.response`
- to `com.abs.casino.common.client.canex.response`

Wave touched 5 files (3 package declarations in `common`, imports in `common-wallet` and `web-gs`).

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-094646-hardcut-m2-wave6-canex-response`

## Key migration result
- Remaining legacy refs for this family: `0`
- New `com.abs` refs for this family: `6`

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
- This wave is focused package/import migration with no class-string runtime mapping changes.

## Next wave proposal
- M2 Wave 7: continue with another low-fanout family outside `common-gs` boundary.
