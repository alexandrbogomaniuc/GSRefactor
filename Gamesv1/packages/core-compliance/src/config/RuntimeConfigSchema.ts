import { z } from "zod";

const PositiveNumber = z.number().positive();
const Volume = z.number().min(0).max(1);

export const LadderBetConfigSchema = z.object({
  mode: z.literal("ladder"),
  betLadder: z.array(PositiveNumber).min(1),
  coinValues: z.array(PositiveNumber).min(1),
});

export const DynamicBetConstraintsSchema = z.object({
  mode: z.literal("dynamic"),
  dynamicBetConstraints: z.object({
    minStep: PositiveNumber,
    maxStep: PositiveNumber,
    step: PositiveNumber,
  }),
});

export const BetConfigSchema = z.discriminatedUnion("mode", [
  LadderBetConfigSchema,
  DynamicBetConstraintsSchema,
]);

export const TurboplayConfigSchema = z.object({
  allowed: z.boolean(),
  speedId: z.string().min(1),
  preferred: z.boolean(),
});

const MinReelSpinTimeBaseSchema = z.object({
  normalMs: z.number().int().nonnegative(),
  turboMs: z.number().int().nonnegative(),
});

export const MinReelSpinTimeConfigSchema = MinReelSpinTimeBaseSchema.refine(
  (v) => v.turboMs <= v.normalMs,
  {
    message: "turboMs cannot exceed normalMs",
    path: ["turboMs"],
  },
);

export const SoundDefaultsSchema = z.object({
  enabled: z.boolean(),
  masterVolume: Volume,
  bgmVolume: Volume,
  sfxVolume: Volume,
});

export const LocalizationSettingsSchema = z.object({
  defaultLang: z.string().min(2),
  showMissingLocalizationError: z.boolean(),
  contentPath: z.string().min(1),
  customTranslationsEnabled: z.boolean(),
});

export const RealityCheckConfigSchema = z.object({
  enabled: z.boolean(),
  intervalMinutes: z.number().int().positive(),
});

export const ResolvedConfigSchema = z
  .object({
    currencyCode: z.string().min(3).max(3),
    betConfig: BetConfigSchema,
    minBet: PositiveNumber,
    maxBet: PositiveNumber,
    maxExposure: PositiveNumber,
    defaultBet: PositiveNumber,
    turboplay: TurboplayConfigSchema,
    minReelSpinTime: MinReelSpinTimeConfigSchema,
    soundDefaults: SoundDefaultsSchema,
    localization: LocalizationSettingsSchema,
    realityCheck: RealityCheckConfigSchema,
  })
  .superRefine((config, ctx) => {
    if (config.minBet > config.maxBet) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["minBet"],
        message: "minBet cannot be greater than maxBet",
      });
    }

    if (config.defaultBet < config.minBet || config.defaultBet > config.maxBet) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["defaultBet"],
        message: "defaultBet must be within minBet/maxBet",
      });
    }

    if (config.betConfig.mode === "ladder") {
      const ladderMin = Math.min(...config.betConfig.betLadder);
      const ladderMax = Math.max(...config.betConfig.betLadder);

      if (config.minBet < ladderMin || config.maxBet > ladderMax) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ["betConfig", "betLadder"],
          message:
            "For ladder mode, minBet/maxBet must stay inside configured betLadder range",
        });
      }
    } else if (
      config.betConfig.dynamicBetConstraints.minStep >
      config.betConfig.dynamicBetConstraints.maxStep
    ) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["betConfig", "dynamicBetConstraints", "minStep"],
        message: "dynamic minStep cannot exceed maxStep",
      });
    }
  });

export type ResolvedConfig = z.infer<typeof ResolvedConfigSchema>;

export const LayerConfigSchema = z.object({
  currencyCode: z.string().min(3).max(3).optional(),
  betConfig: BetConfigSchema.optional(),
  minBet: PositiveNumber.optional(),
  maxBet: PositiveNumber.optional(),
  maxExposure: PositiveNumber.optional(),
  defaultBet: PositiveNumber.optional(),
  turboplay: TurboplayConfigSchema.partial().optional(),
  minReelSpinTime: MinReelSpinTimeBaseSchema.partial().optional(),
  soundDefaults: SoundDefaultsSchema.partial().optional(),
  localization: LocalizationSettingsSchema.partial().optional(),
  realityCheck: RealityCheckConfigSchema.partial().optional(),
});

export type LayerConfig = z.infer<typeof LayerConfigSchema>;

export const CurrencyOverridesSchema = z.record(LayerConfigSchema);
export type CurrencyOverrides = z.infer<typeof CurrencyOverridesSchema>;

export const LaunchParamsSchema = LayerConfigSchema.extend({
  devMode: z.boolean().optional(),
});

export type LaunchParams = z.infer<typeof LaunchParamsSchema>;

export const ConfigResolverInputSchema = z.object({
  templateDefaults: LayerConfigSchema,
  bankProperties: LayerConfigSchema,
  gameOverrides: LayerConfigSchema.default({}),
  currencyOverrides: CurrencyOverridesSchema.default({}),
  launchParams: LaunchParamsSchema.default({}),
  devMode: z.boolean().optional(),
});

export type ConfigResolverInput = z.infer<typeof ConfigResolverInputSchema>;

export const DefaultResolvedConfig: ResolvedConfig = {
  currencyCode: "EUR",
  betConfig: {
    mode: "ladder",
    betLadder: [10, 20, 50, 100, 200],
    coinValues: [0.01, 0.02, 0.05, 0.1],
  },
  minBet: 10,
  maxBet: 200,
  maxExposure: 100000,
  defaultBet: 20,
  turboplay: {
    allowed: true,
    speedId: "turbo-x2",
    preferred: false,
  },
  minReelSpinTime: {
    normalMs: 2000,
    turboMs: 1200,
  },
  soundDefaults: {
    enabled: true,
    masterVolume: 0.8,
    bgmVolume: 0.7,
    sfxVolume: 0.8,
  },
  localization: {
    defaultLang: "en",
    showMissingLocalizationError: false,
    contentPath: "./locales",
    customTranslationsEnabled: false,
  },
  realityCheck: {
    enabled: false,
    intervalMinutes: 60,
  },
};
