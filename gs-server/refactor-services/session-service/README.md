# ABS Session Service (Refactor Foundation)

Second extracted microservice in target order (after Config Service).

## Goals
- Isolate session lifecycle operations from monolith.
- Enforce idempotent session create/touch/close operations.
- Emit outbox events for Kafka publication.

## Endpoints
- `GET /health`
- `GET /api/v1/routing/decision?bankId=...`
- `GET /api/v1/sessions?bankId=...&status=...`
- `GET /api/v1/sessions/:bankId/:sessionId`
- `POST /api/v1/sessions/create`
- `POST /api/v1/sessions/touch`
- `POST /api/v1/sessions/close`
- `GET /api/v1/outbox?status=NEW`
- `GET /api/v1/outbox/replay-report?limit=20`
- `POST /api/v1/outbox/:eventId/ack`
- `POST /api/v1/outbox/:eventId/requeue?reason=...`

## Notes
- File-backed persistence at `/data/session-service-store.json`.
- Outbox event types: `session.created`, `session.touched`, `session.closed`.
- Canary routing controls via env:
  - `SESSION_SERVICE_ROUTE_ENABLED`
  - `SESSION_SERVICE_CANARY_BANKS` (comma-separated bank IDs)
- Kafka outbox relay controls via env:
  - `SESSION_SERVICE_OUTBOX_RELAY_ENABLED` (`false` by default)
  - `SESSION_SERVICE_KAFKA_BROKERS` (for example `kafka:9092`)
  - `SESSION_SERVICE_OUTBOX_TOPIC` (default `abs.session.events.v1`)
  - `SESSION_SERVICE_OUTBOX_DLQ_TOPIC` (default `abs.session.events.dlq.v1`)
  - `SESSION_SERVICE_OUTBOX_RELAY_POLL_MS`
  - `SESSION_SERVICE_OUTBOX_MAX_ATTEMPTS`
  - `SESSION_SERVICE_OUTBOX_RETRY_BASE_MS`
  - `SESSION_SERVICE_OUTBOX_BATCH_LIMIT`
  - `SESSION_SERVICE_OUTBOX_REPLAY_MAX_COUNT`
  - `SESSION_SERVICE_OUTBOX_REPLAY_WINDOW_SECONDS`
- Next phase: Cassandra-backed session store and monolith compatibility-facade routing.
