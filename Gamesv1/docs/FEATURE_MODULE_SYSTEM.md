# FEATURE_MODULE_SYSTEM

## Goal

Feature behavior is productized as reusable modules under `ui-kit`, enabled by resolved runtime config plus server-provided presentation state.

## Canonical Module Stack

- Manager: `packages/ui-kit/src/shell/features/FeatureModuleManager.ts`
- Modules:
  - `FreeSpinsFeatureModule`
  - `RespinFeatureModule`
  - `HoldAndWinFeatureModule`
  - `BuyFeatureModule`
  - `JackpotHooksFeatureModule`

## Enablement Model

A module is active only when both are true:

1. capability/config allows it (`ResolvedConfig.capabilities.*`)
2. server round state indicates active condition (`counters` / `labels`)

## Module Outputs

Modules return a composable output frame:

- overlays
- messages
- sound cues
- animation cues
- dynamic control visibility (generic HUD control patches, not limited to buy feature)

`FeatureModuleManager.resolve(...)` merges all active module outputs into one frame consumed by `MainScreen`.
`MainScreen.applyDynamicControlVisibility(...)` applies this frame through
`mergePremiumHudVisibility(...)`, preserving no-gap reflow.

## Enabled vs Active Semantics

`FeatureFrame` explicitly carries two module-id lists:

- `enabledModuleIds`: modules enabled by runtime capability/config
- `activeModuleIds`: modules that produced current-round signals (overlay/message/cue/control-visibility)

This avoids ambiguity between capability-level enablement and round-level activation.

## Runtime Integration

- runtime config source: `ResolvedRuntimeConfigStore`
- round source: `mapPlayRoundToPresentation(...)`
- consumer: `games/premium-slot/src/app/screens/main/MainScreen.ts`

## Validation

- `tests/game/feature-module-manager.test.ts`
- `tests/game/premium-shell-smoke.test.ts`
