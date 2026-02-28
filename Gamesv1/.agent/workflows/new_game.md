# /new_game - Create a New Game Package

Trigger:
Use this workflow whenever a new game must be added under `games/<gameId>`.

Required Inputs:
- `gameId`: kebab-case unique identifier (example: `dragon-flare`)
- `name`: marketing display name (example: `Dragon Flare`)
- `themeId`: theme key used by theme config (example: `eastern-fantasy`)
- `languages` (optional): comma-separated locales; must include `en` and at least 2 additional languages

Canonical Command:
```bash
npm run create-game -- --gameId <gameId> --name "<name>" --themeId <themeId> --languages en,es,de
```

Steps (must be followed in order):
1. Choose metadata:
- Validate `gameId` is lowercase kebab-case.
- Confirm `name` and `themeId` are finalized before scaffolding.

2. Scaffold `games/<gameId>` with required files:
- `game.json`
- `theme/theme.json`
- `i18n/en.json` and at least 2 more locale files
- `math/math-pack.json`
- `src/` entrypoints (`main.ts`, `bootstrap.ts`, `index.ts`)

3. Register translation keys (no raw strings):
- Create `src/i18n/keys.ts`.
- UI/game code must consume keys from this registry only.
- Add all keys to every locale file.

4. Implement outcome mapping stub:
- Create `src/outcome/OutcomeMapper.ts`.
- Export a mapper function with TODO hooks for provider payload normalization.

5. Integrate shared HUD schema + feature flags:
- Create `src/config/hud.ts` importing shared HUD schema from `@gamesv1/ui-kit`.
- Create `src/config/featureFlags.ts` using `FeatureFlags` from `@gamesv1/core-compliance`.

6. Run smoke tests using config stubs:
- Ensure `tests/smoke/configs/guest.json`, `free.json`, `real.json` exist.
- Run:
```bash
npm --prefix games/<gameId> run smoke:test
```

7. Produce release checklist + PR summary:
- Fill `docs/release-checklist.md`.
- Fill `docs/pr-summary.md` with scope, risks, tests, and rollback notes.

Acceptance Criteria:
- Scaffold command finishes without manual file creation.
- Required structure/files exist and compile as TypeScript.
- No raw UI strings in runtime code; translation keys are centralized.
- Smoke test passes for guest/free/real stub configs.
- Release checklist and PR summary files are present.