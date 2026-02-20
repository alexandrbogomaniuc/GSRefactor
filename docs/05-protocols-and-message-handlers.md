# 05 Protocols And Message Handlers

## Plain-English Summary
The multiplayer protocol is JSON over WebSocket.
There are two main socket channels:
- lobby channel (`/websocket/mplobby`) for entering lobby and getting start-game URLs
- game channel (`/websocket/mpgame`) for room/game actions (sit in, bet, crash bet, round results)

Both MaxQuest and Crash protocol docs use message `"class"` names (for example `EnterLobby`, `OpenRoom`, `CrashBet`) instead of numeric message IDs.

## Identity Parameter Note (Wallet Integration)
- During CWv3 launch, GS authenticates token via wallet API and receives `USERID`.
- GS account/session/wallet flow depends on GS `externalId` mapping in Cassandra.
- If GS `externalId` and casino-side numeric `USERID` diverge, tools/operations can target different identities.
- This is not a websocket protocol error; it is an identity-mapping/config consistency issue.

## Evidence Sources Read
- `/Users/alexb/Documents/Dev/readme all you need to know from md files/MaxQuest_ProtocolV2.txt`
- `/Users/alexb/Documents/Dev/readme all you need to know from md files/CrashGame_Protocol.txt`
- `/Users/alexb/Documents/Dev/mq-mp-clean-version/web/src/main/java/com/betsoft/casino/mp/config/WebSocketRouter.java`
- `/Users/alexb/Documents/Dev/mq-mp-clean-version/web/src/main/java/com/betsoft/casino/mp/web/socket/LobbyWebSocketHandler.java`
- `/Users/alexb/Documents/Dev/mq-mp-clean-version/web/src/main/java/com/betsoft/casino/mp/web/socket/GameWebSocketHandler.java`
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/BaseStartGameAction.java`
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/free/mp/template.jsp`

## Transport And Endpoint Map (Code-Proven)
| Layer | Protocol | Endpoint | Where proven |
|---|---|---|---|
| Client -> MP lobby | WebSocket JSON | `/websocket/mplobby` | `WebSocketRouter.webSocketMapping(...)` in `/Users/alexb/Documents/Dev/mq-mp-clean-version/web/src/main/java/com/betsoft/casino/mp/config/WebSocketRouter.java` |
| Client -> MP game | WebSocket JSON | `/websocket/mpgame` | `WebSocketRouter.webSocketMapping(...)` in `/Users/alexb/Documents/Dev/mq-mp-clean-version/web/src/main/java/com/betsoft/casino/mp/config/WebSocketRouter.java` |
| GS start action -> template | HTTP redirect | `/<mode>/mp/template.jsp` | `BaseStartGameAction.getMultiPlayerForward(...)` in `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/BaseStartGameAction.java` |
| Template -> client runtime params | JS object `getParams()` | includes `websocket`, `bankId`, `sessionId`, `gameId` | `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/free/mp/template.jsp` |

## GS Start HTTP Routes (Code-Proven)
These are GS HTTP entry points that start or restart game sessions before template/websocket steps.

| HTTP route | Action class |
|---|---|
| `/cwstartgamev2` | `com.dgphoenix.casino.actions.enter.game.cwv3.CWStartGameAction` |
| `/bsstartgame` | `com.dgphoenix.casino.actions.enter.game.bonus.BSStartGameAction` |
| `/restartgame` | `com.dgphoenix.casino.actions.enter.game.frb.RestartGameAction` |
| `/cwstartgameidfrb` | `com.dgphoenix.casino.actions.game.cwv3.frb.CWStartFRBGameAction` |

Evidence:
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml:394`
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml:466`
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml:496`
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/WEB-INF/struts-config.xml:534`

## Message Envelope (Spec-Proven)
From both protocol docs:
- interaction is via WebSocket JSON
- each message has `"class"` name
- requests include `"rid"` (request id)
- error responses include `"code"`, `"msg"`, `"class":"Error"`, `"rid"`

Examples and sections:
- `EnterLobby` request and `EnterLobbyResponse`
- `BalanceUpdated` async event
- `GetStartGameUrl` / `GetStartGameUrlResponse`
- Crash-specific: `CrashBet`, `CrashCancelBet`

Source:
- `/Users/alexb/Documents/Dev/readme all you need to know from md files/MaxQuest_ProtocolV2.txt`
- `/Users/alexb/Documents/Dev/readme all you need to know from md files/CrashGame_Protocol.txt`

## Error Model (Spec-Proven)
Error code ranges (both docs):
- `1-999` = fatal errors
- `1000-4999` = errors
- `5000-9999` = warnings

Selected codes listed in both docs:
- `1 INTERNAL_ERROR`
- `2 SERVER_SHUTDOWN`
- `3 INVALID_SESSION`
- `1000 ILLEGAL_NICKNAME`
- `1001 BAD_REQUEST`
- `1002 NOT_LOGGED_IN`
- `1003 ROOM_NOT_FOUND`

Source:
- `/Users/alexb/Documents/Dev/readme all you need to know from md files/MaxQuest_ProtocolV2.txt` (section `4. Error codes`)
- `/Users/alexb/Documents/Dev/readme all you need to know from md files/CrashGame_Protocol.txt` (section `3. Error codes`)

## Message Class -> Runtime Handler Map (Code-Proven)
Lobby channel mappings are registered in `LobbyWebSocketHandler`:
- `EnterLobby` -> `EnterLobbyHandler`
- `GetRoomInfo` -> `GetRoomInfoHandler`
- `GetStartGameUrl` -> `GetStartGameUrlHandler`
- `RefreshBalance` -> `LobbyRefreshBalanceHandler`
- `ReBuy` -> `LobbyReBuyHandler`

Evidence:
- `/Users/alexb/Documents/Dev/mq-mp-clean-version/web/src/main/java/com/betsoft/casino/mp/web/socket/LobbyWebSocketHandler.java` (`register(...)` calls in constructor)

Game channel mappings are registered in `GameWebSocketHandler`:
- `OpenRoom` -> `OpenRoomHandler`
- `SitIn` -> `SitInHandler`
- `SitOut` -> `SitOutHandler`
- `BuyIn` -> `BuyInHandler`
- `ReBuy` -> `ReBuyHandler`
- `BetLevel` -> `BetLevelHandler`
- `RefreshBalance` -> `RefreshBalanceHandler`
- `CrashBet` -> `CrashBetHandler`
- `CrashCancelBet` -> `CrashCancelBetHandler`
- `CloseRoundResults` -> `CloseRoundResultsHandler`

Evidence:
- `/Users/alexb/Documents/Dev/mq-mp-clean-version/web/src/main/java/com/betsoft/casino/mp/web/socket/GameWebSocketHandler.java` (`register(...)` calls in constructor)

## GS -> MP Start Link (Code-Proven)
GS prepares multiplayer launch by passing a websocket URL to the template:
- `redirect.addParameter(BaseAction.WEB_SOCKET_URL, mpLobbyUrl + "/websocket/mplobby")`

Evidence:
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/BaseStartGameAction.java`

Template checks required params and fails fast if missing:
- `bankId` required
- `SID` required
- `gameId` required
- on failure: redirects to `/error_pages/sessionerror.jsp`

Evidence:
- `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/free/mp/template.jsp`

## Gaps / Not Found Yet
- Numeric protocol message IDs are not present in the current `MaxQuest_ProtocolV2.txt` and `CrashGame_Protocol.txt` docs (class-name routing is used instead).
- If a binary ID mapping exists, it is not found yet in the currently inspected files.
- Next search target: serializer/transport object factory internals in MP.
