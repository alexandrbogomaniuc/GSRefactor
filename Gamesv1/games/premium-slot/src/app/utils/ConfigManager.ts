import {
  ConfigResolver,
  CurrencyOverrides,
  LaunchParams,
  LayerConfig,
  LayerConfigSchema,
} from "@gamesv1/core-compliance";
import { ResolvedRuntimeConfigStore } from "../stores/ResolvedRuntimeConfigStore";

export interface ConfigManagerInitOptions {
  runtimeConfigFromGs?: Record<string, unknown>;
  capabilitiesFromGs?: Record<string, unknown>;
  currencyCodeFromGs?: string;
}

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

export class ConfigManager {
  public static async init(options: ConfigManagerInitOptions = {}) {
    const templateDefaults = await this.loadTemplateDefaults();
    const bankProperties = await this.fetchBankProperties();
    const gameOverrides = await this.loadGameOverrides();
    const currencyOverrides = await this.loadCurrencyOverrides();

    const gsRuntimeLayer = this.buildGsRuntimeLayer(options);
    const launchParams = this.getLaunchParams(gsRuntimeLayer);

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
    const candidate: Record<string, unknown> = {
      ...(isRecord(options.runtimeConfigFromGs) ? options.runtimeConfigFromGs : {}),
    };

    if (isRecord(options.capabilitiesFromGs) && !isRecord(candidate.capabilities)) {
      candidate.capabilities = options.capabilitiesFromGs;
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

  private static getLaunchParams(gsRuntimeLayer: LayerConfig): LaunchParams {
    const urlParams = new URLSearchParams(window.location.search);

    const currencyCode = urlParams.get("currency") ?? gsRuntimeLayer.currencyCode ?? undefined;
    const lang = urlParams.get("lang") ?? gsRuntimeLayer.localization?.defaultLang ?? undefined;
    const devConfig = urlParams.get("devConfig") === "1";

    const launchParams: LaunchParams = {
      devMode: devConfig,
      ...gsRuntimeLayer,
      currencyCode,
      localization: {
        ...gsRuntimeLayer.localization,
        defaultLang: lang,
      },
    };

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
