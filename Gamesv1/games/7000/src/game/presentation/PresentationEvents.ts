export type PresentationTriggerKind =
  | "line_win"
  | "collect"
  | "boost"
  | "bonus"
  | "jackpot"
  | "buy_bonus"
  | "spin_start"
  | "spin_end";

export type PresentationWinTier = "none" | "small" | "big" | "mega" | "jackpot";

export type PresentationTrigger = {
  kind: PresentationTriggerKind;
  lineId?: number;
  payout?: number;
  multiplier?: number;
  jackpotTier?: "mini" | "minor" | "major" | "grand";
  metadata?: Record<string, unknown>;
};

export const deriveWinTier = (
  totalWinMultiplier: number,
  jackpotTier?: PresentationTrigger["jackpotTier"],
): PresentationWinTier => {
  if (jackpotTier) {
    return "jackpot";
  }

  if (totalWinMultiplier >= 50) {
    return "mega";
  }
  if (totalWinMultiplier >= 15) {
    return "big";
  }
  if (totalWinMultiplier > 0) {
    return "small";
  }
  return "none";
};
