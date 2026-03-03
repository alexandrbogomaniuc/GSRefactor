# SAFE_AREA_POLICY

## Source Of Safe-Area Truth

Safe-area insets are resolved in `packages/pixi-engine/src/layout/LayoutContext.ts` from CSS env vars:

- `env(safe-area-inset-top)`
- `env(safe-area-inset-right)`
- `env(safe-area-inset-bottom)`
- `env(safe-area-inset-left)`

`ResponsiveLayoutManager` can override these values explicitly when needed.

## Application Rules

1. Safe-area is part of the canonical viewport object.
2. HUD and layout systems must consume safe-area and never hardcode notch offsets.
3. Controls and metrics must stay within `width - left/right inset` and above `bottom inset`.
4. Hidden controls must collapse without reserving safe-area-adjusted empty slots.

## Runtime Consumers

- `MainScreen.resize(...)` uses layout viewport safe-area for reel/HUD placement.
- `PremiumTemplateHud.resize(...)` and `computeHudLayout(...)` pass safe-area into `@pixi/layout` flow logic.

## Validation

Safe-area behavior across device classes is covered by:

- `tests/layout/layout-matrix.test.ts`
- `tests/game/premium-shell-smoke.test.ts`
