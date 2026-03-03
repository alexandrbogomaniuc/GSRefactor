import { z } from "zod";

import type { PlayRoundResponse } from "@gamesv1/core-protocol";
import { GameConfig } from "@gamesv1/ui-kit/config";

export interface SlotPresentationOutcome {
  roundId: string;
  winAmount: number;
  reelStopColumns: number[][];
  slotIndex: number;
}

export interface PresentationCounters {
  freeSpinsRemaining?: number;
  respinRemaining?: number;
  holdAndWinRemaining?: number;
  jackpotLevel?: number;
  buyFeatureAvailable?: boolean;
  cashBonusMode?: boolean;
}

export interface RoundPresentationModel {
  roundId: string;
  winAmount: number;
  slotIndex: number;
  reels: {
    stopColumns: number[][];
  };
  symbolGrid: number[][];
  counters: PresentationCounters;
  messages: string[];
  soundCues: string[];
  animationCues: string[];
  labels: Record<string, string>;
}

const CountersSchema = z
  .object({
    freeSpinsRemaining: z.number().int().nonnegative().optional(),
    respinRemaining: z.number().int().nonnegative().optional(),
    holdAndWinRemaining: z.number().int().nonnegative().optional(),
    jackpotLevel: z.number().int().nonnegative().optional(),
    buyFeatureAvailable: z.boolean().optional(),
    cashBonusMode: z.boolean().optional(),
  })
  .default({});

const LabelRecordSchema = z.record(z.string(), z.string()).default({});

const ReelStopsSchema = z
  .array(z.array(z.number().int().nonnegative()).min(GameConfig.numRows))
  .length(GameConfig.numReels);

const SymbolGridSchema = z
  .array(z.array(z.number().int().nonnegative()).length(GameConfig.numReels))
  .length(GameConfig.numRows);

const PresentationPayloadSchema = z.object({
  reelStops: ReelStopsSchema,
  symbolGrid: SymbolGridSchema.optional(),
  uiMessages: z.array(z.string()).default([]),
  animationCues: z.array(z.string()).default([]),
  audioCues: z.array(z.string()).default([]),
  counters: CountersSchema,
  labels: LabelRecordSchema,
});

type RuntimePresentationPayload = z.infer<typeof PresentationPayloadSchema>;

const normalizeColumns = (columns: number[][]): number[][] =>
  columns.map((column) =>
    column.slice(0, GameConfig.numRows).map((id) => id % GameConfig.symbolCount),
  );

const normalizeGrid = (grid: number[][]): number[][] =>
  grid.map((row) =>
    row.slice(0, GameConfig.numReels).map((id) => id % GameConfig.symbolCount),
  );

const toPayloadObject = (value: unknown): Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : {};

const parsePresentationPayload = (result: PlayRoundResponse): RuntimePresentationPayload => {
  const payloadObject = toPayloadObject(result.presentationPayload);
  const parsed = PresentationPayloadSchema.safeParse(payloadObject);
  if (!parsed.success) {
    throw new Error(
      `[RuntimeOutcomeMapper] Invalid presentationPayload: ${parsed.error.issues
        .map((issue) => `${issue.path.join(".")}: ${issue.message}`)
        .join("; ")}`,
    );
  }

  return parsed.data;
};

const toRoundRecord = (result: PlayRoundResponse): Record<string, unknown> =>
  typeof result.round === "object" && result.round !== null && !Array.isArray(result.round)
    ? (result.round as Record<string, unknown>)
    : {};

export const mapPlayRoundToPresentation = (
  result: PlayRoundResponse,
): RoundPresentationModel => {
  const payload = parsePresentationPayload(result);
  const round = toRoundRecord(result);
  const normalizedStops = normalizeColumns(payload.reelStops);
  const normalizedGrid = normalizeGrid(
    payload.symbolGrid ??
      Array.from({ length: GameConfig.numRows }, (_, rowIndex) =>
        normalizedStops.map((column) => column[rowIndex] ?? 0),
      ),
  );

  return {
    roundId:
      (typeof round.roundId === "string" && round.roundId) || result.requestId,
    winAmount:
      typeof round.winAmountMinor === "number" && Number.isFinite(round.winAmountMinor)
        ? Math.max(0, Math.round(round.winAmountMinor))
        : 0,
    slotIndex: 0,
    reels: {
      stopColumns: normalizedStops,
    },
    symbolGrid: normalizedGrid,
    counters: payload.counters,
    messages: payload.uiMessages,
    soundCues: payload.audioCues,
    animationCues: payload.animationCues,
    labels: payload.labels,
  };
};

export const mapPlayRoundToSlotOutcome = (
  result: PlayRoundResponse,
): SlotPresentationOutcome => {
  const mapped = mapPlayRoundToPresentation(result);

  return {
    roundId: mapped.roundId,
    winAmount: mapped.winAmount,
    reelStopColumns: mapped.reels.stopColumns,
    slotIndex: mapped.slotIndex,
  };
};
