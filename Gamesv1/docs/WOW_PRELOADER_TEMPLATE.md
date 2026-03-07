# WOW_PRELOADER_TEMPLATE

## Purpose

The WOW preloader is a reusable shell surface for premium slot games. It renders immediately using procedural Pixi graphics, accepts brand injection, and shows real asset-load progress from the navigation layer.

## Canonical Modules

- Shared preloader renderer: `packages/ui-kit/src/shell/preloader/WowPreloader.ts`
- Theme and token resolver: `packages/ui-kit/src/shell/theme/ShellThemeTokens.ts`
- Premium-slot integration: `games/premium-slot/src/app/screens/LoadScreen.ts`
- Premium-slot brand presets: `games/premium-slot/src/app/theme/brandKit.ts`

## Token Contract

- Preloader branding lives inside the unified `ShellThemeTokens` object.
- Canonical fields:
  - `brand.displayName`
  - `brand.logoAssetKey`
  - `brand.logoUrl`
  - `brand.primaryColor`
  - `brand.accentColor`
  - `preloader.style`
  - `preloader.heroFx`
  - `preloader.vfxIntensity`
  - `preloader.audioStingerCue`
- Premium-slot resolves those tokens through `@gamesv1/ui-kit` exports, not source-relative theme imports.

## Behavior

1. Preload bundle assets initialize first.
2. `LoadScreen` is shown before bootstrap, config, and localization complete.
3. Boot phases advance the branded preloader through:
   - `CONNECTING TO GS`
   - `SYNCING CONFIG`
   - `LOCALIZING SHELL`
4. `navigation.showScreen(MainScreen)` feeds real `onLoad(progress)` values into the preloader.
5. The progress bar blends boot progress with real asset loading, with `20..100%` reserved for bundle load.

## Visual Stack

- brand lockup, including logo and display name
- hero FX ring, vortex, or sweep
- animated progress bar with glint
- lightweight orbital particles
- reduced-motion or minimal fallback

## Audio

- Optional `audioStingerCue` support is gesture-gated.
- If no user gesture happens, the preloader remains silent.
- Preloader audio uses the shared cue registry and respects stored master-volume settings.

## Query Proof Modes

- `?brand=A`
- `?brand=B`
- `?motion=minimal`
- `?preloaderHoldMs=2000` for proof capture only

These are proof and development selectors only. They do not change GS runtime contracts.

## Proof Artifacts

- `docs/_visual_proof/preloader-wow-2026-03-06/brand-a-preloader.png`
- `docs/_visual_proof/preloader-wow-2026-03-06/brand-b-preloader.png`
- `docs/_visual_proof/preloader-wow-2026-03-06/preloader-wow.gif`

## Stability Contract

1. Keep preloader customization inside `ShellThemeTokens.brand` and `ShellThemeTokens.preloader`.
2. Preserve the canonical field set: `displayName`, `logoAssetKey`, `logoUrl`, `primaryColor`, `accentColor`, `style`, `heroFx`, `vfxIntensity`, `audioStingerCue`.
3. Do not add GS transport, outcome state, or release state to preloader tokens.
4. Use patch-shaped brand seeds for operator review instead of cloning a full theme object.
5. Preserve reduced-motion fallback behavior when `style=minimal` or low-performance mode is active.

## Seed Example

- `docs/examples/release-pack/premium-slot/brand-betonline-token-seed.example.json`

This seed is placeholder-only until approved logo assets and approved brand colors are provided.
