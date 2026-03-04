import type { SlotSymbol } from "../../slots/SlotSymbol.ts";

export interface VisibleReelLike {
  getVisibleSymbols(): SlotSymbol[];
}

export interface WinTargetLayoutConstraints {
  reelCount: number;
  rowCount: number;
  highlightReelIndexes?: number[];
  highlightRowIndex?: number;
}

const clamp = (value: number, min: number, max: number): number =>
  Math.min(max, Math.max(min, value));

const toInt = (value: number, fallback: number): number => {
  if (!Number.isFinite(value)) {
    return fallback;
  }

  return Math.round(value);
};

const defaultReelIndexes = (reelCount: number): number[] =>
  Array.from({ length: reelCount }, (_, index) => index);

export const resolveWinSymbolsFromReels = (
  reels: VisibleReelLike[],
  constraints: WinTargetLayoutConstraints,
): SlotSymbol[] => {
  const reelCount = Math.max(1, toInt(constraints.reelCount, reels.length || 1));
  const rowCount = Math.max(1, toInt(constraints.rowCount, 1));
  const sourceReelIndexes =
    constraints.highlightReelIndexes && constraints.highlightReelIndexes.length > 0
      ? constraints.highlightReelIndexes
      : defaultReelIndexes(reelCount);
  const highlightRowIndex = clamp(
    toInt(constraints.highlightRowIndex ?? Math.floor((rowCount - 1) / 2), 0),
    0,
    rowCount - 1,
  );

  const winSymbols: SlotSymbol[] = [];
  for (const reelIndex of sourceReelIndexes) {
    const resolvedReel = reels[reelIndex];
    if (!resolvedReel) {
      continue;
    }

    const visibleSymbols = resolvedReel.getVisibleSymbols();
    const symbol = visibleSymbols[highlightRowIndex];
    if (symbol) {
      winSymbols.push(symbol);
    }
  }

  return winSymbols;
};
