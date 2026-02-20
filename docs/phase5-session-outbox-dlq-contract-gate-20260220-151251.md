# Phase 5 Session Outbox DLQ + Contract Gate (2026-02-20 15:12:51)

## Goal
Harden session-service outbox publication with retry/DLQ policy and add executable event contract validation for canary rollout gates.

## Delivered
1. Outbox retry + DLQ behavior in service store
- File: `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/session-service/src/store.js`
- Added:
  - outbox fields: `attempts`, `lastError`, `nextAttemptAt`, `dlqAt`, `replayCount`, `lastReplayReason`
  - `claimOutboxForDelivery(limit)`
  - `failOutboxDelivery(eventId, errorMessage, maxAttempts, retryBaseMs)`
  - `requeueOutbox(eventId, replayReason, maxReplayCount)`
  - status transitions: `NEW -> RETRY -> DLQ` and `SENT`

2. Relay policy wiring
- File: `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/session-service/src/outboxRelay.js`
- Added env controls:
  - `SESSION_SERVICE_OUTBOX_DLQ_TOPIC`
  - `SESSION_SERVICE_OUTBOX_MAX_ATTEMPTS`
  - `SESSION_SERVICE_OUTBOX_RETRY_BASE_MS`
  - `SESSION_SERVICE_OUTBOX_BATCH_LIMIT`
  - `SESSION_SERVICE_OUTBOX_REPLAY_MAX_COUNT`
- Behavior:
  - publish to main topic,
  - on failure increment retry state,
  - on max attempts move to DLQ and publish DLQ payload.

3. Contract artifacts + checker
- Schema:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/contracts/jsonschema/session-outbox-event-v1.schema.json`
- Validator:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/contracts/validators/validate-session-event-stream.js`
- Runner script:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-session-event-contract-check.sh`

4. DLQ operations tooling
- Requeue endpoint:
  - `POST /api/v1/outbox/:eventId/requeue?reason=...`
- Replay guard:
  - only `DLQ` events are accepted,
  - replay count is capped by `SESSION_SERVICE_OUTBOX_REPLAY_MAX_COUNT`,
  - every replay action is written into service audit events (`OUTBOX_REQUEUE`).
- Ops scripts:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-session-dlq-replay.sh`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-session-outbox-alert-check.sh`

5. Centralized config and runtime wiring
- Updated source-of-truth config:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/cluster-hosts.properties`
- Updated sync + compose wiring:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-cluster-hosts.sh`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml`

## Verification
1. Node syntax checks passed:
```bash
node --check src/store.js
node --check src/outboxRelay.js
node --check src/server.js
node --check contracts/validators/validate-session-event-stream.js
```

2. Deterministic retry/DLQ transition check:
```text
new:NEW:0
after_fail1:RETRY:1:movedToDlq=false
after_fail2:DLQ:2:movedToDlq=true
```

3. Runtime service boot (after rebuild/recreate):
```text
session-service listening on port 18073
session-service outbox relay started topic=abs.session.events.v1 dlq=abs.session.events.dlq.v1 brokers=kafka:9092 pollMs=2000 attempts=5
```

4. Event contract check (manual execution path in this runner):
```bash
docker exec refactor-kafka-1 bash -lc "kafka-console-consumer --bootstrap-server kafka:9092 --topic abs.session.events.v1 --from-beginning --max-messages 5 --timeout-ms 10000" | tee /tmp/session_events_topic.log
rg '^\\{' /tmp/session_events_topic.log > /tmp/session_events_topic.jsonl
node /Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/contracts/validators/validate-session-event-stream.js /tmp/session_events_topic.jsonl
```
Result:
```text
validated=5 invalid=0 total=5
```

5. Requeue endpoint guard checks:
```bash
# unknown event -> 404
curl -sS -X POST "http://session-service:18073/api/v1/outbox/nonexistent-event-id/requeue?reason=canary-test"
# non-DLQ event -> 409
curl -sS -X POST "http://session-service:18073/api/v1/outbox/<sentEventId>/requeue?reason=non-dlq-check"
```
Observed:
- `{"error":"outbox event not found"}` with HTTP `404`
- `{"error":"only DLQ events can be requeued"}` with HTTP `409`

6. Canary flow remains valid after hardening:
- `cwstartgamev2.do` launch -> HTTP `302`
- session-service list includes newly created session:
  - `1_c0b67c31eabe7b81f0e50000019c94fc_R1EGQWxBHgsQOUFTWksLDwY`
- Kafka topic includes matching `session.created` event for this session id.

7. Current outbox health:
- `GET /api/v1/outbox?status=NEW` -> empty
- `GET /api/v1/outbox?status=RETRY` -> empty
- `GET /api/v1/outbox?status=DLQ` -> empty

## Note
- `phase5-session-event-contract-check.sh` is added as standard gate command; in this sandbox, Docker socket permission can intermittently block script-mode Docker calls. Direct command path above is validated and equivalent.

## Rollout impact
- Backward compatibility preserved (relay hardening is inside extracted session-service only).
- Canary safety improved with explicit retry and DLQ behavior.
