# GS Browser Runtime API Contract

Status: canonical browser -> GS runtime contract for `slot-browser-v1`.

## Canonical endpoints

- `POST /slot/v1/bootstrap`
- `POST /slot/v1/opengame`
- `POST /slot/v1/playround`
- `POST /slot/v1/featureaction`
- `POST /slot/v1/resumegame`
- `POST /slot/v1/closegame`
- `POST /slot/v1/gethistory`

## Exact request wire fields

Common request fields used by canonical runtime requests:
- `contractVersion`
- `sessionId`
- `requestCounter`
- `idempotencyKey`
- `clientOperationId`
- `currentStateVersion`
- `bootstrapRef`
- `selectedBet`
- `selectedFeatureChoice`
- `resumeRef`
- `closeReason`
- `historyQuery`

### selectedBet (exact)

```json
{
  "coinValueMinor": 100,
  "lines": 20,
  "multiplier": 1,
  "totalBetMinor": 2000
}
```

### selectedFeatureChoice (exact)

```json
{
  "featureType": "BUY_FEATURE",
  "action": "PURCHASE",
  "priceMinor": 5000,
  "payload": {}
}
```

## Exact response wire envelope

All canonical responses preserve this envelope shape:
- `ok`
- `requestId`
- `sessionId`
- `requestCounter`
- `stateVersion`
- `wallet`
- `round`
- `feature`
- `presentationPayload`
- `restore`
- `idempotency`
- `retry`

Note:
- `bootstrap` returns a separate bootstrap object contract (`session/context/assets/runtime/policies/uiPolicy/integrity`).
- Mutating runtime envelope responses apply to `opengame`, `playround`, `featureaction`, `resumegame`, `closegame`, `gethistory`.

## Read-only endpoint behavior

- `bootstrap` and `gethistory` are read-only.
- They do not require idempotency/clientOperation headers.
- `gethistory` uses the current accepted `requestCounter` and does not advance state.

## Header behavior

Common headers:
- `X-GS-Client-Contract`
- `X-Request-Id`
- `X-Session-Id`

Mutating runtime calls additionally require:
- `X-Idempotency-Key`
- `X-Client-Operation-Id`

Mutating calls in canonical scope:
- `opengame`, `playround`, `featureaction`, `resumegame`, `closegame`
