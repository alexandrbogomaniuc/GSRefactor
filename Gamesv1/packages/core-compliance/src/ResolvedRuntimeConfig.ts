import { z } from "zod";

import {
  CapabilityMatrixPatchSchema,
  CapabilityMatrixSchema,
  DefaultCapabilityMatrix,
  type CapabilityMatrix,
  type CapabilityMatrixPatch,
} from "./CapabilityMatrix.ts";

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
  modeByDefault: z.enum(["on", "off", "muted"]),
  masterVolume: Volume,
  bgmVolume: Volume,
  sfxVolume: Volume,
});

export const LocalizationSettingsSchema = z.object({
  defaultLang: z.string().min(2),
  localizedTitleKey: z.string().min(1),
  localizedTitle: z.string().optional(),
  showMissingLocalizationError: z.boolean(),
  contentPath: z.string().min(1),
  customTranslationsEnabled: z.boolean(),
  serverNotificationsEnabled: z.boolean(),
});

export const HistorySettingsSchema = z.object({
  enabled: z.boolean(),
  url: z.string().min(1),
  openInSameWindow: z.boolean(),
});

export const WalletDisplayPolicySchema = z.object({
  showBalance: z.boolean(),
  showCurrencyCode: z.boolean(),
  showDelayedIndicator: z.boolean(),
});

export const SessionUiPolicySchema = z.object({
  showSessionTimer: z.boolean(),
  showRealityCheckBanner: z.boolean(),
  closeButtonPolicy: z.enum(["allow", "confirm", "hidden"]),
});

export const AnimationPolicyGroupSchema = z.object({
  forcedSpinStopEnabled: z.boolean(),
  forcedSkipWinPresentation: z.boolean(),
  minReelSpinTimeMs: z.object({
    normal: z.number().int().nonnegative(),
    turbo: z.number().int().nonnegative(),
  }),
  autoplayMinDelayMs: z.number().int().nonnegative(),
  lowPerformanceMode: z.boolean(),
  spinProfilingEnabled: z.boolean(),
});

export const SoundPolicyGroupSchema = z.object({
  soundModeByDefault: z.enum(["on", "off", "muted"]),
  showToggle: z.boolean(),
  masterVolume: Volume,
  bgmVolume: Volume,
  sfxVolume: Volume,
});

export const LocalizationPolicyGroupSchema = z.object({
  defaultLanguage: z.string().min(2),
  localizedTitleKey: z.string().min(1),
  localizedTitle: z.string().optional(),
  showMissingLocalizationError: z.boolean(),
  contentPath: z.string().min(1),
  customTranslationsEnabled: z.boolean(),
  serverNotificationsEnabled: z.boolean(),
});

export const HistoryPolicyGroupSchema = z.object({
  enabled: z.boolean(),
  url: z.string().min(1),
  openInSameWindow: z.boolean(),
});

export const WalletDisplayPolicyGroupSchema = z.object({
  showBalance: z.boolean(),
  showCurrencyCode: z.boolean(),
  showDelayedIndicator: z.boolean(),
  delayedWalletMessages: z.boolean(),
});

export const FeaturePolicyGroupSchema = z.object({
  autoplay: z.boolean(),
  buyFeature: z.boolean(),
  buyFeatureForCashBonus: z.boolean(),
  buyFeatureDisabledForCashBonus: z.boolean(),
  freeSpins: z.boolean(),
  respin: z.boolean(),
  holdAndWin: z.boolean(),
  inGameHistory: z.boolean(),
  holidayMode: z.boolean(),
  customSkins: z.boolean(),
  frb: z.boolean(),
  ofrb: z.boolean(),
  jackpotHooksEnabled: z.boolean(),
});

export const SessionUiPolicyGroupSchema = SessionUiPolicySchema;

export const JackpotHooksSchema = z.object({
  enabled: z.boolean(),
  source: z.enum(["none", "gs"]),
});

export const RuntimePoliciesSchema = z.object({
  requestCounterRequired: z.boolean(),
  idempotencyKeyRequired: z.boolean(),
  clientOperationIdRequired: z.boolean(),
  currentStateVersionSupported: z.boolean(),
  unfinishedRoundRestoreSupported: z.boolean(),
});

export const RealityCheckConfigSchema = z.object({
  enabled: z.boolean(),
  intervalMinutes: z.number().int().positive(),
});

export const ResolvedRuntimeConfigSchema = z
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
    history: HistorySettingsSchema,
    walletDisplay: WalletDisplayPolicySchema,
    sessionUi: SessionUiPolicySchema,
    jackpotHooks: JackpotHooksSchema,
    runtimePolicies: RuntimePoliciesSchema,
    realityCheck: RealityCheckConfigSchema,
    GL_MAX_BET: PositiveNumber.optional(),
    exposureDerivedMaxBet: PositiveNumber.optional(),
    animationPolicy: AnimationPolicyGroupSchema,
    soundPolicy: SoundPolicyGroupSchema,
    localizationPolicy: LocalizationPolicyGroupSchema,
    historyPolicy: HistoryPolicyGroupSchema,
    walletDisplayPolicy: WalletDisplayPolicyGroupSchema,
    featurePolicy: FeaturePolicyGroupSchema,
    sessionUiPolicy: SessionUiPolicyGroupSchema,
    capabilities: CapabilityMatrixSchema,
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

    if (config.maxBet > config.maxExposure) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["maxBet"],
        message: "maxBet cannot exceed maxExposure",
      });
    }

    if (config.defaultBet > config.maxExposure) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["defaultBet"],
        message: "defaultBet cannot exceed maxExposure",
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
    } else if (config.betConfig.dynamicBetConstraints.maxStep > config.maxExposure) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["betConfig", "dynamicBetConstraints", "maxStep"],
        message: "dynamic maxStep cannot exceed maxExposure",
      });
    }

    if (config.history.enabled && config.history.url.startsWith("javascript:")) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["history", "url"],
        message: "history URL cannot use javascript: scheme",
      });
    }
  });

export type ResolvedRuntimeConfig = z.infer<typeof ResolvedRuntimeConfigSchema>;

export const LayerRuntimeConfigSchema = z.object({
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
  history: HistorySettingsSchema.partial().optional(),
  walletDisplay: WalletDisplayPolicySchema.partial().optional(),
  sessionUi: SessionUiPolicySchema.partial().optional(),
  jackpotHooks: JackpotHooksSchema.partial().optional(),
  runtimePolicies: RuntimePoliciesSchema.partial().optional(),
  realityCheck: RealityCheckConfigSchema.partial().optional(),
  GL_MAX_BET: PositiveNumber.optional(),
  exposureDerivedMaxBet: PositiveNumber.optional(),
  animationPolicy: AnimationPolicyGroupSchema.partial().optional(),
  soundPolicy: SoundPolicyGroupSchema.partial().optional(),
  localizationPolicy: LocalizationPolicyGroupSchema.partial().optional(),
  historyPolicy: HistoryPolicyGroupSchema.partial().optional(),
  walletDisplayPolicy: WalletDisplayPolicyGroupSchema.partial().optional(),
  featurePolicy: FeaturePolicyGroupSchema.partial().optional(),
  sessionUiPolicy: SessionUiPolicyGroupSchema.partial().optional(),
  capabilities: CapabilityMatrixPatchSchema.optional(),
});

export type LayerRuntimeConfig = z.infer<typeof LayerRuntimeConfigSchema>;

export const CurrencyOverridesSchema = z.record(z.string(), LayerRuntimeConfigSchema);
export type CurrencyOverrides = z.infer<typeof CurrencyOverridesSchema>;

export const LegacyLaunchDefaultsSchema = z.object({
  GL_DEFAULT_BET: PositiveNumber.optional(),
  DEFCOIN: PositiveNumber.optional(),
});

export const LaunchBootstrapSchema = LayerRuntimeConfigSchema.extend({
  devMode: z.boolean().optional(),
  GL_DEFAULT_BET: PositiveNumber.optional(),
  DEFCOIN: PositiveNumber.optional(),
  legacyDefaults: LegacyLaunchDefaultsSchema.optional(),
});

export type LaunchBootstrapValues = z.infer<typeof LaunchBootstrapSchema>;

export const ConfigResolverInputSchema = z.object({
  templateDefaults: LayerRuntimeConfigSchema,
  bankProperties: LayerRuntimeConfigSchema,
  gameOverrides: LayerRuntimeConfigSchema.default({}),
  currencyOverrides: CurrencyOverridesSchema.default({}),
  launchParams: LaunchBootstrapSchema.default({}),
  devMode: z.boolean().optional(),
});

export type ConfigResolverInput = z.infer<typeof ConfigResolverInputSchema>;

export const DefaultResolvedRuntimeConfig: ResolvedRuntimeConfig = {
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
    modeByDefault: "on",
    masterVolume: 0.8,
    bgmVolume: 0.7,
    sfxVolume: 0.8,
  },
  localization: {
    defaultLang: "en",
    localizedTitleKey: "game.title",
    localizedTitle: "",
    showMissingLocalizationError: false,
    contentPath: "./locales",
    customTranslationsEnabled: false,
    serverNotificationsEnabled: false,
  },
  history: {
    enabled: true,
    url: "/history",
    openInSameWindow: true,
  },
  walletDisplay: {
    showBalance: true,
    showCurrencyCode: true,
    showDelayedIndicator: true,
  },
  sessionUi: {
    showSessionTimer: false,
    showRealityCheckBanner: true,
    closeButtonPolicy: "confirm",
  },
  jackpotHooks: {
    enabled: false,
    source: "none",
  },
  runtimePolicies: {
    requestCounterRequired: true,
    idempotencyKeyRequired: true,
    clientOperationIdRequired: true,
    currentStateVersionSupported: true,
    unfinishedRoundRestoreSupported: true,
  },
  realityCheck: {
    enabled: false,
    intervalMinutes: 60,
  },
  animationPolicy: {
    forcedSpinStopEnabled: true,
    forcedSkipWinPresentation: true,
    minReelSpinTimeMs: {
      normal: 2000,
      turbo: 1200,
    },
    autoplayMinDelayMs: 300,
    lowPerformanceMode: false,
    spinProfilingEnabled: false,
  },
  soundPolicy: {
    soundModeByDefault: "on",
    showToggle: true,
    masterVolume: 0.8,
    bgmVolume: 0.7,
    sfxVolume: 0.8,
  },
  localizationPolicy: {
    defaultLanguage: "en",
    localizedTitleKey: "game.title",
    localizedTitle: "",
    showMissingLocalizationError: false,
    contentPath: "./locales",
    customTranslationsEnabled: false,
    serverNotificationsEnabled: false,
  },
  historyPolicy: {
    enabled: true,
    url: "/history",
    openInSameWindow: true,
  },
  walletDisplayPolicy: {
    showBalance: true,
    showCurrencyCode: true,
    showDelayedIndicator: true,
    delayedWalletMessages: false,
  },
  featurePolicy: {
    autoplay: true,
    buyFeature: false,
    buyFeatureForCashBonus: false,
    buyFeatureDisabledForCashBonus: true,
    freeSpins: true,
    respin: false,
    holdAndWin: false,
    inGameHistory: true,
    holidayMode: false,
    customSkins: false,
    frb: false,
    ofrb: false,
    jackpotHooksEnabled: false,
  },
  sessionUiPolicy: {
    showSessionTimer: false,
    showRealityCheckBanner: true,
    closeButtonPolicy: "confirm",
  },
  capabilities: DefaultCapabilityMatrix,
};

// Backward-compatible aliases used by existing imports.
export const ResolvedConfigSchema = ResolvedRuntimeConfigSchema;
export type ResolvedConfig = ResolvedRuntimeConfig;
export const LayerConfigSchema = LayerRuntimeConfigSchema;
export type LayerConfig = LayerRuntimeConfig;
export const LaunchParamsSchema = LaunchBootstrapSchema;
export type LaunchParams = LaunchBootstrapValues;
export const DefaultResolvedConfig = DefaultResolvedRuntimeConfig;

export type { CapabilityMatrix, CapabilityMatrixPatch };
