# Hard-Cut M2 Wave 35 Report (ServerOnlineStatus)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W35-server-online-status`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.cache.data.server.ServerOnlineStatus` -> `com.abs.casino.common.cache.data.server.ServerOnlineStatus`

Wave touched 5 target files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-121044-hardcut-m2-wave35-server-online-status`

## Key migration result
- Remaining legacy refs for this scope: `0`
- New `com.abs` refs for this scope: `5`

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

## Compatibility corrections during validation
- Applied minimal cross-module type-alignment fixes (web-gs/common-gs) required to keep the validation matrix green after prior hard-cut waves.
- These fixes did not change business behavior; they align compile-time types and protocol version constant usage.

## Risk assessment
- Runtime logic risk: low.
- Enum migration with bounded import rewrites and full matrix pass.

## Next wave proposal
- M2 Wave 36: continue with next low-fanout server/session model declaration migration.
