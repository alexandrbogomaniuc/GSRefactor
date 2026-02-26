# Hard-Cut M2 Wave 63 Report (StartGameHelpers)

Date (UTC): 2026-02-26
Project: `02-runtime-renaming-refactor`
Milestone: `M2 - package migration`
Wave: `W63-start-game-helpers`
Status: `COMPLETE`

## Scope
Migrated namespace:
- `com.dgphoenix.casino.common.games.StartGameHelpers` -> `com.abs.casino.common.games.StartGameHelpers`

Wave touched 5 files:
- `gs-server/common/src/main/java/com/dgphoenix/casino/common/games/StartGameHelpers.java`
- `gs-server/common-wallet/src/main/java/com/dgphoenix/casino/payment/wallet/CommonWalletManager.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/dblink/DBLink.java`
- `gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/filters/StartGameServletFilter.java`
- `gs-server/game-server/web-gs/src/main/webapp/support/listGameIds.jsp`

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/projects/02-runtime-renaming-refactor/evidence/20260226-150121-hardcut-m2-wave63-start-game-helpers`

## Key migration result
- Pre-scan legacy refs for wave scope: `4`
- Remaining legacy refs for wave scope: `0`
- New `com.abs` refs for wave scope: `4`

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
- Runtime logic risk: low to moderate.
- Includes wallet and DBLink import rewires around helper lookup path; behavior validated by full matrix and smoke.

## Next wave proposal
- M2 Wave 64: migrate `IStartGameHelper` to align helper API package with migrated helper registry.
