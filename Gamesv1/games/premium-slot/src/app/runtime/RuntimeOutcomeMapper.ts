import type { PlayRoundResponse } from "@gamesv1/core-protocol";
import {
  GameConfig,
  mapPlayRoundToPresentation as mapSharedPlayRoundToPresentation,
  mapPlayRoundToSlotOutcome as mapSharedPlayRoundToSlotOutcome,
  type PresentationCounters,
  type PresentationLayoutConstraints,
  type RoundPresentationModel,
  type SlotPresentationOutcome,
} from "@gamesv1/ui-kit";

const premiumSlotLayoutConstraints: PresentationLayoutConstraints = {
  reelCount: GameConfig.numReels,
  rowCount: GameConfig.numRows,
  symbolModulo: GameConfig.symbolCount,
};

export const mapPlayRoundToPresentation = (
  result: PlayRoundResponse,
): RoundPresentationModel =>
  mapSharedPlayRoundToPresentation(result, premiumSlotLayoutConstraints);

export const mapPlayRoundToSlotOutcome = (
  result: PlayRoundResponse,
): SlotPresentationOutcome =>
  mapSharedPlayRoundToSlotOutcome(result, premiumSlotLayoutConstraints);

export type { PresentationCounters, RoundPresentationModel, SlotPresentationOutcome };
