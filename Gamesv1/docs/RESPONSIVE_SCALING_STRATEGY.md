# RESPONSIVE_SCALING_STRATEGY

## Policy

Responsive layout and scaling use a two-layer policy:

1. `pixi-engine` provides viewport/safe-area/orientation truth.
2. `ui-kit` layouts consume that viewport and place HUD controls without gaps.

## Core Components

- viewport + safe area: `packages/pixi-engine/src/layout/LayoutContext.ts`
- runtime manager + debug toggle: `packages/pixi-engine/src/layout/ResponsiveLayoutManager.ts`
- HUD flow layout: `packages/ui-kit/src/layout/HudLayout.ts`
- premium screen scaling: `games/premium-slot/src/app/screens/main/MainScreen.ts`

## Breakpoints / Modes

Orientation-driven behavior:

- portrait: larger HUD region (`regionHeight: 250`)
- landscape: compact HUD region (`regionHeight: 150`)

Reference validation matrix:

- phone portrait
- phone landscape
- tablet portrait
- tablet landscape
- desktop
- ultrawide

(See `tests/layout/layout-matrix.test.ts`.)

## DPR / Resolution

Renderer resolution handling stays in pixi-engine utility flow; presentation layout logic remains resolution-agnostic and operates on viewport coordinates.

## Layout Debug Mode

`ResponsiveLayoutManager` supports a debug overlay and runtime toggle key (`KeyL` by default).

This allows visual inspection of viewport orientation/safe-area boxes during integration.
