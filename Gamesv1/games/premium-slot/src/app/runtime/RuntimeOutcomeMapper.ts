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
  slotIndex?: unknown;
  winAmount?: unknown;
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

const resolveSlotIndex = (payload: RuntimePresentationPayload): number => {
  const parsed = Number(payload.slotIndex);
  if (!Number.isFinite(parsed)) {
    return 0;
  }

  return Math.max(0, Math.floor(parsed));
};

export const mapPlayRoundToSlotOutcome = (
  result: PlayRoundResponse,
): SlotPresentationOutcome => {
  const payload = asRuntimePayload(result.presentationPayload);
  if (!payload) {
    throw new Error("Missing runtime presentationPayload in playRound response.");
  }

  if (!isValidReelStopColumns(payload.reelStopColumns)) {
    throw new Error(
      "Missing or invalid presentationPayload.reelStopColumns in playRound response.",
    );
  }

  const payloadWinAmount = Number(payload.winAmount);
  const winAmount = Number.isFinite(payloadWinAmount)
    ? Math.max(0, Math.round(payloadWinAmount))
    : Math.max(0, Math.round(result.winAmount));

  return {
    roundId: result.roundId,
    winAmount,
    reelStopColumns: normalizeColumns(payload.reelStopColumns),
    slotIndex: resolveSlotIndex(payload),
  };
};
