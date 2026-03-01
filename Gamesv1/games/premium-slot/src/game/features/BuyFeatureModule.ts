import type { FeatureModule, FeatureModuleContext, FeatureModuleInput } from "./types.ts";

export class BuyFeatureModule implements FeatureModule {
  public readonly id = "buy-feature";

  public isEnabled(context: FeatureModuleContext): boolean {
    return context.runtimeConfig.capabilities.features.buyFeature;
  }

  public resolve(input: FeatureModuleInput) {
    const features = input.runtimeConfig.capabilities.features;
    const cashBonus = input.serverState.cashBonusMode === true;
    const buyAllowedForCashBonus = features.buyFeatureForCashBonus;
    const buyDisabledForCashBonus = features.buyFeatureDisabledForCashBonus;

    const buyAllowed = !cashBonus
      ? features.buyFeature
      : buyAllowedForCashBonus && !buyDisabledForCashBonus;

    const buyAvailable = input.serverState.buyFeatureAvailable !== false && buyAllowed;

    const output = {
      controlVisibility: {
        buyFeature: buyAvailable,
      },
      overlays: [] as Array<{
        id: string;
        type: string;
        label: string;
        visible: boolean;
      }>,
      messages: [] as string[],
    };

    if (buyAvailable) {
      output.overlays.push({
        id: "buy-feature-overlay",
        type: "buy-feature",
        label: "BUY FEATURE AVAILABLE",
        visible: true,
      });
    }

    if (cashBonus && buyDisabledForCashBonus) {
      output.messages.push("BUY FEATURE DISABLED FOR CASH BONUS");
    }

    return output;
  }
}

