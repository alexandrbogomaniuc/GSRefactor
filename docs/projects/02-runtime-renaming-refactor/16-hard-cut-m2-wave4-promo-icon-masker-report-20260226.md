# Hard-Cut M2 Wave 4 Report (Promo Icon + Masker)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W4-promo-icon-masker`
Status: `COMPLETE`

## Scope
Migrated package/import references:
- from `com.dgphoenix.casino.promo.masker` to `com.abs.casino.promo.masker`
- from `com.dgphoenix.casino.promo.icon` to `com.abs.casino.promo.icon`

Wave touched 10 files across promo persisters, common-gs consumers, and support JSP imports.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-093723-hardcut-m2-wave4-promo-icon-masker`

## Key migration result
- Remaining legacy refs for these package families: `0`
- New `com.abs` refs for these package families: `11`

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
- Changes include runtime-facing imports in common-gs and support JSP import directives, but end-to-end smoke remained green.

## Next wave proposal
- M2 Wave 5: continue with a narrow `common.client` subfamily to keep import fan-out controlled.
