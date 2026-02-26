# Hard-Cut M2 Wave 46 Report (CWStLobbyGuestLoginLobbyRequest)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W46-cw-stlobby-guest-login-request`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.web.login.cw.CWStLobbyGuestLoginLobbyRequest` -> `com.abs.casino.common.web.login.cw.CWStLobbyGuestLoginLobbyRequest`

Wave touched 1 target file.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-131427-hardcut-m2-wave46-cw-stlobby-guest-login-request`

## Key migration result
- Pre-scan legacy refs for scope: `1`
- Remaining legacy refs for scope: `0`
- New `com.abs` refs for scope: `1`

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
- Isolated request model declaration migration with no dependent rewrites.

## Next wave proposal
- M2 Wave 47: continue with next low-fanout declaration migration.
