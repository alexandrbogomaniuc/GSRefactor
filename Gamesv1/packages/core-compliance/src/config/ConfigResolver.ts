import {
  ConfigResolverInputSchema,
  DefaultResolvedConfig,
  LayerConfigSchema,
  ResolvedConfigSchema,
} from "./RuntimeConfigSchema.ts";
import type {
  ConfigResolverInput,
  LayerConfig,
  ResolvedConfig,
} from "./RuntimeConfigSchema.ts";

export type ConfigLayerName =
  | "templateDefaults"
  | "bankProperties"
  | "gameOverrides"
  | `currencyOverrides.${string}`
  | "launchParams";

export interface ConfigDiffEntry {
  layer: ConfigLayerName;
  key: string;
  previous: unknown;
  next: unknown;
}

export interface ResolveConfigResult {
  config: ResolvedConfig;
  diffLog: ConfigDiffEntry[];
}

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

const applyLayer = (
  current: ResolvedConfig,
  patch: LayerConfig,
  layer: ConfigLayerName,
  diffLog: ConfigDiffEntry[],
): ResolvedConfig => {
  const parsedPatch = LayerConfigSchema.parse(patch);
  const merged = deepMerge(current as unknown as Record<string, unknown>, parsedPatch);

  for (const [key] of flattenLeafEntries(parsedPatch)) {
    const previous = getByPath(current as unknown as Record<string, unknown>, key);
    const next = getByPath(merged, key);

    if (valuesDiffer(previous, next)) {
      diffLog.push({ layer, key, previous, next });
    }
  }

  return merged as unknown as ResolvedConfig;
};

const formatDiffLog = (diffLog: ConfigDiffEntry[]): string => {
  const lines = ["[ConfigResolver] Layer overrides (dev mode):"];

  for (const entry of diffLog) {
    lines.push(
      `- ${entry.layer} -> ${entry.key}: ${JSON.stringify(entry.previous)} => ${JSON.stringify(entry.next)}`,
    );
  }

  return lines.join("\n");
};

export const resolveConfigWithMetadata = (
  input: ConfigResolverInput,
): ResolveConfigResult => {
  const parsedInput = ConfigResolverInputSchema.parse(input);
  const diffLog: ConfigDiffEntry[] = [];

  let resolved = clone(DefaultResolvedConfig);

  resolved = applyLayer(resolved, parsedInput.templateDefaults, "templateDefaults", diffLog);
  resolved = applyLayer(resolved, parsedInput.bankProperties, "bankProperties", diffLog);
  resolved = applyLayer(resolved, parsedInput.gameOverrides, "gameOverrides", diffLog);

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
    );
  }

  resolved = applyLayer(resolved, parsedInput.launchParams, "launchParams", diffLog);

  if (!resolved.turboplay.allowed && resolved.turboplay.preferred) {
    diffLog.push({
      layer: "launchParams",
      key: "turboplay.preferred",
      previous: true,
      next: false,
    });
    resolved.turboplay.preferred = false;
  }

  const config = ResolvedConfigSchema.parse(resolved);
  return { config, diffLog };
};

export const resolveConfig = (input: ConfigResolverInput): ResolvedConfig => {
  const { config, diffLog } = resolveConfigWithMetadata(input);
  const devMode = Boolean(input.devMode ?? input.launchParams.devMode);

  if (devMode && diffLog.length > 0) {
    console.info(formatDiffLog(diffLog));
  }

  return config;
};

export class ConfigResolver {
  public static resolve(input: ConfigResolverInput): ResolvedConfig {
    return resolveConfig(input);
  }

  public static resolveWithMetadata(input: ConfigResolverInput): ResolveConfigResult {
    return resolveConfigWithMetadata(input);
  }
}

export type { ConfigResolverInput };
