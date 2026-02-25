# GS Internal API Contract v1 (Frozen)

Status: Frozen for implementation
Version: `v1`
Freeze date: 2026-02-11

## Scope
This contract is only between:
- `new-games-server` (NGS)
- Legacy GS internal API layer

NGS must not call casino-side wallet endpoints directly.

## Transport
- HTTP JSON over private/internal network only.
- Base URL is selected per bank (through GS bank properties) and passed at launch to NGS.
- Required headers on every request:
  - `X-Request-Id`: unique per API call
  - `X-Session-Id`: GS session id
  - `X-NGS-Contract`: `v1`
  - `X-Idempotency-Key`: required for reserve/settle

## Error Envelope
All non-2xx responses:

```json
{
  "error": {
    "code": "STRING_CODE",
    "message": "human readable",
    "retryable": false,
    "traceId": "uuid-or-request-id",
    "details": {}
  }
}
```

## 1) Session Validate
`POST /gs-internal/newgames/v1/session/validate`

Request:
```json
{
  "sessionId": "string",
  "bankId": 6274,
  "playerId": "external-or-internal-user"
}
```

Success response:
```json
{
  "ok": true,
  "sessionId": "string",
  "playerId": "string",
  "bankId": 6274,
  "balance": 100000,
  "currency": {
    "code": "USD",
    "prefix": "$",
    "suffix": "",
    "grouping": ",",
    "decimal": ".",
    "precision": 1,
    "denomination": 1
  }
}
```

## 2) Wallet Reserve (Bet Debit)
`POST /gs-internal/newgames/v1/wallet/reserve`

Request:
```json
{
  "sessionId": "string",
  "gameId": 10,
  "roundId": "string",
  "requestCounter": 14,
  "betAmount": 100,
  "clientOperationId": "client-unique-op-id"
}
```

Success response:
```json
{
  "ok": true,
  "walletOperationId": "gs-wallet-operation-id",
  "balance": 99900
}
```

Rules:
- Idempotent by `(sessionId, clientOperationId)`.
- Duplicate requests must return the same `walletOperationId`.

## 3) Wallet Settle (Collect Credit)
`POST /gs-internal/newgames/v1/wallet/settle`

Request:
```json
{
  "sessionId": "string",
  "gameId": 10,
  "roundId": "string",
  "walletOperationId": "gs-wallet-operation-id",
  "requestCounter": 15,
  "winAmount": 250,
  "clientOperationId": "client-unique-op-id"
}
```

Success response:
```json
{
  "ok": true,
  "balance": 100150
}
```

Rules:
- Idempotent by `(sessionId, clientOperationId)`.
- Must never create duplicate credit.

## 4) History Write
`POST /gs-internal/newgames/v1/history/write`

Request:
```json
{
  "sessionId": "string",
  "roundId": "string",
  "eventType": "BET_PLACED|ROUND_COLLECTED|ROUND_FAILED",
  "data": {
    "betAmount": 100,
    "winAmount": 250,
    "betType": "medium_12"
  }
}
```

Success response:
```json
{
  "ok": true
}
```

## Timeout And Retry Policy
- NGS timeout to GS internal APIs: configurable by `GS_INTERNAL_TIMEOUT_MS` (default `3000` ms).
- Retry allowed only for requests with idempotency key.
- Max retry attempts: 2 with jittered backoff.

## Request Counter Rule
- `requestCounter` is strictly monotonic per session.
- Any out-of-order counter must fail with:
  - `INVALID_REQUEST_COUNTER`

## Trace Correlation (Mandatory)
For every round command (`placebet`, `collect`), log and propagate:
- `requestId`
- `sessionId`
- `roundId`
- `clientOperationId`
- `walletOperationId` (after reserve)

## Implementation Notes (Current)
- Endpoints are implemented in GS as a dedicated servlet mapped to:
  - `/gs-internal/newgames/v1/*`
- Contract headers are now sent by `new-games-server`.
- Request payload extension `gameId` is currently used by GS wallet adapter.
- Reserve/settle now execute through GS wallet client calls (casino side via GS), with response balance returned from GS.
- Current idempotency and request-counter state is in-memory on GS node (phase-1 baseline for single-node/local setup).
