export type PolicyWinTier = "none" | "big" | "huge" | "mega";

export type PresentationWinTier = "none" | "normal" | "big" | "huge" | "mega";

export const toPresentationWinTier = (
  policyTier: PolicyWinTier,
  winAmountMinor: number,
): PresentationWinTier => {
  if (winAmountMinor <= 0) return "none";
  if (policyTier === "big") return "big";
  if (policyTier === "huge") return "huge";
  if (policyTier === "mega") return "mega";
  return "normal";
};

export const getWinPresentationTitle = (tier: PresentationWinTier): string => {
  if (tier === "none") return "";
  if (tier === "mega") return "MEGA WIN";
  if (tier === "huge") return "HUGE WIN";
  if (tier === "big") return "BIG WIN";
  return "WIN";
};
