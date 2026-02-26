# RENAME-FINAL Mini-Wave M3.4 (Support `jsp:useBean` class decoupling)

Timestamp (UTC): 2026-02-26 07:24

## Scope
Reduce hard dependency on legacy package names in support JSP bean wiring by removing explicit `jsp:useBean class="com.dgphoenix..."` usage where request beans are already provided.

## Files changed
- `gs-server/game-server/web-gs/src/main/webapp/support/cache/bank/properties/edit/editProperties.jsp`
- `gs-server/game-server/web-gs/src/main/webapp/support/cache/bank/common/addBank.jsp`
- `gs-server/game-server/web-gs/src/main/webapp/support/cache/bank/common/subCasinoInfo.jsp`

## Changes
- Removed hardcoded `jsp:useBean` class declarations for request-scoped forms in `editProperties.jsp` and `addBank.jsp`.
- Removed hardcoded `SubcasinoForm` useBean class in `subCasinoInfo.jsp`; replaced with request-attribute based id extraction using reflective `getId()` fallback.
- Added explicit guard response for missing `subcasinoId` after fallback resolution.

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
