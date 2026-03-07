# BRAND_KIT_TOKENS

## Purpose

Brand kits provide lightweight visual injection for reusable shell surfaces without forking the runtime client. They are one family inside the unified `ShellThemeTokens` object and should remain visual-only.

## Brand Tokens

- `displayName`: primary lockup label shown in the shell
- `logoAssetKey`: use a bundled preload asset when the logo is part of the game package
- `logoUrl`: use an external or inline logo source when a preload asset key is not available
- `primaryColor`: main shell accent color in `#RRGGBB` or `#RRGGBBAA`
- `accentColor`: secondary highlight or glint color in `#RRGGBB` or `#RRGGBBAA`

## Preloader Tokens

- `style`: `wow` or `minimal`
- `heroFx`: `energyRing`, `coinVortex`, or `slotSweep`
- `vfxIntensity`: `0..1`
- `audioStingerCue`: optional cue name resolved through the shared audio cue registry

## Canonical Resolver

- `resolveShellThemeTokens({ overrides })`
- Module: `packages/ui-kit/src/shell/theme/ShellThemeTokens.ts`

Brand and preloader overrides are merged into the existing shell token defaults. They do not replace the rest of the shell theme surface.

## Premium-Slot Proof Presets

- `brand=A`
  - `Aurora Vault`
  - preload logo via `logoAssetKey`
  - WOW preloader using `energyRing`
- `brand=B`
  - `Neon Harbor`
  - inline SVG logo via `logoUrl`
  - reduced-motion-friendly `minimal` preloader using `slotSweep`

## Seed Patch Pattern

Example seed file:

- `docs/examples/release-pack/premium-slot/brand-betonline-token-seed.example.json`

Seed rules:

1. Commit only placeholder text or approved asset identifiers for `logoAssetKey` and `logoUrl`.
2. Commit only approved hex colors or explicit placeholder hex values.
3. Do not add operator runtime state, GS config, wallet/session data, or release metadata here.
4. Prefer patch-shaped seeds over full copied token objects to reduce drift when defaults evolve.

## Drift Prevention

1. Add new shell token families in `ShellThemeTokens.ts` first.
2. Document token meaning here before per-game adoption.
3. Keep token values visual and configural only; no GS runtime state belongs here.
4. Keep the WOW preloader field set stable: `style`, `heroFx`, `vfxIntensity`, `audioStingerCue`.
