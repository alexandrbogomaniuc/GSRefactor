# Hard-Cut M2 Wave 45 Report (LoginCommonConstants)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W45-login-common-constants`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.web.login.LoginCommonConstants` -> `com.abs.casino.common.web.login.LoginCommonConstants`

Wave touched 2 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-130750-hardcut-m2-wave45-login-common-constants`

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
- Constant holder declaration migration plus one static import rewrite.

## Next wave proposal
- M2 Wave 46: continue with next low-fanout declaration migration.
