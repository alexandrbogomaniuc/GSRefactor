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

export type { PresentationCounters, RoundPresentationModel, SlotPresentationOutcome };
