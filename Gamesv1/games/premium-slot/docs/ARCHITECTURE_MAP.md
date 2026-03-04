# Premium Slot Architecture Map

This is the gold-standard reference architecture for future Gamesv1 slot games.

## Runtime flow

1. `GsRuntimeClient.bootstrap()` hydrates session/wallet/config from GS.
2. `ConfigManager` resolves runtime config with GS payload as highest practical runtime source.
3. `MainScreen` issues `playRound` and maps GS `presentationPayload` through one canonical mapper.
4. Visual rendering uses mapped reel data/counters/overlays/cues only.

## Core components

- Transport client:
  - `src/app/runtime/GsRuntimeClient.ts`
- Presentation mapping:
  - `src/app/runtime/RuntimeOutcomeMapper.ts`
- Runtime stores:
  - `src/app/stores/SessionRuntimeStore.ts`
  - `src/app/stores/ResolvedRuntimeConfigStore.ts`
  - `src/app/stores/PresentationStateStore.ts`
- Main composition:
  - `src/app/screens/main/MainScreen.ts`

## Shared template HUD

- `@gamesv1/ui-kit` reusable HUD:
  - `packages/ui-kit/src/hud/PremiumTemplateHud.ts`
  - `packages/ui-kit/src/shell/hud/PremiumHudPolicy.ts`

Controls:
- spin
- turbo
- autoplay
- buy feature
- sound
- settings
- history

HUD visibility and layout are driven by resolved runtime config and feature module outputs.
`MainScreen` now merges feature-frame control visibility generically (`buyFeature`, `turbo`, `autoplay`, `history`) through shared shell policy helpers, preserving no-gap reflow.

## Feature module pipeline

- Module manager:
  - `packages/ui-kit/src/shell/features/FeatureModuleManager.ts`
- Modules:
  - `packages/ui-kit/src/shell/features/FreeSpinsFeatureModule.ts`
  - `packages/ui-kit/src/shell/features/RespinFeatureModule.ts`
  - `packages/ui-kit/src/shell/features/HoldAndWinFeatureModule.ts`
  - `packages/ui-kit/src/shell/features/BuyFeatureModule.ts`
  - `packages/ui-kit/src/shell/features/JackpotHooksFeatureModule.ts`

Enablement source:
- resolved runtime config capabilities
- server feature state from mapped `presentationPayload`

## Presentation mapper

- Canonical mapper:
  - `packages/ui-kit/src/shell/presentation/PremiumPresentationMapper.ts`
- premium-slot compatibility export:
  - `src/app/runtime/RuntimeOutcomeMapper.ts`

## WOW / VFX orchestration

- Canonical tiers:
  - `packages/ui-kit/src/shell/vfx/WinPresentationTiers.ts`
- Canonical orchestrator:
  - `packages/ui-kit/src/shell/vfx/WowVfxOrchestrator.ts`
- Shared audio cue registry / dispatcher:
  - `packages/ui-kit/src/shell/vfx/AudioCueRegistry.ts`
- Premium implementation adapters:
  - `src/game/fx/WinHighlight.ts`
  - `src/game/fx/ParticleBurst.ts`
  - `src/game/ui/WinCounter.ts`

VFX timing and low-performance behavior are driven by `AnimationPolicyEngine` and runtime config.
Audio cue execution is delegated to shared shell cue dispatching (no screen-local hardcoded cue branching).

## Asset and localization handoff paths

- Asset source folders:
  - `raw-assets/preload/`
  - `raw-assets/main/`
  - `raw-assets/promo/`
- Runtime manifest:
  - `src/manifest.json`
- Per-game asset manifest sample:
  - `docs/asset-manifest.sample.json`
- Localization folders:
  - `locales/<lang>/common.json`
  - `locales/<lang>/paytable.json`
  - `locales/<lang>/rules.json`
- Math package reference:
  - `math/math-package-manifest.json`

## Release-pack quality gate

`tools/release-pack/create-release.ts` generates deterministic release artifacts including:
- bundle/asset/localization manifests
- math manifest reference
- registration artifact
- checksums
- canary/smoke/rollback packs
