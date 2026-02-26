# RENAME-FINAL Mini-Wave M3.2 (Template support SP processor compatibility)

Timestamp (UTC): 2026-02-26 07:14

## Scope
Remove single-package hardcoding for SP game processor class in support template-generation JSP flows.

## Files changed
- `gs-server/game-server/web-gs/src/main/webapp/support/templateManager/cloneTemplate.jsp`
- `gs-server/game-server/web-gs/src/main/webapp/support/games/829_step1_AddGameInfoTemplate.jsp`
- `gs-server/game-server/web-gs/src/main/webapp/support/games/829_step2_AddGameInfo.jsp`

## Changes
- Added runtime fallback resolution for SP processor class name:
  - preferred: `com.abs.casino.gs.singlegames.tools.cbservtools.SPGameProcessor`
  - fallback: `com.dgphoenix.casino.gs.singlegames.tools.cbservtools.SPGameProcessor`
- Kept behavior backward compatible with current package layout while making these support paths ready for renamed package rollout.

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
