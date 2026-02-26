# RENAME-FINAL Mini-Wave M3.3 (GameBankConfig class-default compatibility)

Timestamp (UTC): 2026-02-26 07:16

## Scope
Make GameBankConfig support flows resilient to staged package rename for default SP processor and default single-game servlet class names.

## Files changed
- `gs-server/game-server/web-gs/src/main/webapp/support/gameBankConfig/GameClass.jsp`
- `gs-server/game-server/web-gs/src/main/webapp/support/gameBankConfig/editGameForm.jsp`

## Changes
- Added helper-based runtime class resolution for defaults:
  - prefer `com.abs...` class name when present
  - fallback to `com.dgphoenix...` class name
- Applied to:
  - SP processor default in `GameClass.jsp`
  - default single-game servlet class in `editGameForm.jsp`

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
