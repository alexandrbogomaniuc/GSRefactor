import type { PlayRoundResponse } from "@gamesv1/core-protocol";
import { GameConfig } from "@gamesv1/ui-kit";

export interface SlotPresentationOutcome {
  roundId: string;
  winAmount: number;
  reelStopColumns: number[][];
  slotIndex: number;
}

type RuntimePresentationPayload = {
  reelStopColumns?: unknown;
  math?: {
    details?: {
      ballInfo?: Array<{ slot?: unknown }>;
      reelStopColumns?: unknown;
    };
  };
};

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const asRuntimePayload = (value: unknown): RuntimePresentationPayload | null => {
  if (!isRecord(value)) return null;
  return value as RuntimePresentationPayload;
};

const isValidReelStopColumns = (value: unknown): value is number[][] => {
  if (!Array.isArray(value)) return false;
  if (value.length !== GameConfig.numReels) return false;

  return value.every(
    (column) =>
      Array.isArray(column) &&
      column.length >= GameConfig.numRows &&
      column.every((symbolId) => Number.isInteger(symbolId) && Number(symbolId) >= 0),
  );
};

const normalizeColumns = (columns: number[][]): number[][] =>
  columns.map((column) =>
    column.slice(0, GameConfig.numRows).map((id) => id % GameConfig.symbolCount),
  );

const extractSlotIndex = (result: PlayRoundResponse): number => {
  const payload = asRuntimePayload(result.presentationPayload);
  const slotRaw = payload?.math?.details?.ballInfo?.[0]?.slot;
  const parsed = Number(slotRaw);
  if (!Number.isFinite(parsed)) {
    throw new Error(
      "Missing required presentationPayload.math.details.ballInfo[0].slot from runtime round response.",
    );
  }

  return Math.max(0, Math.floor(parsed));
};

const buildReelColumnsFromSlot = (slotIndex: number, winAmount: number): number[][] => {
  const centerSymbol = slotIndex % GameConfig.symbolCount;
  const winningColumn = [centerSymbol, centerSymbol, centerSymbol];
  const nonWinningColumn = (reel: number): number[] => [
    (slotIndex + reel + 1) % GameConfig.symbolCount,
    (slotIndex + reel + 2) % GameConfig.symbolCount,
    (slotIndex + reel + 3) % GameConfig.symbolCount,
  ];

  const reels: number[][] = [];
  for (let reel = 0; reel < GameConfig.numReels; reel += 1) {
    const isCenterWinReel = winAmount > 0 && reel >= 1 && reel <= 3;
    reels.push(isCenterWinReel ? winningColumn.slice() : nonWinningColumn(reel));
  }
  return reels;
};

export const mapPlayRoundToSlotOutcome = (
  result: PlayRoundResponse,
): SlotPresentationOutcome => {
  const payload = asRuntimePayload(result.presentationPayload);
  if (!payload) {
    throw new Error("Missing runtime presentationPayload in playRound response.");
  }

  const payloadColumns = payload.reelStopColumns ?? payload.math?.details?.reelStopColumns;
  const reelStopColumns = isValidReelStopColumns(payloadColumns)
    ? normalizeColumns(payloadColumns)
    : undefined;

  const slotIndex = extractSlotIndex(result);
  const winAmount = Math.max(0, Math.round(result.winAmount));

  return {
    roundId: result.roundId,
    winAmount,
    reelStopColumns: reelStopColumns ?? buildReelColumnsFromSlot(slotIndex, winAmount),
    slotIndex,
  };
};
