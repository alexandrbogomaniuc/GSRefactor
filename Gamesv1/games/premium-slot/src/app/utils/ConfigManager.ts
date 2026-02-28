import {
  ConfigResolver,
  CurrencyOverrides,
  LaunchParams,
  LayerConfig,
} from "@gamesv1/core-compliance";
import { RuntimeStore } from "./RuntimeStore";

export class ConfigManager {
  public static async init() {
    const templateDefaults = await this.loadTemplateDefaults();
    const bankProperties = await this.fetchBankProperties();
    const gameOverrides = await this.loadGameOverrides();
    const currencyOverrides = await this.loadCurrencyOverrides();
    const launchParams = this.getLaunchParams();

    const resolved = ConfigResolver.resolve({
      templateDefaults,
      bankProperties,
      gameOverrides,
      currencyOverrides,
      launchParams,
      devMode: import.meta.env.DEV,
    });

    RuntimeStore.set(resolved);
  }

  private static async loadTemplateDefaults(): Promise<LayerConfig> {
    await this.readOptionalGameSettings();

    return {
      currencyCode: "EUR",
      betConfig: {
        mode: "ladder",
        betLadder: [10, 20, 50, 100, 200, 500, 1000],
        coinValues: [0.01, 0.02, 0.05, 0.1],
      },
      minBet: 10,
      maxBet: 1000,
      maxExposure: 100000,
      defaultBet: 100,
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
        showMissingLocalizationError: false,
        contentPath: "./locales",
        customTranslationsEnabled: false,
      },
      capabilities: {
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
          autoplayMinDelayMs: 250,
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
        features: {
          autoplay: true,
          buyFeature: true,
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
      },
      realityCheck: {
        enabled: false,
        intervalMinutes: 60,
      },
    };
  }

  private static async fetchBankProperties(): Promise<LayerConfig> {
    return {
      currencyCode: "EUR",
      minBet: 10,
      maxBet: 1000,
      defaultBet: 100,
      maxExposure: 75000,
      realityCheck: {
        enabled: true,
        intervalMinutes: 30,
      },
    };
  }

  private static async loadGameOverrides(): Promise<LayerConfig> {
    return {
      soundDefaults: {
        masterVolume: 0.75,
      },
      capabilities: {
        walletMessaging: {
          delayedWalletMessages: true,
        },
        spinProfiling: {
          enabled: true,
        },
      },
    };
  }

  private static async loadCurrencyOverrides(): Promise<CurrencyOverrides> {
    return {
      USD: {
        currencyCode: "USD",
        betConfig: {
          mode: "dynamic",
          dynamicBetConstraints: {
            minStep: 50,
            maxStep: 5000,
            step: 50,
          },
        },
        minBet: 50,
        maxBet: 5000,
        defaultBet: 150,
      },
      EUR: {
        currencyCode: "EUR",
        defaultBet: 100,
      },
    };
  }

  private static getLaunchParams(): LaunchParams {
    const urlParams = new URLSearchParams(window.location.search);

    const currencyCode = urlParams.get("currency") ?? undefined;
    const lang = urlParams.get("lang") ?? undefined;
    const devConfig = urlParams.get("devConfig") === "1";

    const launchParams: LaunchParams = {
      devMode: devConfig,
      currencyCode,
      localization: {
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

  private static async readOptionalGameSettings(): Promise<Record<string, unknown> | null> {
    try {
      const response = await fetch("./game.settings.json");
      if (!response.ok) return null;
      return (await response.json()) as Record<string, unknown>;
    } catch {
      return null;
    }
  }
}

