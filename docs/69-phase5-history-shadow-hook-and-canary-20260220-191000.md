# Phase 5 History Shadow Hook and Canary (2026-02-20 19:10 UTC)

## What was done
- Added GS fail-open history-service shadow bridge and wired New Games history-write path to it.
- Added history-service bank canary routing decision endpoint.
- Added history canary probe script for append/query validation.

## Files changed
- GS bridge + wiring:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/routing/HistoryServiceRoutingBridge.java`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/api/newgames/NewGamesInternalApiServlet.java`
- History service/contracts:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/history-service/src/server.js`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/history-service/README.md`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/contracts/openapi/history-service-v1.yaml`
- Canary tooling:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-history-canary-probe.sh`

## Backward compatibility
- Legacy history path remains authoritative.
- Shadow path is fail-open and logs-only on errors.
- No protocol contract change for existing integrations.

## Result
- History extraction now has executable canary shadow coverage aligned with Phase 5 migration policy.
