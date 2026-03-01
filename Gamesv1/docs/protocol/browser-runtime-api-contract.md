# Browser Runtime API Contract (Phase-1)

Status: canonical for Gamesv1 browser -> GS runtime client integration.

## Scope

This contract covers only browser-facing runtime calls.
Browser never calls internal slot-engine APIs directly.

## Base

- Runtime API base URL is provided by launch/bootstrap parameters.
- Default local development example: `http://127.0.0.1:6400`.

## Endpoints

1. `POST /v1/opengame`
- Purpose: open/bootstrap runtime session.
- Required: `sessionId`
- Optional: `bankId`, `playerId`, `gameId`, `gsInternalBaseUrl`, `language`, `internalClientCode`
- Returns: `sessionId`, `balance`, `requestCounter`, optional limits/state payload.

2. `POST /v1/placebet`
- Purpose: submit round bet request.
- Required: `sessionId`, `requestCounter`, `bets[]`
- Required per request: `clientOperationId` (or transport-generated).
- Optional: `currentStateVersion`
- Returns: `roundId`, optional `math/details` for presentation mapping.

3. `POST /v1/collect`
- Purpose: settle/collect round result.
- Required: `sessionId`, `requestCounter`, `roundId`
- Required per request: `clientOperationId` (or transport-generated).
- Optional: `currentStateVersion`
- Returns: `balance`, `winAmount`, optional state version.

4. `POST /v1/readhistory`
- Purpose: browser-facing history access.
- Required: `sessionId`, `requestCounter`
- Optional: `pageNumber`, `currentStateVersion`
- Returns: `history[]`, `requestCounter`.

5. Optional endpoints when supported by backend:
- `POST /v1/featureaction`
- `POST /v1/closegame`

## Sequencing + Idempotency

1. `requestCounter` must be monotonic per session.
2. Transport must send `idempotencyKey` and `clientOperationId` for money-impacting operations.
3. Retries reuse the same idempotency identity.
4. `currentStateVersion` is forwarded when provided by GS.

## Browser State Ownership Rules

1. Browser is presentation-only for wallet/session/unfinished-round truth.
2. Wallet and unfinished-round values in browser stores are only mirrored from GS responses.
3. Local animation state is allowed, but local round truth is not.

## Gamesv1 Mapping

- `IGameTransport.bootstrap/openGame` -> `/v1/opengame`
- `IGameTransport.playRound` -> `/v1/placebet` + `/v1/collect`
- `IGameTransport.readHistory` -> `/v1/readhistory`
- `IGameTransport.featureAction` -> `/v1/featureaction` (if available)
- `IGameTransport.closeGame` -> `/v1/closegame` (if available)
