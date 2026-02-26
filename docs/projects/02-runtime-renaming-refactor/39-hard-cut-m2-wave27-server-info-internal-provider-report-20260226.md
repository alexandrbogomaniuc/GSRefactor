# Hard-Cut M2 Wave 27 Report (IServerInfoInternalProvider)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W27-server-info-internal-provider`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.cache.data.server.IServerInfoInternalProvider` -> `com.abs.casino.common.cache.data.server.IServerInfoInternalProvider`

Wave touched 4 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-111037-hardcut-m2-wave27-server-info-internal-provider`

## Key migration result
- Remaining legacy refs for this scope: `0`
- New `com.abs` refs for this scope: `3`

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

## Notes
- Initial Wave 27 compile failed because the migrated interface lost implicit access to `ServerInfo` from the old package.
- Added explicit `import com.dgphoenix.casino.common.cache.data.server.ServerInfo;` in the interface and reran the full matrix successfully.

## Risk assessment
- Runtime logic risk: low.
- Interface/import migration with explicit compatibility bridge.

## Next wave proposal
- M2 Wave 28: continue with next low-fanout candidate (`SessionConstants` or `BankConstants`) after dependency pre-scan.
