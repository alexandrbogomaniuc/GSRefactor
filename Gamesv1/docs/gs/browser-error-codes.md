# GS Browser Runtime Error Codes

- Status: Draft for implementation (Phase 1)
- Date: 2026-02-28
- Contract version: `slot-browser-v1`
- Related:
  - `docs/gs/browser-runtime-api-contract.md`

## 1) Error Envelope (Normative)

All non-success responses must use:

```json
{
  "ok": false,
  "requestId": "req-uuid",
  "sessionId": "SID-123",
  "requestCounter": 42,
  "error": {
    "code": "STRING_CODE",
    "message": "human readable",
    "retryable": false,
    "details": {}
  },
  "idempotency": {
    "isDuplicate": false,
    "duplicateOfRequestId": null,
    "replaySafe": false
  },
  "retry": {
    "clientMayRetrySameKey": false,
    "clientMustIncrementCounterOnNewAction": false
  }
}
```

## 2) Code Catalog

| Code | HTTP | Retryable | Typical Cause | Client Handling |
|---|---:|---|---|---|
| `INVALID_SESSION` | 401 | No | SID missing/expired/mismatch | Force reload/relaunch |
| `SESSION_EXPIRED` | 401 | No | Session timeout | Show session-expired UX and relaunch |
| `INVALID_REQUEST_COUNTER` | 409 | No | Counter out of order | Call `resumegame`/`opengame` sync path |
| `IDEMPOTENCY_KEY_REUSE` | 409 | No | Same key with different payload | Generate new key for new action |
| `STATE_VERSION_MISMATCH` | 409 | No | Stale `currentStateVersion` | Call `resumegame` then retry |
| `BOOTSTRAP_CONFIG_MISMATCH` | 409 | No | `bootstrapRef` mismatch (`configId`/version drift) | Re-bootstrap and reopen |
| `MATH_PACKAGE_MISMATCH` | 409 | No | Browser references stale math package version | Re-bootstrap and reopen |
| `INVALID_BET_CONFIGURATION` | 400 | No | Bet outside resolved limits | Refresh UI limits and prompt user |
| `FEATURE_ACTION_INVALID` | 400 | No | Action not allowed in current feature mode | Refresh feature state and block action |
| `FEATURE_ACTION_CONFLICT` | 409 | No | Feature action no longer valid for current state | Resume state and ask user to retry |
| `BUY_FEATURE_NOT_ALLOWED` | 403 | No | Policy blocks buy feature (including cash-bonus restriction) | Disable UI entry and show reason |
| `WALLET_RESERVE_REJECTED` | 409 | No | Reserve denied (insufficient funds/rules) | Show business error from details |
| `WALLET_SETTLE_REJECTED` | 409 | No | Settle denied by wallet | Show error and request support if persistent |
| `WALLET_GATEWAY_ERROR` | 502 | Yes | Upstream wallet transient failure | Retry with same idempotency key |
| `HISTORY_NOT_ENABLED` | 403 | No | In-game history disabled by policy | Hide history UI |
| `HISTORY_NOT_FOUND` | 404 | No | No rows for query window | Show empty history state |
| `RATE_LIMITED` | 429 | Yes | Client exceeded endpoint rate | Backoff and retry same request |
| `INTERNAL_RUNTIME_ERROR` | 500 | Maybe | Unexpected server/runtime fault | Retry only if `retryable=true` |
| `DEPENDENCY_UNAVAILABLE` | 503 | Yes | Internal service unavailable | Retry with backoff and same key |
| `BAD_REQUEST` | 400 | No | Malformed payload or missing required fields | Fix payload serialization |

## 3) Retry Rules (Normative)

- Endpoint class split:
  - mutating: `opengame`, `playround`, `featureaction`, `resumegame`, `closegame`,
  - read-only: `bootstrap`, `gethistory`.
- Retry only when:
  - `error.retryable = true`, and
  - `retry.clientMayRetrySameKey = true`.
- For retry of the same action:
  - keep same `sessionId`, `requestCounter`, `idempotencyKey`, `clientOperationId`, payload.
- For a new action:
  - increment `requestCounter`,
  - generate new `idempotencyKey` and `clientOperationId`.
- Read-only endpoints do not require `idempotencyKey`/`clientOperationId` and do not advance request counter/state version.

## 4) Duplicate Semantics

- A duplicate request that matches prior payload/key should return:
  - `ok=true`,
  - `idempotency.isDuplicate=true`,
  - same logical result body as original action.
- A duplicate key with changed payload returns `IDEMPOTENCY_KEY_REUSE`.

## 5) Browser UX Mapping

- `401` session errors:
  - block controls and initiate relaunch flow.
- `409` consistency errors:
  - show non-fatal sync warning and invoke `resumegame`.
- `409` wallet business errors:
  - show actionable localized message (`details.walletCode` if present).
- `5xx/503` transient errors:
  - show retry banner, auto-backoff retry when allowed.

## 6) Non-Exposed Error Internals

Do not expose to browser:
- internal slot-engine stack traces,
- serverAudit/rng trace details,
- sensitive dependency addresses/secrets.

## 7) Type Invariants (Cross-Doc)

Error and success envelopes in this document follow the same wire-type rules as:
- `docs/gs/bootstrap-config-contract.md`
- `docs/gs/browser-runtime-api-contract.md`

Mandatory numeric JSON integer fields:
- `requestCounter`
- `stateVersion`
- `currentStateVersion`
- all minor-unit money fields
