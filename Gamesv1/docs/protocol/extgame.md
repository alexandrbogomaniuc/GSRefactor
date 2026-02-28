# Protocol: GS HTTP Runtime Path (Canonical)

## Overview

This document defines the canonical production runtime path for Gamesv1 clients.
The client interacts with GS over HTTP runtime endpoints and treats GS responses as authoritative state.

## Ownership Model

- GS owns session, wallet, DB state, restore data, requestCounter, and idempotency decisions.
- Browser client is presentation-only.
- Client must never fabricate or authoritatively mutate financial/session state.

## Canonical Flow

### 1. Init/Enter
- Client sends launch/session token and context.
- GS returns session + wallet snapshot + runtime config.
- Client stores this as source state for rendering.

### 2. processTransaction
- Client sends financial action request with required idempotency key and sequencing data.
- GS validates requestCounter/idempotency and resolves outcome.
- GS returns updated wallet/session/game state.

### 3. Restore
- On reconnect/reload, client re-enters via HTTP init endpoint.
- GS may return restore payload for interrupted round state.
- Client resumes presentation from GS restore payload.

## Rules

1. Retry requests must preserve idempotency keys.
2. Client must respect GS sequencing/requestCounter semantics.
3. Client must apply GS-provided config/limits before initiating requests.
4. Client must not derive wallet truth from local estimates.

## Legacy Compatibility

`abs.gs.v1` WebSocket may exist for experiments/legacy integrations, but it is not canonical production runtime.
