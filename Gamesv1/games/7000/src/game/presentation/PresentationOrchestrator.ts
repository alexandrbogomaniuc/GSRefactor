import { listMissingRuntimeAssetKeys } from "../../app/assets/runtimeSlotContract";
import { type LayeredFxController } from "./LayeredFxController";
import { type JackpotPlaqueController } from "./JackpotPlaqueController";
import {
  deriveWinTier,
  type PresentationTrigger,
  type PresentationWinTier,
} from "./PresentationEvents";
import { type TopperMascotController } from "./TopperMascotController";

type PresentationOrchestratorOptions = {
  fx: LayeredFxController;
  topper: TopperMascotController;
  jackpots: JackpotPlaqueController;
};

export class PresentationOrchestrator {
  constructor(private readonly options: PresentationOrchestratorOptions) {}

  public getMissingSlots(): string[] {
    return listMissingRuntimeAssetKeys();
  }

  public route(trigger: PresentationTrigger): PresentationWinTier {
    const winTier = deriveWinTier(trigger.multiplier ?? 0, trigger.jackpotTier);
    const fxAny = this.options.fx as unknown as Record<string, (...args: unknown[]) => void>;
    const topperAny = this.options.topper as unknown as Record<string, (...args: unknown[]) => void>;
    const jackpotAny = this.options.jackpots as unknown as Record<string, (...args: unknown[]) => void>;

    switch (trigger.kind) {
      case "spin_start":
        fxAny.resetFeatureState?.();
        topperAny.onSpinStart?.();
        break;
      case "line_win":
        fxAny.playPaylinePulse?.(trigger.lineId ?? 0, trigger.multiplier ?? 1);
        topperAny.onLineWin?.(trigger.lineId ?? 0, trigger.multiplier ?? 1);
        break;
      case "collect":
        fxAny.playCollectStrike?.();
        topperAny.onCollect?.();
        break;
      case "boost":
        fxAny.playBoostStrike?.(trigger.multiplier ?? 1);
        topperAny.onBoost?.(trigger.multiplier ?? 1);
        break;
      case "bonus":
        fxAny.playBonusEntry?.();
        topperAny.onBonusEntry?.();
        break;
      case "jackpot":
        fxAny.playJackpotStrike?.(trigger.jackpotTier ?? "mini");
        jackpotAny.highlightTier?.(trigger.jackpotTier ?? "mini");
        topperAny.onJackpot?.(trigger.jackpotTier ?? "mini");
        break;
      case "buy_bonus":
        fxAny.playBuyBonusCue?.();
        topperAny.onBuyBonus?.();
        break;
      case "spin_end":
        topperAny.onSpinEnd?.();
        break;
      default:
        break;
    }

    return winTier;
  }
}
