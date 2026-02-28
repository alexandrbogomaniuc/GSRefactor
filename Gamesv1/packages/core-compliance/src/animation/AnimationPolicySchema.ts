import { z } from "zod";

export const WinTierSchema = z.enum(["none", "big", "huge", "mega"]);
export type WinTier = z.infer<typeof WinTierSchema>;

export const BigWinThresholdsSchema = z
  .object({
    bigMultiplier: z.number().positive(),
    hugeMultiplier: z.number().positive(),
    megaMultiplier: z.number().positive(),
  })
  .superRefine((value, ctx) => {
    if (!(value.bigMultiplier < value.hugeMultiplier && value.hugeMultiplier < value.megaMultiplier)) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        message: "bigMultiplier < hugeMultiplier < megaMultiplier must hold",
        path: ["bigMultiplier"],
      });
    }
  });

export const AutoplayTimingContractSchema = z.object({
  minDelayBetweenSpinStartsMs: z.number().int().nonnegative(),
  delayAfterSkipMs: z.number().int().nonnegative(),
  delayAfterWinByTierMs: z.object({
    none: z.number().int().nonnegative(),
    big: z.number().int().nonnegative(),
    huge: z.number().int().nonnegative(),
    mega: z.number().int().nonnegative(),
  }),
});

export const TurboAnimationPolicySchema = z.object({
  allowed: z.boolean(),
  speed_id: z.string().min(1),
  preferred: z.boolean(),
  selected: z.boolean(),
  speedMultiplier: z.number().positive(),
});

export const AnimationPolicySchema = z.object({
  turbo: TurboAnimationPolicySchema,
  forcedSkipWinPresentation: z.boolean(),
  minReelsSpinTimeSecs: z.number().nonnegative(),
  minReelsSpinTimeTurboSecs: z.number().nonnegative(),
  bigWinThresholds: BigWinThresholdsSchema,
  lowPerformanceMode: z.boolean(),
  autoplay: AutoplayTimingContractSchema,
  spinStaggerBaseMs: z.number().int().nonnegative(),
  winPresentationMsByTier: z.object({
    none: z.number().int().nonnegative(),
    big: z.number().int().nonnegative(),
    huge: z.number().int().nonnegative(),
    mega: z.number().int().nonnegative(),
  }),
});

export type BigWinThresholds = z.infer<typeof BigWinThresholdsSchema>;
export type AutoplayTimingContract = z.infer<typeof AutoplayTimingContractSchema>;
export type TurboAnimationPolicy = z.infer<typeof TurboAnimationPolicySchema>;
export type AnimationPolicy = z.infer<typeof AnimationPolicySchema>;

export const DefaultBigWinThresholds: BigWinThresholds = {
  bigMultiplier: 10,
  hugeMultiplier: 25,
  megaMultiplier: 50,
};

export const DefaultAutoplayTimingContract: AutoplayTimingContract = {
  minDelayBetweenSpinStartsMs: 250,
  delayAfterSkipMs: 120,
  delayAfterWinByTierMs: {
    none: 300,
    big: 700,
    huge: 1100,
    mega: 1600,
  },
};