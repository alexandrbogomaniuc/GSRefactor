# PHASE1_GOLDEN_PATH

Canonical happy-path runtime flow for Gamesv1 phase-1 slot clients.

Contracts:
- Runtime/release wire contract: `docs/gs/*`
- Client behavior/capabilities: `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md`

## 1. bootstrap

Endpoint: `POST /slot/v1/bootstrap`

Request essentials:
- `contractVersion`
- `sessionId`
- `requestCounter`
- `currentStateVersion`
- `bootstrapRef`
- `launchContext`

Rules:
- Read-only operation.
- No idempotency/client-operation headers.
- Hydrates bootstrap/session/context/policy/config state only.
- Bootstrap is not a mutating runtime envelope.

## 2. opengame

Endpoint: `POST /slot/v1/opengame`

Request essentials:
- mutating sequencing fields (`requestCounter`, `currentStateVersion`, `idempotencyKey`, `clientOperationId`)
- `bootstrapRef`

Rules:
- Advances runtime sequencing.
- Produces open-session snapshot for browser presentation layer.

## 3. playround

Endpoint: `POST /slot/v1/playround`

Request essentials:
- mutating sequencing fields
- `bootstrapRef`
- `selectedBet` (`coinValueMinor`, `lines`, `multiplier`, `totalBetMinor`)
- optional `selectedFeatureChoice`

Rules:
- Outcome/wallet/session truth is server-authoritative.
- Browser renders only from canonical `presentationPayload`.

## 4. featureaction / resumegame

Endpoints:
- `POST /slot/v1/featureaction`
- `POST /slot/v1/resumegame`

Rules:
- `featureaction` for runtime feature decisions (buy feature, etc.)
- `resumegame` for restore/recovery path (uses `resumeRef` when provided)
- Both are mutating and require canonical sequencing/idempotency headers.

## 5. gethistory

Endpoint: `POST /slot/v1/gethistory`

Request essentials:
- `requestCounter`
- `currentStateVersion`
- `historyQuery`

Rules:
- Read-only operation.
- Reuses accepted request/state counters.
- Must not advance gameplay state.

## 6. closegame

Endpoint: `POST /slot/v1/closegame`

Request essentials:
- mutating sequencing fields
- `closeReason`

Rules:
- Canonical session close operation.
- Final envelope is recorded and browser session state is cleared.

## Browser state model

Required store split:
- Session/runtime envelope store (server-authoritative values only)
- Bootstrap config/session/context store (bootstrap-authoritative values only)
- Resolved runtime config store
- Presentation/UI state store

Non-negotiable:
- No browser-side RNG or local outcome truth.
- No direct browser -> slot-engine communication.
- No direct canonical runtime `WebSocket` path.
