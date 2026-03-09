# Beta Provider Matrix

Game 7000 supports a single selector workflow for `openai`, `nanobanana`, and `donorlocal`.

## Selection precedence

1. Query param: `assetProvider=openai|nanobanana|donorlocal`
2. Env var: `VITE_ASSET_PROVIDER=openai|nanobanana|donorlocal`
3. Config default: `game.settings.json -> assets.provider`

## Preferred one-port workflow

Run:

```bash
export PATH=/Users/alexb/.local/v22.22.1/bin:$PATH
corepack pnpm -C Gamesv1/games/7000 dev:oneport
```

URLs:

- OpenAI: `http://localhost:8081/?allowDevFallback=1&assetProvider=openai`
- NanoBanana: `http://localhost:8081/?allowDevFallback=1&assetProvider=nanobanana`
- donorlocal: `http://localhost:8081/?allowDevFallback=1&assetProvider=donorlocal`

## Optional 3-port workflow

Run one per terminal:

```bash
export PATH=/Users/alexb/.local/v22.22.1/bin:$PATH
corepack pnpm -C Gamesv1/games/7000 dev:openai
corepack pnpm -C Gamesv1/games/7000 dev:nanobanana
corepack pnpm -C Gamesv1/games/7000 dev:donorlocal
```

URLs:

- OpenAI: `http://localhost:8081/?allowDevFallback=1`
- NanoBanana: `http://localhost:8082/?allowDevFallback=1`
- donorlocal: `http://localhost:8083/?allowDevFallback=1`

## donorlocal requirements

`donorlocal` is DEV-only and never statically imported into the committed bundle.

Required local manifest path:

`Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/runtime/manifest.json`

Relative URLs inside the manifest are resolved relative to that file.

Expected manifest shape:

```json
{
  "wordmarkUrl": "./logo.svg",
  "backgrounds": {
    "desktop": "./background-desktop-1920x1080.png",
    "landscape": "./background-landscape-safe-1600x900.png",
    "portrait": "./background-portrait-1080x1920.png"
  },
  "uiAtlas": {
    "imageUrl": "./atlas_ui.png",
    "dataUrl": "./atlas_ui.json"
  },
  "symbolAtlas": {
    "imageUrl": "./atlas_symbols.png",
    "dataUrl": "./atlas_symbols.json"
  },
  "vfxAtlas": {
    "imageUrl": "./atlas_vfx.png",
    "dataUrl": "./atlas_vfx.json"
  }
}
```

Required provider keys:

- wordmark/preloader lockup
- background desktop/landscape/portrait
- ui atlas with `reel-frame-panel`
- symbol atlas with `symbol-0-egg`, `symbol-9-rooster`, `collector-symbol`
- vfx atlas with `lightning-arc-01`

## Expected fallback behavior

- If `donorlocal` manifest is missing: warn and fallback to `openai`
- If `donorlocal` manifest is incomplete: warn, expose missing keys, and fallback to `openai`
- If `nanobanana` is selected with an incomplete committed pack: keep the requested provider, log missing keys, and use the existing safe-placeholder behavior instead of crashing

## Debug hooks

- `window.__game7000ProviderPack` exposes `requestedProvider`, `effectiveProvider`, `missingKeys`, `fallbackReason`, and selected backgrounds
- `window.__game7000` keeps the existing demo controls for spin and proof-state checks
