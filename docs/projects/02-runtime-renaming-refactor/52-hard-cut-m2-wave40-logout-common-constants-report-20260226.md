# Hard-Cut M2 Wave 40 Report (LogoutCommonConstants)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W40-logout-common-constants`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.web.logout.LogoutCommonConstants` -> `com.abs.casino.common.web.logout.LogoutCommonConstants`

Wave touched 2 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-124135-hardcut-m2-wave40-logout-common-constants`

## Key migration result
- Pre-scan legacy refs for scope: `2`
- Remaining legacy refs for scope: `0`
- New `com.abs` refs for scope: `2`

## Validation summary
Passing checks:
- `common` install
- `common-wallet` test
- `sb-utils` test
- `promo/persisters` install
- `cassandra-cache/common-persisters` install
- `cassandra-cache/cache` test
- `web-gs` package (reactor-aligned rebuild)
- `mp-server core-interfaces/core/persistance` package
- `refactor-onboard.mjs smoke`

## Risk assessment
- Runtime logic risk: low.
- Constant holder package migration plus one JSP import rewrite.

## Next wave proposal
- M2 Wave 41: continue with next low-fanout declaration migration.
