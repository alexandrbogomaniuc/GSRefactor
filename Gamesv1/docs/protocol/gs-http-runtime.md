# Protocol: GS HTTP Runtime Path (Canonical)

## Overview

This document defines the canonical production runtime path for Gamesv1 clients.
The client interacts with GS over HTTP runtime endpoints and treats GS responses as authoritative state.

## Ownership Model

- GS owns session, wallet, DB state, restore data, requestCounter, and idempotency decisions.
- Browser client is presentation-only.
- Client must never fabricate or authoritatively mutate financial/session state.
- Browser only communicates with GS runtime endpoints.
- Internal slot-engine/RNG concerns are server-side only and not directly visible to the browser transport model.

## Canonical Flow

### 1. Open Game (`POST /v1/opengame`)
- Client sends launch/session token and context.
- GS returns session + wallet snapshot + runtime config.
- Client stores this as source state for rendering.
- Browser API operation name in client: `bootstrap/openGame`.

### 2. Play Round (`POST /v1/placebet` + `POST /v1/collect`)
- Client sends bet request with requestCounter/idempotency/clientOperationId.
- GS validates requestCounter/idempotency and resolves outcome.
- Client settles with `collect` and receives updated wallet/session state.
- Browser API operation name in client: `playRound`.

### 3. Restore (`POST /v1/opengame` via resume)
- On reconnect/reload, client re-enters via HTTP init endpoint.
- GS may return restore payload for interrupted round state.
- Client resumes presentation from GS restore payload.
- Browser API operation name in client: `resumeGame`.

### 4. History (`POST /v1/readhistory`)
- Browser reads GS-facing history endpoint when enabled.
- Browser API operation name in client: `readHistory`.

## Rules

1. Retry requests must preserve idempotency keys.
2. Client must respect GS sequencing/requestCounter semantics.
3. Client must apply GS-provided config/limits before initiating requests.
4. Client must not derive wallet truth from local estimates.
5. Client must treat server audit artifacts as diagnostics, not browser-owned runtime truth.

## Legacy Compatibility

`abs.gs.v1` WebSocket may exist for experiments/legacy integrations, but it is not canonical production runtime.

## Reference Contract

Detailed browser runtime endpoint contract:
- `docs/protocol/browser-runtime-api-contract.md`
