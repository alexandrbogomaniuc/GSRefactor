# 07 Testing And Observability

## Goal
Define repeatable checks and log analysis points.

## Quick Health Checks
- Containers up:
  - `docker ps`
- GS startup signals:
  - `Initialization was successfully completed`
  - `Initializer: ALL INITIALIZED`
  - Jetty started on port `8080`
- MP stability signals:
  - Repeating `WatchServersThreadMaster ... changedServers={}`
  - No crash loop, no fatal exception bursts

## Standard Log Commands
- GS full tail:
  - `docker logs --tail 500 gp3-gs-1`
- GS filtered tail:
  - `/bin/zsh -lc "docker logs --tail 500 gp3-gs-1 2>&1 | rg -i 'error|exception|caused by|fail|invalid|started|initializ|keyspace|mq|maxquest|mp'"`
- MP full tail:
  - `docker logs --tail 500 gp3-mp-1`

## Low-Approval Command Pack (Recommended)
Use this exact small set repeatedly to reduce approval interruptions:
- `docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'`
- `docker logs --tail 500 gp3-gs-1`
- `/bin/zsh -lc "docker logs --tail 500 gp3-gs-1 2>&1 | rg -i 'error|exception|caused by|fail|invalid|started|initializ|keyspace|mq|maxquest|mp'"`
- `docker logs --tail 500 gp3-mp-1`
- `/bin/zsh -lc "docker logs --tail 500 gp3-mp-1 2>&1 | rg -i 'error|exception|caused by|critical|unable|serverid|started|startreactorserver|startjettyserver|bind|listening'"`

## Useful HTTP Smoke Tests (inside GS container)
- Bank list page (valid):
  - `wget -qO- http://localhost:8080/support/bankSupport.do`
- Bank select page (requires `bankId`):
  - `wget -qO- 'http://localhost:8080/support/bankSelectAction.do?bankId=6274'`
- MP template launch (use BaseAction param names):
  - `wget -S -O- 'http://localhost:8080/free/mp/template.jsp?BANKID=6274&SID=bav_game_session_001&gameId=838&LANG=en'`

## Browser Smoke Tests (Chrome DevTools MCP)
- Open bank selector:
  - `http://localhost:81/support/bankSupport.do`
  - Expected:
    - page title `Bank select page`
    - dropdown options include `DEFAULT BANK`, `TEST_BAV`, `TEST_BAV_VND`, `Default`
- Select `TEST_BAV` and click `editProperties`
  - Expected:
    - page title `Edit properties page`
    - fields `START_GAME_DOMAIN`, `ALLOWED_ORIGIN`, `ALLOWED_DOMAINS`, `MQ_CLIENT_LOG_LEVEL` visible

### Current HTTP Results (2026-02-10)
- `GET /support/bankSupport.do` -> `200`
- `GET /support/bankSelectAction.do` -> `500`
- `GET /support/bankSelectAction.do?bankId=6274` -> `200`
- `GET /free/mp/template.jsp?BANKID=6274&SID=bav_game_session_001&gameId=838&LANG=en` -> `200`
- `GET /free/mp/template.jsp?BANKID=6275&SID=bav_game_session_002&gameId=838&LANG=en` -> `200`
- `GET /free/mp/template.jsp?bankId=6274&sessionId=bav_game_session_001&gameId=838&lang=en` -> `302` redirect to `/error_pages/sessionerror.jsp`
- `GET /free/mp/template.jsp?bankId=6275&sessionId=bav_game_session_002&gameId=838&lang=en` -> `302` redirect to `/error_pages/sessionerror.jsp`

### Wallet/Identity Results (2026-02-11)
- `GET /tools/walletsManager.do?...accountData=show` -> `200` (endpoint reachable; no persistent reset).
- walletsManager lookup:
  - `extUserId=8` -> `Cannot find the player.`
  - `extUserId=bav_game_session_001` -> pending operation row appears.
- Casino-side BAV checks after patch:
  - `/bav/balance`, `/bav/betResult`, `/bav/refundBet` accept token-style `userId` and return `200`.

## Known Noisy But Non-blocking Signals (Current Local Baseline)
- `initServerInfo: hardwareInfo is not initialized` with `UnsatisfiedLinkError` (SIGAR native call), while startup continues.
- NTP warnings (`metadata.google.internal`, `pool.ntp.org`) during time provider init.
- Cassandra metrics warnings like `Metric stat data is empty` during early warm-up.
- `stax2-api ... scanned from multiple locations` during Jetty annotation scan.
- `CassandraSubCasinoGroupPersister ... rowList is null or empty` (if feature table empty).
- `CassandraMassAwardPersister ... rowList is null or empty` (if feature table empty).

## Blocking Signals (Treat As Failures)
- No `Initialization was successfully completed` for GS.
- Repeated `Exception` / `Caused by` without reaching `ALL INITIALIZED`.
- Container restart loop (`Up ...` repeatedly resetting in `docker ps`).
- `/support/bankSelectAction.do` called without `bankId`:
  - `java.lang.NumberFormatException: null` in `BankSelectAction.execute(BankSelectAction.java:28)`.
- `/free/mp/template.jsp` called with wrong parameter names (`bankId/sessionId/lang`):
  - `MQ TEMPLATE.JSP:: bankId not found`, redirect to `/error_pages/sessionerror.jsp`.
- Wallet API returning `422` for `userId` parsing:
  - Indicates API contract mismatch (token value passed where endpoint expected integer).

## Expected Test-Induced Errors (Do Not Treat As Platform Failure)
- If `/support/bankSelectAction.do` is opened without `bankId`, GS logs:
  - `java.lang.NumberFormatException: null`
  - This is expected for invalid request shape; valid call is `/support/bankSelectAction.do?bankId=<id>`.
- If MP template is called with lowercase launch params (`bankId/sessionId/lang`), GS logs:
  - `MQ TEMPLATE.JSP:: bankId not found`
  - This is expected for invalid launch query format; valid keys are `BANKID`, `SID`, `LANG`.

## Current Baseline Status (2026-02-10)
- GS reached:
  - `GsInitThread ... Initialization was successfully completed`
  - `Initializer: ALL INITIALIZED`
- GS web endpoint started:
  - `Started ServerConnector ... 0.0.0.0:8080`
- MP is running and stable (watch thread + statistics loop active).
- Bank list endpoint confirms multiple banks visible (`271`, `6274`, `6275`) via `/support/bankSupport.do`.
- Game launch smoke test for bank `6274` (Dragonstone) returns `HTTP/1.1 200 OK` when using required parameter names (`BANKID`, `SID`, `LANG`).
- Game launch smoke test for bank `6275` (Dragonstone) returns `HTTP/1.1 200 OK` with the same required parameter names.
- Chrome MCP verification:
  - Valid URL page title: `Max Quest: Dragonstone`
  - Invalid lowercase-param URL redirects to page title: `Error` (`/error_pages/sessionerror.jsp`)
- `VND` currency warning for bank `6275` was resolved by adding `VND` to `rcasinoscks.currencycf`.
