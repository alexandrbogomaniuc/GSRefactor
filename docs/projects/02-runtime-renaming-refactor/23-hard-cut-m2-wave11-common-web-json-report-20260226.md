# Hard-Cut M2 Wave 11 Report (Common Web JSON)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W11-common-web-json`
Status: `COMPLETE`

## Scope
Migrated namespace:
- from `com.dgphoenix.casino.common.web.json`
- to `com.abs.casino.common.web.json`
for `ZonedDateTimeSerializer`.

Wave touched 1 file.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-100207-hardcut-m2-wave11-common-web-json`

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
- Isolated package declaration migration with no fan-out references.

## Next wave proposal
- M2 Wave 12: continue with next low-fanout family outside common-gs boundary.
