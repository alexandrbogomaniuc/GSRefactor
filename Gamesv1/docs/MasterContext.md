# MasterContext

This is the canonical architecture context for Gamesv1.
If any other doc conflicts, this file wins.

## Program Goal

Gamesv1 is the best-in-class GS slot client shell + release-packaging environment.

## Canonical Runtime Target

- Canonical runtime path is GS HTTP runtime transport.
- Browser client is presentation-only and must not own financial/state truth.
- GS is the source of truth for:
  - session
  - wallet
  - DB state
  - restore/recovery state
  - requestCounter sequencing
  - idempotency decisions

## Transport Policy

- HTTP runtime is primary and required for production path.
- `abs.gs.v1` WebSocket is legacy/experimental only.
- Game modules must consume transport via `@gamesv1/core-protocol` abstractions.
- Browser transport scope is `browser -> GS` only.
- Browser must not directly communicate with internal slot-engine services.

## Internal Engine Boundary

- Internal slot-engine sidecar/host is private GS infrastructure.
- RNG lives in the internal slot-engine host (server-side).
- Slot-engine audit/debug data is server-side only and not part of browser UI state ownership.

## Asset/Release Policy

- Runtime assets are loaded from CDN/static origin.
- Gamesv1 produces versioned release artifacts for GS registration and rollout.

## Scope Guardrails

- Ignore Pariplay/operator-specific messaging in canonical architecture.
- Multiplayer is out of scope.
- No core doc should imply the client or any public game server owns DB/session/wallet state.

## Package Responsibilities

- `packages/core-protocol`: GS HTTP runtime client, transport interfaces, schemas.
- `packages/core-compliance`: config layering + compliance/timing behavior.
- `packages/pixi-engine`: rendering/runtime shell and asset bootstrap.
- `packages/ui-kit`: shared slot UI/HUD primitives.
- `games/premium-slot`: reference implementation.

## Source Of Truth Map

See `docs/DOCS_MAP.md`.

## Update Rule

1. Update this file first for any architecture decision.
2. Update affected canonical docs.
3. Archive or mark deprecated any contradictory document.
