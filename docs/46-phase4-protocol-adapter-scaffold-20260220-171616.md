# Phase 4 Protocol Adapter Scaffold (2026-02-20 17:16:16 UTC)

## Goal
Execute Phase 4 increment by adding a dedicated protocol adapter service for per-bank JSON/XML mode, boundary normalization, and JSON hash policy controls without changing legacy runtime behavior.

## Delivered
1. New protocol-adapter service scaffold
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/protocol-adapter/package.json`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/protocol-adapter/Dockerfile`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/protocol-adapter/src/store.js`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/protocol-adapter/src/server.js`
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/protocol-adapter/README.md`

2. Versioned protocol contract
- Added OpenAPI:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/contracts/openapi/protocol-adapter-v1.yaml`
- Updated contract index:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/contracts/README.md`

3. Centralized config and refactor compose wiring
- Added cluster config keys:
  - `PROTOCOL_ADAPTER_HOST`
  - `PROTOCOL_ADAPTER_PORT`
  - `PROTOCOL_ADAPTER_ROUTE_ENABLED`
  - `PROTOCOL_ADAPTER_CANARY_BANKS`
  - `PROTOCOL_ADAPTER_DEFAULT_MODE`
  - `PROTOCOL_ADAPTER_BANK_MODES`
  - `PROTOCOL_ADAPTER_JSON_HASH_ENABLED`
  - `PROTOCOL_ADAPTER_JSON_HASH_HEADER`
  - `PROTOCOL_ADAPTER_JSON_HASH_ENFORCEMENT_MODE`
  - `PROTOCOL_ADAPTER_JSON_REPLAY_ENABLED`
  - `PROTOCOL_ADAPTER_JSON_REPLAY_WINDOW_SECONDS`
  - `PROTOCOL_ADAPTER_JSON_NONCE_TTL_SECONDS`
  - `PROTOCOL_ADAPTER_JSON_SECRET_REFS`
- Updated files:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/cluster-hosts.properties`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-cluster-hosts.sh`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/README.md`

4. Checklist/dashboard evidence update
- `ip-json-xml-mode` now points to this scaffold evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/data/modernization-checklist.json`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/modernizationProgress.html`

## Backward compatibility
- Legacy XML handling remains active by default.
- Protocol adapter route default is disabled (`PROTOCOL_ADAPTER_ROUTE_ENABLED=false`).
- JSON hash defaults are non-breaking (`PROTOCOL_ADAPTER_JSON_HASH_ENABLED=false`, enforcement metadata preset to `SHADOW`).
- No current Casino Side / MP / New Games contract is replaced in this increment.

## Verification commands
```bash
node --check /Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/protocol-adapter/src/store.js
node --check /Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/protocol-adapter/src/server.js

bash -n /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-cluster-hosts.sh
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-cluster-hosts.sh

docker compose -f /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml \
  --env-file /Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/.env config --services
```
