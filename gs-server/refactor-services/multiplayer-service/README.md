# ABS Multiplayer Service (Scaffold)

Phase 6 scaffold service for multiplayer routing decisions and basic lobby/session operations.

## Scope in this increment
- Bank-level multiplayer capability check via routing decision endpoint.
- Lobby/session sync scaffolding endpoints.
- No production cutover from legacy multiplayer runtime.

## Endpoints
- `GET /health`
- `GET /api/v1/multiplayer/routing/decision?bankId=6275&gameId=838&sessionId=s1&isMultiplayer=true`
- `POST /api/v1/multiplayer/lobby/join`
- `POST /api/v1/multiplayer/room/sit-in`
- `POST /api/v1/multiplayer/room/sit-out`
- `POST /api/v1/multiplayer/session/sync`
- `GET /api/v1/multiplayer/sessions`
