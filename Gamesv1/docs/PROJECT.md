# PROJECT

High-level architecture charter for Gamesv1.

## Mission

Build and maintain the reference GS slot browser client shell and deterministic release-packaging stack.

## Canonical specifications

- Client capability/behavior spec: `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md`
- Runtime/release contract spec: `docs/gs/README.md` and `docs/gs/*`

## Locked architecture

### Runtime ownership

- GS owns session, wallet, DB persistence, restore behavior, requestCounter, idempotency, routing, and config resolution.
- Browser is presentation-only for financial/session truth.
- Internal slot-engine and RNG are private/internal GS concerns.

### Canonical transport

- Browser -> GS only.
- Canonical endpoint prefix: `/slot/v1/*`.
- Canonical operations: `bootstrap`, `opengame`, `playround`, `featureaction`, `resumegame`, `closegame`, `gethistory`.
- Canonical history endpoint: `/slot/v1/gethistory`.
- `abs.gs.v1` WebSocket is legacy/experimental only.

### Assets and release

- Static client assets are served from CDN/static origin.
- Each release produces immutable, versioned artifacts for GS registration, canary, and rollback.

## Scope

In scope:
- single-player slot browser client shell
- capability/config compliance behavior
- deterministic release artifact generation

Out of scope:
- operator-specific runtime messaging
- multiplayer
- browser-direct communication with internal slot-engine

## Non-negotiable constraints

1. Browser never authoritatively mutates wallet/session/DB truth.
2. Game code must not directly use `window.postMessage` or `WebSocket` for canonical runtime.
3. Runtime transport and release contracts come from `docs/gs/*`.
