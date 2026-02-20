# Game Integration Interface and WebSocket Protocol v1 (Current + Future)

Last updated: 2026-02-19 UTC
Audience: internal game teams and third-party game providers

## 1. Purpose
Define one stable integration contract so games can communicate with GS orchestrator consistently, while preserving legacy compatibility.

## 2. Integration Model
- GS remains orchestration authority for session, wallet, bonus/FRB, history, and policy.
- Game engine remains deterministic outcome producer and UI runtime.
- Integration is split into:
  - Launch contract (HTTP redirect/payload),
  - Gameplay contract (HTTP and/or WebSocket messages),
  - Financial contract (idempotent wager/settle semantics),
  - Telemetry contract (trace/correlation fields).

## 3. Backward-Compatible Entry Contract
Current compatibility requirements (must remain):
1. Launch routes and parameters stay stable (`cwstartgamev2`, `bs*`, `frb*`, history routes).
2. Existing clients receiving `WEB_SOCKET_URL` must continue to work.
3. Bank-specific protocol mode is resolved at boundary adapters only (`XML` default; `JSON` optional by bank setting).

## 4. Canonical Message Envelope (for all future WebSocket flows)
All WS messages should use a canonical envelope (JSON):

```json
{
  "version": "1.0",
  "type": "<messageType>",
  "traceId": "<uuid>",
  "sessionId": "<sid>",
  "bankId": "<bank>",
  "gameId": "<game>",
  "operationId": "<op-id-or-empty>",
  "timestamp": "2026-02-19T18:00:00Z",
  "seq": 123,
  "payload": {}
}
```

Rules:
1. `traceId`, `sessionId`, `bankId`, `gameId` are mandatory on all gameplay-affecting messages.
2. `operationId` is mandatory for financial messages (bet, cancel, settle, rollback).
3. `seq` is monotonic per socket session and used for replay/reorder detection.
4. Unknown fields must be ignored (forward compatibility).

## 5. WebSocket Handshake and Session Rules
Handshake requirements:
1. Transport: WSS in production (TLS required), WS allowed only in local/dev.
2. Auth: short-lived signed token bound to `sessionId`, `bankId`, `gameId`.
3. Token TTL: <= 5 minutes; refresh via secure HTTP endpoint.
4. Subprotocol: `abs.gs.v1` (future standard) to make version negotiation explicit.

Session ownership rules:
1. One active primary gameplay channel per `sessionId` unless explicitly marked multi-connection mode.
2. Reconnect must provide last acknowledged `seq` to support recovery.
3. Orchestrator validates ownership; invalid ownership returns terminal error.

## 6. Core Message Types (minimum required)
Client -> GS:
- `PING`
- `GAME_READY`
- `BET_REQUEST`
- `BET_CANCEL_REQUEST`
- `SETTLE_REQUEST`
- `RECONNECT_REQUEST`
- `SESSION_CLOSE_REQUEST`

GS -> Client:
- `PONG`
- `SESSION_ACCEPTED`
- `BALANCE_SNAPSHOT`
- `BET_ACCEPTED` / `BET_REJECTED`
- `SETTLE_ACCEPTED` / `SETTLE_REJECTED`
- `SESSION_SYNC`
- `SESSION_CLOSED`
- `ERROR`

## 7. Financial Safety Rules (non-negotiable)
1. Exactly-once financial effect via idempotency key = `operationId`.
2. Duplicate `BET_REQUEST` with same `operationId` must return same prior result, never re-debit.
3. `SETTLE_REQUEST` is valid only for known reserved/debited operation state.
4. Final state machine is explicit and auditable (`INIT -> RESERVED -> SETTLED|CANCELED`).

## 8. Error Contract
Standard error payload:

```json
{
  "code": "<stable_error_code>",
  "category": "validation|state|dependency|auth|rate_limit|internal",
  "message": "<short safe text>",
  "retryable": false,
  "details": {}
}
```

Rules:
1. Codes are stable and versioned; text is human-readable but non-contractual.
2. `retryable` controls client retry strategy.
3. Fatal state errors must force controlled session sync/close path.

## 9. Reconnect and Recovery Best Practices
1. Client sends `lastAckSeq` and `lastKnownOperationId` on reconnect.
2. GS returns `SESSION_SYNC` with authoritative unresolved operations.
3. Client must reconcile to GS state before allowing new bet action.
4. Recovery timeout and max reconnect attempts are bank-configurable.

## 10. Third-Party Provider Requirements
Mandatory deliverables from provider:
1. Protocol conformance report against this spec.
2. Deterministic round model and seed/audit behavior documentation.
3. Idempotency test results for duplicate bet/settle/reconnect scenarios.
4. Latency and throughput profile under agreed load.
5. Security controls: TLS, token handling, no PII leakage in logs.

Certification checklist (must pass before production):
1. Launch compatibility test pack.
2. Wager/settle idempotency pack.
3. Reconnect/session mismatch pack.
4. Error code conformance pack.
5. Observability pack (required trace fields present).

## 11. Versioning and Deprecation
1. Contract version in envelope (`version`) and WS subprotocol.
2. Breaking changes only in new major version (`v2`), never inside `v1`.
3. Legacy modes remain supported until explicit deprecation approval.
4. Bank-level rollout flags control phased enablement.

## 12. Security Baseline
1. WSS + modern TLS ciphers for production.
2. Signed JWT-like session token with audience and expiry claims.
3. Rate limit per session and per IP.
4. HMAC signature for sensitive server-to-provider callbacks where applicable.
5. Strict input schema validation and payload size limits.

## 13. Observability Contract
Every request/event must propagate:
- `traceId`, `sessionId`, `bankId`, `gameId`, `operationId`, `configVersion`.

Operational telemetry:
1. Launch success/failure by reason.
2. Bet/settle latency and rejection reasons.
3. Reconnect success rate.
4. Provider-specific error heatmap.

## 14. Legacy-to-Future Mapping
- Current launch output (`WEB_SOCKET_URL`) remains accepted.
- Adapter layer maps legacy payloads to canonical envelope internally.
- Bank `protocolMode` (`XML|JSON`) changes serializer only, not business behavior.

## 15. Recommended Next Implementation Steps
1. Publish JSON schema files for envelope + each message type.
2. Build provider sandbox endpoint with automated conformance suite.
3. Add contract test harness for top games and canary banks.
4. Implement API key + provider registry with version and capability metadata.
