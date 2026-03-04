import type { ResolvedConfig } from "@gamesv1/core-compliance";

import type { PremiumHudControlId, PremiumHudVisibility } from "../../hud/PremiumTemplateHud";

export interface PremiumHudFeatureFlags {
  controls?: Partial<Record<PremiumHudControlId, boolean>>;
  metrics?: Partial<PremiumHudVisibility["metrics"]>;
}

export type PremiumHudDynamicControlVisibility = Partial<
  Record<PremiumHudControlId, boolean>
>;

const applyOverrides = (
  base: Record<PremiumHudControlId, boolean>,
  overrides: Partial<Record<PremiumHudControlId, boolean>> | undefined,
): Record<PremiumHudControlId, boolean> => ({
  ...base,
  ...(overrides ?? {}),
});

export const resolvePremiumHudVisibility = (
  runtimeConfig: ResolvedConfig,
  featureFlags: PremiumHudFeatureFlags = {},
): PremiumHudVisibility => {
  const baseControls: Record<PremiumHudControlId, boolean> = {
    spin: true,
    turbo: runtimeConfig.capabilities.turbo.allowed,
    autoplay: runtimeConfig.capabilities.features.autoplay,
    buyFeature:
      runtimeConfig.capabilities.features.buyFeature ||
      runtimeConfig.capabilities.features.buyFeatureForCashBonus,
    sound: runtimeConfig.capabilities.sound.showToggle,
    settings: runtimeConfig.capabilities.sessionUi.closeButtonPolicy !== "hidden",
    history:
      runtimeConfig.capabilities.features.inGameHistory &&
      runtimeConfig.capabilities.history.enabled,
  };

  return {
    controls: applyOverrides(baseControls, featureFlags.controls),
    metrics: {
      balance: runtimeConfig.walletDisplay.showBalance,
      bet: true,
      win: true,
      ...(featureFlags.metrics ?? {}),
    },
  };
};

export const mergePremiumHudVisibility = (
  baseVisibility: PremiumHudVisibility,
  dynamicControlVisibility: PremiumHudDynamicControlVisibility = {},
): PremiumHudVisibility => {
  const safeDynamicControlVisibility = Object.fromEntries(
    Object.entries(dynamicControlVisibility).filter(
      (entry): entry is [PremiumHudControlId, boolean] =>
        typeof entry[1] === "boolean",
    ),
  );

  return {
    controls: {
      ...baseVisibility.controls,
      ...safeDynamicControlVisibility,
    },
    metrics: {
      ...baseVisibility.metrics,
    },
  };
};
