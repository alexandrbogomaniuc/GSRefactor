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

Controls:
- spin
- turbo
- autoplay
- buy feature
- sound
- settings
- history

HUD visibility and layout are driven by resolved runtime config and feature module outputs.

## Feature module pipeline

- Module manager:
  - `src/game/features/FeatureModuleManager.ts`
- Modules:
  - `FreeSpinsFeatureModule`
  - `RespinFeatureModule`
  - `HoldAndWinFeatureModule`
  - `BuyFeatureModule`
  - `JackpotHooksFeatureModule`

Enablement source:
- resolved runtime config capabilities
- server feature state from mapped `presentationPayload`

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
