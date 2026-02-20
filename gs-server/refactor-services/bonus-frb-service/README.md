# ABS Bonus/FRB Service (Scaffold)

Phase 5 scaffold service for FRB-related checks and state transitions.

## Scope in this increment
- Idempotent consume/release operations by `operationId`.
- FRB check/list endpoints.
- No production cutover from legacy FRB logic.
- Canary routing decision endpoint by bank profile.

## Endpoints
- `GET /health`
- `GET /api/v1/bonus/frb/routing/decision?bankId=6275`
- `GET /api/v1/bonus/frb/check`
- `POST /api/v1/bonus/frb/consume`
- `POST /api/v1/bonus/frb/release`
- `GET /api/v1/bonus/frb`
