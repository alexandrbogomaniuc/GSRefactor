# GS Capability Map (Initial, Code-Backed)

Last updated: 2026-02-11

This map is based on direct Java source inspection in `mq-gs-clean-version`.

## 1) Session Lifecycle And Locking
- Thread-local domain session orchestration:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/SessionHelper.java`
- Bank-specific session manager factory:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/sm/PlayerSessionFactory.java`
- Session cache/touch mechanisms:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/cache/PingSessionCache.java`
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/PingSessionAction.java`

## 2) Game Session Control
- MP-specific session finishing and sit-out flow:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/services/mp/MPGameSessionService.java`
- Session manager persistence access:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/session/GameSessionManager.java`
- MP controller endpoint:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/controller/mqb/MPGameSessionController.java`

## 3) Start-Game Routing And Launch Context
- Main GS launch action:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameAction.java`
- Core forward generation and multiplayer template handoff:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/BaseStartGameAction.java`
- Multiplayer template websocket parameter creation:
  - `WEB_SOCKET_URL` setup in `getMultiPlayerForward(...)` in the same file.

## 4) Wallet And Pending Operation Management
- Wallet operation protocols/factories:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/wallet/WalletProtocolFactory.java`
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/payment/wallet/WalletHelper.java`
- Support tooling to inspect/delete stuck wallet/last hand/frb ops:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/support/walletsmanager/WalletsManagerAction.java`
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/tools/walletsManager.jsp`

## 5) History And Last Hand
- Central history manager:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/gs/managers/game/history/HistoryManager.java`
- History web/action stack:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/history/GameHistoryServlet.java`
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/history/GameHistoryListAction.java`
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/history/vba/HistoryByRoundAction.java`
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/api/history/vba/HistoryByTokenAction.java`
- Last hand support paths (UI + persistence references) are present under:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/webapp/support/games/getLasthands.jsp`
  - lasthand deletion path in `WalletsManagerAction`.

## 6) Error Capture And Support Ticketing
- Error persistence helper with support-ticket enrichment:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/support/ErrorPersisterHelper.java`
- Session-level support error cache:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/SessionErrorsCache.java`

## 7) Bank/Domain/Launch Policy Controls
- Bank config model with launch/security toggles (`ALLOWED_ORIGIN`, `ALLOWED_DOMAINS`, `LOGOUT_ON_ERROR`, MQ toggles):
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/common/src/main/java/com/dgphoenix/casino/common/cache/data/bank/BankInfo.java`
- Domain whitelist cache registration:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/common-gs/src/main/java/com/dgphoenix/casino/cache/CachesHolder.java`
- Support UI actions for bank properties and domain whitelist:
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/editproperties/EditPropertiesAction.java`
  - `/Users/alexb/Documents/Dev/mq-gs-clean-version/game-server/web-gs/src/main/java/com/dgphoenix/casino/support/cache/bank/edit/actions/domains/DomainWhiteListAction.java`

## Practical Conclusion
- GS already contains the core capabilities needed for the new games project.
- For phase 1, we should extend through explicit integration points rather than rebuilding these capabilities.

