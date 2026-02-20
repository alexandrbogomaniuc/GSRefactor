# Session Service Canary Routing Policy (Compatibility Facade Prep)

## Purpose
Prepare compatibility-facade routing controls so GS can selectively delegate session operations to `session-service` by bank.

## Cluster config keys
Defined in:
- `/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/config/cluster-hosts.properties`

Keys:
- `SESSION_SERVICE_ROUTE_ENABLED`
- `SESSION_SERVICE_CANARY_BANKS`
- `SESSION_SERVICE_HOST`
- `SESSION_SERVICE_PORT`
- `SESSION_SERVICE_KAFKA_BROKERS`
- `SESSION_SERVICE_OUTBOX_TOPIC`
- `SESSION_SERVICE_OUTBOX_DLQ_TOPIC`
- `SESSION_SERVICE_OUTBOX_RELAY_ENABLED`
- `SESSION_SERVICE_OUTBOX_RELAY_POLL_MS`
- `SESSION_SERVICE_OUTBOX_MAX_ATTEMPTS`
- `SESSION_SERVICE_OUTBOX_RETRY_BASE_MS`
- `SESSION_SERVICE_OUTBOX_BATCH_LIMIT`
- `SESSION_SERVICE_OUTBOX_REPLAY_MAX_COUNT`
- `SESSION_SERVICE_OUTBOX_REPLAY_WINDOW_SECONDS`
- `GAMEPLAY_ORCHESTRATOR_HOST`
- `GAMEPLAY_ORCHESTRATOR_PORT`
- `GAMEPLAY_ORCHESTRATOR_ROUTE_ENABLED`
- `GAMEPLAY_ORCHESTRATOR_CANARY_BANKS`

## Initial values
- `SESSION_SERVICE_ROUTE_ENABLED=true`
- `SESSION_SERVICE_CANARY_BANKS=6275`
- `SESSION_SERVICE_OUTBOX_RELAY_ENABLED=true`
- `SESSION_SERVICE_OUTBOX_TOPIC=abs.session.events.v1`

Canary is currently active only for bank `6275` in refactor runtime; outbox relay publishes canary events to Kafka topic `abs.session.events.v1`.

## Rollout intent
1. Enable route flag in refactor canary environment only.
2. Route only selected bankIds from canary list.
3. Keep fallback to monolith session path on any downstream error/timeout.
4. Capture parity metrics and compare before widening bank list.

## Rollback
- Immediate rollback: set `SESSION_SERVICE_ROUTE_ENABLED=false` and resync env.
- Keep `SESSION_SERVICE_CANARY_BANKS` list intact for later re-attempt.

## Implementation status
- GS facade gate is now implemented at source level:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/routing/SessionServiceRoutingBridge.java`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/java/com/dgphoenix/casino/actions/enter/game/cwv3/CWStartGameAction.java`
- Current behavior:
  - route decision is evaluated per bank (feature flag + canary list + session-service decision endpoint),
  - shadow `sessions/create` is sent only when canary route is enabled,
  - any failure/timeouts fall back to legacy GS flow.
- Evidence:
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase5-session-canary-hook-source-20260220-143243.md`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase5-session-canary-runtime-activation-20260220-143713.md`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase5-session-canary-live-validation-20260220-144933.md`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase5-session-outbox-dlq-contract-gate-20260220-151251.md`
  - `/Users/alexb/Documents/Dev/Dev_new/docs/phase5-session-replay-window-and-report-20260220-153020.md`

## Current step status
- Running `session-service` container is using canary env values and shadow session writes are validated for bank `6275`.
- Canary outbox safety baseline is tightened for operations:
  - `NEW <= 10`, `RETRY <= 2`, `DLQ <= 0`,
  - trend gate: fail if NEW or RETRY is strictly increasing across a 15-minute sample window.

## Operator command (temporary)
Use helper script to toggle routing and canary bank list:

```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/set-session-canary.sh --enabled true --banks 6275
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/set-session-canary.sh --enabled false
```

The script updates `deploy/config/cluster-hosts.properties` and auto-runs `sync-cluster-hosts.sh`.

## Canary probe (repeatable)
Use the probe script to validate decision + launch + session creation in one run:

```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-session-canary-probe.sh --bank-id 6275

Outbox canary gate command (15-minute trend window):

```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-session-outbox-alert-check.sh --max-new 10 --max-retry 2 --max-dlq 0 --sample-count 4 --sample-interval-sec 300
```

Gameplay shadow canary probe:

```bash
/Users/alexb/Documents/Dev/Dev_new/gs-server/deploy/scripts/phase5-gameplay-canary-probe.sh --bank-id 6275
```
```
