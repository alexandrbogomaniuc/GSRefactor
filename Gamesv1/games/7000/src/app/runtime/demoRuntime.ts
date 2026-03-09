import type {
  BootstrapResponse,
  FeatureActionResponse,
  HistoryQuery,
  OpenGameResponse,
  PlayRoundResponse,
  SelectedBet,
  SelectedFeatureChoice,
} from "@gamesv1/core-protocol";

import {
  CRAZY_ROOSTER_BET_LIMITS,
  CRAZY_ROOSTER_BIG_WIN_THRESHOLDS,
  CRAZY_ROOSTER_BRAND,
  CRAZY_ROOSTER_FEATURE_FLAGS,
  CRAZY_ROOSTER_IDLE_COLUMNS,
  CRAZY_ROOSTER_LAYOUT,
  CRAZY_ROOSTER_PROVISIONAL_BET_LADDER,
  buildGridFromColumns,
  pickBuyTier,
} from "../../game/config/CrazyRoosterGameConfig";

type DemoScenarioId =
  | "idle"
  | "collect"
  | "boost"
  | "bonus"
  | "big"
  | "mega"
  | "buy-75"
  | "buy-200"
  | "buy-300";

type DemoScenario = {
  roundIdPrefix: string;
  reelStops: number[][];
  winAmountMinor: number;
  messages: string[];
  soundCues: string[];
  animationCues: string[];
  counters: Record<string, unknown>;
  labels: Record<string, string>;
};

type DemoHistoryItem = {
  roundId: string;
  scenario: DemoScenarioId;
  winAmountMinor: number;
};

const clone = <T>(value: T): T => structuredClone(value);

const asBoolLabel = (value: boolean): string => (value ? "true" : "false");

const SCENARIOS: Record<DemoScenarioId, DemoScenario> = {
  idle: {
    roundIdPrefix: "idle",
    reelStops: CRAZY_ROOSTER_IDLE_COLUMNS,
    winAmountMinor: 0,
    messages: ["BETONLINE READY", "8 FIXED LINES LIVE"],
    soundCues: [],
    animationCues: [],
    counters: {
      buyFeatureAvailable: true,
    },
    labels: {
      buyFeatureAvailable: asBoolLabel(true),
    },
  },
  collect: {
    roundIdPrefix: "collect",
    reelStops: [
      [8, 1, 2, 8],
      [2, 8, 4, 6],
      [8, 2, 7, 8],
    ],
    winAmountMinor: 800,
    messages: ["COLLECT FEATURE", "CHICKEN COINS SWEPT"],
    soundCues: ["win-tier-big"],
    animationCues: ["focus-status-banner"],
    counters: {
      buyFeatureAvailable: true,
    },
    labels: {
      buyFeatureAvailable: asBoolLabel(true),
      collectFeatureActive: asBoolLabel(true),
    },
  },
  boost: {
    roundIdPrefix: "boost",
    reelStops: [
      [7, 9, 2, 0],
      [8, 8, 9, 1],
      [7, 9, 8, 2],
    ],
    winAmountMinor: 1200,
    messages: ["LIGHTNING BOOST", "RANDOM BONUS APPLIED"],
    soundCues: ["win-tier-big"],
    animationCues: ["focus-status-banner"],
    counters: {
      buyFeatureAvailable: true,
      jackpotLevel: 2,
    },
    labels: {
      buyFeatureAvailable: asBoolLabel(true),
      boostFeatureActive: asBoolLabel(true),
    },
  },
  bonus: {
    roundIdPrefix: "bonus",
    reelStops: [
      [8, 8, 1, 2],
      [9, 8, 9, 8],
      [8, 8, 2, 3],
    ],
    winAmountMinor: 1800,
    messages: ["BONUS GAME READY", "HOLD & WIN START"],
    soundCues: ["win-tier-big"],
    animationCues: ["hold-and-win-frame"],
    counters: {
      buyFeatureAvailable: false,
      holdAndWinRemaining: 3,
      jackpotLevel: 3,
    },
    labels: {
      holdAndWinActive: asBoolLabel(true),
      buyFeatureAvailable: asBoolLabel(false),
      bonusGameActive: asBoolLabel(true),
    },
  },
  big: {
    roundIdPrefix: "big",
    reelStops: [
      [7, 7, 8, 1],
      [7, 8, 7, 2],
      [7, 7, 8, 3],
    ],
    winAmountMinor: CRAZY_ROOSTER_BIG_WIN_THRESHOLDS.bigMultiplier * 200,
    messages: ["BIG WIN", "ROOSTER RUSH"],
    soundCues: ["win-tier-big"],
    animationCues: ["focus-status-banner"],
    counters: {
      buyFeatureAvailable: true,
    },
    labels: {
      buyFeatureAvailable: asBoolLabel(true),
    },
  },
  mega: {
    roundIdPrefix: "mega",
    reelStops: [
      [9, 7, 8, 7],
      [9, 8, 7, 8],
      [9, 7, 8, 9],
    ],
    winAmountMinor: CRAZY_ROOSTER_BIG_WIN_THRESHOLDS.megaMultiplier * 200,
    messages: ["MEGA WIN", "SUPER CHICKEN STRIKE"],
    soundCues: ["win-tier-mega"],
    animationCues: ["focus-status-banner"],
    counters: {
      buyFeatureAvailable: true,
      jackpotLevel: 4,
    },
    labels: {
      buyFeatureAvailable: asBoolLabel(true),
    },
  },
  "buy-75": {
    roundIdPrefix: "buy75",
    reelStops: [
      [8, 8, 7, 2],
      [9, 8, 9, 7],
      [8, 8, 7, 3],
    ],
    winAmountMinor: 1500,
    messages: ["BUY BONUS 75", "BONUS GAME ENTERED"],
    soundCues: ["win-tier-big"],
    animationCues: ["hold-and-win-frame"],
    counters: {
      buyFeatureAvailable: true,
      holdAndWinRemaining: 3,
    },
    labels: {
      bonusGameActive: asBoolLabel(true),
      holdAndWinActive: asBoolLabel(true),
      buyFeatureAvailable: asBoolLabel(true),
    },
  },
  "buy-200": {
    roundIdPrefix: "buy200",
    reelStops: [
      [8, 9, 7, 8],
      [9, 8, 9, 7],
      [8, 9, 7, 9],
    ],
    winAmountMinor: 3600,
    messages: ["BUY BONUS 200", "BOOSTED BONUS ENTRY"],
    soundCues: ["win-tier-huge"],
    animationCues: ["hold-and-win-frame", "focus-status-banner"],
    counters: {
      buyFeatureAvailable: false,
      holdAndWinRemaining: 2,
      jackpotLevel: 2,
    },
    labels: {
      buyFeatureAvailable: asBoolLabel(false),
      holdAndWinActive: asBoolLabel(true),
      boostFeatureActive: asBoolLabel(true),
    },
  },
  "buy-300": {
    roundIdPrefix: "buy300",
    reelStops: [
      [9, 9, 7, 8],
      [9, 8, 9, 7],
      [9, 9, 7, 8],
    ],
    winAmountMinor: 6000,
    messages: ["BUY BONUS 300", "MEGA BONUS ENTRY"],
    soundCues: ["win-tier-mega"],
    animationCues: ["hold-and-win-frame", "focus-status-banner"],
    counters: {
      buyFeatureAvailable: false,
      holdAndWinRemaining: 1,
      jackpotLevel: 4,
    },
    labels: {
      buyFeatureAvailable: asBoolLabel(false),
      holdAndWinActive: asBoolLabel(true),
      boostFeatureActive: asBoolLabel(true),
      bonusGameActive: asBoolLabel(true),
    },
  },
};

const SEQUENCE: DemoScenarioId[] = ["collect", "boost", "bonus", "big", "idle"];

const buildCapabilities = () => ({
  features: {
    autoplay: CRAZY_ROOSTER_FEATURE_FLAGS.autoplay,
    buyFeature: CRAZY_ROOSTER_FEATURE_FLAGS.buyFeature,
    buyFeatureForCashBonus: false,
    buyFeatureDisabledForCashBonus: false,
    freeSpins: false,
    respin: false,
    holdAndWin: CRAZY_ROOSTER_FEATURE_FLAGS.holdAndWin,
    inGameHistory: true,
    holidayMode: false,
    customSkins: false,
    frb: false,
    ofrb: false,
    jackpotHooks: CRAZY_ROOSTER_FEATURE_FLAGS.jackpotHooks,
  },
  animationPolicy: {
    forcedSkipWinPresentation: false,
    lowPerformanceMode: false,
  },
});

export class CrazyRoosterDemoRuntime {
  private readonly sessionId = "demo-session-7000";
  private requestCounter = 0;
  private stateVersion = 1;
  private balanceMinor = 250_000;
  private roundIndex = 0;
  private history: DemoHistoryItem[] = [];

  private nextRequestId(prefix: string): string {
    this.requestCounter += 1;
    this.stateVersion += 1;
    return `${prefix}-${this.requestCounter}`;
  }

  private currentProofState(): DemoScenarioId {
    const params = new URLSearchParams(window.location.search);
    const requested = params.get("proofState")?.trim().toLowerCase();
    if (requested === "collector") return "collect";
    if (requested === "lightning") return "boost";
    if (requested === "special") return "bonus";
    if (requested === "buy75") return "buy-75";
    if (requested === "buy200") return "buy-200";
    if (requested === "buy300") return "buy-300";
    if (requested && requested in SCENARIOS) {
      return requested as DemoScenarioId;
    }
    return "idle";
  }

  private resolveSpinScenario(): DemoScenarioId {
    const proofState = this.currentProofState();
    if (proofState !== "idle") {
      return proofState;
    }
    const scenario = SEQUENCE[this.roundIndex % SEQUENCE.length];
    this.roundIndex += 1;
    return scenario;
  }

  private envelopeFromScenario(
    scenarioId: DemoScenarioId,
    prefix: string,
    selectedBet: SelectedBet | null,
  ): OpenGameResponse {
    const scenario = SCENARIOS[scenarioId];
    const requestId = this.nextRequestId(prefix);
    const winAmountMinor = scenario.winAmountMinor;
    const totalBetMinor = selectedBet?.totalBetMinor ?? CRAZY_ROOSTER_BET_LIMITS.defaultBet;

    if (prefix !== "opengame" && prefix !== "history") {
      this.balanceMinor = Math.max(0, this.balanceMinor - totalBetMinor + winAmountMinor);
      this.history.unshift({
        roundId: `${scenario.roundIdPrefix}-${this.requestCounter}`,
        scenario: scenarioId,
        winAmountMinor,
      });
      this.history = this.history.slice(0, 20);
    }

    return {
      ok: true,
      requestId,
      sessionId: this.sessionId,
      requestCounter: this.requestCounter,
      stateVersion: this.stateVersion,
      wallet: {
        balanceMinor: this.balanceMinor,
        currencyCode: "USD",
      },
      round: {
        roundId: `${scenario.roundIdPrefix}-${this.requestCounter}`,
        winAmountMinor,
        totalBetMinor,
      },
      feature: {
        history: this.history.map((item) => ({
          roundId: item.roundId,
          featureType: item.scenario,
          winAmountMinor: item.winAmountMinor,
        })),
      },
      presentationPayload: {
        reelStops: clone(scenario.reelStops),
        symbolGrid: buildGridFromColumns(scenario.reelStops),
        uiMessages: clone(scenario.messages),
        audioCues: clone(scenario.soundCues),
        animationCues: clone(scenario.animationCues),
        counters: clone(scenario.counters),
        labels: clone(scenario.labels),
      },
      restore: {
        hasUnfinishedRound: false,
        resumeRef: {
          roundId: `${scenario.roundIdPrefix}-${this.requestCounter}`,
        },
      },
      idempotency: {},
      retry: {},
    };
  }

  public bootstrap(): { bootstrap: BootstrapResponse; opengame: OpenGameResponse } {
    const capabilities = buildCapabilities();
    const bootstrap: BootstrapResponse = {
      contractVersion: "slot-bootstrap-v1",
      session: {
        sessionId: this.sessionId,
        requestCounter: this.requestCounter,
        stateVersion: this.stateVersion,
      },
      context: {
        gameId: 7000,
        displayName: CRAZY_ROOSTER_BRAND.displayName,
      },
      assets: {
        provider: CRAZY_ROOSTER_BRAND.defaultProvider,
      },
      runtime: {
        wallet: {
          currencyCode: "USD",
          balanceMinor: this.balanceMinor,
        },
        runtimeConfig: {
          currencyCode: "USD",
          betConfig: {
            mode: "ladder",
            betLadder: [...CRAZY_ROOSTER_PROVISIONAL_BET_LADDER],
          },
          defaultBet: CRAZY_ROOSTER_BET_LIMITS.defaultBet,
          minBet: CRAZY_ROOSTER_BET_LIMITS.minBet,
          maxBet: CRAZY_ROOSTER_BET_LIMITS.maxBet,
          capabilities,
          localization: {
            defaultLang: "en",
            contentPath: "./locales",
            localizedTitleKey: "game.title",
            showMissingLocalizationError: false,
            customTranslationsEnabled: false,
          },
        },
      },
      policies: {
        capabilities,
      },
      integrity: {
        source: "demo-runtime",
      },
    };

    const opengame = this.envelopeFromScenario("idle", "opengame", {
      coinValueMinor: 1,
      lines: 8,
      multiplier: 1,
      totalBetMinor: CRAZY_ROOSTER_BET_LIMITS.defaultBet,
    });

    return { bootstrap, opengame };
  }

  public playround(selectedBet: SelectedBet): PlayRoundResponse {
    return this.envelopeFromScenario(this.resolveSpinScenario(), "playround", selectedBet);
  }

  public featureaction(
    selectedBet: SelectedBet | null,
    selectedFeatureChoice: SelectedFeatureChoice | null,
  ): FeatureActionResponse {
    const totalBetMinor =
      selectedBet?.totalBetMinor ?? CRAZY_ROOSTER_BET_LIMITS.defaultBet;
    const requestedTier = pickBuyTier(
      selectedFeatureChoice?.priceMinor
        ? Math.round(selectedFeatureChoice.priceMinor / Math.max(totalBetMinor, 1))
        : null,
    );
    const scenarioId =
      requestedTier.priceMultiplier === 300
        ? "buy-300"
        : requestedTier.priceMultiplier === 200
          ? "buy-200"
          : "buy-75";

    return this.envelopeFromScenario(scenarioId, "featureaction", selectedBet);
  }

  public gethistory(_historyQuery?: HistoryQuery): Array<Record<string, unknown>> {
    return this.history.map((item) => ({
      roundId: item.roundId,
      featureType: item.scenario,
      winAmountMinor: item.winAmountMinor,
    }));
  }

  public resumegame(): OpenGameResponse {
    return this.envelopeFromScenario(this.currentProofState(), "resumegame", {
      coinValueMinor: 1,
      lines: CRAZY_ROOSTER_LAYOUT.reelCount,
      multiplier: 1,
      totalBetMinor: CRAZY_ROOSTER_BET_LIMITS.defaultBet,
    });
  }

  public reset(): void {
    this.requestCounter = 0;
    this.stateVersion = 1;
    this.balanceMinor = 250_000;
    this.roundIndex = 0;
    this.history = [];
  }

  public close(): void {
    this.reset();
  }
}

export const crazyRoosterDemoRuntime = new CrazyRoosterDemoRuntime();

export const isDemoRuntimeRequested = (
  params: URLSearchParams = new URLSearchParams(window.location.search),
): boolean =>
  params.get("devConfig") === "1" ||
  params.get("allowDevFallback") === "1" ||
  params.has("proofState") ||
  (import.meta.env.DEV && import.meta.env.VITE_ALLOW_DEV_CONFIG_FALLBACK === "1");
