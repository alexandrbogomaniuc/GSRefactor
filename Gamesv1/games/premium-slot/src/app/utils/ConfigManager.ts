import {
  ConfigResolver,
  CurrencyOverrides,
  LaunchParams,
  LayerConfig,
  LayerConfigSchema,
} from "@gamesv1/core-compliance";
import { BootstrapConfigStore } from "../stores/BootstrapConfigStore";
import { ResolvedRuntimeConfigStore } from "../stores/ResolvedRuntimeConfigStore";

export interface ConfigManagerInitOptions {
  runtimeConfigFromGs?: Record<string, unknown>;
  capabilitiesFromGs?: Record<string, unknown>;
  currencyCodeFromGs?: string;
  allowDevFallback?: boolean;
}

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

export class ConfigManager {
  public static async init(options: ConfigManagerInitOptions = {}) {
    const gsRuntimeLayer = this.buildGsRuntimeLayer(options);
    const devFallbackEnabled = this.isDevFallbackEnabled(options.allowDevFallback);

    if (!devFallbackEnabled && Object.keys(gsRuntimeLayer).length === 0) {
      throw new Error(
        "[ConfigManager] GS bootstrap runtimeConfig is required unless explicit dev fallback is enabled.",
      );
    }

    if (devFallbackEnabled && Object.keys(gsRuntimeLayer).length === 0) {
      console.warn(
        "[ConfigManager] Explicit dev fallback active without GS runtime payload. Using local defaults only.",
      );
    }

    const templateDefaults = devFallbackEnabled
      ? await this.loadTemplateDefaults()
      : {};
    const bankProperties = devFallbackEnabled ? await this.fetchBankProperties() : {};
    const gameOverrides = devFallbackEnabled ? await this.loadGameOverrides() : {};
    const currencyOverrides = devFallbackEnabled ? await this.loadCurrencyOverrides() : {};
    const launchParams = this.getLaunchParams(gsRuntimeLayer, devFallbackEnabled);

    const resolved = ConfigResolver.resolve({
      templateDefaults,
      bankProperties,
      gameOverrides,
      currencyOverrides,
      launchParams,
      devMode: import.meta.env.DEV,
    });

    ResolvedRuntimeConfigStore.set(resolved);
  }

  private static isDevFallbackEnabled(explicit?: boolean): boolean {
    if (explicit !== undefined) return explicit;
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get("devConfig") === "1" || urlParams.get("allowDevFallback") === "1") {
      return true;
    }
    return import.meta.env.DEV && import.meta.env.VITE_ALLOW_DEV_CONFIG_FALLBACK === "1";
  }

  private static async loadTemplateDefaults(): Promise<LayerConfig> {
    const gameSettings = await this.readOptionalGameSettings();

    const template: LayerConfig = {
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
        localizedTitleKey: "game.title",
        showMissingLocalizationError: false,
        contentPath: "./locales",
        customTranslationsEnabled: false,
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
      capabilities: {
        features: {
          autoplay: true,
          buyFeature: Boolean(gameSettings?.features?.buyFeature),
          buyFeatureForCashBonus: false,
          buyFeatureDisabledForCashBonus: true,
          freeSpins: Boolean(gameSettings?.features?.freeSpins ?? true),
          respin: false,
          holdAndWin: false,
          inGameHistory: true,
          holidayMode: false,
          customSkins: false,
          frb: Boolean(gameSettings?.gs?.isFrb),
          ofrb: Boolean(gameSettings?.gs?.ocb),
          jackpotHooks: false,
        },
      },
      realityCheck: {
        enabled: false,
        intervalMinutes: 60,
      },
    };

    return template;
  }

  private static async fetchBankProperties(): Promise<LayerConfig> {
    // Canonical authority is GS bootstrap payload; local bank defaults remain empty.
    return {};
  }

  private static async loadGameOverrides(): Promise<LayerConfig> {
    // Keep empty by default to avoid hardcoded production authority values.
    return {};
  }

  private static async loadCurrencyOverrides(): Promise<CurrencyOverrides> {
    return {};
  }

  private static buildGsRuntimeLayer(options: ConfigManagerInitOptions): LayerConfig {
    const bootstrapSnapshot = BootstrapConfigStore.getSnapshot();
    const bootstrapRuntime = isRecord(bootstrapSnapshot?.runtime)
      ? bootstrapSnapshot.runtime
      : {};
    const bootstrapPolicies = isRecord(bootstrapSnapshot?.policies)
      ? bootstrapSnapshot.policies
      : {};
    const bootstrapWallet = isRecord(bootstrapRuntime.wallet) ? bootstrapRuntime.wallet : {};

    const runtimeConfigFromBootstrap = isRecord(bootstrapRuntime.runtimeConfig)
      ? bootstrapRuntime.runtimeConfig
      : {};
    const capabilitiesFromBootstrap = isRecord(bootstrapPolicies.capabilities)
      ? bootstrapPolicies.capabilities
      : isRecord(runtimeConfigFromBootstrap.capabilities)
        ? runtimeConfigFromBootstrap.capabilities
        : {};

    const candidate: Record<string, unknown> = {
      ...runtimeConfigFromBootstrap,
      ...(isRecord(options.runtimeConfigFromGs) ? options.runtimeConfigFromGs : {}),
    };

    if (!isRecord(candidate.capabilities) && Object.keys(capabilitiesFromBootstrap).length > 0) {
      candidate.capabilities = capabilitiesFromBootstrap;
    }

    if (isRecord(options.capabilitiesFromGs) && !isRecord(candidate.capabilities)) {
      candidate.capabilities = options.capabilitiesFromGs;
    }

    if (!candidate.currencyCode && typeof bootstrapWallet.currencyCode === "string") {
      candidate.currencyCode = bootstrapWallet.currencyCode;
    }

    if (options.currencyCodeFromGs && !candidate.currencyCode) {
      candidate.currencyCode = options.currencyCodeFromGs;
    }

    const parsed = LayerConfigSchema.safeParse(candidate);
    if (parsed.success) {
      return parsed.data;
    }

    if (import.meta.env.DEV && Object.keys(candidate).length > 0) {
      console.warn("[ConfigManager] Failed to parse GS runtime config, using fallback layer", {
        issues: parsed.error.issues,
      });
    }

    return {};
  }

  private static getLaunchParams(
    gsRuntimeLayer: LayerConfig,
    devFallbackEnabled: boolean,
  ): LaunchParams {
    const launchParams: LaunchParams = {
      devMode: devFallbackEnabled,
      ...gsRuntimeLayer,
      currencyCode: gsRuntimeLayer.currencyCode,
      localization: gsRuntimeLayer.localization
        ? {
            ...gsRuntimeLayer.localization,
          }
        : undefined,
    };

    if (!devFallbackEnabled) {
      return launchParams;
    }

    const urlParams = new URLSearchParams(window.location.search);

    const currencyCode = urlParams.get("currency");
    if (currencyCode) {
      launchParams.currencyCode = currencyCode;
    }

    const lang = urlParams.get("lang");
    if (lang) {
      launchParams.localization = {
        ...launchParams.localization,
        defaultLang: lang,
      };
    }

    const defaultBet = urlParams.get("defaultBet");
    if (defaultBet) {
      launchParams.defaultBet = Number(defaultBet);
    }

    const glDefaultBet = urlParams.get("GL_DEFAULT_BET");
    if (glDefaultBet) {
      launchParams.GL_DEFAULT_BET = Number(glDefaultBet);
    }

    const defCoin = urlParams.get("DEFCOIN");
    if (defCoin) {
      launchParams.DEFCOIN = Number(defCoin);
    }

    return launchParams;
  }

  private static async readOptionalGameSettings(): Promise<Record<string, any> | null> {
    try {
      const response = await fetch("./game.settings.json");
      if (!response.ok) return null;
      return (await response.json()) as Record<string, any>;
    } catch {
      return null;
    }
  }
}
