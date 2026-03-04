# SHARED_HUD_ARCHITECTURE

## Purpose

`packages/ui-kit` now contains one reusable premium HUD shell that future slot games can reuse without structural drift.

## Canonical Modules

- HUD component: `packages/ui-kit/src/hud/PremiumTemplateHud.ts`
- HUD config policy resolver: `packages/ui-kit/src/shell/hud/PremiumHudPolicy.ts`
- HUD layout engine: `packages/ui-kit/src/layout/HudLayout.ts`
- HUD theme tokens: `packages/ui-kit/src/shell/theme/ShellThemeTokens.ts`
- Safe-area viewport source: `packages/pixi-engine/src/layout/ResponsiveLayoutManager.ts`

## Control Surface

Supported controls:

- spin
- turbo
- autoplay
- buy feature
- sound
- settings
- history

All control visibility is derived from resolved runtime config via `resolvePremiumHudVisibility(...)`, with optional runtime feature-flag overrides.

## Visibility Resolution Flow

1. `ResolvedRuntimeConfigStore` provides resolved GS-authoritative capability values.
2. `resolvePremiumHudVisibility` maps capabilities into HUD control and metric visibility.
3. Optional flags (for smoke/demo/query overrides) patch visibility without changing core capability truth.
4. `PremiumTemplateHud.applyVisibility` applies final control/metric visibility.
5. `ResponsiveHudLayoutController` reflows visible controls and collapses hidden controls with no empty gaps.

## Runtime Integration

`games/premium-slot/src/app/screens/main/MainScreen.ts` composes:

- `resolvePremiumHudVisibility(...)` for base visibility
- `FeatureModuleManager` dynamic control updates
- `mergePremiumHudVisibility(...)` for generic dynamic control patches (`buyFeature`, `turbo`, `autoplay`, `history`)
- `PremiumTemplateHud` state updates (balance/bet/win/turbo/sound)
- `PremiumTemplateHud.applyTheme(...)` with shell theme tokens (visual style, panel alpha, control skin hooks)

This keeps GS-authoritative session/wallet/runtime truth unchanged; HUD is presentation-only.
