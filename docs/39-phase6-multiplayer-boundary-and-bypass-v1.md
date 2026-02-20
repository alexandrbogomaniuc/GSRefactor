# Phase 6 Multiplayer Boundary and Bypass Rules v1

Last updated: 2026-02-20 UTC
Scope: isolate multiplayer logic into dedicated service while non-multiplayer remains main product path.

## 1) Ownership split
1. Core GS (non-multiplayer focus): launch/session/gameplay for non-MP games.
2. Multiplayer Service: lobbies, rooms, sit-in/sit-out, MP websocket ownership.

## 2) Mandatory bank flag
- `isMultiplayer` at bank-level config controls MP paths.
- If `isMultiplayer=false`:
  - skip multiplayer code paths entirely,
  - do not call MP lobby/room flows,
  - keep launch and wallet flows on non-MP stack.

## 3) Routing precedence
1. Explicit bank `isMultiplayer=false` -> force non-MP path.
2. `isMultiplayer=true` and game supports MP -> route to multiplayer service boundary.
3. Any MP service failure -> fail-open to legacy compatible fallback (until deprecation approved).

## 4) API boundary (initial)
- `GET /api/v1/multiplayer/routing/decision?bankId&gameId&sessionId`
- `POST /api/v1/multiplayer/lobby/join`
- `POST /api/v1/multiplayer/room/sit-in`
- `POST /api/v1/multiplayer/room/sit-out`
- `POST /api/v1/multiplayer/session/sync`

## 5) Telemetry requirements
Each MP boundary call must include/emit:
- `traceId`, `sessionId`, `bankId`, `gameId`, `operationId`, `configVersion`.

## 6) Canary gates (MP extraction)
1. No reconnect regression for MP banks.
2. No room ownership mismatch.
3. No duplicate financial operation from MP transitions.
4. Rollback switch validated per bank.

## 7) Rollout
1. Start with one MP canary bank.
2. Keep `isMultiplayer=false` for non-MP banks (default safe state).
3. Expand only after parity and reconnect gates pass.
