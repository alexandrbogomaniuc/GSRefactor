# PROJECT

High-level architecture charter for Gamesv1.

## 1. Mission

Build and maintain the reference GS slot browser client shell and deterministic release-packaging stack.

## 2. Locked Architecture

### Runtime ownership
- GS owns session, wallet, DB state, restore behavior, requestCounter, idempotency, routing, and config resolution.
- Browser is presentation-only and renders GS-provided truth.
- Internal slot-engine sidecar and RNG are GS-private/internal.

### Canonical transport
- Browser -> GS only, using slot-browser-v1 HTTP operations.
- Canonical operations: `bootstrap`, `opengame`, `playround`, `featureaction`, `resumegame`, `closegame`, `gethistory`.
- Legacy `abs.gs.v1` WebSocket is non-canonical, experimental only.

### Assets and release
- Static client assets are served from CDN/static origin.
- Each release produces immutable, versioned artifacts for GS registration, canary, and rollback.

## 3. Scope

In scope:
- single-player slot browser client shell
- capability/config compliance runtime behavior
- deterministic release artifact generation

Out of scope:
- operator-specific messaging as canonical runtime path
- multiplayer
- browser-direct internal slot-engine integrations

## 4. Core layers

1. Protocol layer: `packages/core-protocol`
2. Compliance layer: `packages/core-compliance`
3. Engine layer: `packages/pixi-engine`
4. UI layer: `packages/ui-kit`
5. Game layer: `games/premium-slot`

## 5. Non-negotiable constraints

1. Browser never authoritatively mutates wallet/session/DB truth.
2. Game code must not directly use `window.postMessage` or `WebSocket` for canonical runtime.
3. Browser transport contracts come from `docs/gs/browser-runtime-api-contract.md`.
4. Bootstrap config truth comes from `docs/gs/bootstrap-config-contract.md`.
5. Release registration artifact contract comes from `docs/gs/release-registration-contract.md`.
