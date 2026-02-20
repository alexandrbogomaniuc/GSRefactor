# ABS Gameplay Orchestrator (Scaffold)

Phase 5 scaffold service for gameplay intent orchestration behind compatibility facade.

## Scope in this increment
- Versioned HTTP contract surface for launch/wager/settle intents.
- Bank canary routing decision endpoint.
- Idempotent operation handling by `operationId` per intent type.
- Local outbox list/ack endpoints for future Kafka relay integration.

## Non-goals in this increment
- No replacement of legacy GS gameplay path yet.
- No wallet settlement execution yet.
- No protocol break for existing Casino Side / MP / New Games integrations.

## Run locally
```bash
npm install
PORT=18074 npm start
```

## Key endpoints
- `GET /health`
- `GET /api/v1/gameplay/routing/decision?bankId=271&isMultiplayer=false`
- `POST /api/v1/gameplay/launch-intents`
- `POST /api/v1/gameplay/wager-intents`
- `POST /api/v1/gameplay/settle-intents`
- `GET /api/v1/gameplay/intents`
- `GET /api/v1/outbox`
- `POST /api/v1/outbox/:eventId/ack`
