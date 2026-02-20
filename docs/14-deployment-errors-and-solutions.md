# 14 Deployment Errors And Solutions

## Purpose
Single incident report for the full local deployment/debug cycle:
- what failed,
- how it was diagnosed,
- what was changed,
- what is the current status.

## Environment
- GS: `gp3-gs-1` (`81 -> 8080`)
- MP: `gp3-mp-1` (`6300/6301`)
- Static: `gp3-static-1` (`80 -> 80`)
- Cassandra: `gp3-c1-1` (`9142 -> 9042`)
- Kafka: `gp3-kafka-1`
- Zookeeper: `gp3-zookeeper-1`
- Casino-side wallet API: `casino_side` (`8000 -> 8000`)

## Incident Timeline (Problem -> Fix -> Result)

### A) Missing/incorrect bank-game runtime mapping
- Problem:
  - launch behavior differed between banks; missing rows/indexes for game `838`.
- Diagnosis:
  - Cassandra queries on `rcasinoscks.gameinfocf` showed missing or incomplete `6275+838` and bad `gameTypeId/index` values.
- Fix:
  - applied CQL patches:
    - `/Users/alexb/Documents/Dev/docs/sql/add_game_838_for_bank_6275.cql`
    - `/Users/alexb/Documents/Dev/docs/sql/fix_838_gameType_to_mp.cql`
  - ensured `VND` currency row exists for bank `6275`.
- Result:
  - both `6274+838` and `6275+838` rows present with correct MP type/indexes.

### B) Support tool usage error (`bankSelectAction` without bankId)
- Problem:
  - `/support/bankSelectAction.do` returned `500`.
- Diagnosis:
  - action requires explicit `bankId`; missing value triggers `NumberFormatException`.
- Fix:
  - operational rule: always call `/support/bankSelectAction.do?bankId=<id>`.
- Result:
  - support editor works for `6274`, `6275`, `271`.

### C) Game handoff 404 after template
- Problem:
  - launch reached `template.jsp`, then `/html5pc/actiongames/dragonstone/game/?...` returned `404`.
- Diagnosis:
  - static nginx rule only checked exact file path (`try_files $uri =404`) and failed on directory URL with query.
- Fix:
  - static nginx updated to:
  - `try_files $uri $uri/ $uri/index.html =404;`
  - static service rebuilt/recreated.
- Result:
  - handoff endpoint now returns `200`; game assets load.

### D) MP websocket reset during lobby connect
- Problem:
  - browser showed websocket handshake reset (`ERR_CONNECTION_RESET`) and reconnect loop.
- Diagnosis:
  - MP process existed but runtime listener health was unstable.
- Fix:
  - restarted `gp3-mp-1`; verified listeners active on `6300/6301`.
- Result:
  - websocket reset loop removed for lobby connection.

### E) Wallet API `422` due to `userId` type mismatch
- Problem:
  - GS sent token-like `userId` (`bav_game_session_001`) to casino-side balance/wager/refund.
  - casino-side expected int -> `422 Unprocessable Entity`.
- Diagnosis:
  - GS logs + casino-side logs + direct endpoint checks.
- Fix:
  - patched casino-side BAV endpoints to accept `userId` as string and resolve token->numeric uid.
  - fixed hash handling to use raw incoming user value where required.
  - rebuilt/redeployed `casino_side` container.
- Result:
  - `/bav/balance`, `/bav/betResult`, `/bav/refundBet` return `200` for token-style userId tests.

### F) walletsManager "Cannot find the player" for `extUserId=8`
- Problem:
  - support tool could not find player by external id `8`.
- Diagnosis:
  - `accountcf_ext` mapping in this environment is token-based:
  - `bankid=6274`, `extid=bav_game_session_001` -> `accountid=40962`.
- Fix:
  - use `extUserId=bav_game_session_001` or `accountId=40962` for pending-op management.
- Result:
  - pending rows visible and can be cleaned.

### G) GS log noise obscuring signal
- Problem:
  - repetitive warnings (`Bad header found`, NTP timeout) polluted logs during support testing.
- Fix:
  - adjusted active runtime/source log4j config to suppress these classes to `ERROR`.
  - added long-term code-level noise throttling (pending rebuild for source-only parts).
- Result:
  - cleaner GS logs for launch/wallet debugging.

## Database Changes Summary
- `rcasinoscks.gameinfocf`
  - ensured rows: `6274+838`, `6275+838`
  - set MP type via `gameTypeId=1`
  - fixed indexes: `bankidx`, `bankandcuridx`
- `rcasinoscks.currencycf`
  - added `VND` row for bank `6275` compatibility.
- `rcasinoscks.accountcf_ext`
  - observed current active mapping:
  - `6274 + bav_game_session_001 -> 40962`

## Port/Routing Changes Summary
- Static nginx routing fixed for directory handoff URL:
  - enabled `$uri/` and `$uri/index.html` fallback.
- Verified runtime ports:
  - GS `81`, Static `80`, MP `6300/6301`, Casino-side `8000`, Cassandra `9142`.

## Current Status
- Proven working:
  - launch URL reaches template/lobby/game bootstrap,
  - static handoff is fixed,
  - websocket transport is stable after MP recovery,
  - casino-side wallet endpoints accept token-style userId.
- Active risk:
  - identity consistency (`USERID=8` vs token externalId mapping) must be standardized to avoid tool/flow mismatch.

## Remaining Errors / Open Items
1. Identity mapping policy is not unified yet.
- Need single canonical external-id mapping for this bank profile.
2. Full round lifecycle verification still pending.
- Need one complete run: launch -> wager -> settle -> no stuck `STARTED` operation.

## Plan To Close Remaining Issues
1. Decide canonical external id per bank (`wallet USERID` vs token) and enforce in one place.
2. Recreate/refresh affected account mapping rows in test bank to match chosen policy.
3. Run scripted end-to-end validation and store logs (GS + MP + casino-side) by SID/rid.
4. Update flow docs with final identity policy and regression checklist.
