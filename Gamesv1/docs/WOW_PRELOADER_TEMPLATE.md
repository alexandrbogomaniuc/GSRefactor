# WOW_PRELOADER_TEMPLATE

## Purpose

The WOW preloader is a reusable shell surface for premium slot games. It renders immediately using procedural Pixi graphics, accepts brand injection, and shows real asset-load progress from the navigation layer.

## Canonical Modules

- Shared preloader renderer: `packages/ui-kit/src/shell/preloader/WowPreloader.ts`
- Theme/token resolver: `packages/ui-kit/src/shell/theme/ShellThemeTokens.ts`
- Premium-slot integration: `games/premium-slot/src/app/screens/LoadScreen.ts`
- Premium-slot brand presets: `games/premium-slot/src/app/theme/brandKit.ts`

## Behavior

1. Preload bundle assets initialize first.
2. `LoadScreen` is shown before bootstrap/config/localization completes.
3. Boot phases advance the branded preloader through:
   - `CONNECTING TO GS`
   - `SYNCING CONFIG`
   - `LOCALIZING SHELL`
4. `navigation.showScreen(MainScreen)` feeds real `onLoad(progress)` values into the preloader.
5. The progress bar blends boot progress with real asset loading (`20..100%` reserved for bundle load).

## Visual Stack

- brand lockup (logo + display name)
- hero FX ring/vortex/sweep
- animated progress bar with glint
- lightweight orbital particles
- reduced-motion/minimal fallback

## Audio

- Optional `audioStingerCue` support is gesture-gated.
- If no user gesture happens, the preloader remains silent.
- Preloader audio uses the shared cue registry and respects stored master-volume settings.

## Query Proof Modes

- `?brand=A`
- `?brand=B`
- `?motion=minimal`
- `?preloaderHoldMs=2000` for proof capture only

These are proof/dev selectors only. They do not change GS runtime contracts.
