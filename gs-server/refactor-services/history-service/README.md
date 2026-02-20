# ABS History Service (Scaffold)

Phase 5 scaffold service for append/query of gameplay lifecycle history records.

## Scope in this increment
- Idempotent append via `operationId`.
- Query by bank/session/event type.
- No cutover from legacy history path.

## Endpoints
- `GET /health`
- `POST /api/v1/history/records`
- `GET /api/v1/history/records`
