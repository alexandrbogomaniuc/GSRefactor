# Hard-Cut M2 Wave 49 Report (GameServerResponse)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W49-apub-game-server-response`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.web.login.apub.GameServerResponse` -> `com.abs.casino.common.web.login.apub.GameServerResponse`

Wave touched 5 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/web/login/apub/GameServerResponse.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/cw/CWGuestLogin.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameAction.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/BaseStartGameAction.java`
- `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/bonus/BSStartGameAction.java`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-133833-hardcut-m2-wave49-apub-game-server-response`

## Key migration result
- Pre-scan legacy refs for wave scope: `5`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `5`

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
- Data response class namespace move with bounded import rewrites in four GS action classes.

## Next wave proposal
- M2 Wave 50: continue with next low-fanout declaration migration.
