# ABS Protocol Adapter (Scaffold)

Phase 4 scaffold service for per-bank protocol mode routing (JSON/XML) and JSON hash security controls.

## Scope in this increment
- Bank-level effective protocol mode resolution (`XML` or `JSON`).
- Canonical request boundary endpoint (`/api/v1/protocol/requests/normalize`).
- JSON hash verification policy (`OFF`/`SHADOW`/`ENFORCE`) with fail-open defaults.
- Replay-check hooks (`X-Timestamp`, `X-Nonce`) in local cache mode for scaffolding.
- Hash exemption support for non-signed endpoints (config-driven).
- GET hash-string support with ordered query-field concatenation (config-driven).

## Non-goals
- No cutover from legacy protocol handlers.
- No business logic duplication; adapter only handles boundary normalization and security checks.

## Run locally
```bash
npm install
PORT=18078 npm start
```

## Endpoints
- `GET /health`
- `GET /api/v1/protocol/routing/decision`
- `GET /api/v1/protocol/banks/settings`
- `GET /api/v1/protocol/banks/:bankId/settings`
- `POST /api/v1/protocol/banks/:bankId/settings`
- `POST /api/v1/protocol/requests/normalize`
- `GET /api/v1/protocol/events`

## Important config keys
- `PROTOCOL_ADAPTER_JSON_HASH_EXEMPT_ENDPOINTS`
- `PROTOCOL_ADAPTER_JSON_GET_HASH_RULES`
