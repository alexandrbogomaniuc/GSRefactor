# THEME_SHELL_FOUNDATION

## Goal

Provide a reusable shell-token foundation so future games can theme HUD/VFX/audio behavior without changing core runtime logic.

## Canonical Token Source

- `packages/ui-kit/src/shell/theme/ShellThemeTokens.ts`

## Token Families

- `metadata`: `themeId`, `skinId`
- `hud`: visual style, panel alpha, metric accent color, control skin hooks
- `winPresentation`: tier label/style hooks
- `audio`: base cue overrides + themed cue override maps
- `vfx`: intensity + heavy-fx/coin-burst toggles
- `roundActions`: shared round/bet/action defaults
- `winTargets`: generic win-highlight target constraints

### Round-Action Defaults Ownership

- Template bet defaults are owned by `ShellThemeTokens.DEFAULT_THEME_TOKENS.roundActions.bet`.
- `BetSelectionBuilder` keeps neutral fallback values (`lineCount=1`, `multiplier=1`) to avoid premium-slot leakage when tokens are not provided.
- Premium-slot keeps template defaults (`lineCount=20`, `multiplier=1`) through shell tokens, not screen-local hardcoding.

### Buy Feature Price Fallback

- Default buy pricing in shell tokens is neutral: `roundActions.buyFeature.priceMinor = 0`.
- Manual runtime smoke against the GS fixture runtime confirmed `featureaction` succeeds with `selectedFeatureChoice.priceMinor=0`.
- If an operator/runtime requires a non-zero buy price, override via shell tokens/config (`RoundActionBuilderConfig`) instead of `MainScreen` hardcoding.

## Runtime Consumption

`games/premium-slot/src/app/screens/main/MainScreen.ts` resolves shell tokens once and passes them into:

- `PremiumTemplateHud.applyTheme(...)`
- `createAudioCueRegistry(...)`
- `RoundActionBuilder(...)`
- `WowVfxOrchestrator(..., themeTokens)`
- `resolveWinSymbolsFromReels(...)`

## Boundaries

- Tokens customize presentation behavior only.
- GS remains authoritative for wallet/session/round truth.
- No GS wire-contract changes are introduced by token usage.
