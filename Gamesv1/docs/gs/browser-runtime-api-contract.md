# GS Browser Runtime API Contract for New Slots

- Status: Draft for implementation (Phase 1)
- Date: 2026-02-28
- Contract version: `slot-browser-v1`
- Scope: Browser Client <-> GS Public/Gateway API only
- Out of scope: Any direct Browser <-> Slot-Engine Host communication
- Related:
  - `docs/gs/bootstrap-config-contract.md`
  - `docs/gs/internal-slot-runtime-contract.md`
  - `docs/gs/math-package-spec.md`

## 1) Separation of Concerns (Normative)

- Browser talks only to GS public endpoints defined in this document.
- GS talks to internal slot-engine host through internal contract only.
- Browser must never call internal endpoints such as `/internal/slot/v1/*`.

## 2) Canonical Browser API Surface

## 2.1 Launch and Bootstrap Handoff

1. `GET /cwstartgamev2.do`
   - Purpose: launch entry and session/bootstrap handoff redirect.
2. `POST /slot/v1/bootstrap`
   - Purpose: fetch resolved bootstrap payload in JSON form for runtime initialization.

## 2.2 Gameplay Runtime Endpoints

1. `POST /slot/v1/opengame`
2. `POST /slot/v1/playround`
3. `POST /slot/v1/featureaction`
4. `POST /slot/v1/resumegame`
5. `POST /slot/v1/closegame`
6. `POST /slot/v1/gethistory`

No placeholder endpoint names are permitted for Phase 1.

## 3) Headers, Auth, and Versioning

### 3.1 Common Required Headers (`POST /slot/v1/*`)

- `Content-Type: application/json`
- `Accept: application/json`
- `X-GS-Client-Contract: slot-browser-v1`
- `X-Request-Id: <uuid>`
- `X-Session-Id: <sessionId>`

### 3.2 Additional Required Headers (State-Mutating Endpoints)

Applies only to:
- `POST /slot/v1/opengame`
- `POST /slot/v1/playround`
- `POST /slot/v1/featureaction`
- `POST /slot/v1/resumegame`
- `POST /slot/v1/closegame`

Required:
- `X-Idempotency-Key: <idempotencyKey>`
- `X-Client-Operation-Id: <clientOperationId>`

### 3.3 Read-Only Endpoint Header Rules

Applies to:
- `POST /slot/v1/bootstrap`
- `POST /slot/v1/gethistory`

Rules:
- `X-Idempotency-Key` is not required.
- `X-Client-Operation-Id` is not required.
- If provided, GS may log these headers for diagnostics but must not use them for idempotency decisions.

Global rules:
- Header `X-Session-Id` must match body `sessionId`.
- For mutating endpoints, `X-Idempotency-Key` must match body `idempotencyKey`.
- For mutating endpoints, `X-Client-Operation-Id` must match body `clientOperationId`.
- Launch token is consumed by launch route; runtime endpoints rely on GS session identity (`sessionId`) and GS auth context.

### 3.4 Endpoint Header Matrix (Canonical)

| Endpoint | Content-Type / Accept | `X-GS-Client-Contract` | `X-Request-Id` | `X-Session-Id` | `X-Idempotency-Key` | `X-Client-Operation-Id` |
|---|---|---|---|---|---|---|
| `POST /slot/v1/bootstrap` | Required | Required | Required | Required | Not required | Not required |
| `POST /slot/v1/opengame` | Required | Required | Required | Required | Required | Required |
| `POST /slot/v1/playround` | Required | Required | Required | Required | Required | Required |
| `POST /slot/v1/featureaction` | Required | Required | Required | Required | Required | Required |
| `POST /slot/v1/resumegame` | Required | Required | Required | Required | Required | Required |
| `POST /slot/v1/closegame` | Required | Required | Required | Required | Required | Required |
| `POST /slot/v1/gethistory` | Required | Required | Required | Required | Not required | Not required |

Read-only endpoints (`bootstrap`, `gethistory`) must not apply idempotency-key conflict behavior.

## 4) Canonical Request Schemas

## 4.1 Bootstrap Request (`POST /slot/v1/bootstrap`, Read-Only)

```json
{
  "contractVersion": "slot-browser-v1",
  "sessionId": "SID-123",
  "bootstrapRef": {
    "clientPackageVersion": "client-pkg-2.6.0",
    "configId": "cfg-8f42"
  }
}
```

Semantics:
- `requestCounter`: not required.
- `idempotencyKey`: not required.
- `clientOperationId`: not required.
- `currentStateVersion`: not required.
- `selectedBet` and `selectedFeatureChoice`: must be omitted.
- Bootstrap is read-only and must not advance `requestCounter` or `stateVersion`.

## 4.2 State-Mutating Request Envelope

Endpoints using this envelope:
- `POST /slot/v1/opengame`
- `POST /slot/v1/playround`
- `POST /slot/v1/featureaction`
- `POST /slot/v1/resumegame`
- `POST /slot/v1/closegame`

```json
{
  "contractVersion": "slot-browser-v1",
  "sessionId": "SID-123",
  "requestCounter": 42,
  "idempotencyKey": "8bb4b9c6-7e3d-4a6f-9a51-54f0eb53f2f0",
  "clientOperationId": "client-op-42",
  "currentStateVersion": 12,
  "bootstrapRef": {
    "configId": "cfg-8f42",
    "clientPackageVersion": "client-pkg-2.6.0",
    "mathPackageVersion": "1.4.2"
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

## 4.3 State-Mutating Endpoint-Specific Requirements

- `POST /slot/v1/opengame`
  - Requires state-mutating envelope.
  - `selectedBet` and `selectedFeatureChoice` must be `null`.
- `POST /slot/v1/playround`
  - Requires non-null `selectedBet`.
  - `selectedFeatureChoice` must be `null`.
- `POST /slot/v1/featureaction`
  - Requires non-null `selectedFeatureChoice`:
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
- `POST /slot/v1/resumegame`
  - Requires:
```json
{
  "resumeRef": {
    "unfinishedRoundId": "R-987",
    "resumeToken": "rsm-abc123"
  }
}
```
  - `selectedBet` and `selectedFeatureChoice` must be `null`.
- `POST /slot/v1/closegame`
  - Requires:
```json
{
  "closeReason": "PLAYER_EXIT|TIMEOUT|FORCED_LOGOUT|MAINTENANCE"
}
```
  - `selectedBet` and `selectedFeatureChoice` must be `null`.

## 4.4 GetHistory Request (`POST /slot/v1/gethistory`, Read-Only)

```json
{
  "contractVersion": "slot-browser-v1",
  "sessionId": "SID-123",
  "requestCounter": 42,
  "historyQuery": {
    "fromRoundId": null,
    "limit": 50,
    "includeFeatureDetails": true
  }
}
```

Semantics:
- `requestCounter` is required for correlation and must equal the latest accepted session counter.
- `idempotencyKey` is not required.
- `clientOperationId` is not required.
- `currentStateVersion` is not required.
- `selectedBet` and `selectedFeatureChoice` must be omitted.
- GetHistory is read-only and must not advance `requestCounter` or `stateVersion`.

## 4.5 Request Body Requirement Matrix (Canonical)

| Endpoint | `requestCounter` | `idempotencyKey` | `clientOperationId` | `currentStateVersion` | `selectedBet` | `selectedFeatureChoice` |
|---|---|---|---|---|---|---|
| `POST /slot/v1/bootstrap` | Not required | Not required | Not required | Not required | Omit | Omit |
| `POST /slot/v1/opengame` | Required | Required | Required | Required | Must be `null` | Must be `null` |
| `POST /slot/v1/playround` | Required | Required | Required | Required | Required non-null | Must be `null` |
| `POST /slot/v1/featureaction` | Required | Required | Required | Required | Usually `null` | Required non-null |
| `POST /slot/v1/resumegame` | Required | Required | Required | Required | Must be `null` | Must be `null` |
| `POST /slot/v1/closegame` | Required | Required | Required | Required | Must be `null` | Must be `null` |
| `POST /slot/v1/gethistory` | Required (current accepted value) | Not required | Not required | Not required | Omit | Omit |

## 5) Canonical Response Schema

`POST /slot/v1/bootstrap` response is defined by:
- `docs/gs/bootstrap-config-contract.md`

The success/error envelopes below apply to gameplay runtime endpoints and history responses.

Success envelope:

```json
{
  "ok": true,
  "requestId": "req-uuid",
  "sessionId": "SID-123",
  "requestCounter": 42,
  "stateVersion": 13,
  "wallet": {
    "balanceMinor": 99224,
    "previousBalanceMinor": 99024,
    "currencyCode": "USD",
    "truncateCents": false,
    "delayedWalletMessagePending": false
  },
  "round": {
    "roundId": "R-987",
    "status": "NONE|IN_PROGRESS|FINAL",
    "betMinor": 2000,
    "winMinor": 2500,
    "netEffectMinor": 500,
    "outcomeHash": "sha256:2e04..."
  },
  "feature": {
    "mode": "BASE|FREE_SPINS|RESPIN|HOLD_AND_WIN",
    "remainingActions": 0,
    "nextAllowedActions": [],
    "featureContext": {}
  },
  "presentationPayload": {
    "featureMode": "BASE",
    "reelStops": [],
    "symbolGrid": [],
    "uiMessages": [],
    "animationCues": [],
    "audioCues": [],
    "counters": [],
    "labels": {}
  },
  "restore": {
    "hasUnfinishedRound": false,
    "unfinishedRoundId": null,
    "resumeStateVersion": 13,
    "opaqueRestorePayload": null
  },
  "idempotency": {
    "isDuplicate": false,
    "duplicateOfRequestId": null,
    "replaySafe": true
  },
  "retry": {
    "clientMayRetrySameKey": true,
    "clientMustIncrementCounterOnNewAction": true
  }
}
```

Error envelope:

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

## 5.1 Endpoint-Specific Response Notes

- `POST /slot/v1/bootstrap`
  - Response schema is defined in:
    - `docs/gs/bootstrap-config-contract.md`
- `POST /slot/v1/gethistory`
  - Returns the full canonical success envelope (`ok`, `requestId`, `sessionId`, `requestCounter`, `stateVersion`, `wallet`, `round`, `feature`, `presentationPayload`, `restore`, `idempotency`, `retry`) plus a `history` block:
```json
{
  "ok": true,
  "requestId": "req-uuid",
  "sessionId": "SID-123",
  "requestCounter": 42,
  "stateVersion": 13,
  "wallet": {
    "balanceMinor": 99224,
    "previousBalanceMinor": 99024,
    "currencyCode": "USD",
    "truncateCents": false,
    "delayedWalletMessagePending": false
  },
  "round": {
    "roundId": null,
    "status": "NONE",
    "betMinor": 0,
    "winMinor": 0,
    "netEffectMinor": 0,
    "outcomeHash": "sha256:0000..."
  },
  "feature": {
    "mode": "BASE",
    "remainingActions": 0,
    "nextAllowedActions": [],
    "featureContext": {}
  },
  "presentationPayload": {
    "featureMode": "BASE",
    "reelStops": [],
    "symbolGrid": [],
    "uiMessages": [],
    "animationCues": [],
    "audioCues": [],
    "counters": [],
    "labels": {}
  },
  "restore": {
    "hasUnfinishedRound": false,
    "unfinishedRoundId": null,
    "resumeStateVersion": 13,
    "opaqueRestorePayload": null
  },
  "idempotency": {
    "isDuplicate": false,
    "duplicateOfRequestId": null,
    "replaySafe": true
  },
  "retry": {
    "clientMayRetrySameKey": false,
    "clientMustIncrementCounterOnNewAction": false
  },
  "history": {
    "items": [
      {
        "roundId": "R-986",
        "completedAtUtc": "2026-03-01T10:00:00Z",
        "betMinor": 2000,
        "winMinor": 2500,
        "netEffectMinor": 500,
        "status": "FINAL",
        "featureMode": "BASE",
        "presentationPayload": {
          "featureMode": "BASE",
          "reelStops": [],
          "symbolGrid": [],
          "uiMessages": [],
          "animationCues": [],
          "audioCues": [],
          "counters": [],
          "labels": {}
        }
      }
    ],
    "nextCursor": null
  }
}
```

## 6) Request Counter and Idempotency Rules

- `bootstrap`:
  - read-only; does not use idempotency.
  - does not require `requestCounter`.
  - does not advance state.
- `opengame`, `playround`, `featureaction`, `resumegame`, `closegame`:
  - must use strictly monotonic `requestCounter = last + 1`.
  - require idempotency key semantics.
- `gethistory`:
  - read-only; must send current `requestCounter = last` for request/session correlation.
  - does not use idempotency keys.
  - does not advance state.
- Idempotency scope:
  - `(sessionId, endpoint, idempotencyKey)`.
- Duplicate request with same payload and key must return logically identical response and:
  - `idempotency.isDuplicate = true`.
- Reusing `idempotencyKey` with different payload must fail.
- Idempotency scope/rules apply only to state-mutating endpoints.

## 7) Presentation Payload Contract

Allowed browser-visible payload fields:
- `reelStops`: reel index stop data for rendering.
- `symbolGrid`: symbol matrix for visible result state.
- `featureMode`: current gameplay mode.
- `uiMessages`: localized display messages and severity.
- `animationCues`: deterministic animation directives.
- `audioCues`: deterministic sound directives.
- `counters` and `labels`: UI numeric/text values (for example free-spins-left, multiplier, jackpot label).

Server-only fields that must not be sent to browser:
- `serverAudit`
- `rngTraceRef`
- internal engine diagnostics / stack traces / raw debug state
- raw wallet operation ledger internals

## 8) Restore Payload Contract

`restore` block meaning:
- `hasUnfinishedRound`: whether GS has recoverable unfinished state.
- `unfinishedRoundId`: authoritative round identifier if present.
- `resumeStateVersion`: state version to resume from.
- `opaqueRestorePayload`: engine-agnostic opaque state blob for deterministic continuation (browser does not interpret semantics; browser passes through if requested by GS flow).

## 9) Ambiguity Resolution (Locked)

## 9.1 Canonical `outcomeHash` Serialization

`outcomeHash` MUST be computed as:

1. Construct canonical object:
```json
{
  "contractVersion": "slot-browser-v1",
  "gameId": "10045",
  "sessionId": "SID-123",
  "roundId": "R-987",
  "requestCounter": 42,
  "stateVersion": 13,
  "mathPackageVersion": "1.4.2",
  "rtpModelId": "base-96.20",
  "selectedBet": {"coinValueMinor":100,"lines":20,"multiplier":1,"totalBetMinor":2000},
  "selectedFeatureChoice": null,
  "roundResult": {"status":"FINAL","winMinor":2500,"netEffectMinor":500,"featureMode":"BASE"}
}
```
2. Serialize with RFC 8785 JCS canonical JSON (UTF-8).
3. Compute SHA-256.
4. Encode as lowercase hex with prefix: `sha256:<hex>`.

## 9.2 Final Production Key Name for Server/Localization Notifications

- Final server notification enable key in browser policy mapping:
  - `USE_JP_NOTIFICATION`.
- Localization/custom translation path source key:
  - `CUSTOMER_SETTINGS_URL` (resolved by GS into runtime `contentPath` field).

## 9.3 History Flow Decision

- History is browser-pulled directly from GS using:
  - `POST /slot/v1/gethistory`.
- History is not embedded as a mandatory full payload in every gameplay response.
- Gameplay responses may include minimal history hints, but canonical history retrieval is `gethistory`.

## 10) Canonical Type Precision (Normative)

These wire types are mandatory and must be consistent with:
- `docs/gs/bootstrap-config-contract.md`
- `docs/gs/internal-slot-runtime-contract.md`

- `requestCounter`: JSON integer (`number`), non-negative, max `9007199254740991`.
- `stateVersion`: JSON integer (`number`), non-negative, max `9007199254740991`.
- `currentStateVersion`: JSON integer (`number`), non-negative, max `9007199254740991`.
- Minor-unit monetary fields (`balanceMinor`, `previousBalanceMinor`, `coinValueMinor`, `totalBetMinor`, `betMinor`, `winMinor`, `netEffectMinor`): JSON integer (`number`) in minor units only (no decimal fractions, no string encoding).
