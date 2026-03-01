# Gamesv1 Monorepo

Gamesv1 is the client-shell and release-packaging environment for new GS slots.

## Target Architecture (Canonical)

1. Runtime transport target: GS HTTP runtime path.
2. Browser is presentation-only for financial/state truth.
3. GS owns session, wallet, DB state, restore, requestCounter, and idempotency.
4. Static assets are loaded from CDN/static origin.
5. Gamesv1 produces versioned release artifacts for GS registration.
6. `abs.gs.v1` WebSocket support is legacy/experimental only.

## Out Of Scope

- Operator-specific messaging (Pariplay/iframe bridge) is not part of canonical runtime.
- Multiplayer is out of scope.

## Repository Layout

- `packages/core-protocol`: transport abstraction + GS HTTP runtime client + schemas.
- `packages/core-compliance`: runtime config resolution, compliance rules, animation policy.
- `packages/pixi-engine`: Pixi bootstrap, asset loading, audio, resize/layout loop.
- `packages/ui-kit`: reusable slot UI/HUD components.
- `games/premium-slot`: reference game implementation.
- `tools/*`: scaffolding and release/config helper tools.
- `docs/*`: architecture, capability, release, and pipeline canon.

Legacy/optional packages may exist but are not canonical runtime dependencies.

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

## Tests

```bash
corepack pnpm run test:config
corepack pnpm run test:animation-policy
corepack pnpm run test:layout
corepack pnpm run test:contract
```

## Create New Game (Canonical)

```bash
npm run create-game -- --gameId <gameId> --name "<name>" --themeId <themeId> --languages en,es,de
```

## Artifact Outputs

Per release, Gamesv1 must produce:

- Client build output (`dist/`) for CDN/static hosting
- Asset manifest/bundles
- GS registration artifacts (`template-params.*`, release manifest, SQL artifact)
- Versioned release metadata tied to git SHA

## Source Of Truth Docs

- `docs/MasterContext.md`
- `docs/PROJECT.md`
- `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md`
- `docs/RELEASE_PROCESS.md`
- `docs/DOCS_MAP.md`
