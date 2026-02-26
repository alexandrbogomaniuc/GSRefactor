# RENAME-FINAL Mini-Wave M3.1 (Support JSP class-string compatibility)

Timestamp (UTC): 2026-02-26 07:11

## Scope
Apply compatibility-safe runtime naming updates in support JSP logic where hardcoded legacy class/package strings are used in decision paths.

## Files changed
- `gs-server/game-server/web-gs/src/main/webapp/support/initGames.jsp`
- `gs-server/game-server/web-gs/src/main/webapp/support/setIdGeneratorStartValue.jsp`
- `gs-server/game-server/web-gs/src/main/webapp/support/bankReleaseReport.jsp`

## Changes
1. `initGames.jsp`
- Added dual-prefix acceptance for single-game controller classification:
  - `com.dgphoenix.casino.singlegames.*`
  - `com.abs.casino.singlegames.*`

2. `setIdGeneratorStartValue.jsp`
- Replaced single hardcoded sequencer key lookup with fallback chain:
  - `com.abs.casino.gs.biz.DBWalletOperation`
  - `com.dgphoenix.casino.gs.biz.DBWalletOperation`
- Added output of the resolved key used.

3. `bankReleaseReport.jsp`
- Updated "default wallet client" detection to treat both class names as standard:
  - `com.dgphoenix.casino.payment.wallet.client.v4.StandartRESTCWClient`
  - `com.abs.casino.payment.wallet.client.v4.StandartRESTCWClient`
- Prevents false custom-integration warnings during staged rename.

## Validation
- `mvn test` (`gs-server/sb-utils`) PASS
- `mvn -DskipTests install` (`gs-server/promo/persisters`) PASS
- `mvn -DskipTests install` (`gs-server/cassandra-cache/common-persisters`) PASS
- `mvn test` (`gs-server/cassandra-cache/cache`) PASS
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` (`gs-server/game-server/web-gs`) PASS
- `mvn -pl core-interfaces,core,persistance -am -DskipTests package` (`mp-server`) PASS
- `node gs-server/deploy/scripts/bank-template-audit.mjs --bank-id 6275,6276 --mode multiplayer --base-url http://127.0.0.1:18081` PASS

## Artifacts
- `mvn-sb-utils-test.log`
- `mvn-promo-persisters-install.log`
- `mvn-common-persisters-install.log`
- `mvn-cache-test.log`
- `mvn-web-gs-package.log`
- `mvn-mp-subset-package.log`
- `bank-template-audit.log`
