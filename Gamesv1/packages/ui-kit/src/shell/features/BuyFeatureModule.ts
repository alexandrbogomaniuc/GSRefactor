import type { FeatureModule, FeatureModuleContext, FeatureModuleInput } from "./types.ts";
import { readLabelBoolean } from "./types.ts";

export class BuyFeatureModule implements FeatureModule {
  public readonly id = "buy-feature";

  public isEnabled(context: FeatureModuleContext): boolean {
    const features = context.runtimeConfig.capabilities.features;
    return features.buyFeature || features.buyFeatureForCashBonus;
  }

  public resolve(input: FeatureModuleInput) {
    const features = input.runtimeConfig.capabilities.features;
    const cashBonus =
      input.counters.cashBonusMode === true ||
      readLabelBoolean(input.round.labels, "cashBonusMode") === true;

    const buyAllowedForCashBonus = features.buyFeatureForCashBonus;
    const buyDisabledForCashBonus = features.buyFeatureDisabledForCashBonus;

    const buyAllowed = !cashBonus
      ? features.buyFeature
      : buyAllowedForCashBonus && !buyDisabledForCashBonus;

    const buyAvailableCounter = input.counters.buyFeatureAvailable;
    const buyAvailableLabel = readLabelBoolean(input.round.labels, "buyFeatureAvailable");
    const buyAvailableSignal = buyAvailableCounter ?? buyAvailableLabel ?? true;
    const buyAvailable = buyAvailableSignal && buyAllowed;

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
