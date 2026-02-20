# Trace and Correlation Standard v1 (GS Modernization)

Last updated: 2026-02-20 UTC  
Scope: GS modernization in `/Users/alexb/Documents/Dev/Dev_new`

## Mandatory correlation fields
All sync/async operations must carry:
1. `traceId`
2. `sessionId`
3. `bankId`
4. `gameId`
5. `operationId` (required for financial actions)
6. `configVersion`

## Transport mappings

### HTTP (entry + internal)
- Request headers:
  - `X-Trace-Id`
  - `X-Session-Id`
  - `X-Bank-Id`
  - `X-Game-Id`
  - `X-Operation-Id`
  - `X-Config-Version`
- Response headers echo:
  - `X-Trace-Id`
  - `X-Session-Id`
  - `X-Operation-Id` (if present)

### WebSocket
- Include fields in canonical envelope (see `/Users/alexb/Documents/Dev/Dev_new/docs/contracts/ws-v1/abs-gs-v1-envelope.schema.json`).
- `seq` is required for ordering/replay diagnostics.

### Kafka
- Required record headers:
  - `traceId`
  - `sessionId`
  - `bankId`
  - `gameId`
  - `operationId`
  - `configVersion`
- Topic payload must also include these fields for downstream persistence/debug.

## Logging format baseline
Structured JSON logs must include:
1. `timestamp`
2. `level`
3. `service`
4. `eventType`
5. correlation fields listed above
6. `errorCode` / `errorCategory` when failed
7. `latencyMs`

Sample:
```json
{
  "timestamp": "2026-02-20T09:55:00Z",
  "level": "INFO",
  "service": "gs-orchestrator",
  "eventType": "BET_ACCEPTED",
  "traceId": "trc-1",
  "sessionId": "sid-1",
  "bankId": "6274",
  "gameId": "838",
  "operationId": "op-55",
  "configVersion": "cfg-2026-02-20-01",
  "latencyMs": 18
}
```

## Generation and propagation rules
1. Inbound `traceId` is reused if valid; otherwise create UUIDv4.
2. Child async tasks must preserve parent `traceId` and annotate `spanType`.
3. `operationId` is immutable once assigned to a financial action.
4. `configVersion` is resolved at request start and fixed for request lifetime.

## Validation and SLO checks
1. 99.9% of logs/events must contain all mandatory fields.
2. Missing-field rate alert threshold: >0.1% over 5 minutes.
3. Missing `operationId` in financial flows is a P1 incident.

## Rollout
1. Phase A: add field extraction at compatibility facade and response echo.
2. Phase B: propagate to wallet adapter/history events and Kafka outbox.
3. Phase C: enforce strict validation and alerting.

## Implementation Status
- Phase A is in progress in GS:
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/common-gs/src/main/java/com/dgphoenix/casino/filters/CorrelationContextFilter.java`
  - `/Users/alexb/Documents/Dev/Dev_new/gs-server/game-server/web-gs/src/main/webapp/WEB-INF/web.xml`
- Runtime validation is pending refactor deployment with private build dependencies.

## Related artifacts
- `/Users/alexb/Documents/Dev/Dev_new/docs/27-error-taxonomy-v1.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/26-bank-canary-policy-v1.md`
- `/Users/alexb/Documents/Dev/Dev_new/docs/25-game-integration-interface-and-websocket-protocol-v1.md`
