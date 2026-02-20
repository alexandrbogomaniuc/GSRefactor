# ABS History Service (Scaffold)

Phase 5 scaffold service for append/query of gameplay lifecycle history records.

## Scope in this increment
- Idempotent append via `operationId`.
- Query by bank/session/event type.
- No cutover from legacy history path.
- Canary routing decision endpoint by bank profile.

## Endpoints
- `GET /health`
- `GET /api/v1/history/routing/decision?bankId=6275`
- `POST /api/v1/history/records`
- `GET /api/v1/history/records`
