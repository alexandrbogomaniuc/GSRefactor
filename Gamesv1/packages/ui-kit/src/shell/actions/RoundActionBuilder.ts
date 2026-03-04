import type { SelectedBet, SelectedFeatureChoice } from "@gamesv1/core-protocol";

import {
  buildSelectedBet,
  type BetSelectionBuilderOptions,
} from "./BetSelectionBuilder.ts";

export interface RoundActionBuilderConfig {
  bet?: BetSelectionBuilderOptions;
  buyFeature?: {
    featureType?: SelectedFeatureChoice["featureType"];
    action?: SelectedFeatureChoice["action"];
    priceMinor?: number;
    priceMultiplier?: number;
    payloadDefaults?: Record<string, unknown>;
  };
}

export interface BuildBuyFeatureActionInput {
  totalBetMinor: number;
  selectedBet?: SelectedBet;
  payload?: Record<string, unknown>;
  explicitPriceMinor?: number;
}

export interface BuiltFeatureAction {
  selectedBet: SelectedBet;
  selectedFeatureChoice: SelectedFeatureChoice;
}

const toFiniteNumber = (value: unknown): number | undefined =>
  typeof value === "number" && Number.isFinite(value) ? value : undefined;

const toNonNegativeInt = (value: number | undefined): number | undefined => {
  if (typeof value !== "number" || !Number.isFinite(value)) {
    return undefined;
  }

  const rounded = Math.round(value);
  return rounded >= 0 ? rounded : undefined;
};

export class RoundActionBuilder {
  private readonly config: RoundActionBuilderConfig;

  constructor(config: RoundActionBuilderConfig = {}) {
    this.config = config;
  }

  public buildSpinBet(totalBetMinor: number): SelectedBet {
    return buildSelectedBet(totalBetMinor, this.config.bet);
  }

  public buildBuyFeatureAction(input: BuildBuyFeatureActionInput): BuiltFeatureAction {
    const selectedBet = input.selectedBet ?? this.buildSpinBet(input.totalBetMinor);
    const configuredPriceMinor = toNonNegativeInt(this.config.buyFeature?.priceMinor);
    const priceMultiplier = toFiniteNumber(this.config.buyFeature?.priceMultiplier);
    const multiplierPriceMinor =
      priceMultiplier !== undefined
        ? toNonNegativeInt(selectedBet.totalBetMinor * priceMultiplier)
        : undefined;
    const explicitPriceMinor = toNonNegativeInt(input.explicitPriceMinor);

    // If no explicit/configured pricing is provided, keep priceMinor neutral (0) and let GS resolve.
    const priceMinor = explicitPriceMinor ?? configuredPriceMinor ?? multiplierPriceMinor ?? 0;

    return {
      selectedBet,
      selectedFeatureChoice: {
        featureType: this.config.buyFeature?.featureType ?? "BUY_FEATURE",
        action: this.config.buyFeature?.action ?? "CONFIRM",
        priceMinor,
        payload: {
          ...(this.config.buyFeature?.payloadDefaults ?? {}),
          ...(input.payload ?? {}),
        },
      },
    };
  }
}
