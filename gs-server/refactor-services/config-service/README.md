# ABS Config Service (Refactor Foundation)

First extracted microservice for config workflow modernization.

## Goals
- Keep current GS behavior backward-compatible.
- Introduce isolated config workflow lifecycle API.
- Provide outbox-compatible event stream for Kafka publisher integration.

## Endpoints
- `GET /health`
- `GET /api/v1/config/drafts?bankId=...`
- `GET /api/v1/config/drafts/:draftVersion`
- `POST /api/v1/config/drafts`
- `POST /api/v1/config/workflow/{validate|approve|publish|rollback}`
- `GET /api/v1/outbox?status=NEW`
- `POST /api/v1/outbox/:eventId/ack`

## Draft payload example
```json
{
  "draftVersion": "draft-20260220-1",
  "bankId": "6274",
  "performedBy": "ops-user",
  "changeReason": "Enable new limits",
  "payload": {
    "KEY_GL_MIN_BET": "1",
    "KEY_GL_MAX_BET": "5000"
  }
}
```

## Notes
- Persistence is file-based in `/data/config-workflow-store.json` for initial bootstrap.
- This is phase foundation; next step is Cassandra-backed storage and Kafka producer/outbox dispatcher.
