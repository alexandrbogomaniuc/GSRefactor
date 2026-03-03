# HUD_CONFIG_SCHEMA

## Canonical HUD Visibility Shape

```ts
interface PremiumHudVisibility {
  controls: {
    spin: boolean;
    turbo: boolean;
    autoplay: boolean;
    buyFeature: boolean;
    sound: boolean;
    settings: boolean;
    history: boolean;
  };
  metrics: {
    balance: boolean;
    bet: boolean;
    win: boolean;
  };
}

interface PremiumHudFeatureFlags {
  controls?: Partial<PremiumHudVisibility["controls"]>;
  metrics?: Partial<PremiumHudVisibility["metrics"]>;
}
```

## Runtime Capability Mapping

`resolvePremiumHudVisibility(runtimeConfig, featureFlags?)` maps:

- `turbo` <- `runtimeConfig.capabilities.turbo.allowed`
- `autoplay` <- `runtimeConfig.capabilities.features.autoplay`
- `buyFeature` <- `buyFeature || buyFeatureForCashBonus`
- `sound` <- `runtimeConfig.capabilities.sound.showToggle`
- `settings` <- `closeButtonPolicy !== "hidden"`
- `history` <- `inGameHistory && history.enabled`
- `balance metric` <- `runtimeConfig.walletDisplay.showBalance`

`spin`, `bet`, and `win` remain visible by default for playability.

## No-Gap Rule

Hidden controls must not reserve layout slots. `computeHudLayout(...)` only places visible controls and is validated by:

- `tests/layout/layout-matrix.test.ts`
- `tests/game/premium-shell-smoke.test.ts`

## Safe-Area Inputs

HUD layout consumes viewport safe-area values from pixi-engine layout manager (`env(safe-area-inset-*)` + optional overrides).
