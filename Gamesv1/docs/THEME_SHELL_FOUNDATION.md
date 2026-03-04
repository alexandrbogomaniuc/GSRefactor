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
