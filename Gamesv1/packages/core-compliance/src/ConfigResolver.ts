import {
  collectCapabilityWarnings,
  type CapabilityWarning,
} from "./CapabilityMatrix.ts";
import {
  ConfigResolverInputSchema,
  DefaultResolvedRuntimeConfig,
  LayerRuntimeConfigSchema,
  ResolvedRuntimeConfigSchema,
  type ConfigResolverInput,
  type LayerRuntimeConfig,
  type ResolvedRuntimeConfig,
} from "./ResolvedRuntimeConfig.ts";

export type ConfigLayerName =
  | "templateDefaults"
  | "bankProperties"
  | "gameOverrides"
  | `currencyOverrides.${string}`
  | "launchParams"
  | "launchParams.GL_MAX_BET"
  | "launchParams.exposureDerivedMaxBet"
  | "launchParams.maxBetRule"
  | "launchParams.legacy.GL_DEFAULT_BET"
  | "launchParams.legacy.DEFCOIN";

export interface ConfigDiffEntry {
  layer: ConfigLayerName;
  key: string;
  previous: unknown;
  next: unknown;
}

export interface ResolveConfigResult {
  config: ResolvedRuntimeConfig;
  diffLog: ConfigDiffEntry[];
  warnings: CapabilityWarning[];
}

const LAYER_ALLOWED_KEYS = new Set([
  "currencyCode",
  "betConfig",
  "minBet",
  "maxBet",
  "maxExposure",
  "defaultBet",
  "turboplay",
  "minReelSpinTime",
  "soundDefaults",
  "localization",
  "history",
  "walletDisplay",
  "sessionUi",
  "jackpotHooks",
  "runtimePolicies",
  "realityCheck",
  "GL_MAX_BET",
  "exposureDerivedMaxBet",
  "animationPolicy",
  "soundPolicy",
  "localizationPolicy",
  "historyPolicy",
  "walletDisplayPolicy",
  "featurePolicy",
  "sessionUiPolicy",
  "capabilities",
]);

const LAUNCH_ALLOWED_KEYS = new Set([
  ...LAYER_ALLOWED_KEYS,
  "devMode",
  "GL_DEFAULT_BET",
  "DEFCOIN",
  "legacyDefaults",
]);

const isObject = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const clone = <T>(value: T): T => JSON.parse(JSON.stringify(value));

const deepMerge = <T extends Record<string, unknown>>(
  base: T,
  patch: Record<string, unknown>,
): T => {
  const merged: Record<string, unknown> = clone(base);

  for (const [key, value] of Object.entries(patch)) {
    if (value === undefined) continue;

    if (isObject(value) && isObject(merged[key])) {
      merged[key] = deepMerge(
        merged[key] as Record<string, unknown>,
        value as Record<string, unknown>,
      );
      continue;
    }

    merged[key] = clone(value);
  }

  return merged as T;
};

const flattenLeafEntries = (
  input: Record<string, unknown>,
  prefix = "",
): Array<[string, unknown]> => {
  const entries: Array<[string, unknown]> = [];

  for (const [key, value] of Object.entries(input)) {
    if (value === undefined) continue;
    const fullKey = prefix ? `${prefix}.${key}` : key;

    if (isObject(value) && Object.keys(value).length > 0) {
      entries.push(...flattenLeafEntries(value, fullKey));
      continue;
    }

    entries.push([fullKey, value]);
  }

  return entries;
};

const getByPath = (input: Record<string, unknown>, path: string): unknown =>
  path
    .split(".")
    .reduce<unknown>((acc, key) => (isObject(acc) ? acc[key] : undefined), input);

const valuesDiffer = (a: unknown, b: unknown): boolean =>
  JSON.stringify(a) !== JSON.stringify(b);

const collectUnsupportedKeys = (
  layer: string,
  input: unknown,
  allowedKeys: Set<string>,
): CapabilityWarning[] => {
  if (!isObject(input)) return [];

  const warnings: CapabilityWarning[] = [];

  for (const key of Object.keys(input)) {
    if (!allowedKeys.has(key)) {
      warnings.push({
        layer,
        key,
        message: "unsupported config key",
      });
    }
  }

  return warnings;
};

const parseBooleanLike = (value: unknown): boolean | undefined => {
  if (typeof value === "boolean") return value;
  if (typeof value === "number") {
    if (value === 1) return true;
    if (value === 0) return false;
  }
  if (typeof value === "string") {
    const normalized = value.trim().toLowerCase();
    if (["1", "true", "yes", "on", "enabled"].includes(normalized)) return true;
    if (["0", "false", "no", "off", "disabled"].includes(normalized)) return false;
  }
  return undefined;
};

const setNestedValue = (
  target: Record<string, unknown>,
  path: string,
  value: unknown,
): void => {
  const keys = path.split(".");
  let cursor: Record<string, unknown> = target;

  for (let i = 0; i < keys.length - 1; i += 1) {
    const key = keys[i];
    const existing = cursor[key];
    if (!isObject(existing)) {
      cursor[key] = {};
    }
    cursor = cursor[key] as Record<string, unknown>;
  }

  cursor[keys[keys.length - 1]] = value;
};

const normalizeLegacyAliases = (
  layer: ConfigLayerName,
  input: Record<string, unknown>,
  warnings: CapabilityWarning[],
): Record<string, unknown> => {
  const normalized = clone(input) as Record<string, unknown>;

  const mapBooleanAlias = (
    aliasKey: string,
    canonicalPaths: string[],
    warningMessage: string,
  ): void => {
    if (!(aliasKey in normalized)) return;

    const parsed = parseBooleanLike(normalized[aliasKey]);
    delete normalized[aliasKey];

    if (parsed === undefined) {
      warnings.push({
        layer,
        key: aliasKey,
        message: `${warningMessage} (invalid boolean-like value)`,
      });
      return;
    }

    for (const canonicalPath of canonicalPaths) {
      setNestedValue(normalized, canonicalPath, parsed);
    }

    warnings.push({
      layer,
      key: aliasKey,
      message: `${warningMessage} (mapped to canonical path)`,
    });
  };

  if ("content_path" in normalized && typeof normalized.content_path === "string") {
    const contentPath = normalized.content_path;
    delete normalized.content_path;
    setNestedValue(normalized, "localization.contentPath", contentPath);
    setNestedValue(normalized, "localizationPolicy.contentPath", contentPath);
    warnings.push({
      layer,
      key: "content_path",
      message: "legacy alias mapped to localization.contentPath",
    });
  }

  mapBooleanAlias("USE_JP_NOTIFICATION", [
    "localization.serverNotificationsEnabled",
    "localizationPolicy.serverNotificationsEnabled",
  ], "legacy alias for server notifications");

  mapBooleanAlias("spinProfilingEnabled", [
    "animationPolicy.spinProfilingEnabled",
    "capabilities.spinProfiling.enabled",
  ], "legacy alias for spin profiling");

  mapBooleanAlias("PRECSPINSTAT", [
    "animationPolicy.spinProfilingEnabled",
    "capabilities.spinProfiling.enabled",
  ], "legacy alias for spin profiling");

  mapBooleanAlias("delayedWalletMessages", [
    "walletDisplayPolicy.delayedWalletMessages",
    "capabilities.walletMessaging.delayedWalletMessages",
  ], "legacy alias for delayed wallet messages");

  mapBooleanAlias("BUY_FEATURE_DISABLED_FOR_CASH_BONUS", [
    "featurePolicy.buyFeatureDisabledForCashBonus",
    "capabilities.features.buyFeatureDisabledForCashBonus",
  ], "legacy alias for buy feature cash bonus disable policy");

  mapBooleanAlias("FRB", [
    "featurePolicy.frb",
    "capabilities.features.frb",
  ], "legacy alias for FRB");

  mapBooleanAlias("OFRB", [
    "featurePolicy.ofrb",
    "capabilities.features.ofrb",
  ], "legacy alias for OFRB");

  mapBooleanAlias("jackpotHooksEnabled", [
    "featurePolicy.jackpotHooksEnabled",
    "capabilities.features.jackpotHooks",
    "jackpotHooks.enabled",
    "capabilities.jackpotHooks.enabled",
  ], "legacy alias for jackpot hooks");

  return normalized;
};

const normalizeResolverAliases = (
  input: ConfigResolverInput,
  warnings: CapabilityWarning[],
): ConfigResolverInput => {
  const normalized = clone(input) as ConfigResolverInput;

  if (isObject(normalized.templateDefaults)) {
    normalized.templateDefaults = normalizeLegacyAliases(
      "templateDefaults",
      normalized.templateDefaults,
      warnings,
    ) as LayerRuntimeConfig;
  }

  if (isObject(normalized.bankProperties)) {
    normalized.bankProperties = normalizeLegacyAliases(
      "bankProperties",
      normalized.bankProperties,
      warnings,
    ) as LayerRuntimeConfig;
  }

  if (isObject(normalized.gameOverrides)) {
    normalized.gameOverrides = normalizeLegacyAliases(
      "gameOverrides",
      normalized.gameOverrides,
      warnings,
    ) as LayerRuntimeConfig;
  }

  if (isObject(normalized.currencyOverrides)) {
    const updatedOverrides: Record<string, unknown> = {};
    for (const [currencyCode, override] of Object.entries(normalized.currencyOverrides)) {
      if (isObject(override)) {
        updatedOverrides[currencyCode] = normalizeLegacyAliases(
          `currencyOverrides.${currencyCode}`,
          override,
          warnings,
        );
      } else {
        updatedOverrides[currencyCode] = override;
      }
    }
    normalized.currencyOverrides = updatedOverrides as ConfigResolverInput["currencyOverrides"];
  }

  if (isObject(normalized.launchParams)) {
    normalized.launchParams = normalizeLegacyAliases(
      "launchParams",
      normalized.launchParams,
      warnings,
    ) as ConfigResolverInput["launchParams"];
  }

  return normalized;
};

const syncPolicyGroups = (config: ResolvedRuntimeConfig): ResolvedRuntimeConfig => {
  const next = clone(config);

  next.animationPolicy = {
    forcedSpinStopEnabled: next.capabilities.animationPolicy.forcedSpinStopAllowed,
    forcedSkipWinPresentation: next.capabilities.animationPolicy.forcedSkipWinPresentation,
    minReelSpinTimeMs: {
      normal: next.minReelSpinTime.normalMs,
      turbo: next.minReelSpinTime.turboMs,
    },
    autoplayMinDelayMs: next.capabilities.animationPolicy.autoplayMinDelayMs,
    lowPerformanceMode: next.capabilities.animationPolicy.lowPerformanceMode,
    spinProfilingEnabled: next.capabilities.spinProfiling.enabled,
  };

  next.soundPolicy = {
    soundModeByDefault: next.soundDefaults.modeByDefault,
    showToggle: next.capabilities.sound.showToggle,
    masterVolume: next.soundDefaults.masterVolume,
    bgmVolume: next.soundDefaults.bgmVolume,
    sfxVolume: next.soundDefaults.sfxVolume,
  };

  next.localizationPolicy = {
    defaultLanguage: next.localization.defaultLang,
    localizedTitleKey: next.localization.localizedTitleKey,
    localizedTitle: next.localization.localizedTitle,
    showMissingLocalizationError: next.localization.showMissingLocalizationError,
    contentPath: next.localization.contentPath,
    customTranslationsEnabled: next.localization.customTranslationsEnabled,
    serverNotificationsEnabled: next.localization.serverNotificationsEnabled,
  };

  next.historyPolicy = {
    enabled: next.history.enabled,
    url: next.history.url,
    openInSameWindow: next.history.openInSameWindow,
  };

  next.walletDisplayPolicy = {
    showBalance: next.walletDisplay.showBalance,
    showCurrencyCode: next.walletDisplay.showCurrencyCode,
    showDelayedIndicator: next.walletDisplay.showDelayedIndicator,
    delayedWalletMessages: next.capabilities.walletMessaging.delayedWalletMessages,
  };

  next.featurePolicy = {
    autoplay: next.capabilities.features.autoplay,
    buyFeature: next.capabilities.features.buyFeature,
    buyFeatureForCashBonus: next.capabilities.features.buyFeatureForCashBonus,
    buyFeatureDisabledForCashBonus: next.capabilities.features.buyFeatureDisabledForCashBonus,
    freeSpins: next.capabilities.features.freeSpins,
    respin: next.capabilities.features.respin,
    holdAndWin: next.capabilities.features.holdAndWin,
    inGameHistory: next.capabilities.features.inGameHistory,
    holidayMode: next.capabilities.features.holidayMode,
    customSkins: next.capabilities.features.customSkins,
    frb: next.capabilities.features.frb,
    ofrb: next.capabilities.features.ofrb,
    jackpotHooksEnabled:
      next.capabilities.features.jackpotHooks || next.capabilities.jackpotHooks.enabled,
  };

  next.sessionUiPolicy = {
    showSessionTimer: next.sessionUi.showSessionTimer,
    showRealityCheckBanner: next.sessionUi.showRealityCheckBanner,
    closeButtonPolicy: next.sessionUi.closeButtonPolicy,
  };

  return next;
};

const harmonizeRuntimeAndCapabilities = (
  config: ResolvedRuntimeConfig,
  patch: LayerRuntimeConfig,
): ResolvedRuntimeConfig => {
  const next = clone(config);

  if (patch.animationPolicy) {
    const animation = next.animationPolicy;
    next.capabilities.animationPolicy.forcedSpinStopAllowed =
      animation.forcedSpinStopEnabled;
    next.capabilities.animationPolicy.forcedSkipWinPresentation =
      animation.forcedSkipWinPresentation;
    next.capabilities.animationPolicy.minReelSpinTimeMs.normal =
      animation.minReelSpinTimeMs.normal;
    next.capabilities.animationPolicy.minReelSpinTimeMs.turbo =
      animation.minReelSpinTimeMs.turbo;
    next.capabilities.animationPolicy.autoplayMinDelayMs = animation.autoplayMinDelayMs;
    next.capabilities.animationPolicy.lowPerformanceMode = animation.lowPerformanceMode;
    next.capabilities.spinProfiling.enabled = animation.spinProfilingEnabled;
    next.minReelSpinTime.normalMs = animation.minReelSpinTimeMs.normal;
    next.minReelSpinTime.turboMs = animation.minReelSpinTimeMs.turbo;
  }

  if (patch.soundPolicy) {
    const sound = next.soundPolicy;
    next.soundDefaults.modeByDefault = sound.soundModeByDefault;
    next.soundDefaults.enabled = sound.soundModeByDefault !== "off";
    next.soundDefaults.masterVolume = sound.masterVolume;
    next.soundDefaults.bgmVolume = sound.bgmVolume;
    next.soundDefaults.sfxVolume = sound.sfxVolume;
    next.capabilities.sound.modeByDefault = sound.soundModeByDefault;
    next.capabilities.sound.enabledByDefault = sound.soundModeByDefault !== "off";
    next.capabilities.sound.showToggle = sound.showToggle;
    next.capabilities.sound.masterVolume = sound.masterVolume;
    next.capabilities.sound.bgmVolume = sound.bgmVolume;
    next.capabilities.sound.sfxVolume = sound.sfxVolume;
  }

  if (patch.localizationPolicy) {
    const localization = next.localizationPolicy;
    next.localization.defaultLang = localization.defaultLanguage;
    next.localization.localizedTitleKey = localization.localizedTitleKey;
    next.localization.localizedTitle = localization.localizedTitle;
    next.localization.showMissingLocalizationError =
      localization.showMissingLocalizationError;
    next.localization.contentPath = localization.contentPath;
    next.localization.customTranslationsEnabled =
      localization.customTranslationsEnabled;
    next.localization.serverNotificationsEnabled =
      localization.serverNotificationsEnabled;
    next.capabilities.localization.defaultLanguage = localization.defaultLanguage;
    next.capabilities.localization.localizedTitleKey = localization.localizedTitleKey;
    next.capabilities.localization.localizedTitle = localization.localizedTitle;
    next.capabilities.localization.showMissingLocalizationError =
      localization.showMissingLocalizationError;
    next.capabilities.localization.contentPath = localization.contentPath;
    next.capabilities.localization.customTranslationsEnabled =
      localization.customTranslationsEnabled;
    next.capabilities.localization.serverNotificationsEnabled =
      localization.serverNotificationsEnabled;
  }

  if (patch.historyPolicy) {
    const history = next.historyPolicy;
    next.history.enabled = history.enabled;
    next.history.url = history.url;
    next.history.openInSameWindow = history.openInSameWindow;
    next.capabilities.history.enabled = history.enabled;
    next.capabilities.history.url = history.url;
    next.capabilities.history.openInSameWindow = history.openInSameWindow;
  }

  if (patch.walletDisplayPolicy) {
    const walletDisplayPolicy = next.walletDisplayPolicy;
    next.walletDisplay.showBalance = walletDisplayPolicy.showBalance;
    next.walletDisplay.showCurrencyCode = walletDisplayPolicy.showCurrencyCode;
    next.walletDisplay.showDelayedIndicator = walletDisplayPolicy.showDelayedIndicator;
    next.capabilities.walletDisplay.showBalance = walletDisplayPolicy.showBalance;
    next.capabilities.walletDisplay.showCurrencyCode = walletDisplayPolicy.showCurrencyCode;
    next.capabilities.walletDisplay.showDelayedIndicator =
      walletDisplayPolicy.showDelayedIndicator;
    next.capabilities.walletMessaging.delayedWalletMessages =
      walletDisplayPolicy.delayedWalletMessages;
  }

  if (patch.capabilities?.walletMessaging) {
    next.walletDisplayPolicy.delayedWalletMessages =
      next.capabilities.walletMessaging.delayedWalletMessages;
  }

  if (patch.featurePolicy) {
    const featurePolicy = next.featurePolicy;
    next.capabilities.features.autoplay = featurePolicy.autoplay;
    next.capabilities.features.buyFeature = featurePolicy.buyFeature;
    next.capabilities.features.buyFeatureForCashBonus =
      featurePolicy.buyFeatureForCashBonus;
    next.capabilities.features.buyFeatureDisabledForCashBonus =
      featurePolicy.buyFeatureDisabledForCashBonus;
    next.capabilities.features.freeSpins = featurePolicy.freeSpins;
    next.capabilities.features.respin = featurePolicy.respin;
    next.capabilities.features.holdAndWin = featurePolicy.holdAndWin;
    next.capabilities.features.inGameHistory = featurePolicy.inGameHistory;
    next.capabilities.features.holidayMode = featurePolicy.holidayMode;
    next.capabilities.features.customSkins = featurePolicy.customSkins;
    next.capabilities.features.frb = featurePolicy.frb;
    next.capabilities.features.ofrb = featurePolicy.ofrb;
    next.capabilities.features.jackpotHooks = featurePolicy.jackpotHooksEnabled;
    next.capabilities.jackpotHooks.enabled = featurePolicy.jackpotHooksEnabled;
    if (featurePolicy.jackpotHooksEnabled && next.capabilities.jackpotHooks.source === "none") {
      next.capabilities.jackpotHooks.source = "gs";
    }
    next.jackpotHooks.enabled = next.capabilities.jackpotHooks.enabled;
    next.jackpotHooks.source = next.capabilities.jackpotHooks.source;
  }

  if (patch.sessionUiPolicy) {
    const sessionUiPolicy = next.sessionUiPolicy;
    next.sessionUi.showSessionTimer = sessionUiPolicy.showSessionTimer;
    next.sessionUi.showRealityCheckBanner = sessionUiPolicy.showRealityCheckBanner;
    next.sessionUi.closeButtonPolicy = sessionUiPolicy.closeButtonPolicy;
    next.capabilities.sessionUi.showSessionTimer = sessionUiPolicy.showSessionTimer;
    next.capabilities.sessionUi.showRealityCheckBanner =
      sessionUiPolicy.showRealityCheckBanner;
    next.capabilities.sessionUi.closeButtonPolicy = sessionUiPolicy.closeButtonPolicy;
  }

  if (patch.turboplay) {
    next.capabilities.turbo.allowed = next.turboplay.allowed;
    next.capabilities.turbo.speedId = next.turboplay.speedId;
    next.capabilities.turbo.preferred = next.turboplay.preferred;
  } else if (patch.capabilities?.turbo) {
    next.turboplay.allowed = next.capabilities.turbo.allowed;
    next.turboplay.speedId = next.capabilities.turbo.speedId;
    next.turboplay.preferred = next.capabilities.turbo.preferred;
  }

  if (patch.minReelSpinTime) {
    next.capabilities.animationPolicy.minReelSpinTimeMs.normal = next.minReelSpinTime.normalMs;
    next.capabilities.animationPolicy.minReelSpinTimeMs.turbo = next.minReelSpinTime.turboMs;
  } else if (patch.capabilities?.animationPolicy?.minReelSpinTimeMs) {
    next.minReelSpinTime.normalMs = next.capabilities.animationPolicy.minReelSpinTimeMs.normal;
    next.minReelSpinTime.turboMs = next.capabilities.animationPolicy.minReelSpinTimeMs.turbo;
  }

  if (patch.soundDefaults) {
    next.capabilities.sound.enabledByDefault = next.soundDefaults.enabled;
    next.capabilities.sound.modeByDefault = next.soundDefaults.modeByDefault;
    next.capabilities.sound.masterVolume = next.soundDefaults.masterVolume;
    next.capabilities.sound.bgmVolume = next.soundDefaults.bgmVolume;
    next.capabilities.sound.sfxVolume = next.soundDefaults.sfxVolume;
  } else if (patch.capabilities?.sound) {
    next.soundDefaults.enabled = next.capabilities.sound.enabledByDefault;
    next.soundDefaults.modeByDefault = next.capabilities.sound.modeByDefault;
    next.soundDefaults.masterVolume = next.capabilities.sound.masterVolume;
    next.soundDefaults.bgmVolume = next.capabilities.sound.bgmVolume;
    next.soundDefaults.sfxVolume = next.capabilities.sound.sfxVolume;
  }

  if (patch.localization) {
    next.capabilities.localization.defaultLanguage = next.localization.defaultLang;
    next.capabilities.localization.localizedTitleKey = next.localization.localizedTitleKey;
    next.capabilities.localization.localizedTitle = next.localization.localizedTitle;
    next.capabilities.localization.showMissingLocalizationError =
      next.localization.showMissingLocalizationError;
    next.capabilities.localization.contentPath = next.localization.contentPath;
    next.capabilities.localization.customTranslationsEnabled =
      next.localization.customTranslationsEnabled;
    next.capabilities.localization.serverNotificationsEnabled =
      next.localization.serverNotificationsEnabled;
  } else if (patch.capabilities?.localization) {
    next.localization.defaultLang = next.capabilities.localization.defaultLanguage;
    next.localization.localizedTitleKey = next.capabilities.localization.localizedTitleKey;
    next.localization.localizedTitle = next.capabilities.localization.localizedTitle;
    next.localization.showMissingLocalizationError =
      next.capabilities.localization.showMissingLocalizationError;
    next.localization.contentPath = next.capabilities.localization.contentPath;
    next.localization.customTranslationsEnabled =
      next.capabilities.localization.customTranslationsEnabled;
    next.localization.serverNotificationsEnabled =
      next.capabilities.localization.serverNotificationsEnabled;
  }

  if (patch.history) {
    next.capabilities.history.enabled = next.history.enabled;
    next.capabilities.history.url = next.history.url;
    next.capabilities.history.openInSameWindow = next.history.openInSameWindow;
  } else if (patch.capabilities?.history) {
    next.history.enabled = next.capabilities.history.enabled;
    next.history.url = next.capabilities.history.url;
    next.history.openInSameWindow = next.capabilities.history.openInSameWindow;
  }

  if (patch.walletDisplay) {
    next.capabilities.walletDisplay.showBalance = next.walletDisplay.showBalance;
    next.capabilities.walletDisplay.showCurrencyCode = next.walletDisplay.showCurrencyCode;
    next.capabilities.walletDisplay.showDelayedIndicator =
      next.walletDisplay.showDelayedIndicator;
  } else if (patch.capabilities?.walletDisplay) {
    next.walletDisplay.showBalance = next.capabilities.walletDisplay.showBalance;
    next.walletDisplay.showCurrencyCode = next.capabilities.walletDisplay.showCurrencyCode;
    next.walletDisplay.showDelayedIndicator =
      next.capabilities.walletDisplay.showDelayedIndicator;
  }

  if (patch.sessionUi) {
    next.capabilities.sessionUi.showSessionTimer = next.sessionUi.showSessionTimer;
    next.capabilities.sessionUi.showRealityCheckBanner = next.sessionUi.showRealityCheckBanner;
    next.capabilities.sessionUi.closeButtonPolicy = next.sessionUi.closeButtonPolicy;
  } else if (patch.capabilities?.sessionUi) {
    next.sessionUi.showSessionTimer = next.capabilities.sessionUi.showSessionTimer;
    next.sessionUi.showRealityCheckBanner = next.capabilities.sessionUi.showRealityCheckBanner;
    next.sessionUi.closeButtonPolicy = next.capabilities.sessionUi.closeButtonPolicy;
  }

  if (patch.jackpotHooks) {
    next.capabilities.jackpotHooks.enabled = next.jackpotHooks.enabled;
    next.capabilities.jackpotHooks.source = next.jackpotHooks.source;
  } else if (patch.capabilities?.jackpotHooks) {
    next.jackpotHooks.enabled = next.capabilities.jackpotHooks.enabled;
    next.jackpotHooks.source = next.capabilities.jackpotHooks.source;
  }

  if (patch.runtimePolicies) {
    next.capabilities.runtimePolicies.requestCounterRequired =
      next.runtimePolicies.requestCounterRequired;
    next.capabilities.runtimePolicies.idempotencyKeyRequired =
      next.runtimePolicies.idempotencyKeyRequired;
    next.capabilities.runtimePolicies.clientOperationIdRequired =
      next.runtimePolicies.clientOperationIdRequired;
    next.capabilities.runtimePolicies.currentStateVersionSupported =
      next.runtimePolicies.currentStateVersionSupported;
    next.capabilities.runtimePolicies.unfinishedRoundRestoreSupported =
      next.runtimePolicies.unfinishedRoundRestoreSupported;
  } else if (patch.capabilities?.runtimePolicies) {
    next.runtimePolicies.requestCounterRequired =
      next.capabilities.runtimePolicies.requestCounterRequired;
    next.runtimePolicies.idempotencyKeyRequired =
      next.capabilities.runtimePolicies.idempotencyKeyRequired;
    next.runtimePolicies.clientOperationIdRequired =
      next.capabilities.runtimePolicies.clientOperationIdRequired;
    next.runtimePolicies.currentStateVersionSupported =
      next.capabilities.runtimePolicies.currentStateVersionSupported;
    next.runtimePolicies.unfinishedRoundRestoreSupported =
      next.capabilities.runtimePolicies.unfinishedRoundRestoreSupported;
  }

  if (patch.capabilities?.spinProfiling) {
    next.animationPolicy.spinProfilingEnabled = next.capabilities.spinProfiling.enabled;
  }

  if (patch.capabilities?.features) {
    const features = next.capabilities.features;

    if (features.buyFeatureForCashBonus) {
      features.buyFeatureDisabledForCashBonus = false;
    }
    if (features.buyFeatureDisabledForCashBonus) {
      features.buyFeatureForCashBonus = false;
    }

    if (features.jackpotHooks) {
      next.capabilities.jackpotHooks.enabled = true;
      if (next.capabilities.jackpotHooks.source === "none") {
        next.capabilities.jackpotHooks.source = "gs";
      }
      next.jackpotHooks.enabled = true;
      next.jackpotHooks.source = next.capabilities.jackpotHooks.source;
    }
  }

  return next;
};

const applyLayer = (
  current: ResolvedRuntimeConfig,
  rawPatch: unknown,
  layer: ConfigLayerName,
  diffLog: ConfigDiffEntry[],
  warnings: CapabilityWarning[],
): ResolvedRuntimeConfig => {
  if (!isObject(rawPatch)) {
    return current;
  }

  const normalizedPatch = normalizeLegacyAliases(layer, rawPatch, warnings);

  warnings.push(...collectUnsupportedKeys(layer, normalizedPatch, LAYER_ALLOWED_KEYS));
  warnings.push(...collectCapabilityWarnings(layer, normalizedPatch.capabilities));

  const parsedPatch = LayerRuntimeConfigSchema.parse(normalizedPatch);
  const merged = deepMerge(current as unknown as Record<string, unknown>, parsedPatch);
  const harmonized = harmonizeRuntimeAndCapabilities(
    merged as unknown as ResolvedRuntimeConfig,
    parsedPatch,
  );

  for (const [key] of flattenLeafEntries(parsedPatch)) {
    const previous = getByPath(current as unknown as Record<string, unknown>, key);
    const next = getByPath(harmonized as unknown as Record<string, unknown>, key);

    if (valuesDiffer(previous, next)) {
      diffLog.push({ layer, key, previous, next });
    }
  }

  return harmonized;
};

const formatDiffLog = (diffLog: ConfigDiffEntry[], warnings: CapabilityWarning[]): string => {
  const lines = ["[ConfigResolver] Layer overrides (dev mode):"];

  if (diffLog.length === 0) {
    lines.push("- No layer overrides detected.");
  } else {
    for (const entry of diffLog) {
      lines.push(
        `- ${entry.layer} -> ${entry.key}: ${JSON.stringify(entry.previous)} => ${JSON.stringify(entry.next)}`,
      );
    }
  }

  if (warnings.length > 0) {
    lines.push("[ConfigResolver] Capability/config warnings:");
    for (const warning of warnings) {
      lines.push(`- ${warning.layer} -> ${warning.key}: ${warning.message}`);
    }
  }

  return lines.join("\n");
};

const applyLegacyDefaultBetFallback = (
  resolved: ResolvedRuntimeConfig,
  input: ConfigResolverInput,
  diffLog: ConfigDiffEntry[],
  warnings: CapabilityWarning[],
): ResolvedRuntimeConfig => {
  const launch = input.launchParams;
  if (launch.defaultBet !== undefined) {
    return resolved;
  }

  const glDefaultBet = launch.GL_DEFAULT_BET ?? launch.legacyDefaults?.GL_DEFAULT_BET;
  if (glDefaultBet !== undefined && valuesDiffer(resolved.defaultBet, glDefaultBet)) {
    diffLog.push({
      layer: "launchParams.legacy.GL_DEFAULT_BET",
      key: "defaultBet",
      previous: resolved.defaultBet,
      next: glDefaultBet,
    });
    warnings.push({
      layer: "launchParams",
      key: "GL_DEFAULT_BET",
      message: "legacy fallback applied to defaultBet",
    });
    return { ...resolved, defaultBet: glDefaultBet };
  }

  const defCoin = launch.DEFCOIN ?? launch.legacyDefaults?.DEFCOIN;
  if (defCoin !== undefined && valuesDiffer(resolved.defaultBet, defCoin)) {
    diffLog.push({
      layer: "launchParams.legacy.DEFCOIN",
      key: "defaultBet",
      previous: resolved.defaultBet,
      next: defCoin,
    });
    warnings.push({
      layer: "launchParams",
      key: "DEFCOIN",
      message: "legacy DEFCOIN fallback applied to defaultBet",
    });
    return { ...resolved, defaultBet: defCoin };
  }

  return resolved;
};

const applyFinalMaxBetResolution = (
  resolved: ResolvedRuntimeConfig,
  input: ConfigResolverInput,
  diffLog: ConfigDiffEntry[],
  warnings: CapabilityWarning[],
): ResolvedRuntimeConfig => {
  const launch = input.launchParams;
  const glMaxBet = launch.GL_MAX_BET ?? resolved.GL_MAX_BET ?? resolved.maxBet;
  const exposureDerivedMaxBet =
    launch.exposureDerivedMaxBet ?? resolved.exposureDerivedMaxBet ?? resolved.maxExposure;

  let next = {
    ...resolved,
    GL_MAX_BET: glMaxBet,
    exposureDerivedMaxBet,
  };

  if (valuesDiffer(resolved.GL_MAX_BET, glMaxBet)) {
    diffLog.push({
      layer: "launchParams.GL_MAX_BET",
      key: "GL_MAX_BET",
      previous: resolved.GL_MAX_BET,
      next: glMaxBet,
    });
  }

  if (valuesDiffer(resolved.exposureDerivedMaxBet, exposureDerivedMaxBet)) {
    diffLog.push({
      layer: "launchParams.exposureDerivedMaxBet",
      key: "exposureDerivedMaxBet",
      previous: resolved.exposureDerivedMaxBet,
      next: exposureDerivedMaxBet,
    });
  }

  const finalMaxBet = Math.min(glMaxBet, exposureDerivedMaxBet);
  if (valuesDiffer(next.maxBet, finalMaxBet)) {
    diffLog.push({
      layer: "launchParams.maxBetRule",
      key: "maxBet",
      previous: next.maxBet,
      next: finalMaxBet,
    });
    warnings.push({
      layer: "launchParams",
      key: "maxBet",
      message: "resolved maxBet with min(GL_MAX_BET, exposureDerivedMaxBet)",
    });
    next = { ...next, maxBet: finalMaxBet };
  }

  if (next.defaultBet > next.maxBet) {
    diffLog.push({
      layer: "launchParams.maxBetRule",
      key: "defaultBet",
      previous: next.defaultBet,
      next: next.maxBet,
    });
    warnings.push({
      layer: "launchParams",
      key: "defaultBet",
      message: "defaultBet clamped to final maxBet",
    });
    next = { ...next, defaultBet: next.maxBet };
  }

  return syncPolicyGroups(next);
};

export const resolveConfigWithMetadata = (
  input: ConfigResolverInput,
): ResolveConfigResult => {
  const warnings: CapabilityWarning[] = [];
  const normalizedInput = normalizeResolverAliases(input, warnings);
  const parsedInput = ConfigResolverInputSchema.parse(normalizedInput);
  const diffLog: ConfigDiffEntry[] = [];

  warnings.push(
    ...collectUnsupportedKeys("launchParams", normalizedInput.launchParams, LAUNCH_ALLOWED_KEYS),
  );
  warnings.push(
    ...collectCapabilityWarnings("launchParams", normalizedInput.launchParams?.capabilities),
  );

  let resolved = clone(DefaultResolvedRuntimeConfig);

  resolved = applyLayer(resolved, parsedInput.templateDefaults, "templateDefaults", diffLog, warnings);
  resolved = applyLayer(resolved, parsedInput.bankProperties, "bankProperties", diffLog, warnings);
  resolved = applyLayer(resolved, parsedInput.gameOverrides, "gameOverrides", diffLog, warnings);

  const selectedCurrencyCode =
    parsedInput.launchParams.currencyCode ??
    parsedInput.bankProperties.currencyCode ??
    parsedInput.templateDefaults.currencyCode ??
    resolved.currencyCode;

  const selectedCurrencyOverride = parsedInput.currencyOverrides[selectedCurrencyCode];
  if (selectedCurrencyOverride) {
    resolved = applyLayer(
      resolved,
      selectedCurrencyOverride,
      `currencyOverrides.${selectedCurrencyCode}`,
      diffLog,
      warnings,
    );
  } else {
    warnings.push({
      layer: "currencyOverrides",
      key: selectedCurrencyCode,
      message: "no currency override found; using previously resolved values",
    });
  }

  resolved = applyLayer(resolved, parsedInput.launchParams, "launchParams", diffLog, warnings);
  resolved = applyLegacyDefaultBetFallback(resolved, parsedInput, diffLog, warnings);
  resolved = applyFinalMaxBetResolution(resolved, parsedInput, diffLog, warnings);

  if (!resolved.turboplay.allowed && resolved.turboplay.preferred) {
    diffLog.push({
      layer: "launchParams",
      key: "turboplay.preferred",
      previous: true,
      next: false,
    });
    resolved.turboplay.preferred = false;
    resolved.capabilities.turbo.preferred = false;
  }

  const config = ResolvedRuntimeConfigSchema.parse(resolved);
  return { config, diffLog, warnings };
};

export const resolveConfig = (input: ConfigResolverInput): ResolvedRuntimeConfig => {
  const { config, diffLog, warnings } = resolveConfigWithMetadata(input);
  const devMode = Boolean(input.devMode ?? input.launchParams.devMode);

  if (devMode) {
    console.info(formatDiffLog(diffLog, warnings));
  }

  return config;
};

export class ConfigResolver {
  public static resolve(input: ConfigResolverInput): ResolvedRuntimeConfig {
    return resolveConfig(input);
  }

  public static resolveWithMetadata(input: ConfigResolverInput): ResolveConfigResult {
    return resolveConfigWithMetadata(input);
  }
}

export type { ConfigResolverInput };
