# Phase 4 Protocol Adapter Wallet Shadow Hook (2026-02-20 17:37:37 UTC)

## Goal
Extend GS protocol-adapter shadowing to wager/settle ingress in New Games internal API while preserving legacy financial behavior.

## Delivered
1. Bridge enhancement
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/routing/ProtocolAdapterRoutingBridge.java`
- Added `shadowNormalizeWalletOperation(...)` for fail-open shadow POST normalization.

2. Reserve/settle ingress hooks
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/api/newgames/NewGamesInternalApiServlet.java`
- Added shadow calls after successful reserve/settle transaction processing:
  - `/gs-internal/newgames/v1/wallet/reserve`
  - `/gs-internal/newgames/v1/wallet/settle`

3. Canary probe script
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-wallet-canary-probe.sh`
- Executes reserve+settle flow and validates protocol-adapter event growth.

## Compatibility and rollback
- Shadow-only, fail-open behavior.
- Legacy reserve/settle response path unchanged.
- Route remains disabled by default via `PROTOCOL_ADAPTER_ROUTE_ENABLED=false`.

## Verification
```bash
bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-wallet-canary-probe.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-wallet-canary-probe.sh --help

rg -n "shadowNormalizeWalletOperation|shadowProtocolWalletNormalize|/wallet/reserve|/wallet/settle" \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/routing/ProtocolAdapterRoutingBridge.java \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/web/api/newgames/NewGamesInternalApiServlet.java
```
