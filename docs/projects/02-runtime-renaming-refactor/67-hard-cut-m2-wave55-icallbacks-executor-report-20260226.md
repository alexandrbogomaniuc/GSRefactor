# Hard-Cut M2 Wave 55 Report (ICallbacksExecutor)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W55-icallbacks-executor`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.gs.socket.async.ICallbacksExecutor` -> `com.abs.casino.gs.socket.async.ICallbacksExecutor`

Wave touched 2 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/gs/socket/async/ICallbacksExecutor.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/gs/socket/async/CallbacksExecutor.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-141600-hardcut-m2-wave55-icallbacks-executor`

## Key migration result
- Pre-scan legacy refs for wave scope: `1`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `2`

## Validation summary
Passing checks:
- `common` install
- `common-wallet` test
- `sb-utils` test
- `promo/persisters` install
- `cassandra-cache/common-persisters` install
- `cassandra-cache/cache` test
- `web-gs` package (with explicit `-Dcluster.properties=common.properties`)
- `mp-server core-interfaces/core/persistance` package
- `refactor-onboard.mjs smoke`

## Risk assessment
- Runtime logic risk: low.
- Interface namespace migration with a single implementation import update.

## Next wave proposal
- M2 Wave 56: continue with next low-fanout package declaration migration.
