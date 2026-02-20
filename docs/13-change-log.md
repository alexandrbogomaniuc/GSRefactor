# 13 Change Log (GS/MP Launch Debug)

## Scope
This changelog summarizes what was broken, what we changed, and what is still failing for:
- `http://localhost/cwstartgamev2.do?bankId=6274&gameId=838&mode=real&token=bav_game_session_001&lang=en`

It includes runtime wiring, Cassandra updates, static routing fixes, and current wallet errors.

## Environment Snapshot (Current)
- `gp3-static-1` -> `80:80`
- `gp3-gs-1` -> `81:8080`
- `gp3-mp-1` -> `6300-6301:6300-6301`
- `casino_side` -> `8000:8000`
- `gp3-c1-1` -> `9142:9042`
- `gp3-kafka-1` -> `9092:29092`
- `gp3-zookeeper-1` -> `2181:2181`

Evidence:
- `docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'`

## Timeline Of Problems And Fixes

### 1) Bank/game baseline data missing or incomplete
Problem:
- Launches for bank/game combinations were inconsistent because Cassandra data was incomplete.

Symptoms:
- Missing or unstable launch behavior for `6275+838`.
- Index fields needed for lookup were missing for `6274+838`.

Root cause:
- Missing bank-game row(s) and missing index fields in `rcasinoscks.gameinfocf`.

Fixes applied:
- Added bank 6275 game mapping:
  - `/Users/alexb/Documents/Dev/docs/sql/add_game_838_for_bank_6275.cql`
- Corrected 838 to MP game type (`gameTypeId=1`) for:
  - template row `key=838`
  - bank rows `key='6274+838'`, `key='6275+838'`
  - file: `/Users/alexb/Documents/Dev/docs/sql/fix_838_gameType_to_mp.cql`
- Corrected index fields:
  - `6274+838` -> `bankidx=6274`, `bankandcuridx=6274_USD`
  - `6275+838` -> `bankidx=6275`, `bankandcuridx=6275_VND`
- Added missing `VND` currency row in `rcasinoscks.currencycf` (for bank 6275 startup consistency).

Verification:
- `SELECT key,bankidx,bankandcuridx FROM rcasinoscks.gameinfocf WHERE key IN ('6274+838','6275+838');`
- `SELECT key,jcn FROM rcasinoscks.gameinfocf WHERE key IN ('6274+838','6275+838');` -> both rows show `"gameTypeId":1`

### 2) Support UI bank editing path caused avoidable errors
Problem:
- Bank edit URL used without explicit `bankId`.

Symptoms:
- `bankSelectAction.do` could return HTTP 500 if `bankId` missing.

Fix:
- Operational rule: always open support with explicit bank id:
  - `http://localhost:81/support/bankSelectAction.do?bankId=6274`
  - `http://localhost:81/support/bankSelectAction.do?bankId=6275`

Evidence:
- `/Users/alexb/Documents/Dev/docs/07-testing-and-observability.md`
- `/Users/alexb/Documents/Dev/docs/10-operator-command-pack.md`

### 3) Launch flow reached template but game handoff returned 404
Problem:
- Browser reached `template.jsp`, but `/html5pc/actiongames/dragonstone/game/?...` returned 404.

Symptoms:
- Launch stopped after template/lobby stage.
- Game handoff URL was not served as directory index.

Root cause:
- Static nginx rule used `try_files $uri =404;`, which failed for directory URLs with query string.

Fix:
- Updated static nginx rule:
  - from: `try_files $uri =404;`
  - to: `try_files $uri $uri/ $uri/index.html =404;`

### 4) MP websocket handshake reset during lobby boot
Problem:
- Browser websocket to `ws://localhost:6300/websocket/mplobby` failed (`ERR_CONNECTION_RESET`, close code `1006` loop).

Root cause:
- MP process was up, but runtime was not consistently listening on required websocket ports due to startup instability.

Fix:
- Restarted MP runtime container and revalidated listeners on `6300/6301`.

Result:
- Lobby websocket reset loop stopped.

### 5) Wallet API contract mismatch on `userId` type
Problem:
- GS sent token-style `userId` (for example `bav_game_session_001`) to casino-side endpoints.
- Casino-side endpoints expected integer `userId`, returning `422`.

Fix:
- Patched casino-side BAV endpoints (`balance`, `betResult`, `refundBet`) to:
  - accept `userId` as string,
  - resolve token-like values to numeric internal user id,
  - keep hash validation against raw incoming userId value.
- Rebuilt/recreated `casino_side` container to activate new code.

Result:
- `/bav/balance`, `/bav/betResult`, `/bav/refundBet` now return `200` for token-style userId tests.

### 6) walletsManager showed "Cannot find the player" for extUserId=8
Problem:
- Tool lookup by `extUserId=8` returned no player while pending transaction existed.

Root cause:
- Current `accountcf_ext` mapping for bank `6274` is token-based:
  - `bav_game_session_001 -> 40962`
  - no `8 -> ...` row in this environment.

Fix/Workaround:
- Use `extUserId=bav_game_session_001` or `accountId=40962` in walletsManager for current test account.

Result:
- Pending operations are visible and manageable in tool.
- File:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/deploy/docker/configs/static/games`
- Rebuilt/restarted static service with compose.

Result:
- Handoff endpoint now returns HTTP 200 and game files are served.

Evidence:
- `/Users/alexb/Documents/Dev/docs/11-game-launch-forensics.md`
- static config file above

### 4) WebSocket target mismatches during local run
Problem:
- Local launch used non-local or wrong WebSocket routing in earlier attempts.

Symptoms:
- Session/gameplay stage instability and session bridge failures in MP/GS traces.

Fix:
- Bank properties for 6274/6275 now use:
  - `"MP_LOBBY_WS_URL":"localhost:6300"`
- Verified in Cassandra `bankinfocf.jcn`.

Notes:
- `START_GAME_DOMAIN` remains `localhost:8081` for those banks.
- This is expected to affect browser-facing launch/start URLs separately from MP lobby socket target.

Verification:
- `SELECT key,jcn FROM rcasinoscks.bankinfocf WHERE key IN (6274,6275);`

### 5) Template parameter shape issues (negative test clarified)
Problem:
- Incorrect param names caused session errors.

Symptoms:
- Lowercase parameters redirected to `sessionerror.jsp`.

Fix/Rule:
- Use required keys:
  - `BANKID`, `SID`, `gameId`, `LANG`
- Example valid:
  - `/free/mp/template.jsp?BANKID=6274&SID=bav_game_session_001&gameId=838&LANG=en`

Evidence:
- `/Users/alexb/Documents/Dev/docs/04-bank-and-game-settings.md`
- `/Users/alexb/Documents/Dev/docs/07-testing-and-observability.md`

## Cassandra Changes (Consolidated)
- Applied CQL sources:
  - `/Users/alexb/Documents/Dev/readme all you need to know from md files/Game provider side_Source code_mq-gs-clean-version_FINAL_BAV_BANKS.cql`
  - `/Users/alexb/Documents/Dev/readme all you need to know from md files/FIX_SUBCASINO_LOCALHOST.cql`
  - `/Users/alexb/Documents/Dev/readme all you need to know from md files/update_838_fixed.cql`
  - `/Users/alexb/Documents/Dev/docs/sql/add_game_838_for_bank_6275.cql`
  - `/Users/alexb/Documents/Dev/docs/sql/fix_838_gameType_to_mp.cql`
- Verified final state:
  - `6274+838` and `6275+838` both exist, indexed, and MP type (`gameTypeId=1`)
  - bank 6274 and 6275 include wallet URLs to `http://host.docker.internal:8000/bav/*`
  - bank 6274 and 6275 include `MP_LOBBY_WS_URL=localhost:6300`

## Port And Routing Changes (Consolidated)
- Static routing fix in nginx:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/deploy/docker/configs/static/games`
  - directory/index fallback enabled for `/html5pc/...`
- Runtime service ports:
  - static `80`, GS `81`, MP `6300/6301`, casino side `8000`, Cassandra `9142`
- Wallet integration endpoint target:
  - GS bank config points to `host.docker.internal:8000` for auth/balance/bet/refund.

## Current Status
- Proven working now:
  - Core containers are up.
  - GS initializes successfully.
  - Launch URL reaches template, lobby, and game handoff.
  - Static 404 blocker is fixed.
  - Cassandra bank/game baseline for 6274/6275 + 838 is consistent.
- Current blocking issue:
  - Wallet calls fail during runtime with HTTP 422 because `userId` is token-like (`bav_game_session_001`) while casino-side endpoints expect integer `userId`.

Live evidence (GS logs):
- `Invalid response code: 422 ... /bav/refundBet`
- `Input should be a valid integer ... input: bav_game_session_001`
- Follow-on GS wallet state errors:
  - `previous operation is not completed`
  - `Unable to close game session for multi player room`

## Active Errors To Fix Next
1. Casino-side BAV API contract mismatch:
   - `balance`, `betResult`, `refundBet` reject token-form `userId`.
2. Downstream GS wallet tracker loops:
   - debit/refund operation remains pending because upstream wallet call fails.

## Fix Plan (Next)
1. Patch casino-side BAV endpoints to accept token-based `userId` and resolve it to numeric uid from active session table.
   - Targets:
     - `/Users/alexb/Documents/Dev/Casino side/inst_app/igw/app/providers/bsg_bav/endpoints/balance.py`
     - `/Users/alexb/Documents/Dev/Casino side/inst_app/igw/app/providers/bsg_bav/endpoints/bet_result.py`
     - `/Users/alexb/Documents/Dev/Casino side/inst_app/igw/app/providers/bsg_bav/endpoints/refund_bet.py`
2. Keep existing authenticate flow compatible and reuse same token->uid strategy.
3. Re-test launch URL in browser and confirm:
   - no 422 from `/bav/*`
   - GS no longer logs `previous operation is not completed` for this session.
4. Update:
   - `/Users/alexb/Documents/Dev/docs/11-game-launch-forensics.md`
   - `/Users/alexb/Documents/Dev/docs/12-work-diary.md`

## 2026-02-11 Incremental Record: WebSocket Reset Incident

### Incident
- Launch URL reproduced websocket failure:
  - `http://localhost/cwstartgamev2.do?bankId=6274&gameId=838&mode=real&token=bav_game_session_001&lang=en`
- Browser console showed:
  - `WebSocket connection to 'ws://localhost:6300/websocket/mplobby' failed: Error during WebSocket handshake: net::ERR_CONNECTION_RESET`
  - repeated reconnect loop with close code `1006`.

### What was verified
- GS was generating correct redirect and lobby websocket parameter:
  - `WEB_SOCKET_URL=ws://localhost:6300/websocket/mplobby`.
- MP container process existed, but websocket ports were not listening initially.
- In-container socket check before recovery showed no active listeners on `6300/6301`.

### Root cause (runtime state)
- MP service became unhealthy at runtime and was not serving websocket endpoints despite container being `Up`.
- Historical MP log chain in runtime showed Cassandra initialization failures (`NoHostAvailableException` to `c1:9042`) during failed startup cycles.

### Recovery applied
- Restarted MP service:
  - `docker restart gp3-mp-1`
- Re-validated inside container that listeners exist:
  - `:189C` (hex 6300) LISTEN
  - `:189D` (hex 6301) LISTEN

### Post-recovery result
- Re-ran launch in Chrome MCP.
- Previous websocket handshake reset pattern disappeared (no recurring `ERR_CONNECTION_RESET` + `1006` loop).
- Launch path returned to expected template/lobby progression.

### Follow-up
- Keep runtime changelog updates for each launch-debug checkpoint.
- Continue primary remaining fix track: wallet API contract mismatch (`userId` token vs integer) causing pending operation errors.

## 2026-02-11 Incremental Record: BAV `userId` 422 Fix

### Problem
- Casino-side endpoints were returning `422 Unprocessable Entity` for:
  - `/bav/balance`
  - `/bav/betResult`
  - `/bav/refundBet`
- Trigger condition:
  - `userId=bav_game_session_001` (token-like external id) from GS.
- Error detail:
  - `int_parsing` from FastAPI/Pydantic because endpoints typed `userId` as `int`.

### Root cause
- `authenticate` path already supported non-JWT/token-based sessions, but wallet endpoints still enforced integer-only `userId`.
- Additional runtime detail:
  - `casino_side` container does not bind-mount app source (empty mounts), so restart alone does not pick up host code changes unless image is rebuilt.

### Code changes applied
- `/Users/alexb/Documents/Dev/Casino side/inst_app/igw/app/providers/bsg_bav/helpers.py`
  - added `resolve_bav_user_id(db, user_value)` to map token/ext id to numeric `userId`
  - added `hash_ok_user_value(user_value, pass_key, their_hash)` for raw-value hash validation
- `/Users/alexb/Documents/Dev/Casino side/inst_app/igw/app/providers/bsg_bav/endpoints/balance.py`
  - changed `userId` query type `int -> str`
  - hash now validated against raw incoming user value
  - wallet uses resolved numeric `userId`
- `/Users/alexb/Documents/Dev/Casino side/inst_app/igw/app/providers/bsg_bav/endpoints/bet_result.py`
  - changed `userId` query type `int -> str`
  - hash concat uses raw incoming user value
  - DB transaction/wallet ops use resolved numeric `userId`
- `/Users/alexb/Documents/Dev/Casino side/inst_app/igw/app/providers/bsg_bav/endpoints/refund_bet.py`
  - changed `userId` query type `int -> str`
  - hash validation uses provider formula with raw user value:
    `MD5(userId + casinoTransactionId + passkey)`
  - refund lookup/write uses resolved numeric `userId`

### Deployment step required
- Rebuilt and recreated casino-side service so new code is active:
  - `docker compose -f "/Users/alexb/Documents/Dev/Casino side/inst_app/docker-compose.yml" up -d --build`

### Verification
- In-container function signatures now show string user ids:
  - `balance(... userId: str | None ...)`
  - `bet_result(... userId: str ...)`
  - `refund_bet(... userId: str | None ...)`
- Live endpoint checks now succeed with token user id:
  - `/bav/balance?...userId=bav_game_session_001...` -> `200` XML
  - `/bav/betResult?...userId=bav_game_session_001...` -> `200` XML
  - `/bav/refundBet?...userId=bav_game_session_001...` -> `200` XML
- `casino_side` logs confirm `200 OK` for same request pattern.

### Status after fix
- The previous wallet validation blocker (`422 int_parsing`) is fixed.
- Remaining investigation is now only on downstream GS transaction lifecycle cleanup if any stale `STARTED` rows remain.
