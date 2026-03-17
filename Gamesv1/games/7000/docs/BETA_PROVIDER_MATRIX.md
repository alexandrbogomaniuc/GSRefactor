# Beta Provider Matrix

Game 7000 keeps three selectable provider modes: `openai`, `nanobanana`, and `donorlocal`.

`donorlocal` is now the internal DEV benchmark default when no explicit provider is selected and a valid ignored local manifest is available. `openai` remains the committed fallback provider. `nanobanana` support stays available by explicit opt-in, but it is paused as the benchmark target for current presentation work.

## Selection precedence

1. Query param: `assetProvider=openai|nanobanana|donorlocal`
2. Env var: `VITE_ASSET_PROVIDER=openai|nanobanana|donorlocal`
3. DEV-only benchmark default: `donorlocal`, only when no explicit provider is set and the ignored local manifest validates
4. Safe committed fallback: `openai`

## Preferred one-port workflow

Run:

```bash
export PATH=/Users/alexb/.nvm/versions/node/v22.22.1/bin:$PATH
corepack pnpm -C Gamesv1/games/7000 dev:benchmark
```

URLs:

- donorlocal benchmark default: `http://127.0.0.1:8081/?allowDevFallback=1`
- OpenAI fallback override: `http://127.0.0.1:8081/?allowDevFallback=1&assetProvider=openai`
- NanoBanana explicit override: `http://127.0.0.1:8081/?allowDevFallback=1&assetProvider=nanobanana`

## Optional explicit provider scripts

Run one per terminal if you need a hard-pinned provider:

```bash
export PATH=/Users/alexb/.nvm/versions/node/v22.22.1/bin:$PATH
corepack pnpm -C Gamesv1/games/7000 dev:openai
corepack pnpm -C Gamesv1/games/7000 dev:nanobanana
corepack pnpm -C Gamesv1/games/7000 dev:donorlocal
```

URLs:

- OpenAI: `http://127.0.0.1:8081/?allowDevFallback=1`
- NanoBanana: `http://127.0.0.1:8082/?allowDevFallback=1`
- donorlocal: `http://127.0.0.1:8083/?allowDevFallback=1`

## donorlocal requirements

`donorlocal` is DEV-only and never statically imported into the committed bundle.

Preferred local manifest path:

`Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/runtime/manifest.json`

Relative URLs inside the manifest are resolved relative to that file.

Required provider keys:

- wordmark/preloader lockup
- background desktop/landscape/portrait
- ui atlas with `reel-frame-panel`
- symbol atlas with `symbol-0-egg`, `symbol-9-rooster`, `collector-symbol`
- vfx atlas with `lightning-arc-01`

## Expected fallback behavior

- If DEV benchmark mode auto-selects donorlocal and the local manifest is missing or invalid: warn once and continue on `openai`
- If `assetProvider=donorlocal` is set but the manifest is missing or invalid: warn once and continue on `openai`
- If `nanobanana` is selected with an incomplete committed pack: keep the requested provider, log missing keys, and use the existing safe-placeholder behavior instead of crashing

## Debug hooks

- `window.__game7000ProviderPack` exposes `requestedProvider`, `effectiveProvider`, `missingKeys`, `fallbackReason`, and selected backgrounds
- `window.__game7000` keeps the existing demo controls for spin, proof-state checks, and provisional math presets
