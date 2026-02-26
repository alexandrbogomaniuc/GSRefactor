# Hard-Cut M2 Wave 10 Report (DomainWhiteList)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W10-domain-whitelist`
Status: `COMPLETE`

## Scope
Migrated namespace:
- from `com.dgphoenix.casino.common.cache.data.domain.DomainWhiteList`
- to `com.abs.casino.common.cache.data.domain.DomainWhiteList`

Wave touched 8 files across common cache, cassandra persister, and web-gs support domain actions.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-095853-hardcut-m2-wave10-domain-whitelist`

## Key migration result
- Remaining legacy refs for this scope: `0`
- New `com.abs` refs for this scope: `8`

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
- Runtime logic risk: low to medium.
- Touches support domain-action imports, but runtime smoke and packaging remained green.

## Next wave proposal
- M2 Wave 11: continue low-fanout families and preserve common-gs boundary guardrail.
