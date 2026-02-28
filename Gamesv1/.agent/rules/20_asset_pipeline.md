# Asset Pipeline Rules (Canonical)

This rule file must stay aligned with:
- `docs/ART_AND_PROMO_PIPELINE.md`
- `docs/ASSET_MANIFEST_SPEC.md`

## Path Contract

- Raw source assets: `games/<gameId>/raw-assets/`
- Generated runtime manifest: `games/<gameId>/src/manifest.json`
- Runtime bundled outputs: under game `public/assets/` and `dist/` during build.

## Rules

1. No direct raw file-path usage in runtime game logic.
2. Access assets via manifest aliases/bundle keys only.
3. Keep atlas/page sizes and texture budgets within canonical quality gates.
4. Keep promo/preloader/big-win media listed in per-game asset manifest.
5. If pipeline behavior changes, update canonical docs first, then this rule file.
