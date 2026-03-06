import { z } from "zod";

import type { ResolvedConfig } from "@gamesv1/core-compliance";

import type { PremiumHudControlId } from "../../hud/PremiumTemplateHud.ts";
import type { AudioCueRegistry } from "../vfx/AudioCueRegistry.ts";
import type { PresentationWinTier } from "../vfx/WinPresentationTiers.ts";
import type { RoundActionBuilderConfig } from "../actions/RoundActionBuilder.ts";
import type { WinTargetLayoutConstraints } from "../presentation/WinTargetResolver.ts";

export type VfxIntensity = "low" | "normal" | "high";
export type PreloaderStyle = "wow" | "minimal";
export type PreloaderHeroFx = "energyRing" | "coinVortex" | "slotSweep";

export interface ShellBrandTokens {
  displayName: string;
  logoAssetKey?: string;
  logoUrl?: string;
  primaryColor: string;
  accentColor: string;
}

export interface ShellPreloaderTokens {
  style: PreloaderStyle;
  heroFx: PreloaderHeroFx;
  vfxIntensity: number;
  audioStingerCue?: string;
}

export interface ShellThemeTokens {
  metadata: {
    themeId: string;
    skinId: string;
  };
  brand: ShellBrandTokens;
  hud: {
    visualStyle: string;
    panelAlpha: number;
    metricAccentColor: number;
    controlSkinHooks: Partial<Record<PremiumHudControlId, string>>;
  };
  winPresentation: {
    tierLabels: Partial<Record<PresentationWinTier, string>>;
    tierStyleHooks: Partial<Record<PresentationWinTier, string>>;
  };
  audio: {
    cueOverrides: Partial<AudioCueRegistry>;
    themedCueOverrides: Record<string, Partial<AudioCueRegistry>>;
  };
  vfx: {
    intensity: VfxIntensity;
    heavyFxEnabled: boolean;
    coinBurstEnabled: boolean;
  };
  preloader: ShellPreloaderTokens;
  roundActions: RoundActionBuilderConfig;
  winTargets: WinTargetLayoutConstraints;
}

export interface ShellThemeTokensPatch {
  metadata?: Partial<ShellThemeTokens["metadata"]>;
  brand?: Partial<ShellThemeTokens["brand"]>;
  hud?: Partial<ShellThemeTokens["hud"]>;
  winPresentation?: {
    tierLabels?: Partial<ShellThemeTokens["winPresentation"]["tierLabels"]>;
    tierStyleHooks?: Partial<
      ShellThemeTokens["winPresentation"]["tierStyleHooks"]
    >;
  };
  audio?: {
    cueOverrides?: Partial<AudioCueRegistry>;
    themedCueOverrides?: Record<string, Partial<AudioCueRegistry>>;
  };
  vfx?: Partial<ShellThemeTokens["vfx"]>;
  preloader?: Partial<ShellThemeTokens["preloader"]>;
  roundActions?: RoundActionBuilderConfig;
  winTargets?: Partial<WinTargetLayoutConstraints>;
}

export interface ResolveShellThemeTokensOptions {
  runtimeConfig?: ResolvedConfig;
  queryParams?: URLSearchParams;
  overrides?: ShellThemeTokensPatch;
}

const HexColorSchema = z
  .string()
  .trim()
  .regex(
    /^#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$/,
    "Expected #RRGGBB or #RRGGBBAA",
  );

const ShellBrandTokensSchema = z.object({
  displayName: z.string().trim().min(1).max(48),
  logoAssetKey: z.string().trim().min(1).optional(),
  logoUrl: z.string().trim().min(1).optional(),
  primaryColor: HexColorSchema,
  accentColor: HexColorSchema,
});

const ShellPreloaderTokensSchema = z.object({
  style: z.enum(["wow", "minimal"]),
  heroFx: z.enum(["energyRing", "coinVortex", "slotSweep"]),
  vfxIntensity: z.number().min(0).max(1),
  audioStingerCue: z.string().trim().min(1).optional(),
});

export const DEFAULT_THEME_TOKENS: ShellThemeTokens = {
  metadata: {
    themeId: "premium-default",
    skinId: "base",
  },
  brand: {
    displayName: "Premium Slots",
    primaryColor: "#E7BC56",
    accentColor: "#FF2F7B",
  },
  hud: {
    visualStyle: "premium-default",
    panelAlpha: 1,
    metricAccentColor: 0xf7f2da,
    controlSkinHooks: {},
  },
  winPresentation: {
    tierLabels: {},
    tierStyleHooks: {},
  },
  audio: {
    cueOverrides: {},
    themedCueOverrides: {},
  },
  vfx: {
    intensity: "normal",
    heavyFxEnabled: true,
    coinBurstEnabled: true,
  },
  preloader: {
    style: "wow",
    heroFx: "energyRing",
    vfxIntensity: 0.82,
  },
  roundActions: {
    bet: {
      lineCount: 20,
      multiplier: 1,
      minCoinValueMinor: 1,
    },
    buyFeature: {
      featureType: "BUY_FEATURE",
      action: "CONFIRM",
      priceMinor: 0,
      payloadDefaults: {},
    },
  },
  winTargets: {
    reelCount: 5,
    rowCount: 3,
    highlightRowIndex: 1,
  },
};

export const DefaultShellThemeTokens = DEFAULT_THEME_TOKENS;

const clamp01 = (value: number): number => {
  if (!Number.isFinite(value)) {
    return 1;
  }

  if (value < 0) return 0;
  if (value > 1) return 1;
  return value;
};

const parseIntensity = (
  value: string | null | undefined,
): VfxIntensity | undefined => {
  if (!value) {
    return undefined;
  }

  if (value === "low" || value === "normal" || value === "high") {
    return value;
  }

  return undefined;
};

const parseAccentColor = (
  value: string | null | undefined,
): number | undefined => {
  if (!value) {
    return undefined;
  }

  const normalized = value.startsWith("#") ? value.slice(1) : value;
  const parsed = Number.parseInt(normalized, 16);
  if (!Number.isFinite(parsed)) {
    return undefined;
  }

  return parsed;
};

const parsePreloaderStyle = (
  value: string | null | undefined,
): ShellPreloaderTokens["style"] | undefined => {
  if (value === "wow" || value === "minimal") {
    return value;
  }

  return undefined;
};

const parsePreloaderHeroFx = (
  value: string | null | undefined,
): ShellPreloaderTokens["heroFx"] | undefined => {
  if (
    value === "energyRing" ||
    value === "coinVortex" ||
    value === "slotSweep"
  ) {
    return value;
  }

  return undefined;
};

const parsePreloaderIntensity = (
  value: string | null | undefined,
): number | undefined => {
  if (value === null || value === undefined || value === "") {
    return undefined;
  }

  const parsed = Number.parseFloat(value);
  return Number.isFinite(parsed) ? clamp01(parsed) : undefined;
};

const deepMergeThemeTokens = (
  base: ShellThemeTokens,
  overrides: ShellThemeTokensPatch = {},
): ShellThemeTokens => ({
  metadata: {
    ...base.metadata,
    ...(overrides.metadata ?? {}),
  },
  brand: {
    ...base.brand,
    ...(overrides.brand ?? {}),
  },
  hud: {
    ...base.hud,
    ...(overrides.hud ?? {}),
    controlSkinHooks: {
      ...base.hud.controlSkinHooks,
      ...(overrides.hud?.controlSkinHooks ?? {}),
    },
  },
  winPresentation: {
    ...base.winPresentation,
    ...(overrides.winPresentation ?? {}),
    tierLabels: {
      ...base.winPresentation.tierLabels,
      ...(overrides.winPresentation?.tierLabels ?? {}),
    },
    tierStyleHooks: {
      ...base.winPresentation.tierStyleHooks,
      ...(overrides.winPresentation?.tierStyleHooks ?? {}),
    },
  },
  audio: {
    ...base.audio,
    ...(overrides.audio ?? {}),
    cueOverrides: {
      ...base.audio.cueOverrides,
      ...(overrides.audio?.cueOverrides ?? {}),
    },
    themedCueOverrides: {
      ...base.audio.themedCueOverrides,
      ...(overrides.audio?.themedCueOverrides ?? {}),
    },
  },
  vfx: {
    ...base.vfx,
    ...(overrides.vfx ?? {}),
  },
  preloader: {
    ...base.preloader,
    ...(overrides.preloader ?? {}),
  },
  roundActions: {
    ...base.roundActions,
    ...(overrides.roundActions ?? {}),
    bet: {
      ...(base.roundActions.bet ?? {}),
      ...(overrides.roundActions?.bet ?? {}),
    },
    buyFeature: {
      ...(base.roundActions.buyFeature ?? {}),
      ...(overrides.roundActions?.buyFeature ?? {}),
    },
  },
  winTargets: {
    ...base.winTargets,
    ...(overrides.winTargets ?? {}),
  },
});

const resolveFromRuntimeConfig = (
  runtimeConfig: ResolvedConfig | undefined,
): ShellThemeTokensPatch => {
  if (!runtimeConfig) {
    return {};
  }

  return {
    metadata: {
      themeId: runtimeConfig.featurePolicy.holidayMode
        ? "holiday"
        : "premium-default",
      skinId: runtimeConfig.featurePolicy.customSkins ? "custom" : "base",
    },
    vfx: {
      intensity: runtimeConfig.animationPolicy.lowPerformanceMode
        ? "low"
        : "normal",
      heavyFxEnabled: !runtimeConfig.animationPolicy.lowPerformanceMode,
      coinBurstEnabled: !runtimeConfig.animationPolicy.lowPerformanceMode,
    },
    preloader: {
      style: runtimeConfig.animationPolicy.lowPerformanceMode
        ? "minimal"
        : "wow",
      vfxIntensity: runtimeConfig.animationPolicy.lowPerformanceMode
        ? 0.2
        : 0.82,
    },
  };
};

const resolveFromQuery = (
  queryParams: URLSearchParams | undefined,
): ShellThemeTokensPatch => {
  if (!queryParams) {
    return {};
  }

  const themeId = queryParams.get("theme") ?? undefined;
  const skinId = queryParams.get("skin") ?? undefined;
  const vfxIntensity = parseIntensity(queryParams.get("vfxIntensity"));
  const panelAlphaRaw = queryParams.get("hudPanelAlpha");
  const panelAlphaParsed =
    panelAlphaRaw !== null
      ? clamp01(Number.parseFloat(panelAlphaRaw))
      : undefined;
  const metricAccentColor = parseAccentColor(
    queryParams.get("hudMetricAccent"),
  );
  const displayName = queryParams.get("brandName")?.trim();
  const logoUrl = queryParams.get("brandLogoUrl")?.trim();
  const primaryColor = queryParams.get("brandPrimaryColor")?.trim();
  const accentColor = queryParams.get("brandAccentColor")?.trim();
  const preloaderStyle = parsePreloaderStyle(queryParams.get("preloaderStyle"));
  const preloaderHeroFx = parsePreloaderHeroFx(
    queryParams.get("preloaderHeroFx"),
  );
  const preloaderIntensity = parsePreloaderIntensity(
    queryParams.get("preloaderVfxIntensity"),
  );
  const preloaderAudioStingerCue = queryParams
    .get("preloaderAudioStingerCue")
    ?.trim();

  return {
    metadata: {
      ...(themeId ? { themeId } : {}),
      ...(skinId ? { skinId } : {}),
    },
    brand: {
      ...(displayName ? { displayName } : {}),
      ...(logoUrl ? { logoUrl } : {}),
      ...(primaryColor ? { primaryColor } : {}),
      ...(accentColor ? { accentColor } : {}),
    },
    hud: {
      ...(panelAlphaParsed !== undefined
        ? { panelAlpha: panelAlphaParsed }
        : {}),
      ...(metricAccentColor !== undefined ? { metricAccentColor } : {}),
    },
    vfx: {
      ...(vfxIntensity ? { intensity: vfxIntensity } : {}),
    },
    preloader: {
      ...(preloaderStyle ? { style: preloaderStyle } : {}),
      ...(preloaderHeroFx ? { heroFx: preloaderHeroFx } : {}),
      ...(preloaderIntensity !== undefined
        ? { vfxIntensity: preloaderIntensity }
        : {}),
      ...(preloaderAudioStingerCue
        ? { audioStingerCue: preloaderAudioStingerCue }
        : {}),
    },
  };
};

const validateThemeTokens = (tokens: ShellThemeTokens): ShellThemeTokens => ({
  ...tokens,
  brand: ShellBrandTokensSchema.parse(tokens.brand),
  preloader: ShellPreloaderTokensSchema.parse(tokens.preloader),
});

export const resolveShellThemeTokens = (
  options: ResolveShellThemeTokensOptions = {},
): ShellThemeTokens => {
  const runtimeResolved = resolveFromRuntimeConfig(options.runtimeConfig);
  const queryResolved = resolveFromQuery(options.queryParams);
  const merged = deepMergeThemeTokens(
    deepMergeThemeTokens(DEFAULT_THEME_TOKENS, runtimeResolved),
    queryResolved,
  );

  return validateThemeTokens(deepMergeThemeTokens(merged, options.overrides));
};

export const getBrandMonogram = (displayName: string): string =>
  displayName
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((segment) => segment[0]?.toUpperCase() ?? "")
    .join("") || "GS";
