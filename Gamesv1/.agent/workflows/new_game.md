# /new_game - Create a New Canonical Game Package

Trigger:
Use this workflow when adding `games/<gameId>`.

Required inputs:
- `gameId` (kebab-case)
- `name`
- `themeId`
- `languages` (must include `en` + at least 2 more)

Read first:
- `docs/PHASE1_GOLDEN_PATH.md`
- `docs/gs/browser-runtime-api-contract.md`
- `docs/gs/release-registration-contract.md`
- `docs/gs/fixtures/release-registration.sample.json`
- `docs/gs/schemas/release-registration.schema.json`

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
- `math/math-package-manifest.json`
- `gs/release-registration.template.json`
- `gs/capability-profile.json`
- `gs/release-pack.expectations.json`
- `docs/asset-manifest.sample.json`
- `raw-assets/preload`, `raw-assets/main`, `raw-assets/promo`
- `src/` entrypoints
3. Register translation keys (`src/i18n/keys.ts`), no raw strings in runtime code.
4. Implement `src/runtime/OutcomeMapper.ts` stub using browser-visible `presentationPayload` only.
5. Integrate shared HUD schema + feature flags (`src/config/hud.ts`, `src/config/featureFlags.ts`).
6. Run smoke tests for guest/free/real stubs.
7. Produce release checklist + PR summary docs.

Release placeholder rule:
- `gs/release-registration.template.json` must follow the upstream `slot-release-registration-v1` shape exactly from `docs/gs/fixtures/release-registration.sample.json`.
- Do not invent additional registration fields or rename canonical fields.

Constraints:
- Do not scaffold legacy `game.json` or flat `i18n/*.json` layout.
- Do not scaffold legacy `math-pack.manifest.json` or `math-pack.json`.
- Runtime transport must go through `@gamesv1/core-protocol`.
- No direct game-level WebSocket/postMessage for canonical runtime.

Acceptance:
- Scaffolded game compiles.
- Smoke tests pass.
- Files follow canonical structure.
