# GS Browser Runtime API Contract (slot-browser-v1)

Status: canonical transport contract for browser -> GS runtime.

## Scope

- Browser talks only to GS public runtime endpoints.
- Browser never calls the internal slot-engine sidecar.
- RequestCounter/idempotency/currentStateVersion are GS-authoritative coordination fields.

## Common Envelope

### Common Request Metadata

```json
{
  "requestCounter": 12,
  "idempotencyKey": "string",
  "clientOperationId": "string",
  "currentStateVersion": "string"
}
```

Rules:
1. `requestCounter` is monotonic per session.
2. Retries must reuse the same idempotency identity.
3. `clientOperationId` must be stable across retries.
4. `currentStateVersion` must be forwarded when provided by GS.

## Endpoints

1. `POST /v1/bootstrap`
- Purpose: resolve launch context and return canonical bootstrap payload.
- Required body: `sessionId`.
- Optional: `gameId`, `bankId`, `playerId`, `language`, `launchParams`.
- Returns: bootstrap contract from `docs/gs/bootstrap-config-contract.md`.

2. `POST /v1/opengame`
- Purpose: enter runtime session for active gameplay.
- Required body: `sessionId`, `gameId`.
- Optional: context fields from bootstrap.
- Returns: session/wallet snapshot + runtime policy subset.

3. `POST /v1/playround`
- Purpose: execute one full round (bet + settle server-side).
- Required body: session + round intent + common metadata.
- Returns: updated wallet/session metadata + browser-safe `presentationPayload`.

4. `POST /v1/featureaction`
- Purpose: execute runtime feature action (buy/free-spin-continue/respin-step/etc).
- Required body: `action`, optional `payload`, common metadata.
- Returns: updated sequencing fields + browser-safe `presentationPayload`.

5. `POST /v1/resumegame`
- Purpose: recover unfinished round/session state after reconnect.
- Required body: `sessionId` + common metadata.
- Returns: session/wallet snapshot + restore payload.

6. `POST /v1/closegame`
- Purpose: close runtime session.
- Required body: `sessionId` + optional reason + common metadata.
- Returns: close acknowledgement.

7. `POST /v1/gethistory`
- Purpose: browser-facing in-game history retrieval.
- Required body: `sessionId` + common metadata.
- Optional: `pageNumber`.
- Returns: history records + sequencing fields.

## Browser-visible Payload Boundaries

- Allowed in browser payload: symbols/stops/win-lines/amounts/feature labels/localized display helpers.
- Not allowed as browser state truth: internal engine RNG state, audit traces, private sidecar internals.

## Gamesv1 Mapping

- `IGameTransport.bootstrap` -> `/v1/bootstrap`
- `IGameTransport.openGame` -> `/v1/opengame`
- `IGameTransport.playRound` -> `/v1/playround`
- `IGameTransport.featureAction` -> `/v1/featureaction`
- `IGameTransport.resumeGame` -> `/v1/resumegame`
- `IGameTransport.closeGame` -> `/v1/closegame`
- `IGameTransport.getHistory` -> `/v1/gethistory`
