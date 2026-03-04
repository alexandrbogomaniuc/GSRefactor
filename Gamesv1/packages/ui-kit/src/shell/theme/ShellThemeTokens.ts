import type { ResolvedConfig } from "@gamesv1/core-compliance";

import type { PremiumHudControlId } from "../../hud/PremiumTemplateHud.ts";
import type { AudioCueRegistry } from "../vfx/AudioCueRegistry.ts";
import type { PresentationWinTier } from "../vfx/WinPresentationTiers.ts";
import type { RoundActionBuilderConfig } from "../actions/RoundActionBuilder.ts";
import type { WinTargetLayoutConstraints } from "../presentation/WinTargetResolver.ts";

export type VfxIntensity = "low" | "normal" | "high";

export interface ShellThemeTokens {
  metadata: {
    themeId: string;
    skinId: string;
  };
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
  roundActions: RoundActionBuilderConfig;
  winTargets: WinTargetLayoutConstraints;
}

export interface ShellThemeTokensPatch {
  metadata?: Partial<ShellThemeTokens["metadata"]>;
  hud?: Partial<ShellThemeTokens["hud"]>;
  winPresentation?: {
    tierLabels?: Partial<ShellThemeTokens["winPresentation"]["tierLabels"]>;
    tierStyleHooks?: Partial<ShellThemeTokens["winPresentation"]["tierStyleHooks"]>;
  };
  audio?: {
    cueOverrides?: Partial<AudioCueRegistry>;
    themedCueOverrides?: Record<string, Partial<AudioCueRegistry>>;
  };
  vfx?: Partial<ShellThemeTokens["vfx"]>;
  roundActions?: RoundActionBuilderConfig;
  winTargets?: Partial<WinTargetLayoutConstraints>;
}

export interface ResolveShellThemeTokensOptions {
  runtimeConfig?: ResolvedConfig;
  queryParams?: URLSearchParams;
  overrides?: ShellThemeTokensPatch;
}

const DEFAULT_THEME_TOKENS: ShellThemeTokens = {
  metadata: {
    themeId: "premium-default",
    skinId: "base",
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
  roundActions: {
    bet: {
      lineCount: 20,
      multiplier: 1,
      minCoinValueMinor: 1,
    },
    buyFeature: {
      featureType: "BUY_FEATURE",
      action: "CONFIRM",
      // Intentionally neutral by default; GS or config should provide pricing.
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

const clamp01 = (value: number): number => {
  if (!Number.isFinite(value)) {
    return 1;
  }

  if (value < 0) return 0;
  if (value > 1) return 1;
  return value;
};

const parseIntensity = (value: string | null | undefined): VfxIntensity | undefined => {
  if (!value) return undefined;
  if (value === "low" || value === "normal" || value === "high") {
    return value;
  }
  return undefined;
};

const parseAccentColor = (value: string | null | undefined): number | undefined => {
  if (!value) return undefined;
  const normalized = value.startsWith("#") ? value.slice(1) : value;
  const parsed = Number.parseInt(normalized, 16);
  if (!Number.isFinite(parsed)) {
    return undefined;
  }
  return parsed;
};

const deepMergeThemeTokens = (
  base: ShellThemeTokens,
  overrides: ShellThemeTokensPatch = {},
): ShellThemeTokens => ({
  metadata: {
    ...base.metadata,
    ...(overrides.metadata ?? {}),
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
      themeId: runtimeConfig.featurePolicy.holidayMode ? "holiday" : "premium-default",
      skinId: runtimeConfig.featurePolicy.customSkins ? "custom" : "base",
    },
    vfx: {
      intensity: runtimeConfig.animationPolicy.lowPerformanceMode ? "low" : "normal",
      heavyFxEnabled: !runtimeConfig.animationPolicy.lowPerformanceMode,
      coinBurstEnabled: !runtimeConfig.animationPolicy.lowPerformanceMode,
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
    panelAlphaRaw !== null ? clamp01(Number.parseFloat(panelAlphaRaw)) : undefined;
  const metricAccentColor = parseAccentColor(queryParams.get("hudMetricAccent"));

  return {
    metadata: {
      ...(themeId ? { themeId } : {}),
      ...(skinId ? { skinId } : {}),
    },
    hud: {
      ...(panelAlphaParsed !== undefined ? { panelAlpha: panelAlphaParsed } : {}),
      ...(metricAccentColor !== undefined ? { metricAccentColor } : {}),
    },
    vfx: {
      ...(vfxIntensity ? { intensity: vfxIntensity } : {}),
    },
  };
};

export const resolveShellThemeTokens = (
  options: ResolveShellThemeTokensOptions = {},
): ShellThemeTokens => {
  const runtimeResolved = resolveFromRuntimeConfig(options.runtimeConfig);
  const queryResolved = resolveFromQuery(options.queryParams);
  const merged = deepMergeThemeTokens(
    deepMergeThemeTokens(DEFAULT_THEME_TOKENS, runtimeResolved),
    queryResolved,
  );
  return deepMergeThemeTokens(merged, options.overrides);
};
