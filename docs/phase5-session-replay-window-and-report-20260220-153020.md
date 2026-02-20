# Phase 5 Session Replay Window + Report Controls (2026-02-20 15:30:20)

## Goal
Add time-window throttling for DLQ replays, provide replay reporting endpoint, and expose these controls in GS configuration portal.

## Delivered
1. Session-service replay safety
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/session-service/src/store.js`
  - Added replay timing field: `lastReplayAt`.
  - `requeueOutbox(...)` now enforces:
    - max replay count (`SESSION_SERVICE_OUTBOX_REPLAY_MAX_COUNT`),
    - replay cooldown window (`SESSION_SERVICE_OUTBOX_REPLAY_WINDOW_SECONDS`).
  - Added replay-report API data function: `getReplayReport(limit)`.

2. API surface
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/session-service/src/server.js`
  - Added env: `SESSION_SERVICE_OUTBOX_REPLAY_WINDOW_SECONDS`.
  - Added endpoint: `GET /api/v1/outbox/replay-report?limit=...`.
  - Requeue endpoint now applies both replay cap and replay window.

3. Contracts and ops scripts
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/refactor-services/contracts/openapi/session-service-v1.yaml`
  - added replay-report path,
  - added `429` response for active replay window.
- Added script:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-session-dlq-report.sh`

4. Centralized config wiring
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/cluster-hosts.properties`
  - `SESSION_SERVICE_OUTBOX_REPLAY_WINDOW_SECONDS=60`
- Propagated through:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/sync-cluster-hosts.sh`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/docker/refactor/docker-compose.yml`

5. Portal visibility
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/support/configPortal.jsp`
  - Added section: `Level 1b: Session Outbox Safety Controls`
  - Shows each outbox control key + value + purpose.

## Verification
1. Syntax checks
```bash
node --check src/store.js
node --check src/server.js
bash -n deploy/scripts/phase5-session-dlq-report.sh
```

2. Deterministic replay window test (temp local store)
```text
requeue1:200:true
requeue2:429:false:replay window active (60s remaining)
report_total=1 replayed=1 dlq=1
```

3. Runtime deploy
```bash
docker compose -f .../refactor/docker-compose.yml build session-service
docker compose -f .../refactor/docker-compose.yml up -d --no-deps --force-recreate session-service
```

4. Runtime checks
- `GET /health` -> OK.
- `GET /api/v1/outbox/replay-report?limit=5` -> returns counts and lists.
- `POST /api/v1/outbox/nonexistent-event-id/requeue?...` -> HTTP 404.
- `POST /api/v1/outbox/<sentEventId>/requeue?...` -> HTTP 409.
- Runtime portal page includes heading:
  - `Level 1b: Session Outbox Safety Controls`.
- Kafka contract check after canary launch:
  - consumed 10 messages from `abs.session.events.v1`,
  - validator result: `validated=10 invalid=0 total=10`.

## Backward compatibility
- No legacy GS protocol or external integration contract was broken.
- Changes are additive in refactor session-service + support portal surface.
