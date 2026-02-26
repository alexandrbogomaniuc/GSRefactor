# RENAME-FINAL Mini-Wave M3.5 (Language-table useBean decoupling)

Timestamp (UTC): 2026-02-26 07:27

## Scope
Remove hardcoded legacy `jsp:useBean` class bindings from support language-table JSP paths.

## Files changed
- `gs-server/game-server/web-gs/src/main/webapp/support/cache/bank/properties/languageTable.jsp`
- `gs-server/game-server/web-gs/src/main/webapp/support/cache/bank/properties/edit/languageTable.jsp`

## Changes
- Removed unused hardcoded `gameBean` declaration from `languageTable.jsp`.
- Removed hardcoded `gameBean` declaration from `edit/languageTable.jsp` and replaced usages with request/context-driven values:
  - resolve `bankId` from request parameter with fallback to `LanguageSupportForm.getBankId()` via reflection
  - use current iterate value (`game`) directly instead of `gameBean.getGameId()`
- Added explicit guard output when `bankId` is missing.

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
