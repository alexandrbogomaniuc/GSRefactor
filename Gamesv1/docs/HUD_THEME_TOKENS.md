# HUD_THEME_TOKENS

## Canonical Type

- `PremiumHudThemeTokens` in `packages/ui-kit/src/hud/PremiumTemplateHud.ts`
- resolved from shell tokens in `packages/ui-kit/src/shell/theme/ShellThemeTokens.ts`

## Supported HUD Token Hooks

- `visualStyle`: named visual profile (for example `premium-default`, `minimal`)
- `panelAlpha`: controls HUD panel opacity
- `metricAccentColor`: tint for balance/bet/win metric text
- `controlSkinHooks`: per-control skin hints
  - keys: `spin`, `turbo`, `autoplay`, `buyFeature`, `sound`, `settings`, `history`
  - values currently support lightweight hooks such as `muted`, `emphasis`

## Dynamic Visibility

Dynamic visibility is still resolved independently from style tokens:

1. `resolvePremiumHudVisibility(...)` builds capability-based base visibility.
2. `FeatureModuleManager` emits round-level control visibility deltas.
3. `mergePremiumHudVisibility(...)` applies generic patches.
4. `PremiumTemplateHud.applyVisibility(...)` reflows controls with no gaps.

## Integration Point

- `games/premium-slot/src/app/screens/main/MainScreen.ts`
