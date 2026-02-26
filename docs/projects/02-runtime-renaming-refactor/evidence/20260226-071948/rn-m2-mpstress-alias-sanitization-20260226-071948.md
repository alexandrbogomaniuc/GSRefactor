# RENAME-FINAL Mini-Wave M2.5 (mpstress config alias + URL sanitization)

Timestamp (UTC): 2026-02-26 07:19

## Scope
Bring `mpstress` bank/server config closer to staged rename and third-party URL-disable policy.

## Files changed
- `gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
- `gs-server/game-server/config/mpstress/com.dgphoenix.casino.common.cache.ServerConfigsCache.xml`

## Changes
1. `BankInfoCache.xml`
- Added alias keys for both active bank entries:
  - `ABS_CLOSE_GAME_PROCESSOR` (paired with `CLOSE_GAME_PROCESSOR`)
  - `ABS_WPM_CLASS` (paired with `WPM_CLASS`)
  - `ABS_WEAPONS_MODE` (paired with `MQ_WEAPONS_MODE`)
- Replaced active external FR bonus win endpoint values:
  - from `https://txs.maxquest.com/.../bonusWin`
  - to `http://gs1-stress.betsoftgaming.com/config/stub/bonus/win.jsp`

2. `ServerConfigsCache.xml`
- Replaced remaining `fromSupportEmail` values using `report-gp3.maxquest.com` with local `support@localhost`.

## Validation
- `mvn test` (`gs-server/sb-utils`) PASS
- `mvn -DskipTests install` (`gs-server/promo/persisters`) PASS
- `mvn -DskipTests install` (`gs-server/cassandra-cache/common-persisters`) PASS
- `mvn test` (`gs-server/cassandra-cache/cache`) PASS
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` (`gs-server/game-server/web-gs`) PASS
- `mvn -pl core-interfaces,core,persistance -am -DskipTests package` (`mp-server`) PASS
- `node gs-server/deploy/scripts/bank-template-audit.mjs --bank-id 6275,6276 --mode multiplayer --base-url http://127.0.0.1:18081` PASS
- Static scans captured:
  - `mpstress-maxquest-scan.log`
  - `mpstress-abs-alias-scan.log`

## Artifacts
- `mvn-sb-utils-test.log`
- `mvn-promo-persisters-install.log`
- `mvn-common-persisters-install.log`
- `mvn-cache-test.log`
- `mvn-web-gs-package.log`
- `mvn-mp-subset-package.log`
- `bank-template-audit.log`
- `mpstress-maxquest-scan.log`
- `mpstress-abs-alias-scan.log`
