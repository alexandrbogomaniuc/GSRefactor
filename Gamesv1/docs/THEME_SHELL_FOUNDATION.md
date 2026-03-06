# THEME_SHELL_FOUNDATION

## Goal

Provide one reusable shell-token surface for HUD, VFX, audio, round-action defaults, win-target rules, and branded preloaders without changing core GS-authoritative runtime behavior.

## Canonical Module

- `packages/ui-kit/src/shell/theme/ShellThemeTokens.ts`

## Canonical Resolver

- `resolveShellThemeTokens(options)`
- Resolver model:
  - starts from shared defaults
  - layers runtime-derived presentation hints
  - layers query-driven dev overrides
  - layers explicit per-game overrides
- The existing deep-merge API remains canonical. Brand/preloader support is added into the same token object instead of introducing a second token resolver.

## Token Families

- `metadata`: `themeId`, `skinId`
- `brand`: `displayName`, `logoAssetKey`, `logoUrl`, `primaryColor`, `accentColor`
- `hud`: visual style, panel alpha, metric accent color, control skin hooks
- `winPresentation`: tier label/style hooks
- `audio`: base cue overrides plus themed cue override maps
- `vfx`: intensity plus heavy-fx/coin-burst toggles
- `preloader`: `style`, `heroFx`, `vfxIntensity`, optional `audioStingerCue`
- `roundActions`: shared round/bet/action defaults
- `winTargets`: generic win-highlight target constraints

## Validation Rules

- Brand and preloader tokens are validated during resolution.
- Invalid brand names, malformed colors, or out-of-range preloader intensity fail fast.
- Validation stays presentation-only. It does not change GS transport or runtime ownership.

## Round-Action Defaults Ownership

- Template bet defaults are owned by `DEFAULT_THEME_TOKENS.roundActions.bet`.
- `BetSelectionBuilder` keeps neutral fallback values (`lineCount=1`, `multiplier=1`) so shell consumers do not inherit premium-slot assumptions accidentally.
- Premium-slot keeps template defaults (`lineCount=20`, `multiplier=1`) through shell tokens, not screen-local hardcoding.

## Buy Feature Price Fallback

- Default buy pricing in shell tokens is neutral: `roundActions.buyFeature.priceMinor = 0`.
- The shell keeps pricing configurable through shared token/config input rather than `MainScreen` hardcoding.
- If a GS runtime or operator profile needs a non-zero default, override it through shell token config, not screen logic.

## Runtime Consumption

`games/premium-slot` resolves shell tokens once and routes them into:

- HUD theme application
- audio cue registry construction
- round-action builders
- WOW/VFX orchestration
- win-target resolution
- branded preloader rendering

## Boundaries

- Shell tokens customize presentation behavior only.
- GS remains authoritative for wallet, session, request sequencing, and round truth.
- No GS wire-contract changes are introduced by token usage.
