# MasterContext

This is the single canonical project context for Gamesv1.
If any other guide conflicts with this file, `docs/MasterContext.md` wins.

## Project Scope

Gamesv1 is a monorepo for compliance-ready slot clients.

Primary goals:
1. Keep one reference game (`games/premium-slot`) as the quality baseline.
2. Keep shared logic in `packages/*` with strict boundaries.
3. Keep protocol/compliance/operator behavior centralized and testable.

## Canonical Structure

- `packages/core-protocol`: network transport abstractions and schemas.
- `packages/core-compliance`: runtime config layering, compliance logic, animation/timing policy.
- `packages/operator-pariplay`: all operator frame `postMessage` integration.
- `packages/pixi-engine`: app bootstrap, navigation, resize, layout manager, asset loading.
- `packages/ui-kit`: shared gameplay UI and HUD components.
- `games/premium-slot`: reference implementation.

## Non-Negotiable Boundaries

1. Game code must not call `WebSocket` directly.
2. Game code must not call `window.postMessage` directly.
3. Game logic must not hardcode runtime asset paths; use manifest/bundle aliases.
4. Runtime behavior is server-authoritative for monetary outcomes.

## Source-of-Truth Docs

See `docs/DOCS_MAP.md` for canonical and archived documentation.

## Update Policy

When changing architecture or process rules:
1. Update this file first.
2. Update the specific canonical guide.
3. Archive or fix any contradictory guide in `docs/_archive`.
