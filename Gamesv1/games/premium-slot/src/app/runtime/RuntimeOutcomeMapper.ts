import { z } from "zod";

import type { PlayRoundResponse } from "@gamesv1/core-protocol";
import { GameConfig } from "@gamesv1/ui-kit/config";

export interface SlotPresentationOutcome {
  roundId: string;
  winAmount: number;
  reelStopColumns: number[][];
  slotIndex: number;
}

export interface PresentationOverlay {
  id: string;
  type: string;
  label: string;
  value?: number;
  visible: boolean;
}

export interface PresentationCounters {
  freeSpinsRemaining?: number;
  respinRemaining?: number;
  holdAndWinRemaining?: number;
  jackpotLevel?: number;
}

export interface RoundPresentationModel {
  roundId: string;
  winAmount: number;
  slotIndex: number;
  reels: {
    stopColumns: number[][];
  };
  featureOverlays: PresentationOverlay[];
  counters: PresentationCounters;
  messages: string[];
  soundCues: string[];
  animationCues: string[];
  serverState: Record<string, unknown>;
}

const OverlaySchema = z.object({
  id: z.string().min(1),
  type: z.string().min(1),
  label: z.string().min(1),
  value: z.number().optional(),
  visible: z.boolean().default(true),
});

const CountersSchema = z
  .object({
    freeSpinsRemaining: z.number().int().nonnegative().optional(),
    respinRemaining: z.number().int().nonnegative().optional(),
    holdAndWinRemaining: z.number().int().nonnegative().optional(),
    jackpotLevel: z.number().int().nonnegative().optional(),
  })
  .default({});

const PresentationPayloadSchema = z.object({
  reelStopColumns: z
    .array(z.array(z.number().int().nonnegative()).min(GameConfig.numRows))
    .length(GameConfig.numReels),
  slotIndex: z.number().int().nonnegative().optional(),
  winAmount: z.number().nonnegative().optional(),
  featureOverlays: z.array(OverlaySchema).default([]),
  counters: CountersSchema,
  messages: z.array(z.string()).default([]),
  soundCues: z.array(z.string()).default([]),
  animationCues: z.array(z.string()).default([]),
  serverState: z.record(z.string(), z.unknown()).default({}),
});

type RuntimePresentationPayload = z.infer<typeof PresentationPayloadSchema>;

const normalizeColumns = (columns: number[][]): number[][] =>
  columns.map((column) =>
    column.slice(0, GameConfig.numRows).map((id) => id % GameConfig.symbolCount),
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

export const mapPlayRoundToPresentation = (
  result: PlayRoundResponse,
): RoundPresentationModel => {
  const payload = parsePresentationPayload(result);

  return {
    roundId: result.roundId,
    winAmount: Math.max(0, Math.round(payload.winAmount ?? result.winAmount)),
    slotIndex: payload.slotIndex ?? 0,
    reels: {
      stopColumns: normalizeColumns(payload.reelStopColumns),
    },
    featureOverlays: payload.featureOverlays,
    counters: payload.counters,
    messages: payload.messages,
    soundCues: payload.soundCues,
    animationCues: payload.animationCues,
    serverState: payload.serverState,
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
