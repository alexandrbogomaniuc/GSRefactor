# Phase 5 Bonus/FRB Service Scaffold (2026-02-20 16:15:00 UTC)

## Goal
Start extraction step #5 (Bonus/FRB Service) with idempotent action contracts and isolated refactor deployment wiring.

## Delivered
1. New bonus-frb-service scaffold
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/bonus-frb-service/package.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/bonus-frb-service/Dockerfile`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/bonus-frb-service/src/store.js`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/bonus-frb-service/src/server.js`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/bonus-frb-service/README.md`

2. Versioned contract
- Added OpenAPI:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/contracts/openapi/bonus-frb-service-v1.yaml`
- Updated contracts index:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/contracts/README.md`

3. Centralized config and compose wiring
- Added cluster keys:
  - `BONUS_FRB_SERVICE_HOST`
  - `BONUS_FRB_SERVICE_PORT`
  - `BONUS_FRB_SERVICE_ROUTE_ENABLED`
  - `BONUS_FRB_SERVICE_CANARY_BANKS`
- Updated:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/cluster-hosts.properties`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-cluster-hosts.sh`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/README.md`

4. Progress dashboard update
- `se-bonus-service` moved to `in_progress` with this evidence in:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`

## Backward compatibility
- Legacy FRB path remains active.
- Refactor route flag defaults to disabled (`BONUS_FRB_SERVICE_ROUTE_ENABLED=false`).
- No external protocol contract changes.

## Verification
```bash
node --check /Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/bonus-frb-service/src/store.js
node --check /Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/bonus-frb-service/src/server.js
bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-cluster-hosts.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-cluster-hosts.sh

docker compose -f /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml \
  --env-file /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/.env config --services
```
