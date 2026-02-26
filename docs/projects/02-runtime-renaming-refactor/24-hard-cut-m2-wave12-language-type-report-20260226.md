# Hard-Cut M2 Wave 12 Report (LanguageType)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W12-language-type`
Status: `COMPLETE`

## Scope
Migrated namespace:
- from `com.dgphoenix.casino.common.cache.data.language.LanguageType`
- to `com.abs.casino.common.cache.data.language.LanguageType`

Wave touched 6 files across common utility and web-gs forms/actions.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-100426-hardcut-m2-wave12-language-type`

## Key migration result
- Remaining legacy refs for this scope: `0`
- New `com.abs` refs for this scope: `6`

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
- Touches web-gs forms/actions, but full matrix and smoke remained green.

## Next wave proposal
- M2 Wave 13: continue with next low-fanout family outside common-gs boundary.
