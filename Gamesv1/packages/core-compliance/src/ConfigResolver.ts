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

const harmonizeRuntimeAndCapabilities = (
  config: ResolvedRuntimeConfig,
  patch: LayerRuntimeConfig,
): ResolvedRuntimeConfig => {
  const next = clone(config);

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

  warnings.push(...collectUnsupportedKeys(layer, rawPatch, LAYER_ALLOWED_KEYS));
  warnings.push(...collectCapabilityWarnings(layer, rawPatch.capabilities));

  const parsedPatch = LayerRuntimeConfigSchema.parse(rawPatch);
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

export const resolveConfigWithMetadata = (
  input: ConfigResolverInput,
): ResolveConfigResult => {
  const parsedInput = ConfigResolverInputSchema.parse(input);
  const diffLog: ConfigDiffEntry[] = [];
  const warnings: CapabilityWarning[] = [];

  warnings.push(...collectUnsupportedKeys("launchParams", input.launchParams, LAUNCH_ALLOWED_KEYS));
  warnings.push(...collectCapabilityWarnings("launchParams", input.launchParams?.capabilities));

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
