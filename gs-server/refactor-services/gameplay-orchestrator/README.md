# ABS Gameplay Orchestrator (Scaffold)

Phase 5 scaffold service for gameplay intent orchestration behind compatibility facade.

## Scope in this increment
- Versioned HTTP contract surface for launch/wager/settle intents.
- Bank canary routing decision endpoint.
- Idempotent operation handling by `operationId` per intent type.
- Local outbox list/ack endpoints for future Kafka relay integration.
- Deterministic state-blob API with Redis-first cache and file fallback.

## Non-goals in this increment
- No replacement of legacy GS gameplay path yet.
- No wallet settlement execution yet.
- No protocol break for existing Casino Side / MP / New Games integrations.

## Run locally
```bash
npm install
PORT=18074 npm start
```

## State blob backend
- `GAMEPLAY_STATE_CACHE_BACKEND=file|redis` (default `file`)
- `GAMEPLAY_STATE_BLOB_TTL_SECONDS=900`
- Redis options:
  - `REDIS_URL` OR
  - `REDIS_HOST`, `REDIS_PORT`, `REDIS_DB`
- If Redis is unavailable, the service degrades to local file storage and returns `degradedFromRedis=true`.

## Key endpoints
- `GET /health`
- `GET /api/v1/gameplay/routing/decision?bankId=271&isMultiplayer=false`
- `POST /api/v1/gameplay/launch-intents`
- `POST /api/v1/gameplay/wager-intents`
- `POST /api/v1/gameplay/settle-intents`
- `PUT /api/v1/gameplay/state-blobs/:stateKey`
- `GET /api/v1/gameplay/state-blobs/:stateKey`
- `GET /api/v1/gameplay/intents`
- `GET /api/v1/outbox`
- `POST /api/v1/outbox/:eventId/ack`
