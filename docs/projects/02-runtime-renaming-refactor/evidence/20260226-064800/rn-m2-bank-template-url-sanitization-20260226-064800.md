# Runtime Renaming Mini-Wave M2.1 (Bank template URL sanitization)

## Objective
Remove third-party runtime integration URLs from active refactor bank templates and keep runtime behavior inside local services only.

## Files changed
- `gs-server/game-server/config/mqb/com.dgphoenix.casino.common.cache.BankInfoCache.xml`
- `gs-server/game-server/config/local-machine/com.dgphoenix.casino.common.cache.BankInfoCache.xml`

## What changed
- Replaced external wallet endpoints (`wallet.mqbase.com`) with local GS stub endpoints:
  - auth/balance/wager/refund -> `http://gs:8080/config/stub/...`
- Disabled non-core multiplayer/social callbacks by routing to local noop endpoint:
  - friends/rooms/private room status/invite/online-status
  - close-game notification callback
  - private-room deactivated callback
  - all -> `http://gs:8080/empty.jsp`
- Replaced external bonus win endpoint (`txs.maxquest.com`) with local stub:
  - `FR_BONUS_WIN_URL` -> `http://gs:8080/config/stub/bonus/frbWin.jsp`
- Replaced external launch-domain/fatal-page allow-list values for the bank templates:
  - `START_GAME_DOMAIN` -> `localhost`
  - `ALLOWED_ORIGIN` -> `http://localhost`
  - `ALLOWED_DOMAINS` -> `localhost`
  - `FATAL_ERROR_PAGE_URL` -> `http://localhost/error_pages/error.jsp`
- Replaced `MP_LOBBY_WS_URL` external host with local mapped MP endpoint:
  - `127.0.0.1:16300`

## Validation matrix (PASS)
- `mvn test` in `gs-server/sb-utils`
- `mvn -DskipTests install` in `gs-server/promo/persisters`
- `mvn -DskipTests install` in `gs-server/cassandra-cache/common-persisters`
- `mvn test` in `gs-server/cassandra-cache/cache`
- `mvn -DskipTests -Dcluster.properties=local/local-machine.properties package` in `gs-server/game-server/web-gs`
- `mvn -pl core-interfaces,core,persistance -am -DskipTests package` in `mp-server`

## Runtime audit check (PASS)
- Command:
  - `node gs-server/deploy/scripts/bank-template-audit.mjs --bank-id 6275,6276 --mode multiplayer --base-url http://127.0.0.1:18081`
- Result:
  - Bank `6275`: PASS (third-party URLs: 0)
  - Bank `6276`: PASS (third-party URLs: 0)
  - Overall: PASS

## Outcome
- Active local/refactor bank templates no longer call third-party wallet/social domains.
- Runtime now uses internal stub/noop paths for non-core callbacks and local endpoints for wallet flow simulation.
