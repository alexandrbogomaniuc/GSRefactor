# Phase 4 Protocol Adapter GS Shadow Hook (2026-02-20 17:23:09 UTC)

## Goal
Connect GS compatibility facade to the new protocol-adapter service in fail-open shadow mode, without changing legacy launch behavior.

## Delivered
1. New GS bridge:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/routing/ProtocolAdapterRoutingBridge.java`
- Uses centralized keys: `PROTOCOL_ADAPTER_HOST/PORT/ROUTE_ENABLED/CANARY_BANKS`.
- Decision endpoint: `/api/v1/protocol/routing/decision`.
- Shadow call endpoint: `/api/v1/protocol/requests/normalize`.

2. Launch path hook in CW start action:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameAction.java`
- Added request attribute `absProtocolAdapterRouteDecision`.
- Added `ProtocolAdapterRoutingBridge` decision logging and shadow normalize call after session creation.

3. Canary probe script:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-adapter-canary-probe.sh`
- Validates route decision + startgame launch + protocol event growth.

4. Dashboard evidence update:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`

## Backward compatibility and rollback
- Fail-open by design: any protocol-adapter failure keeps legacy flow active.
- Default route remains off (`PROTOCOL_ADAPTER_ROUTE_ENABLED=false`).
- Canary gating remains bank-scoped (`6275` by config).
- Rollback is instant via centralized config (disable route flag).

## Verification
```bash
bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-adapter-canary-probe.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase4-protocol-adapter-canary-probe.sh --help

rg -n "ProtocolAdapterRoutingBridge|absProtocolAdapterRouteDecision|shadowNormalizeStartGame" \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameAction.java \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/routing/ProtocolAdapterRoutingBridge.java
```
