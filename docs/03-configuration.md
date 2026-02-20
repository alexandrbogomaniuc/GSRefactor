# 03 Configuration

## Goal
Map how runtime configuration is loaded and overridden.

## Contents
- Maven profile properties
- Application properties
- Environment variables
- XML cache exports/imports
- Cassandra-backed configuration

## Runtime-Effective Configuration Sources (Local)
1. Cassandra `rcasinoscks.bankinfocf.jcn`
- Bank-level runtime behavior (wallet URLs, websocket URL, flags like `STUB_MODE`, `ADD_TOKEN_MODE`).
2. Cassandra `rcasinoscks.gameinfocf.jcn`
- Game-level mapping (`gameTypeId`, provider mapping, bank-game rows like `6274+838`).
3. Runtime GS classpath config
- `/Users/alexb/Documents/Dev/Doker/runtime-gs/webapps/gs/ROOT/WEB-INF/classes/*`
- Includes active log config (`log4j2.xml`, `log4j2_high.xml`) and loaded cache exports.
4. Docker compose wiring
- Port bindings and inter-container DNS/hostnames.

## Important Flags Impacting Current Project
- `STUB_MODE`:
  - If enabled in wallet client flow, some auth behavior can use token as external id.
  - This can change mapping in `accountcf_ext` and affect tools like walletsManager.
- `ADD_TOKEN_MODE`:
  - GS stores and sends token for wallet requests where required.
- `MP_LOBBY_WS_URL`:
  - Controls websocket URL emitted to client; wrong host/port causes lobby websocket failure.

## Known High-Risk Mismatch Pattern
- Casino-side auth returns numeric `<USERID>8</USERID>`, but GS account external id may still be token-based (for example `bav_game_session_001`) in current data.
- Effect:
  - walletsManager lookup by `extUserId=8` fails,
  - lookup by `extUserId=bav_game_session_001` succeeds.
- Evidence:
  - `accountcf_ext` rows for bank `6274`.
