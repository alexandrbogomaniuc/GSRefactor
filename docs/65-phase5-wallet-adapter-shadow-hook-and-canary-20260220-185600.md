# Phase 5 Wallet Adapter Shadow Hook and Canary (2026-02-20 18:56 UTC)

## What was done
- Added GS fail-open wallet-adapter shadow bridge and wired New Games wallet flow to it.
- Added wallet-adapter bank canary routing decision endpoint.
- Added wallet-adapter canary probe script for reserve/settle shadow validation.

## Files changed
- GS bridge + wiring:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/routing/WalletAdapterRoutingBridge.java`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/api/newgames/NewGamesInternalApiServlet.java`
- Wallet-adapter service/contracts:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/wallet-adapter/src/server.js`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/wallet-adapter/README.md`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/contracts/openapi/wallet-adapter-v1.yaml`
- Canary tooling:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-wallet-adapter-canary-probe.sh`

## Backward compatibility
- Legacy wallet flow remains authoritative.
- Shadow path is fail-open and logs-only on errors.
- No protocol changes for existing integrations.

## Result
- Wallet-adapter extraction now has executable canary shadow coverage aligned with Phase 5 migration policy.
