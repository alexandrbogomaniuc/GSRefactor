# BRAND_KIT_TOKENS

## Purpose

Brand kits provide lightweight visual injection for reusable shell surfaces without forking the runtime client. They are one family inside the unified `ShellThemeTokens` object.

## Brand Tokens

- `displayName`: primary lockup label shown in the shell.
- `logoAssetKey`: use a bundled preload asset when the logo is part of the game package.
- `logoUrl`: use an external or inline logo source when a preload asset key is not available.
- `primaryColor`: main shell accent color in `#RRGGBB` or `#RRGGBBAA`.
- `accentColor`: secondary highlight/glint color in `#RRGGBB` or `#RRGGBBAA`.

## Preloader Tokens

- `style`: `wow` or `minimal`
- `heroFx`: `energyRing`, `coinVortex`, or `slotSweep`
- `vfxIntensity`: `0..1`
- `audioStingerCue`: optional cue name resolved through the shared audio cue registry

## Canonical Resolver

- `resolveShellThemeTokens({ overrides })`
- Module: `packages/ui-kit/src/shell/theme/ShellThemeTokens.ts`

Brand/preloader overrides are merged into the existing shell token defaults. They do not replace the rest of the shell theme surface.

## Premium-Slot Proof Presets

- `brand=A`
  - `Aurora Vault`
  - preload logo via `logoAssetKey`
  - WOW preloader using `energyRing`
- `brand=B`
  - `Neon Harbor`
  - inline SVG logo via `logoUrl`
  - reduced-motion-friendly `minimal` preloader using `slotSweep`

## Sample Seed: BetOnline

This is a docs-only seed entry for future approved branding work. It does not add
runtime assets or licensed imagery to the repo.

```json
{
  "brand": {
    "displayName": "BetOnline",
    "logoAssetKey": "TODO_APPROVED_BETONLINE_LOGO",
    "logoUrl": "",
    "primaryColor": "TODO_APPROVED_PRIMARY_COLOR",
    "accentColor": "TODO_APPROVED_ACCENT_COLOR"
  },
  "preloader": {
    "style": "wow",
    "heroFx": "energyRing",
    "vfxIntensity": 0.7
  }
}
```

Approval notes:

- Do not commit a BetOnline logo file unless the user provides an approved SVG/PNG.
- If colors are sampled from public CSS, commit only the resulting hex values.
- When approved assets exist, wire the entry through the per-game brand resolver.

## Drift Prevention

1. Add new shell token families in `ShellThemeTokens.ts` first.
2. Document token meaning here before per-game adoption.
3. Keep token values visual/configural only; no GS runtime state belongs here.
