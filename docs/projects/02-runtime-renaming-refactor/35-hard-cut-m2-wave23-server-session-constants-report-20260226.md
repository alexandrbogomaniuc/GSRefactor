# Hard-Cut M2 Wave 23 Report (Server/Session Constants)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W23-server-session-constants`
Status: `COMPLETE`

## Scope
Migrated namespace declarations:
- `com.dgphoenix.casino.common.cache.data.server.ServerInfoConstants` -> `com.abs.casino.common.cache.data.server.ServerInfoConstants`
- `com.dgphoenix.casino.common.cache.data.session.SessionLimit` -> `com.abs.casino.common.cache.data.session.SessionLimit`
- `com.dgphoenix.casino.common.cache.data.session.SessionStatistics` -> `com.abs.casino.common.cache.data.session.SessionStatistics`

Wave touched 3 files.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-104658-hardcut-m2-wave23-server-session-constants`

## Key migration result
- Remaining legacy declarations for this scope: `0`
- New `com.abs` declarations for this scope: `3`

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
- Isolated declaration-only migration set.

## Execution note
- Initial Wave 23 attempt included `BankMiniGameInfo`, which failed because it depends on in-package `MiniGameInfo`/`BaseGameInfo` still on legacy namespace.
- `BankMiniGameInfo` change was rolled back and final Wave 23 scope was reduced to declaration-safe constants only.

## Next wave proposal
- M2 Wave 24: continue next low-fanout family outside `common-gs` compile boundary.
