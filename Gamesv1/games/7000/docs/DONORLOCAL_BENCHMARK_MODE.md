# Donorlocal Benchmark Mode

Base branch chosen for this pass:

- `codex/qa/7000-beta5d-authored-line-presentation-20260316-1907`

Working branch for this benchmark-mode pass:

- `codex/qa/7000-donorlocal-benchmark-mode-20260317-1117`

This mode is for internal development only.

- `donorlocal` stays local-only and DEV-only
- donor binaries and donor runtime assets are not committed into Git
- GS contracts are unchanged
- `openai` remains the committed fallback provider
- `nanobanana` support remains intact but is paused as the benchmark target for this pass

## What changed

When no explicit `assetProvider` is supplied in DEV mode, Game 7000 now tries to use `donorlocal` as the internal benchmark provider.

Selection order:

1. Query param `assetProvider=...`
2. Env var `VITE_ASSET_PROVIDER=...`
3. DEV-only donorlocal benchmark default when a valid ignored local manifest is present
4. `openai` fallback when donorlocal is absent, invalid, or DEV mode is not active

This keeps the internal presentation benchmark close to the donor while preserving the committed runtime architecture and safe fallback path.

## Local donor manifest path

Preferred manifest path relative to the active branch:

`Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/runtime/manifest.json`

Current resolved local manifest on this workstation:

`/Users/alexb/Documents/Dev/_worktrees/7000-beta4a-runtime-slot-system-20260310-0841/Gamesv1/GameseDonors/ChickenGame/assets/_donor_raw_local/runtime/manifest.json`

The Vite config still searches the wider `/Users/alexb/Documents/Dev` workspace for the first available donor manifest so ignored local donor bundles can stay outside the current active worktree.

## Launch commands

Preferred benchmark command:

```bash
export PATH=/Users/alexb/.nvm/versions/node/v22.22.1/bin:$PATH
corepack pnpm -C Gamesv1/games/7000 dev:benchmark
```

`dev:benchmark` now uses Vite `--strictPort`, so port `8081` is mandatory. If `8081` is already occupied, the command fails loudly instead of silently shifting to another port.

OpenAI-pinned command:

```bash
export PATH=/Users/alexb/.nvm/versions/node/v22.22.1/bin:$PATH
corepack pnpm -C Gamesv1/games/7000 dev:openai
```

## Launch URLs

Donorlocal benchmark:

`http://127.0.0.1:8081/?allowDevFallback=1&mathSource=provisional`

OpenAI fallback:

`http://127.0.0.1:8081/?allowDevFallback=1&mathSource=provisional&assetProvider=openai`

Optional explicit donorlocal pin:

`http://127.0.0.1:8081/?allowDevFallback=1&mathSource=provisional&assetProvider=donorlocal`

## Fallback behavior

If donorlocal is missing or invalid:

- the game logs a clear warning
- `window.__game7000ProviderPack` shows `requestedProvider=donorlocal` and `effectiveProvider=openai`
- runtime startup continues without crashing
- all recent provisional math and line-presentation work remains available on the `openai` fallback path

## Benchmark intent

Use donorlocal as the visual benchmark for:

- layout
- alignment
- topper and cabinet composition
- line presentation
- feature choreography
- preloader composition

This benchmark mode is not a release or production runtime path. It exists to help internal rebuild work track donor presentation quality while keeping our own runtime seams, GS contracts, and committed provider assets intact.
