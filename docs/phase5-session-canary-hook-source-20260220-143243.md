# Phase 5 GS Canary Hook (Source-Level)

Timestamp (UTC): 2026-02-20 14:32:43 UTC
Scope: add GS compatibility-facade hook in launch entry path to consume session-service canary routing decision with fail-open fallback.

## Changes
- Added routing bridge class:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/routing/SessionServiceRoutingBridge.java`
- Wired hook into launch action:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameAction.java`

## Behavior
- Reads centralized routing keys from classpath `cluster-hosts.properties`:
  - `SESSION_SERVICE_HOST`, `SESSION_SERVICE_PORT`, `SESSION_SERVICE_ROUTE_ENABLED`, `SESSION_SERVICE_CANARY_BANKS`.
- Decision flow is fail-open:
  - if disabled/not canary/config missing/remote decision unavailable -> keep legacy monolith path unchanged.
- For canary-enabled banks only:
  - calls `GET /api/v1/routing/decision?bankId=...`.
  - if `routeToSessionService=true`, posts shadow session create to `POST /api/v1/sessions/create` with idempotent `operationId=launch:<bankId>:<sessionId>`.
- No blocking dependency:
  - any network/error path logs warning and continues legacy processing.

## Code evidence
- Route decision invocation in launch flow:
  - `CWStartGameAction` now computes decision at entry and stores request attribute `absSessionRouteDecision`.
- Shadow sync invocation:
  - `CWStartGameAction` calls `shadowCreateSession(...)` after `sessionId` is created.
- Bridge HTTP contracts:
  - decision endpoint and create endpoint paths hardcoded in bridge with low timeouts and safe fallback.

## Verification executed
- Source checks:
  - `rg -n "SessionServiceRoutingBridge|absSessionRouteDecision|shadowCreateSession|routing/decision|sessions/create"` on `web-gs` sources.
- Runtime note:
  - current refactor runtime WAR does not include this new source until GS rebuild/redeploy; behavior is source-ready, deployment pending.
