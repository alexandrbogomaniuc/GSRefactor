# ABS Wallet Adapter (Scaffold)

Phase 5 scaffold service for wallet financial operation intents behind compatibility facade.

## Scope in this increment
- Idempotent reserve/settle/refund operation contract.
- Local operation store and outbox tracking for async relay integration.
- No cutover from legacy wallet path.

## Non-goals
- No direct external wallet call yet.
- No behavior change on legacy GS financial path.

## Run locally
```bash
npm install
PORT=18075 npm start
```

## Endpoints
- `GET /health`
- `POST /api/v1/wallet/reserve`
- `POST /api/v1/wallet/settle`
- `POST /api/v1/wallet/refund`
- `GET /api/v1/wallet/operations`
- `GET /api/v1/outbox`
- `POST /api/v1/outbox/:eventId/ack`
