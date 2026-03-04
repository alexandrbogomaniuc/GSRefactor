import type { SelectedBet } from "@gamesv1/core-protocol";

export interface BetSelectionBuilderOptions {
  lineCount?: number;
  multiplier?: number;
  coinValueMinor?: number;
  minCoinValueMinor?: number;
}

const DEFAULT_LINE_COUNT = 20;
const DEFAULT_MULTIPLIER = 1;
const DEFAULT_MIN_COIN_VALUE_MINOR = 1;

const toPositiveInt = (value: number | undefined, fallback: number): number => {
  if (typeof value !== "number" || !Number.isFinite(value)) {
    return fallback;
  }

  const rounded = Math.round(value);
  return rounded > 0 ? rounded : fallback;
};

export const buildSelectedBet = (
  totalBetMinorInput: number,
  options: BetSelectionBuilderOptions = {},
): SelectedBet => {
  const totalBetMinor = toPositiveInt(totalBetMinorInput, 1);
  const lines = toPositiveInt(options.lineCount, DEFAULT_LINE_COUNT);
  const multiplier = toPositiveInt(options.multiplier, DEFAULT_MULTIPLIER);
  const minCoinValueMinor = toPositiveInt(
    options.minCoinValueMinor,
    DEFAULT_MIN_COIN_VALUE_MINOR,
  );

  const derivedCoinValueMinor = Math.max(
    minCoinValueMinor,
    Math.floor(totalBetMinor / (lines * multiplier)),
  );

  return {
    coinValueMinor: toPositiveInt(options.coinValueMinor, derivedCoinValueMinor),
    lines,
    multiplier,
    totalBetMinor,
  };
};
