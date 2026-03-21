import type { PlayRoundResponse } from "@gamesv1/core-protocol";
import {
  mapPlayRoundToPresentation as mapSharedPlayRoundToPresentation,
  mapPlayRoundToSlotOutcome as mapSharedPlayRoundToSlotOutcome,
  type PresentationCounters,
  type PresentationLayoutConstraints,
  type RoundPresentationModel,
  type SlotPresentationOutcome,
} from "@gamesv1/ui-kit";

import { CRAZY_ROOSTER_LAYOUT } from "../../game/config/CrazyRoosterGameConfig";
import type { MathBridgePresentationHints } from "./provisionalMathSource";

const layoutConstraints: PresentationLayoutConstraints = {
  reelCount: CRAZY_ROOSTER_LAYOUT.reelCount,
  rowCount: CRAZY_ROOSTER_LAYOUT.rowCount,
  symbolModulo: CRAZY_ROOSTER_LAYOUT.symbolCount,
};

export const mapPlayRoundToPresentation = (
  result: PlayRoundResponse,
): RoundPresentationModel =>
  mapSharedPlayRoundToPresentation(result, layoutConstraints);

export const mapPlayRoundToSlotOutcome = (
  result: PlayRoundResponse,
): SlotPresentationOutcome =>
  mapSharedPlayRoundToSlotOutcome(result, layoutConstraints);

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const asString = (value: unknown): string | null =>
  typeof value === "string" && value.length > 0 ? value : null;

const asNumber = (value: unknown): number | null =>
  typeof value === "number" && Number.isFinite(value) ? value : null;

const asNumberMatrix = (value: unknown): number[][] | null => {
  if (!Array.isArray(value)) {
    return null;
  }
  const matrix = value.map((column) => {
    if (!Array.isArray(column)) {
      return [];
    }
    return column
      .map((entry) => asNumber(entry))
      .filter((entry): entry is number => entry !== null);
  });
  return matrix;
};

export const readMathBridgeHints = (
  result: PlayRoundResponse,
): MathBridgePresentationHints | null => {
  const payload = isRecord(result.presentationPayload) ? result.presentationPayload : null;
  const bridge = payload && isRecord(payload.mathBridge) ? payload.mathBridge : null;
  if (!bridge) {
    return null;
  }

  const source = asString(bridge.source);
  const mode = asString(bridge.mode);
  const preset = asString(bridge.preset);
  const winTier = asString(bridge.winTier);
  const lineWinMultiplier = asNumber(bridge.lineWinMultiplier) ?? 0;
  const bonusWinMultiplier = asNumber(bridge.bonusWinMultiplier) ?? 0;
  const totalWinMultiplier = asNumber(bridge.totalWinMultiplier) ?? 0;
  if (
    source !== "provisional" ||
    !mode ||
    !preset ||
    !winTier ||
    !isRecord(bridge.triggers) ||
    !Array.isArray(bridge.lineWins) ||
    !Array.isArray(bridge.eventTriggers) ||
    !isRecord(bridge.timingHints)
  ) {
    return null;
  }

  const timingHintsRaw = bridge.timingHints;
  const reelStopRaw = Array.isArray(timingHintsRaw.reelStopDelaysMs)
    ? timingHintsRaw.reelStopDelaysMs
    : [];
  const reelStops: [number, number, number] = [
    asNumber(reelStopRaw[0]) ?? 483,
    asNumber(reelStopRaw[1]) ?? 567,
    asNumber(reelStopRaw[2]) ?? 650,
  ];

  const lineWins = bridge.lineWins
    .map((entry) => {
      if (!isRecord(entry)) {
        return null;
      }
      const lineId = asNumber(entry.lineId);
      const symbolId = asNumber(entry.symbolId);
      const multiplier = asNumber(entry.multiplier);
      const amountMinor = asNumber(entry.amountMinor);
      const rowsByReelRaw = Array.isArray(entry.rowsByReel) ? entry.rowsByReel : [];
      const positionsRaw = Array.isArray(entry.positions) ? entry.positions : [];
      if (
        lineId === null ||
        symbolId === null ||
        multiplier === null ||
        amountMinor === null
      ) {
        return null;
      }
      return {
        lineId,
        symbolId,
        multiplier,
        amountMinor,
        rowsByReel: rowsByReelRaw.map((value) => asNumber(value) ?? 0),
        positions: positionsRaw
          .map((position) => {
            if (!isRecord(position)) {
              return null;
            }
            const reelIndex = asNumber(position.reelIndex);
            const rowIndex = asNumber(position.rowIndex);
            if (reelIndex === null || rowIndex === null) {
              return null;
            }
            return { reelIndex, rowIndex };
          })
          .filter((entry): entry is { reelIndex: number; rowIndex: number } => Boolean(entry)),
      };
    })
    .filter(
      (
        entry,
      ): entry is MathBridgePresentationHints["lineWins"][number] => Boolean(entry),
    );

  const generatorTraceRaw = isRecord(bridge.generatorTrace)
    ? bridge.generatorTrace
    : null;
  const generatorTrace =
    generatorTraceRaw &&
    (asString(generatorTraceRaw.sourcePath) === "weighted-reel-rng" ||
      asString(generatorTraceRaw.sourcePath) === "canned-preset-override")
      ? {
          mode:
            (asString(generatorTraceRaw.mode) as MathBridgePresentationHints["generatorTrace"]["mode"]) ??
            "base",
          requestedPreset:
            (asString(generatorTraceRaw.requestedPreset) as MathBridgePresentationHints["generatorTrace"]["requestedPreset"]) ??
            null,
          effectivePreset:
            (asString(generatorTraceRaw.effectivePreset) as MathBridgePresentationHints["generatorTrace"]["effectivePreset"]) ??
            null,
          sourcePath: asString(generatorTraceRaw.sourcePath) as
            | "weighted-reel-rng"
            | "canned-preset-override",
          seedStateBefore: asNumber(generatorTraceRaw.seedStateBefore) ?? 0,
          seedStateAfter: asNumber(generatorTraceRaw.seedStateAfter) ?? 0,
          rawGeneratedMatrix:
            asNumberMatrix(generatorTraceRaw.rawGeneratedMatrix) ?? [],
          finalPresentedMatrix:
            asNumberMatrix(generatorTraceRaw.finalPresentedMatrix) ?? [],
          boardHash: asString(generatorTraceRaw.boardHash) ?? "",
          postGenerationRewrite: generatorTraceRaw.postGenerationRewrite === true,
        }
      : {
          mode: mode as MathBridgePresentationHints["generatorTrace"]["mode"],
          requestedPreset: null,
          effectivePreset: null,
          sourcePath: "weighted-reel-rng" as const,
          seedStateBefore: 0,
          seedStateAfter: 0,
          rawGeneratedMatrix: [],
          finalPresentedMatrix: [],
          boardHash: "",
          postGenerationRewrite: false,
        };

  return {
    source: "provisional",
    mode: mode as MathBridgePresentationHints["mode"],
    preset: preset as MathBridgePresentationHints["preset"],
    winTier: winTier as MathBridgePresentationHints["winTier"],
    triggers: {
      collect: bridge.triggers.collect === true,
      boost: bridge.triggers.boost === true,
      bonus: bridge.triggers.bonus === true,
      jackpot: bridge.triggers.jackpot === true,
    },
    lineWins,
    eventTriggers: bridge.eventTriggers
      .map((cue) => asString(cue))
      .filter((cue): cue is string => Boolean(cue)),
    timingHints: {
      reelStopDelaysMs: reelStops,
      lineHighlightDelayMs: asNumber(timingHintsRaw.lineHighlightDelayMs) ?? 90,
      lineHighlightDurationMs: asNumber(timingHintsRaw.lineHighlightDurationMs) ?? 760,
      featureStartDelayMs: asNumber(timingHintsRaw.featureStartDelayMs) ?? 140,
      featureLoopDurationMs: asNumber(timingHintsRaw.featureLoopDurationMs) ?? 1150,
      featureFinishDelayMs: asNumber(timingHintsRaw.featureFinishDelayMs) ?? 480,
      coinFlyDurationMs: asNumber(timingHintsRaw.coinFlyDurationMs) ?? 880,
    },
    jackpotTier: asString(bridge.jackpotTier) as MathBridgePresentationHints["jackpotTier"],
    lineWinMultiplier,
    bonusWinMultiplier,
    totalWinMultiplier,
    generatorTrace,
  };
};

export type { PresentationCounters, RoundPresentationModel, SlotPresentationOutcome };
