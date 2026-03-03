# GS Internal Slot Runtime Contract

- Status: Draft for implementation (v1)
- Date: 2026-02-28
- Scope: GS Core <-> Internal Slot Engine Host only (private network)
- Out of scope: Multiplayer transport and any public browser-to-engine direct API
- Related:
  - `docs/gs-release-runtime-integration-flow.md`
  - `docs/gs/math-package-spec.md`

## 1) Contract Goals

1. Keep GS authoritative for session, wallet, persistence, and unfinished-round restore.
2. Standardize reusable slot command semantics for future titles.
3. Enforce strict idempotency and state-version checks.
4. Return deterministic, auditable outputs needed by GS and client presentation.
5. Keep financial ordering aligned to GS authority: reserve -> engine execution -> settle.

## 2) Transport and Versioning

- Transport: internal HTTP JSON.
- Header requirements:
  - `X-Internal-Contract: slot-runtime-v1`
  - `X-Request-Id: <uuid>`
  - `X-Session-Id: <sessionId>`
- All commands are POST.
- Contract compatibility is governed by `contractVersion` in payload.

## 2.1 Ownership Boundary (Normative)

- GS owns:
  - math package/version and RTP model selection,
  - request sequencing and idempotency authority,
  - wallet reserve and settle ordering/commit,
  - persisted session/round/snapshot authority.
- Slot-engine host owns:
  - loading and executing only pre-registered, verified math package versions selected by GS,
  - deterministic game-state transitions and outcome computation.
- Slot-engine host must not change selected math package/version/model or perform DB writes.

## 3) Common Envelope

Every request body MUST include these common fields:

```json
{
  "contractVersion": "slot-runtime-v1",
  "requestId": "uuid",
  "timestampUtc": "2026-02-28T20:00:00Z",
  "sessionId": "SID-123",
  "requestCounter": 42,
  "idempotencyKey": "uuid-or-stable-key",
  "clientOperationId": "client-op-123",
  "currentStateVersion": 12,
  "gameId": "10045",
  "mathPackageVersion": "1.4.2",
  "rtpModelId": "base-96.20",
  "financialIntent": {
    "betDebitMinor": 2000,
    "pricedFeatureDebitMinor": 0,
    "totalReservedMinor": 2000
  },
  "reserveContext": {
    "reserveCompleted": true,
    "walletOperationId": "ngs-op-123",
    "reservedAmountMinor": 2000
  },
  "selectedBet": {
    "coinValueMinor": 100,
    "lines": 20,
    "multiplier": 1,
    "totalBetMinor": 2000
  },
  "selectedFeatureChoice": null
}
```

Notes:
- `selectedBet` and `selectedFeatureChoice` MUST always exist; use `null` if not applicable.
- GS is source of truth for `requestCounter` and idempotency semantics.
- `financialIntent` is resolved by GS before engine execution.
- `reserveContext` indicates GS reserve step status and operation reference.

## 4) Common Response Shape

Every success response MUST include:

```json
{
  "ok": true,
  "requestId": "uuid",
  "sessionId": "SID-123",
  "requestCounter": 42,
  "stateVersion": 13,
  "idempotency": {
    "isDuplicate": false,
    "duplicateOfRequestId": null,
    "replaySafe": true
  },
  "roundOutcome": {
    "outcomeType": "NONE|IN_PROGRESS|FINAL",
    "roundId": "R-987",
    "winAmountMinor": 0,
    "totalPayoutMinor": 0,
    "jackpotEvents": []
  },
  "nextFeatureState": {
    "featureMode": "BASE|FREE_SPINS|RESPIN|HOLD_AND_WIN",
    "remainingActions": 0,
    "featureContext": {}
  },
  "settlementSummary": {
    "sourceReserveOperationId": "ngs-op-123",
    "settleCreditMinor": 2500,
    "roundWinMinor": 2500,
    "netRoundEffectMinor": 500,
    "walletTags": []
  },
  "serverAudit": {
    "mathPackageVersion": "1.4.2",
    "rtpModelId": "base-96.20",
    "rngAlgorithmVersion": "rng-v3",
    "rngTraceRef": "rngtrace:4d3a...",
    "outcomeHash": "sha256:9abc...",
    "engineBuildVersion": "engine-1.9.0+build.42"
  },
  "presentationPayload": {
    "screen": "base",
    "reels": [],
    "symbols": [],
    "animations": [],
    "audioCues": [],
    "localizedMessages": []
  },
  "retrySemantics": {
    "clientMayRetrySameKey": true,
    "clientMustIncrementCounterOnNewAction": true
  }
}
```

Error response:

```json
{
  "ok": false,
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
  }
}
```

`serverAudit` is server-only metadata for GS persistence, replay, and certification traceability. It must not be exposed as browser presentation data.

## 5) Operations

## 5.1 `openGame`

- Endpoint: `POST /internal/slot/v1/openGame`
- Purpose: initialize runtime state for a session after GS launch validation.

### Request Requirements

- Include common fields.
- `selectedBet`: nullable.
- `selectedFeatureChoice`: nullable.
- Additional required fields:

```json
{
  "bootstrapContext": {
    "bankId": 6274,
    "currencyCode": "USD",
    "lang": "en",
    "mode": "real"
  },
  "resumeHint": {
    "hasPersistedUnfinishedRound": false,
    "persistedRoundId": null
  }
}
```

### Response Requirements

- Must return initialized `stateVersion`.
- `roundOutcome.outcomeType` is normally `NONE`.
- Include `nextFeatureState` baseline and full `presentationPayload`.
- `settlementSummary` values MUST be zero.
- Include `serverAudit`.

## 5.2 `playRound`

- Endpoint: `POST /internal/slot/v1/playRound`
- Purpose: execute base round from current state and selected bet.

### Request Requirements

- Include common fields.
- `selectedBet` required and non-null.
- `selectedFeatureChoice` optional/null.
- `financialIntent` required.
- `reserveContext.reserveCompleted` must be `true` for real-money path.
- Additional required fields:

```json
{
  "roundContext": {
    "roundId": "R-987",
    "triggerSource": "SPIN_BUTTON|AUTOPLAY|TURBO"
  }
}
```

### Response Requirements

- Must return authoritative round outcome for this execution step.
- Must return next feature state including triggered modes.
- Must return settlement fields GS needs after reserve has already happened:
  - `sourceReserveOperationId`
  - `settleCreditMinor`
  - `roundWinMinor`
  - `netRoundEffectMinor`
- Must include `serverAudit`.
- Must include client-facing `presentationPayload`.

## 5.3 `featureAction`

- Endpoint: `POST /internal/slot/v1/featureAction`
- Purpose: apply a player/system action inside an active feature state.

### Request Requirements

- Include common fields.
- `selectedBet` nullable.
- `selectedFeatureChoice` required and non-null for player-driven actions.
- For priced feature actions, `financialIntent` and `reserveContext` are required and reserve must be completed before engine call.

```json
{
  "selectedFeatureChoice": {
    "featureType": "BUY_FEATURE|HOLD_AND_WIN|RESPIN|FREE_SPINS",
    "action": "PICK|CONFIRM|COLLECT|CONTINUE",
    "priceMinor": 0,
    "payload": {}
  }
}
```

### Response Requirements

- Must include updated `nextFeatureState`.
- Must include any resulting `roundOutcome`.
- Must include settlement deltas relevant to GS settle step.
- Must include `serverAudit`.
- Must include presentation payload for client transition.

## 5.4 `resumeGame`

- Endpoint: `POST /internal/slot/v1/resumeGame`
- Purpose: reconstruct runtime state from GS persisted unfinished-round/session snapshot.

### Request Requirements

- Include common fields.
- `selectedBet`: nullable.
- `selectedFeatureChoice`: nullable.
- Additional required fields:

```json
{
  "resumeSnapshot": {
    "snapshotVersion": 3,
    "snapshotPayload": {},
    "persistedStateVersion": 12,
    "persistedRoundId": "R-987"
  }
}
```

### Response Requirements

- Must return reconstructed `stateVersion` and `nextFeatureState`.
- Must not request new financial mutations for replay-only restore path.
- `settlementSummary` should be zero unless explicit recovery action is required.
- Must include `serverAudit`.

## 5.5 `closeGame`

- Endpoint: `POST /internal/slot/v1/closeGame`
- Purpose: close runtime session cleanly, finalize transient engine resources.

### Request Requirements

- Include common fields.
- `selectedBet`: nullable.
- `selectedFeatureChoice`: nullable.
- Additional required fields:

```json
{
  "closeReason": "PLAYER_EXIT|TIMEOUT|FORCED_LOGOUT|MAINTENANCE",
  "allowAutoFinalizeFeature": false
}
```

### Response Requirements

- Must include final `stateVersion` and `nextFeatureState`.
- Must indicate whether unfinished feature state remains for GS persistence.
- Must include settlement summary (typically zero for close-only path).
- Must include `serverAudit`.

## 6) State Ownership and Persistence Boundary

- GS owns persisted authoritative state and unfinished-round snapshots.
- Slot-engine host consumes GS-provided state and returns deterministic transitions.
- Slot-engine host has no direct DB reads/writes.

## 6.1 Deterministic Restore Rule

- GS persisted snapshot must include enough opaque engine state to reconstruct unfinished rounds deterministically.
- Continuation must not depend on hidden in-memory RNG state only.
- If deterministic reconstruction cannot be guaranteed from persisted snapshot + registered math package, GS must fail closed and prevent continuation.

## 6.2 Financial Ordering Rule

- Real-money ordered flow is:
  1. GS validates session/counter/idempotency.
  2. GS performs wallet reserve for resolved financial intent.
  3. GS invokes engine runtime (`playRound` or priced `featureAction`) with `reserveContext`.
  4. GS performs wallet settle from response `settlementSummary`.
  5. GS persists authoritative round/session/wallet/history/snapshot.

## 7) Duplicate and Retry Semantics (Normative)

1. Idempotency scope key:
   - `(sessionId, operationName, idempotencyKey)` with `clientOperationId` as secondary trace.
2. Duplicate requests must return byte-equivalent logical result and set:
   - `idempotency.isDuplicate = true`.
3. New action requires incremented `requestCounter`.
4. Mismatched `currentStateVersion` returns `STATE_VERSION_MISMATCH`.
5. GS decides external retry policy; engine returns `retryable` hints only.
6. Duplicate responses must return identical `serverAudit` block for the same idempotent key.

## 8) Recommended Error Codes

- `INVALID_SESSION`
- `INVALID_REQUEST_COUNTER`
- `IDEMPOTENCY_KEY_REUSE`
- `STATE_VERSION_MISMATCH`
- `MATH_PACKAGE_NOT_AVAILABLE`
- `UNSUPPORTED_FEATURE_ACTION`
- `FEATURE_STATE_CONFLICT`
- `INVALID_BET_CONFIGURATION`
- `INTERNAL_RUNTIME_ERROR`

## 9) Security Controls

- Internal endpoints must be private network only.
- Mutual auth/service identity is required between GS and slot-engine host.
- Requests and responses must be trace-correlated via `requestId`.
- Never trust browser-provided financial totals as authoritative.
- `serverAudit` block is persisted/logged server-side only and must not be rendered as user-facing data.
