# Phase 5 - Session Service Foundation (Microservice Extraction #2)

## Scope
Introduced standalone Session Service in isolated refactor stack:
- `gs-server/refactor-services/session-service`

This extraction is additive and keeps legacy GS session endpoints intact.

## Delivered
1. Service implementation
- Idempotent session lifecycle APIs (`create`, `touch`, `close`) using `operationId` index.
- Session query APIs (single session and filtered list).
- Outbox foundation for async publication.

2. API endpoints
- `GET /health`
- `GET /api/v1/sessions?bankId=...&status=...`
- `GET /api/v1/sessions/:bankId/:sessionId`
- `POST /api/v1/sessions/create`
- `POST /api/v1/sessions/touch`
- `POST /api/v1/sessions/close`
- `GET /api/v1/outbox?status=NEW`
- `GET /api/v1/outbox/replay-report?limit=...`
- `POST /api/v1/outbox/:eventId/ack`
- `POST /api/v1/outbox/:eventId/requeue?reason=...`

API contract file:
- `gs-server/refactor-services/contracts/openapi/session-service-v1.yaml`

3. Refactor stack wiring
- Added `session-service` container to:
  - `gs-server/deploy/docker/refactor/docker-compose.yml`
- Added centralized keys:
  - `SESSION_SERVICE_HOST`
  - `SESSION_SERVICE_PORT`
  - `SESSION_SERVICE_KAFKA_BROKERS`
  - `SESSION_SERVICE_OUTBOX_TOPIC`
  - `SESSION_SERVICE_OUTBOX_RELAY_ENABLED`
  - `SESSION_SERVICE_OUTBOX_RELAY_POLL_MS`
  - `SESSION_SERVICE_OUTBOX_REPLAY_MAX_COUNT`
  - `SESSION_SERVICE_OUTBOX_REPLAY_WINDOW_SECONDS`

4. GS compatibility hook (source-level, fail-open)
- Added launch-entry routing bridge:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/routing/SessionServiceRoutingBridge.java`
- Wired into launch action:
  - `gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameAction.java`
- Behavior:
  - checks canary routing decision for bank,
  - when enabled, sends best-effort shadow `sessions/create` with idempotent operationId,
  - on any error/timeouts, falls back to legacy path without request failure.

5. Outbox relay (feature-flagged)
- Added Kafka relay worker:
  - `gs-server/refactor-services/session-service/src/outboxRelay.js`
- Wired service startup/shutdown lifecycle:
  - `gs-server/refactor-services/session-service/src/server.js`
- Behavior:
  - when `SESSION_SERVICE_OUTBOX_RELAY_ENABLED=true`, polls NEW outbox events and publishes to configured Kafka topic,
  - applies retry backoff on publish failure and moves exhausted events to DLQ,
  - marks events SENT only after producer send success,
  - default remains disabled for safe compatibility rollout.

## Evidence
- Health:
  - `curl -fsS http://127.0.0.1:18073/health`
- Idempotency:
  - first `POST /sessions/create` => `idempotent:false`
  - second same `operationId` => `idempotent:true`
- Lifecycle:
  - `touch` updates `lastTouchedAt`
  - `close` sets status `CLOSED` + reason.
- Outbox:
  - `GET /api/v1/outbox?status=NEW` returns `session.created`, `session.touched`, `session.closed` events.
- Hook evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase5-session-canary-hook-source-20260220-143243.md`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase5-session-canary-runtime-activation-20260220-143713.md`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase5-session-canary-live-validation-20260220-144933.md`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase5-session-outbox-relay-foundation-20260220-145547.md`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase5-session-outbox-dlq-contract-gate-20260220-151251.md`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase5-session-replay-window-and-report-20260220-153020.md`

## Backward compatibility
- Existing GS monolith still handles production session path.
- New Session Service is isolated in refactor stack and currently not on production request path.

## Risks / gaps
- Storage is file-backed (`/data/session-service-store.json`) for bootstrap only.
- No authn/authz or per-bank policy checks yet.
- Outbox relay is enabled in refactor canary runtime only; downstream consumer-side alerting/DLQ replay operations are pending.

## Next actions
1. Add durable storage (Cassandra-backed session tables with compatibility index).
2. Add session policy validation (`isMultiplayer`, timeouts, bank constraints).
3. Add DLQ replay tool + consumer monitoring alerts for canary-to-wave-B promotion.
