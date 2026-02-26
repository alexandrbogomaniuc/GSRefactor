# Hard-Cut M2 Wave 5 Report (Analytics Spin)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W5-analytics-spin`
Status: `COMPLETE`

## Scope
Migrated package namespace:
- from `com.dgphoenix.casino.common.analytics.spin`
- to `com.abs.casino.common.analytics.spin`

Wave touched 2 files in `gs-server/common`.

## Evidence
- Successful wave evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-094413-hardcut-m2-wave5-analytics-spin`
- Retained aborted attempt evidence (rolled back):
  - `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-093957-hardcut-m2-wave5-onlineplayer`

## Key migration result
- Remaining legacy refs for this family: `0`
- New `com.abs` refs for this family: `2`

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

## Guardrail update from aborted candidate
- The attempted `onlineplayer` family migration was rolled back because it requires crossing `common-gs` compile boundary currently blocked by baseline issue (`PROTOCOL_VERSION`).
- Future wave planning must avoid package families that require `common-gs` artifact rebuild until that baseline blocker is fixed.

## Next wave proposal
- M2 Wave 6: migrate another low-fanout family that does not require `common-gs` rebuild.
