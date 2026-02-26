# MP WebSocket External Port Fix Validation (2026-02-26 08:06 UTC)

## Problem
Game launched but runtime failed in browser because game websocket URL used internal MP port `6300`.

## Root cause
`AbstractStartGameUrlHandler#getRoomUrl` built local/dev room websocket URL using `server.port` (container internal port), not the client-facing websocket endpoint.

## Fix
- Updated:
  - `/Users/alexb/Documents/Dev/Dev_new/mp-server/web/src/main/java/com/betsoft/casino/mp/web/handlers/lobby/AbstractStartGameUrlHandler.java`
- Behavior change:
  - local/dev room websocket host+port are now resolved from websocket handshake URI first,
  - fallback to Origin URI,
  - fallback to `server.port` only if both are unavailable.

## Validation
1. Build passed:
- `mvn -f /Users/alexb/Documents/Dev/Dev_new/mp-server/pom.xml -pl web -am -DskipTests package`

2. Runtime check after MP restart:
- start page URL contains lobby websocket with external mapped port:
  - `...WEB_SOCKET_URL=ws://127.0.0.1:16300/websocket/mplobby`
- iframe URL contains game websocket with external mapped port:
  - `...WEB_SOCKET_URL=ws://127.0.0.1:16300/websocket/mpgame`

3. Browser runtime behavior:
- game websocket opens,
- gameplay updates and balance updates continue,
- no persistent `ERR_CONNECTION_REFUSED` on `mpgame`.

## Evidence
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-080619/startgame-head.txt`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-080619/refactor-onboard-smoke.log`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-080619/mp-websocket-log-excerpt.txt`
- `/Users/alexb/Documents/Dev/Dev_new/docs/release-readiness/run-20260226-080619/timestamp.txt`

## Conclusion
GS↔MP browser communication is restored for game websocket traffic in refactor local runtime.
