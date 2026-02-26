# Hard-Cut M2 Wave 48 Report (APUBConstants)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W48-apub-constants`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.web.login.apub.APUBConstants` -> `com.abs.casino.common.web.login.apub.APUBConstants`

Wave touched 3 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/login/apub/APUBConstants.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/game/BaseStartGameAction.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/BaseStartGameAction.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-132929-hardcut-m2-wave48-apub-constants`

## Key migration result
- Pre-scan legacy refs for wave scope: `3`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `3`

## Validation summary
Passing checks:
- `common` install
- `common-wallet` test
- `sb-utils` test
- `promo/persisters` install
- `cassandra-cache/common-persisters` install
- `cassandra-cache/cache` test
- `web-gs` package (reactor-aligned rebuild with explicit `cluster.properties`)
- `mp-server core-interfaces/core/persistance` package
- `refactor-onboard.mjs smoke`

## Risk assessment
- Runtime logic risk: low.
- Constant interface namespace move with bounded import rewrites in two GS action bases.

## Next wave proposal
- M2 Wave 49: continue with next low-fanout declaration migration.
