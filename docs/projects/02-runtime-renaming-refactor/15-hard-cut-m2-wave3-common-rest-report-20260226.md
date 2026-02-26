# Hard-Cut M2 Wave 3 Report (Common REST Package)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W3-common-rest`
Status: `COMPLETE`

## Scope
Migrated package/import references:
- from `com.dgphoenix.casino.common.rest`
- to `com.abs.casino.common.rest`

Wave touched 7 files (3 package declarations in `common`, 4 import sites in `common-wallet`).

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-093419-hardcut-m2-wave3-common-rest`

## Key migration result
- Remaining legacy `common.rest` refs in GS/MP scan: `0`
- New `com.abs` `common.rest` refs in GS/MP scan: `7`

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
- Change type is namespace package/import migration in REST helper classes and direct wallet client consumers; runtime smoke remained green.

## Next wave proposal
- M2 Wave 4: migrate another narrow utility family with low fan-out and keep sequential build ordering.
