# Phase 5 Gameplay Orchestrator Canary Shadow Hook (2026-02-20 16:00:30 UTC)

## Goal
Add a compatibility-facade bridge from GS launch flow to Gameplay Orchestrator in fail-open shadow mode, bank-scoped and backward-compatible.

## Delivered
1. GS routing bridge (new)
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/routing/GameplayOrchestratorRoutingBridge.java`
- Capabilities:
  - reads centralized keys from `cluster-hosts.properties`,
  - evaluates canary decision via `GET /api/v1/gameplay/routing/decision`,
  - shadows launch intent via `POST /api/v1/gameplay/launch-intents`,
  - fail-open behavior on config/network errors.

2. GS launch action hook
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameAction.java`
- Added:
  - gameplay route decision logging + request attribute (`absGameplayRouteDecision`),
  - multiplayer bypass input to decision endpoint,
  - shadow launch-intent submit after session creation.

3. Central config and container wiring already active
- Keys in cluster config and synced env:
  - `GAMEPLAY_ORCHESTRATOR_HOST`, `GAMEPLAY_ORCHESTRATOR_PORT`,
  - `GAMEPLAY_ORCHESTRATOR_ROUTE_ENABLED`, `GAMEPLAY_ORCHESTRATOR_CANARY_BANKS`.
- Compose service present in refactor stack:
  - `gameplay-orchestrator`.

4. Canary probe automation
- Added probe script:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-gameplay-canary-probe.sh`
- Behavior:
  - validates gameplay route decision for bank,
  - triggers GS launch request,
  - checks launch-intent count increase in gameplay-orchestrator.

## Backward compatibility
- No protocol contract changes to existing external endpoints.
- No traffic switch to gameplay-orchestrator by default (`GAMEPLAY_ORCHESTRATOR_ROUTE_ENABLED=false`).
- Shadow path errors only log warning and keep legacy flow running.

## Verification
```bash
rg -n "GameplayOrchestratorRoutingBridge|absGameplayRouteDecision|GAMEPLAY_ORCHESTRATOR" \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/cluster-hosts.properties \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-cluster-hosts.sh \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/.env \
  /Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/resources/cluster-hosts.properties -S

docker compose -f /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml \
  --env-file /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/.env config --services
bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-gameplay-canary-probe.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-gameplay-canary-probe.sh --help
```

## Notes
- Direct probe execution is currently blocked in this sandbox by Docker socket permission denial.

## Next step
- Enable gameplay canary for one bank in refactor stack and add probe script that confirms launch-intent shadow records are created without launch regressions.
