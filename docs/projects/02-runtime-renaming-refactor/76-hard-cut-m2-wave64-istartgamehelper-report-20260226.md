# Hard-Cut M2 Wave 64 Report (IStartGameHelper)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W64-istartgamehelper`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.IStartGameHelper` -> `com.abs.casino.common.games.IStartGameHelper`

Wave touched 9 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/IStartGameHelper.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/IHelperCreator.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/StartGameHelpers.java`
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/AbstractStartGameHelper.java`
- `gs-server/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/CommonWalletManager.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/dblink/DBLink.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/filters/StartGameServletFilter.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/GameServer.java`
- `gs-server/game-server/web-gs/src/main/webapp/support/listGameIds.jsp`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-150802-hardcut-m2-wave64-istartgamehelper`

## Key migration result
- Pre-scan legacy refs for wave scope: `6`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `8`

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
- Runtime logic risk: moderate.
- This wave rewires core helper API type imports through GS init, wallet, filter, DBLink, and support JSP; full matrix and smoke remained green.

## Next wave proposal
- M2 Wave 65: evaluate bounded migration for `TemplateStartGameHelper` dependencies or next low-fanout `common.games` declaration.
