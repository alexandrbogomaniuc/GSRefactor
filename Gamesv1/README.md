# Gamesv1 Monorepo

Gamesv1 is the GS slot browser client shell and release-packaging environment.

## Canonical Runtime Target

1. Browser -> GS HTTP runtime only (`slot-browser-v1`).
2. Canonical browser endpoints are `/slot/v1/*`.
3. Browser is presentation-only for financial/session truth.
4. GS is authoritative for session, wallet, DB, restore, requestCounter, idempotency, routing, and config resolution.
5. `abs.gs.v1` WebSocket is legacy/experimental only.

## Out Of Scope

- Multiplayer.
- Operator/Pariplay messaging in canonical runtime path.

Optional/non-canonical modules:
- `packages/operator-pariplay/*` (integration adapter scope only)
- `packages/core-protocol/src/ws/*` (`abs.gs.v1` legacy/experimental)

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

## Test

```bash
corepack pnpm run test
```

## Canonical New-Game Scaffolder

```bash
corepack pnpm run create-game -- --gameId <gameId> --name "<name>" --themeId <themeId> --languages en,es,de
```

Authoritative path: `tools/create-game.ts`.

## Release-Pack Command

```bash
corepack pnpm run release:pack -- --game <gameId> --version <version> --static-origin <cdnBase>
```

## Source Of Truth

- Client capability spec: `docs/GAME_CLIENT_REQUIREMENTS_MAIN.md`
- Runtime/release contract spec: `docs/gs/README.md` and `docs/gs/*`
- Canon docs: `docs/MasterContext.md`, `docs/PROJECT.md`, `docs/DOCS_MAP.md`, `docs/RELEASE_PROCESS.md`
