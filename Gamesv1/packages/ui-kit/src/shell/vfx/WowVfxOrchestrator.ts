import type { SlotSymbol } from "../../slots/SlotSymbol.ts";
import {
  getWinPresentationTitle,
  type PolicyWinTier,
  toPresentationWinTier,
  type PresentationWinTier,
} from "./WinPresentationTiers.ts";

export interface AnimationPolicyLike {
  classifyWinByMultiplier(multiplier: number): PolicyWinTier;
  shouldPlayHeavyWinFx(winTier: PolicyWinTier): boolean;
  shouldAllowForcedSkip(): boolean;
  getWinPresentationDurationMs(winTier: PolicyWinTier, skipped: boolean): number;
}

export interface WinPresentationCueHooks {
  onAudioCue?: (cue: string) => void;
  onAnimationCue?: (cue: string) => void;
  showWinCounter?: (amountMinor: number, title: string, tier: PresentationWinTier) => void;
  hideWinCounter?: () => void;
  showHeavyWinFx?: (symbols: SlotSymbol[], tier: PresentationWinTier) => void;
  clearHeavyWinFx?: () => void;
  playCoinBurst?: (
    origin: { x: number; y: number },
    tier: PresentationWinTier,
  ) => void;
}

export interface StartWinPresentationInput {
  winAmountMinor: number;
  defaultBetMinor: number;
  winSymbols: SlotSymbol[];
  soundCues: string[];
  animationCues: string[];
  burstOrigin?: { x: number; y: number };
}

export interface WinPresentationState {
  tier: PresentationWinTier;
  policyTier: PolicyWinTier;
  title: string;
  hasWinPresentation: boolean;
  forcedSkip: boolean;
  suppressHeavyFx: boolean;
  heavyFxPlayed: boolean;
  durationMs: number;
}

const cueForcesSkip = (cue: string): boolean => cue === "force-skip-presentation";
const cueDisablesHeavyFx = (cue: string): boolean => cue === "disable-heavy-win-fx";

export class WowVfxOrchestrator {
  private readonly animationPolicy: AnimationPolicyLike;
  private readonly hooks: WinPresentationCueHooks;

  constructor(animationPolicy: AnimationPolicyLike, hooks: WinPresentationCueHooks) {
    this.animationPolicy = animationPolicy;
    this.hooks = hooks;
  }

  public startWinPresentation(input: StartWinPresentationInput): WinPresentationState {
    for (const cue of input.soundCues) {
      this.hooks.onAudioCue?.(cue);
    }

    let suppressHeavyFx = false;
    let forcedSkipFromCues = false;

    for (const cue of input.animationCues) {
      this.hooks.onAnimationCue?.(cue);
      if (cueDisablesHeavyFx(cue)) {
        suppressHeavyFx = true;
      }
      if (cueForcesSkip(cue)) {
        forcedSkipFromCues = true;
      }
    }

    const safeDefaultBet = Math.max(1, input.defaultBetMinor);
    const winMultiplier = input.winAmountMinor / safeDefaultBet;
    const policyTier = this.animationPolicy.classifyWinByMultiplier(winMultiplier);
    const tier = toPresentationWinTier(policyTier, input.winAmountMinor);
    const hasWinPresentation = tier !== "none";
    const title = getWinPresentationTitle(tier);
    const forcedSkip = this.animationPolicy.shouldAllowForcedSkip() || forcedSkipFromCues;

    if (!hasWinPresentation) {
      this.hooks.clearHeavyWinFx?.();
      this.hooks.hideWinCounter?.();
      return {
        tier,
        policyTier,
        title,
        hasWinPresentation: false,
        forcedSkip: true,
        suppressHeavyFx,
        heavyFxPlayed: false,
        durationMs: 0,
      };
    }

    const heavyFxAllowed =
      !forcedSkip && !suppressHeavyFx && this.animationPolicy.shouldPlayHeavyWinFx(policyTier);

    if (heavyFxAllowed) {
      this.hooks.showHeavyWinFx?.(input.winSymbols, tier);
      this.hooks.playCoinBurst?.(input.burstOrigin ?? { x: 0, y: -100 }, tier);
    } else {
      this.hooks.clearHeavyWinFx?.();
    }

    if (!forcedSkip) {
      this.hooks.showWinCounter?.(input.winAmountMinor, title, tier);
    } else {
      this.hooks.hideWinCounter?.();
    }

    const durationMs = forcedSkip
      ? 0
      : this.animationPolicy.getWinPresentationDurationMs(policyTier, forcedSkip);

    return {
      tier,
      policyTier,
      title,
      hasWinPresentation: true,
      forcedSkip,
      suppressHeavyFx,
      heavyFxPlayed: heavyFxAllowed,
      durationMs,
    };
  }

  public finishWinPresentation(): void {
    this.hooks.clearHeavyWinFx?.();
    this.hooks.hideWinCounter?.();
  }
}
