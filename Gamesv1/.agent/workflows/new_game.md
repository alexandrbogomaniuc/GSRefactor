# /new_game - Create a New Canonical Game Package

Trigger:
Use this workflow when adding `games/<gameId>`.

Required inputs:
- `gameId` (kebab-case)
- `name`
- `themeId`
- `languages` (must include `en` + at least 2 more)

Canonical command:
```bash
corepack pnpm run create-game -- --gameId <gameId> --name "<name>" --themeId <themeId> --languages en,es,de
```

Steps:
1. Choose metadata: gameId/name/themeId.
2. Scaffold canonical structure:
- `game.settings.json`
- `theme/theme.json`
- `locales/<lang>/common.json`
- `locales/<lang>/paytable.json`
- `locales/<lang>/rules.json`
- `math/math-pack.manifest.json`
- `docs/asset-manifest.sample.json`
- `raw-assets/preload`, `raw-assets/main`, `raw-assets/promo`
- `src/` entrypoints
3. Register translation keys (`src/i18n/keys.ts`), no raw strings in runtime code.
4. Implement `src/runtime/OutcomeMapper.ts` stub using browser-visible `presentationPayload` only.
5. Integrate shared HUD schema + feature flags (`src/config/hud.ts`, `src/config/featureFlags.ts`).
6. Run smoke tests for guest/free/real stubs.
7. Produce release checklist + PR summary docs.

Constraints:
- Do not scaffold legacy `game.json` or flat `i18n/*.json` layout.
- Runtime transport must go through `@gamesv1/core-protocol`.
- No direct game-level WebSocket/postMessage for canonical runtime.

Acceptance:
- Scaffolded game compiles.
- Smoke tests pass.
- Files follow canonical structure.
