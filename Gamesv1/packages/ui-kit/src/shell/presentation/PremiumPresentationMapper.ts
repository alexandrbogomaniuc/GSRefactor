import { z } from "zod";

import type { PlayRoundResponse } from "@gamesv1/core-protocol";

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

export interface PresentationLayoutConstraints {
  reelCount: number;
  rowCount: number;
  symbolModulo?: number;
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

const ReelStopsSchema = z.array(z.array(z.number().int().nonnegative()).min(1)).min(1);

const SymbolGridSchema = z.array(z.array(z.number().int().nonnegative()).min(1)).min(1);

const LayoutConstraintsSchema = z.object({
  reelCount: z.number().int().positive(),
  rowCount: z.number().int().positive(),
  symbolModulo: z.number().int().positive().optional(),
});

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

const normalizeSymbolId = (id: number, symbolModulo: number | undefined): number => {
  if (!symbolModulo) return id;
  const modulo = id % symbolModulo;
  return modulo < 0 ? modulo + symbolModulo : modulo;
};

const normalizeColumns = (
  columns: number[][],
  layout: PresentationLayoutConstraints,
): number[][] => {
  if (columns.length !== layout.reelCount) {
    throw new Error(
      `[PremiumPresentationMapper] Invalid reelStops width: expected ${layout.reelCount}, got ${columns.length}`,
    );
  }

  return columns.map((column, columnIndex) => {
    if (column.length < layout.rowCount) {
      throw new Error(
        `[PremiumPresentationMapper] Invalid reelStops height for reel ${columnIndex}: expected at least ${layout.rowCount}, got ${column.length}`,
      );
    }

    return column
      .slice(0, layout.rowCount)
      .map((id) => normalizeSymbolId(id, layout.symbolModulo));
  });
};

const normalizeGrid = (
  grid: number[][],
  layout: PresentationLayoutConstraints,
): number[][] => {
  if (grid.length !== layout.rowCount) {
    throw new Error(
      `[PremiumPresentationMapper] Invalid symbolGrid height: expected ${layout.rowCount}, got ${grid.length}`,
    );
  }

  return grid.map((row, rowIndex) => {
    if (row.length !== layout.reelCount) {
      throw new Error(
        `[PremiumPresentationMapper] Invalid symbolGrid width for row ${rowIndex}: expected ${layout.reelCount}, got ${row.length}`,
      );
    }

    return row
      .slice(0, layout.reelCount)
      .map((id) => normalizeSymbolId(id, layout.symbolModulo));
  });
};

const buildGridFromStops = (
  stopColumns: number[][],
  layout: PresentationLayoutConstraints,
): number[][] =>
  Array.from({ length: layout.rowCount }, (_, rowIndex) =>
    stopColumns.map((column) => column[rowIndex] ?? 0),
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
      `[PremiumPresentationMapper] Invalid presentationPayload: ${parsed.error.issues
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
  layoutConstraintsInput: PresentationLayoutConstraints,
): RoundPresentationModel => {
  const payload = parsePresentationPayload(result);
  const round = toRoundRecord(result);
  const layoutConstraints = LayoutConstraintsSchema.parse(layoutConstraintsInput);

  const normalizedStops = normalizeColumns(payload.reelStops, layoutConstraints);
  const normalizedGrid = normalizeGrid(
    payload.symbolGrid ?? buildGridFromStops(normalizedStops, layoutConstraints),
    layoutConstraints,
  );

  return {
    roundId: (typeof round.roundId === "string" && round.roundId) || result.requestId,
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
  layoutConstraintsInput: PresentationLayoutConstraints,
): SlotPresentationOutcome => {
  const mapped = mapPlayRoundToPresentation(result, layoutConstraintsInput);

  return {
    roundId: mapped.roundId,
    winAmount: mapped.winAmount,
    reelStopColumns: mapped.reels.stopColumns,
    slotIndex: mapped.slotIndex,
  };
};
