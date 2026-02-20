# ABS GS WebSocket Contract Schemas (v1)

This folder contains the canonical JSON Schema files for the `abs.gs.v1` WebSocket contract.

## Files
- `abs-gs-v1-envelope.schema.json`: shared envelope for all messages.
- `abs-gs-v1-bet-request.schema.json`: client -> GS bet request.
- `abs-gs-v1-settle-request.schema.json`: client -> GS settle request.
- `abs-gs-v1-reconnect-request.schema.json`: client -> GS reconnect request.
- `abs-gs-v1-error.schema.json`: GS -> client error payload.
- `abs-gs-v1-session-sync.schema.json`: GS -> client reconnect sync payload.
- `examples/*.valid.json`: canonical sample payloads for conformance checks.

## Smoke conformance command
```bash
cd /Users/alexb/Documents/Dev/Dev_new
gs-server/deploy/scripts/ws-contract-smoke.sh
```

## Validation notes
- Financial amounts are encoded as decimal strings to preserve precision (`0.001` support).
- Unknown top-level fields remain allowed for forward compatibility.
- `operationId` is required for financial state transitions (`BET_REQUEST`, `SETTLE_REQUEST`).
