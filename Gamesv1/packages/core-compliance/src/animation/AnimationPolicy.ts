import type { ResolvedConfig } from "../config/RuntimeConfigSchema.ts";
import {
  AnimationPolicySchema,
  DefaultAutoplayTimingContract,
  DefaultBigWinThresholds,
  type AnimationPolicy,
  type AutoplayTimingContract,
  type BigWinThresholds,
  type WinTier,
} from "./AnimationPolicySchema.ts";

export interface CreateAnimationPolicyInput {
  runtimeConfig: Pick<ResolvedConfig, "turboplay" | "minReelSpinTime">;
  forcedSkipWinPresentation?: boolean;
  lowPerformanceMode?: boolean;
  preferredTurboSelection?: boolean;
  bigWinThresholds?: Partial<BigWinThresholds>;
  autoplay?: Partial<AutoplayTimingContract>;
  spinStaggerBaseMs?: number;
  winPresentationMsByTier?: Partial<Record<WinTier, number>>;
  turboSpeedMultipliers?: Record<string, number>;
}

export interface SpinTimingResolution {
  turboEnabled: boolean;
  speed_id: string;
  speedMultiplier: number;
  minSpinMs: number;
  spinStaggerMs: number;
}

const DEFAULT_TURBO_MULTIPLIERS: Record<string, number> = {
  "turbo-x2": 2,
  "turbo-x3": 3,
  "turbo-x4": 4,
};

const getSpeedMultiplier = (
  speedId: string,
  overrides?: Record<string, number>,
): number => {
  const merged = { ...DEFAULT_TURBO_MULTIPLIERS, ...(overrides ?? {}) };
  return merged[speedId] ?? 2;
};

const mergeAutoplayContract = (
  overrides?: Partial<AutoplayTimingContract>,
): AutoplayTimingContract => ({
  ...DefaultAutoplayTimingContract,
  ...(overrides ?? {}),
  delayAfterWinByTierMs: {
    ...DefaultAutoplayTimingContract.delayAfterWinByTierMs,
    ...(overrides?.delayAfterWinByTierMs ?? {}),
  },
});

const DEFAULT_PRESENTATION_MS_BY_TIER: Record<WinTier, number> = {
  none: 350,
  big: 1500,
  huge: 2200,
  mega: 3000,
};

export const createAnimationPolicy = (input: CreateAnimationPolicyInput): AnimationPolicy => {
  const thresholdConfig = {
    ...DefaultBigWinThresholds,
    ...(input.bigWinThresholds ?? {}),
  };

  const selectedTurbo = Boolean(input.preferredTurboSelection ?? input.runtimeConfig.turboplay.preferred);

  const policy = {
    turbo: {
      allowed: input.runtimeConfig.turboplay.allowed,
      speed_id: input.runtimeConfig.turboplay.speedId,
      preferred: input.runtimeConfig.turboplay.preferred,
      selected: input.runtimeConfig.turboplay.allowed ? selectedTurbo : false,
      speedMultiplier: getSpeedMultiplier(
        input.runtimeConfig.turboplay.speedId,
        input.turboSpeedMultipliers,
      ),
    },
    forcedSkipWinPresentation: Boolean(input.forcedSkipWinPresentation),
    minReelsSpinTimeSecs: Math.max(0, input.runtimeConfig.minReelSpinTime.normalMs / 1000),
    minReelsSpinTimeTurboSecs: Math.max(0, input.runtimeConfig.minReelSpinTime.turboMs / 1000),
    bigWinThresholds: thresholdConfig,
    lowPerformanceMode: Boolean(input.lowPerformanceMode),
    autoplay: mergeAutoplayContract(input.autoplay),
    spinStaggerBaseMs: input.spinStaggerBaseMs ?? 180,
    winPresentationMsByTier: {
      ...DEFAULT_PRESENTATION_MS_BY_TIER,
      ...(input.winPresentationMsByTier ?? {}),
    },
  };

  return AnimationPolicySchema.parse(policy);
};

export class AnimationPolicyEngine {
  private readonly policy: AnimationPolicy;

  constructor(policy: AnimationPolicy) {
    this.policy = policy;
  }

  public get value(): AnimationPolicy {
    return this.policy;
  }

  public classifyWinByMultiplier(multiplier: number): WinTier {
    if (multiplier >= this.policy.bigWinThresholds.megaMultiplier) {
      return "mega";
    }

    if (multiplier >= this.policy.bigWinThresholds.hugeMultiplier) {
      return "huge";
    }

    if (multiplier >= this.policy.bigWinThresholds.bigMultiplier) {
      return "big";
    }

    return "none";
  }

  public resolveSpinTiming(requestTurboSelection?: boolean): SpinTimingResolution {
    const turboEnabled =
      this.policy.turbo.allowed &&
      Boolean(requestTurboSelection ?? this.policy.turbo.selected);

    const minSpinMs = turboEnabled
      ? Math.round(this.policy.minReelsSpinTimeTurboSecs * 1000)
      : Math.round(this.policy.minReelsSpinTimeSecs * 1000);

    const staggerScale = turboEnabled ? 0.65 : 1;

    return {
      turboEnabled,
      speed_id: this.policy.turbo.speed_id,
      speedMultiplier: turboEnabled ? this.policy.turbo.speedMultiplier : 1,
      minSpinMs,
      spinStaggerMs: Math.round(this.policy.spinStaggerBaseMs * staggerScale),
    };
  }

  public shouldAllowForcedSkip(): boolean {
    return this.policy.forcedSkipWinPresentation;
  }

  public shouldPlayHeavyWinFx(winTier: WinTier): boolean {
    if (this.policy.lowPerformanceMode) {
      return false;
    }

    return winTier !== "none";
  }

  public getWinPresentationDurationMs(winTier: WinTier, skipped: boolean): number {
    if (skipped || this.policy.forcedSkipWinPresentation) {
      return 0;
    }

    return this.policy.winPresentationMsByTier[winTier];
  }

  public getAutoplayDelayMs(winTier: WinTier, skipped: boolean): number {
    const postWinDelay = skipped
      ? this.policy.autoplay.delayAfterSkipMs
      : this.policy.autoplay.delayAfterWinByTierMs[winTier];

    return Math.max(this.policy.autoplay.minDelayBetweenSpinStartsMs, postWinDelay);
  }
}