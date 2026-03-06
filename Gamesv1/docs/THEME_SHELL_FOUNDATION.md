# THEME_SHELL_FOUNDATION

## Purpose

The shell theme layer is the reusable brand/config surface for premium slot presentation systems. It stays presentation-only and does not change GS-authoritative runtime/session/wallet ownership.

## Canonical Module

- `packages/ui-kit/src/shell/theme/ShellThemeTokens.ts`

## Token Families

- `brand.displayName`
- `brand.logoUrl`
- `brand.logoAssetKey`
- `brand.primaryColor`
- `brand.accentColor`
- `preloader.style`
- `preloader.heroFx`
- `preloader.vfxIntensity`
- `preloader.audioStingerCue`

## Rules

1. Tokens are resolved through `resolveShellThemeTokens(...)`.
2. Defaults are strict and shared; games should override only what they need.
3. Validation is schema-driven so invalid colors, empty brand names, or out-of-range VFX intensity fail fast.
4. Theme tokens are presentation-only. They must not carry GS runtime truth or feature-state ownership.

## Current Usage

- Premium-slot brand presets: `games/premium-slot/src/app/theme/brandKit.ts`
- WOW preloader composition: `games/premium-slot/src/app/screens/LoadScreen.ts`
- Shared preloader renderer: `packages/ui-kit/src/shell/preloader/WowPreloader.ts`
