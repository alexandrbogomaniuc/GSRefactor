# RENAME-FINAL Mini-Wave M3.6 (Support history type decoupling)

Timestamp (UTC): 2026-02-26 07:30

## Scope
Remove hardcoded iterate type class binding from `supporthistory.jsp` while preserving URL handling and JSPC compatibility.

## File changed
- `gs-server/game-server/web-gs/src/main/webapp/support/supporthistory.jsp`

## Changes
- Removed hardcoded class in iterate tag:
  - from `logic:iterate ... type="com.dgphoenix.casino.web.history.GameHistoryListEntry"`
  - to generic `logic:iterate` without explicit type.
- Replaced direct typed scriptlet call `entry.getHistoryUrl()` with Struts property extraction:
  - `<bean:define id="historyUrl" name="entry" property="historyUrl" type="java.lang.String"/>`
  - scriptlet now uses `<%=historyUrl%>`

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
