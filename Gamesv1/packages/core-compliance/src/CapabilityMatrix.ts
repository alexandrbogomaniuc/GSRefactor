import { z } from "zod";

const Volume = z.number().min(0).max(1);

export const TurboCapabilitySchema = z.object({
  allowed: z.boolean(),
  speedId: z.string().min(1),
  preferred: z.boolean(),
});

export const AnimationPolicyCapabilitySchema = z
  .object({
    forcedSpinStopAllowed: z.boolean(),
    forcedSkipWinPresentation: z.boolean(),
    minReelSpinTimeMs: z.object({
      normal: z.number().int().nonnegative(),
      turbo: z.number().int().nonnegative(),
    }),
    autoplayMinDelayMs: z.number().int().nonnegative(),
    lowPerformanceMode: z.boolean(),
  })
  .superRefine((value, ctx) => {
    if (value.minReelSpinTimeMs.turbo > value.minReelSpinTimeMs.normal) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["minReelSpinTimeMs", "turbo"],
        message: "turbo min reel spin time cannot exceed normal min reel spin time",
      });
    }
  });

export const SoundCapabilitySchema = z.object({
  enabledByDefault: z.boolean(),
  modeByDefault: z.enum(["on", "off", "muted"]),
  showToggle: z.boolean(),
  masterVolume: Volume,
  bgmVolume: Volume,
  sfxVolume: Volume,
});

export const LocalizationCapabilitySchema = z.object({
  defaultLanguage: z.string().min(2),
  localizedTitleKey: z.string().min(1),
  localizedTitle: z.string().optional(),
  showMissingLocalizationError: z.boolean(),
  contentPath: z.string().min(1),
  customTranslationsEnabled: z.boolean(),
  serverNotificationsEnabled: z.boolean(),
});

export const SpinProfilingCapabilitySchema = z.object({
  enabled: z.boolean(),
  payloadKey: z.literal("PRECSPINSTAT"),
});

export const WalletMessagingCapabilitySchema = z.object({
  delayedWalletMessages: z.boolean(),
  externalWalletMessages: z.boolean(),
});

export const WalletDisplayPolicyCapabilitySchema = z.object({
  showBalance: z.boolean(),
  showCurrencyCode: z.boolean(),
  showDelayedIndicator: z.boolean(),
});

export const HistoryCapabilitySchema = z.object({
  enabled: z.boolean(),
  url: z.string().min(1),
  openInSameWindow: z.boolean(),
});

export const RuntimePoliciesCapabilitySchema = z.object({
  requestCounterRequired: z.boolean(),
  idempotencyKeyRequired: z.boolean(),
  clientOperationIdRequired: z.boolean(),
  currentStateVersionSupported: z.boolean(),
  unfinishedRoundRestoreSupported: z.boolean(),
});

export const SessionUiPolicyCapabilitySchema = z.object({
  showSessionTimer: z.boolean(),
  showRealityCheckBanner: z.boolean(),
  closeButtonPolicy: z.enum(["allow", "confirm", "hidden"]),
});

export const JackpotHooksCapabilitySchema = z.object({
  enabled: z.boolean(),
  source: z.enum(["none", "gs"]),
});

export const FeatureFlagsCapabilitySchema = z.object({
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
  jackpotHooks: z.boolean(),
});

export const BigWinFlowCapabilitySchema = z
  .object({
    enabled: z.boolean(),
    allowSkipPresentation: z.boolean(),
    thresholds: z.object({
      bigMultiplier: z.number().nonnegative(),
      hugeMultiplier: z.number().nonnegative(),
      megaMultiplier: z.number().nonnegative(),
    }),
  })
  .superRefine((value, ctx) => {
    if (value.thresholds.bigMultiplier > value.thresholds.hugeMultiplier) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["thresholds", "bigMultiplier"],
        message: "bigMultiplier cannot exceed hugeMultiplier",
      });
    }

    if (value.thresholds.hugeMultiplier > value.thresholds.megaMultiplier) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ["thresholds", "hugeMultiplier"],
        message: "hugeMultiplier cannot exceed megaMultiplier",
      });
    }
  });

export const CapabilityMatrixSchema = z.object({
  turbo: TurboCapabilitySchema,
  animationPolicy: AnimationPolicyCapabilitySchema,
  sound: SoundCapabilitySchema,
  localization: LocalizationCapabilitySchema,
  spinProfiling: SpinProfilingCapabilitySchema,
  walletMessaging: WalletMessagingCapabilitySchema,
  walletDisplay: WalletDisplayPolicyCapabilitySchema,
  history: HistoryCapabilitySchema,
  runtimePolicies: RuntimePoliciesCapabilitySchema,
  sessionUi: SessionUiPolicyCapabilitySchema,
  jackpotHooks: JackpotHooksCapabilitySchema,
  features: FeatureFlagsCapabilitySchema,
  bigWinFlow: BigWinFlowCapabilitySchema,
});

export type CapabilityMatrix = z.infer<typeof CapabilityMatrixSchema>;

export const CapabilityMatrixPatchSchema = z.object({
  turbo: TurboCapabilitySchema.partial().optional(),
  animationPolicy: z
    .object({
      forcedSpinStopAllowed: z.boolean().optional(),
      forcedSkipWinPresentation: z.boolean().optional(),
      minReelSpinTimeMs: z
        .object({
          normal: z.number().int().nonnegative().optional(),
          turbo: z.number().int().nonnegative().optional(),
        })
        .optional(),
      autoplayMinDelayMs: z.number().int().nonnegative().optional(),
      lowPerformanceMode: z.boolean().optional(),
    })
    .optional(),
  sound: SoundCapabilitySchema.partial().optional(),
  localization: LocalizationCapabilitySchema.partial().optional(),
  spinProfiling: SpinProfilingCapabilitySchema.partial().optional(),
  walletMessaging: WalletMessagingCapabilitySchema.partial().optional(),
  walletDisplay: WalletDisplayPolicyCapabilitySchema.partial().optional(),
  history: HistoryCapabilitySchema.partial().optional(),
  runtimePolicies: RuntimePoliciesCapabilitySchema.partial().optional(),
  sessionUi: SessionUiPolicyCapabilitySchema.partial().optional(),
  jackpotHooks: JackpotHooksCapabilitySchema.partial().optional(),
  features: FeatureFlagsCapabilitySchema.partial().optional(),
  bigWinFlow: z
    .object({
      enabled: z.boolean().optional(),
      allowSkipPresentation: z.boolean().optional(),
      thresholds: z
        .object({
          bigMultiplier: z.number().nonnegative().optional(),
          hugeMultiplier: z.number().nonnegative().optional(),
          megaMultiplier: z.number().nonnegative().optional(),
        })
        .optional(),
    })
    .optional(),
});

export type CapabilityMatrixPatch = z.infer<typeof CapabilityMatrixPatchSchema>;

export const DefaultCapabilityMatrix: CapabilityMatrix = {
  turbo: {
    allowed: true,
    speedId: "turbo-x2",
    preferred: false,
  },
  animationPolicy: {
    forcedSpinStopAllowed: true,
    forcedSkipWinPresentation: true,
    minReelSpinTimeMs: {
      normal: 2000,
      turbo: 1200,
    },
    autoplayMinDelayMs: 300,
    lowPerformanceMode: false,
  },
  sound: {
    enabledByDefault: true,
    modeByDefault: "on",
    showToggle: true,
    masterVolume: 0.8,
    bgmVolume: 0.7,
    sfxVolume: 0.8,
  },
  localization: {
    defaultLanguage: "en",
    localizedTitleKey: "game.title",
    localizedTitle: "",
    showMissingLocalizationError: false,
    contentPath: "./locales",
    customTranslationsEnabled: false,
    serverNotificationsEnabled: false,
  },
  spinProfiling: {
    enabled: false,
    payloadKey: "PRECSPINSTAT",
  },
  walletMessaging: {
    delayedWalletMessages: false,
    externalWalletMessages: false,
  },
  walletDisplay: {
    showBalance: true,
    showCurrencyCode: true,
    showDelayedIndicator: true,
  },
  history: {
    enabled: true,
    url: "/history",
    openInSameWindow: true,
  },
  runtimePolicies: {
    requestCounterRequired: true,
    idempotencyKeyRequired: true,
    clientOperationIdRequired: true,
    currentStateVersionSupported: true,
    unfinishedRoundRestoreSupported: true,
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
  features: {
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
    jackpotHooks: false,
  },
  bigWinFlow: {
    enabled: true,
    allowSkipPresentation: true,
    thresholds: {
      bigMultiplier: 10,
      hugeMultiplier: 25,
      megaMultiplier: 50,
    },
  },
};

export type CapabilityWarning = {
  layer: string;
  key: string;
  message: string;
};

const CAPABILITY_FAMILY_KEYS: Record<string, readonly string[]> = {
  turbo: ["allowed", "speedId", "preferred"],
  animationPolicy: [
    "forcedSpinStopAllowed",
    "forcedSkipWinPresentation",
    "minReelSpinTimeMs",
    "autoplayMinDelayMs",
    "lowPerformanceMode",
  ],
  sound: [
    "enabledByDefault",
    "modeByDefault",
    "showToggle",
    "masterVolume",
    "bgmVolume",
    "sfxVolume",
  ],
  localization: [
    "defaultLanguage",
    "localizedTitleKey",
    "localizedTitle",
    "showMissingLocalizationError",
    "contentPath",
    "customTranslationsEnabled",
    "serverNotificationsEnabled",
  ],
  spinProfiling: ["enabled", "payloadKey"],
  walletMessaging: ["delayedWalletMessages", "externalWalletMessages"],
  walletDisplay: ["showBalance", "showCurrencyCode", "showDelayedIndicator"],
  history: ["enabled", "url", "openInSameWindow"],
  runtimePolicies: [
    "requestCounterRequired",
    "idempotencyKeyRequired",
    "clientOperationIdRequired",
    "currentStateVersionSupported",
    "unfinishedRoundRestoreSupported",
  ],
  sessionUi: ["showSessionTimer", "showRealityCheckBanner", "closeButtonPolicy"],
  jackpotHooks: ["enabled", "source"],
  features: [
    "autoplay",
    "buyFeature",
    "buyFeatureForCashBonus",
    "buyFeatureDisabledForCashBonus",
    "freeSpins",
    "respin",
    "holdAndWin",
    "inGameHistory",
    "holidayMode",
    "customSkins",
    "frb",
    "ofrb",
    "jackpotHooks",
  ],
  bigWinFlow: ["enabled", "allowSkipPresentation", "thresholds"],
};

const BIG_WIN_THRESHOLD_KEYS = ["bigMultiplier", "hugeMultiplier", "megaMultiplier"] as const;
const MIN_REEL_SPIN_KEYS = ["normal", "turbo"] as const;

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

export const collectCapabilityWarnings = (
  layer: string,
  patch: unknown,
): CapabilityWarning[] => {
  if (!isRecord(patch)) return [];

  const warnings: CapabilityWarning[] = [];

  for (const [family, value] of Object.entries(patch)) {
    if (!(family in CAPABILITY_FAMILY_KEYS)) {
      warnings.push({
        layer,
        key: `capabilities.${family}`,
        message: "unsupported capability family",
      });
      continue;
    }

    if (!isRecord(value)) continue;
    const allowedKeys = new Set(CAPABILITY_FAMILY_KEYS[family]);

    for (const familyKey of Object.keys(value)) {
      if (!allowedKeys.has(familyKey)) {
        warnings.push({
          layer,
          key: `capabilities.${family}.${familyKey}`,
          message: "unsupported capability key",
        });
      }
    }

    if (family === "bigWinFlow" && isRecord(value.thresholds)) {
      for (const thresholdKey of Object.keys(value.thresholds)) {
        if (!BIG_WIN_THRESHOLD_KEYS.includes(thresholdKey as (typeof BIG_WIN_THRESHOLD_KEYS)[number])) {
          warnings.push({
            layer,
            key: `capabilities.bigWinFlow.thresholds.${thresholdKey}`,
            message: "unsupported big win threshold key",
          });
        }
      }
    }

    if (family === "animationPolicy" && isRecord(value.minReelSpinTimeMs)) {
      for (const timeKey of Object.keys(value.minReelSpinTimeMs)) {
        if (!MIN_REEL_SPIN_KEYS.includes(timeKey as (typeof MIN_REEL_SPIN_KEYS)[number])) {
          warnings.push({
            layer,
            key: `capabilities.animationPolicy.minReelSpinTimeMs.${timeKey}`,
            message: "unsupported min reel spin time key",
          });
        }
      }
    }
  }

  return warnings;
};
