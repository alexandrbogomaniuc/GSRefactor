# Gamesv1 Monorepo

Clean monorepo for slot clients with strict package boundaries and one active reference game.

## Repository Layout

- `packages/core-protocol`: `IGameTransport`, abs.gs.v1 WS transport, extgame HTTP transport, Zod message schemas.
- `packages/core-compliance`: runtime config resolution, truncate-cents checks, min spin time and compliance flags.
- `packages/operator-pariplay`: operator iframe bridge (`postMessage`) and typings.
- `packages/pixi-engine`: Pixi app init, asset loading, audio plugin, resize/layout loop, navigation.
- `packages/ui-kit`: shared slot UI, HUD controls, dialogs/popups, reusable visual components.
- `games/premium-slot`: reference "gold standard" game.
- `games/_archive`: legacy/archived games and experiments.

## Workspace

This repo uses `pnpm` workspaces (`pnpm-workspace.yaml` + root `package.json` workspaces).

## Install

```bash
corepack pnpm install
```

## Run Dev

```bash
corepack pnpm run dev
```

## Build

```bash
corepack pnpm run build
```

## Contract Tests

```bash
corepack pnpm run test:contract
```

## Architecture Rules

- Games must not call `window.postMessage` directly: use `@gamesv1/operator-pariplay`.
- Games must not call `WebSocket` directly: use `@gamesv1/core-protocol`.
- Games must use manifest/bundle keys (no inline asset-path strings in game logic).
- Prefer shared code in `packages/*`; keep game folders thin.


