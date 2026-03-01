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
  showToggle: z.boolean(),
  masterVolume: Volume,
  bgmVolume: Volume,
  sfxVolume: Volume,
});

export const LocalizationCapabilitySchema = z.object({
  defaultLanguage: z.string().min(2),
  localizedTitleKey: z.string().min(1),
  showMissingLocalizationError: z.boolean(),
  contentPath: z.string().min(1),
  customTranslationsEnabled: z.boolean(),
});

export const SpinProfilingCapabilitySchema = z.object({
  enabled: z.boolean(),
  payloadKey: z.literal("PRECSPINSTAT"),
});

export const WalletMessagingCapabilitySchema = z.object({
  delayedWalletMessages: z.boolean(),
  externalWalletMessages: z.boolean(),
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

export const FeatureFlagsCapabilitySchema = z.object({
  autoplay: z.boolean(),
  buyFeature: z.boolean(),
  buyFeatureForCashBonus: z.boolean(),
  freeSpins: z.boolean(),
  respin: z.boolean(),
  holdAndWin: z.boolean(),
  inGameHistory: z.boolean(),
  holidayMode: z.boolean(),
  customSkins: z.boolean(),
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
  history: HistoryCapabilitySchema,
  runtimePolicies: RuntimePoliciesCapabilitySchema,
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
    })
    .optional(),
  sound: SoundCapabilitySchema.partial().optional(),
  localization: LocalizationCapabilitySchema.partial().optional(),
  spinProfiling: SpinProfilingCapabilitySchema.partial().optional(),
  walletMessaging: WalletMessagingCapabilitySchema.partial().optional(),
  history: HistoryCapabilitySchema.partial().optional(),
  runtimePolicies: RuntimePoliciesCapabilitySchema.partial().optional(),
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
    forcedSpinStopAllowed: false,
    forcedSkipWinPresentation: true,
    minReelSpinTimeMs: {
      normal: 2000,
      turbo: 1200,
    },
    autoplayMinDelayMs: 300,
  },
  sound: {
    enabledByDefault: true,
    showToggle: true,
    masterVolume: 0.8,
    bgmVolume: 0.7,
    sfxVolume: 0.8,
  },
  localization: {
    defaultLanguage: "en",
    localizedTitleKey: "game.title",
    showMissingLocalizationError: false,
    contentPath: "./locales",
    customTranslationsEnabled: false,
  },
  spinProfiling: {
    enabled: false,
    payloadKey: "PRECSPINSTAT",
  },
  walletMessaging: {
    delayedWalletMessages: false,
    externalWalletMessages: false,
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
  features: {
    autoplay: true,
    buyFeature: false,
    buyFeatureForCashBonus: false,
    freeSpins: true,
    respin: false,
    holdAndWin: false,
    inGameHistory: true,
    holidayMode: false,
    customSkins: false,
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
  ],
  sound: ["enabledByDefault", "showToggle", "masterVolume", "bgmVolume", "sfxVolume"],
  localization: [
    "defaultLanguage",
    "localizedTitleKey",
    "showMissingLocalizationError",
    "contentPath",
    "customTranslationsEnabled",
  ],
  spinProfiling: ["enabled", "payloadKey"],
  walletMessaging: ["delayedWalletMessages", "externalWalletMessages"],
  history: ["enabled", "url", "openInSameWindow"],
  runtimePolicies: [
    "requestCounterRequired",
    "idempotencyKeyRequired",
    "clientOperationIdRequired",
    "currentStateVersionSupported",
    "unfinishedRoundRestoreSupported",
  ],
  features: [
    "autoplay",
    "buyFeature",
    "buyFeatureForCashBonus",
    "freeSpins",
    "respin",
    "holdAndWin",
    "inGameHistory",
    "holidayMode",
    "customSkins",
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
