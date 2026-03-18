import { Assets, Container, Sprite, Text, Texture } from "pixi.js";

import {
  AnimationPolicyEngine,
  createAnimationPolicy,
  type ResolvedConfig,
} from "@gamesv1/core-compliance";
import { engine } from "@gamesv1/pixi-engine";
import {
  applyAudioCue,
  createAudioCueRegistry,
  FeatureModuleManager,
  mergePremiumHudVisibility,
  PremiumTemplateHud,
  resolvePremiumHudVisibility,
  RoundActionBuilder,
  SettingsPopup,
  type AudioCueRegistry,
  type FeatureFrame,
  type PremiumHudControlId,
  type PremiumHudFeatureFlags,
  type PremiumHudVisibility,
  type PresentationWinTier,
  type RoundPresentationModel,
  UiAssetKeys,
  WowVfxOrchestrator,
} from "@gamesv1/ui-kit";

import {
  gsRuntimeClient,
  mapPlayRoundToPresentation,
  readMathBridgeHints,
  type MathBridgePresentationHints,
} from "../../runtime";
import {
  PresentationStateStore,
  ResolvedRuntimeConfigStore,
  SessionRuntimeStore,
} from "../../stores";
import { AppAssetKeys } from "../../assets/assetKeys";
import {
  getProviderPackStatus,
  resolveProviderBackgroundUrl,
} from "../../assets/providerPackRegistry.ts";
import { resolveCrazyRoosterBrandKit } from "../../theme/brandKit.ts";
import { userSettings } from "../../utils/userSettings";
import { resolveProviderSymbolRoot } from "../../../game/assets/provider.ts";
import { CrazyRoosterSlotMachine } from "../../../game/slots/CrazyRoosterSlotMachine.ts";
import {
  CRAZY_ROOSTER_BRAND,
  CRAZY_ROOSTER_BUY_TIERS,
  CRAZY_ROOSTER_IDLE_COLUMNS,
  CRAZY_ROOSTER_LAYOUT,
  CRAZY_ROOSTER_PAYLINES,
  buildGridFromColumns,
} from "../../../game/config/CrazyRoosterGameConfig.ts";
import { ParticleBurst } from "../../../game/fx/ParticleBurst";
import {
  PaylineOverlay,
  type PaylineOverlayLine,
  type PaylineOverlayTone,
} from "../../../game/fx/PaylineOverlay";
import { WinHighlight } from "../../../game/fx/WinHighlight";
import { LayeredFxController } from "../../../game/presentation/LayeredFxController";
import { TopperMascotController } from "../../../game/presentation/TopperMascotController";
import { WinCounter } from "../../../game/ui/WinCounter";
import { Beta3VisualChrome } from "./Beta3VisualChrome";
import { DebugOverlay } from "./DebugOverlay";
import { HeroHudChrome } from "./HeroHudChrome";
import { RulesHelpPopup } from "./RulesHelpPopup";

type PendingRoundResolution = {
  presentation: RoundPresentationModel;
  featureFrame: FeatureFrame;
  mathBridgeHints: MathBridgePresentationHints | null;
};

type HighlightSymbolLike = {
  getGlobalPosition: () => { x: number; y: number };
};

type ResolvedLinePresentation = PaylineOverlayLine & {
  symbols: HighlightSymbolLike[];
};

type HudButtonHandle = {
  x: number;
  y: number;
  width: number;
  height: number;
  alpha: number;
  visible: boolean;
  onDown: {
    connect: (callback: () => void) => void;
  };
  text: string;
};

type HudInternals = {
  buttons: Record<PremiumHudControlId, HudButtonHandle>;
};

const readHudFeatureFlagsFromQuery = (): PremiumHudFeatureFlags => {
  const params = new URLSearchParams(window.location.search);
  const controls: PremiumHudFeatureFlags["controls"] = {};

  const maybeOverride = (key: PremiumHudControlId, queryKey?: string) => {
    const value = params.get(queryKey ?? key);
    if (value === "0") controls[key] = false;
    if (value === "1") controls[key] = true;
  };

  maybeOverride("spin");
  maybeOverride("turbo");
  maybeOverride("autoplay");
  maybeOverride("buyFeature", "buybonus");
  maybeOverride("sound");
  maybeOverride("settings");
  maybeOverride("history");

  return { controls };
};

const readLegacyTopUiFlag = (): boolean =>
  new URLSearchParams(window.location.search).get("legacyTopUi") === "1";

const formatMessages = (messages: string[]): string =>
  messages.filter(Boolean).slice(0, 3).join(" | ");

const BETA5D_AUDIO_CUE_OVERRIDES = {
  "line-win-standard": [
    {
      type: "sfx" as const,
      assetKey: UiAssetKeys.SFX_HOVER,
      volume: 0.42,
      respectSoundEnabled: true,
    },
  ],
  "line-sequence-advance": [
    {
      type: "sfx" as const,
      assetKey: UiAssetKeys.SFX_HOVER,
      volume: 0.35,
      respectSoundEnabled: true,
    },
  ],
  "line-win-collect": [
    {
      type: "sfx" as const,
      assetKey: UiAssetKeys.SFX_HOVER,
      volume: 0.54,
      respectSoundEnabled: true,
    },
  ],
  "line-win-boost": [
    {
      type: "sfx" as const,
      assetKey: UiAssetKeys.SFX_PRESS,
      volume: 0.62,
      respectSoundEnabled: true,
    },
  ],
  "line-win-bonus": [
    {
      type: "sfx" as const,
      assetKey: UiAssetKeys.SFX_PRESS,
      volume: 0.58,
      respectSoundEnabled: true,
    },
  ],
  "line-win-jackpot": [
    {
      type: "sfx" as const,
      assetKey: UiAssetKeys.SFX_PRESS,
      volume: 0.8,
      respectSoundEnabled: true,
    },
  ],
  "feature-collect-enter": [
    {
      type: "sfx" as const,
      assetKey: UiAssetKeys.SFX_HOVER,
      volume: 0.68,
      respectSoundEnabled: true,
    },
  ],
  "feature-boost-enter": [
    {
      type: "sfx" as const,
      assetKey: UiAssetKeys.SFX_PRESS,
      volume: 0.76,
      respectSoundEnabled: true,
    },
  ],
  "feature-bonus-enter": [
    {
      type: "sfx" as const,
      assetKey: UiAssetKeys.SFX_PRESS,
      volume: 0.74,
      respectSoundEnabled: true,
    },
  ],
  "feature-jackpot-enter": [
    {
      type: "sfx" as const,
      assetKey: UiAssetKeys.SFX_PRESS,
      volume: 0.9,
      respectSoundEnabled: true,
    },
  ],
  "feature-win-tier": [
    {
      type: "sfx" as const,
      assetKey: UiAssetKeys.SFX_PRESS,
      volume: 0.7,
      respectSoundEnabled: true,
    },
  ],
};

const formatLineMultiplier = (value: number): string => {
  const rounded = Math.round(value * 100) / 100;
  return Number.isInteger(rounded)
    ? `${rounded}`
    : rounded.toFixed(2).replace(/0+$/, "").replace(/\.$/, "");
};

const formatMinorCurrency = (valueMinor: number): string => `$${(valueMinor / 100).toFixed(2)}`;

const buildPreviewPresentation = (): RoundPresentationModel => ({
  roundId: "preview-idle",
  winAmount: 0,
  slotIndex: 0,
  reels: {
    stopColumns: CRAZY_ROOSTER_IDLE_COLUMNS,
  },
  symbolGrid: buildGridFromColumns(CRAZY_ROOSTER_IDLE_COLUMNS),
  counters: {
    buyFeatureAvailable: true,
  },
  messages: ["BETONLINE READY", "8 FIXED LINES LIVE"],
  soundCues: [],
  animationCues: [],
  labels: {
    state: "idle",
  },
});

const SUPPORTED_MATH_PRESETS = [
  "normal",
  "collect",
  "boost",
  "bonus",
  "jackpot",
  "mega",
] as const;
type SupportedMathPreset = (typeof SUPPORTED_MATH_PRESETS)[number];
const SUPPORTED_MATH_MODES = ["base", "buy75", "buy200", "buy300"] as const;
type SupportedMathMode = (typeof SUPPORTED_MATH_MODES)[number];

const isSupportedMathPreset = (value: string): value is SupportedMathPreset =>
  (SUPPORTED_MATH_PRESETS as readonly string[]).includes(value);

const isSupportedMathMode = (value: string): value is SupportedMathMode =>
  (SUPPORTED_MATH_MODES as readonly string[]).includes(value);

export class MainScreen extends Container {
  public static assetBundles = ["main"];

  private readonly runtimeConfig: ResolvedConfig;
  private readonly animationPolicy: AnimationPolicyEngine;
  private readonly audioCueRegistry: AudioCueRegistry;
  private readonly wowVfx: WowVfxOrchestrator;
  private readonly featureModules: FeatureModuleManager;
  private readonly baseHudVisibility: PremiumHudVisibility;
  private readonly roundActionBuilder: RoundActionBuilder;
  private readonly shellTheme: ReturnType<typeof resolveCrazyRoosterBrandKit>;

  private readonly backgroundSprite = new Sprite(Texture.WHITE);
  private backgroundAssetUrl: string | null = null;
  private readonly reelsLayer = new Container();
  private readonly fxLayer = new Container();
  private readonly uiLayer = new Container();
  private readonly visualChrome = new Beta3VisualChrome();
  private readonly topperMascot = new TopperMascotController();
  private readonly slotMachine = new CrazyRoosterSlotMachine(resolveProviderSymbolRoot());
  private readonly layeredFx = new LayeredFxController();
  private readonly paylineOverlay = new PaylineOverlay();
  private readonly paylineHighlight = new WinHighlight();
  private readonly winHighlight = new WinHighlight();
  private readonly particleBurst = new ParticleBurst();
  private readonly winCounter = new WinCounter();
  private readonly hud = new PremiumTemplateHud();
  private readonly hudChrome = new HeroHudChrome();
  private readonly statusText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 18,
      fill: 0xffe6d2,
      stroke: { color: 0x160406, width: 4 },
      fontWeight: "800",
      align: "center",
      letterSpacing: 1,
    },
  });
  private readonly titleText = new Text({
    text: "CRAZY ROOSTER HOLD&WIN",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 24,
      fill: 0xffd88a,
      fontWeight: "900",
      stroke: { color: 0x080808, width: 4 },
      letterSpacing: 1,
    },
  });
  private readonly footerText = new Text({
    text: CRAZY_ROOSTER_BRAND.footerText,
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 15,
      fill: 0xcfcfcf,
      fontWeight: "700",
    },
  });
  private readonly debugOverlay = new DebugOverlay();
  private readonly showLegacyTopUi = readLegacyTopUiFlag();

  private paused = false;
  private isSpinning = false;
  private isPresentingWin = false;
  private turboSelected = false;
  private soundEnabled = true;
  private autoplayActive = false;
  private holdTurboRequested = false;
  private buyTierIndex = 0;
  private activeMathBridgeHints: MathBridgePresentationHints | null = null;
  private currentWinPresentationTimeout: number | null = null;
  private autoplayTimeout: number | null = null;
  private spinHoldTimeout: number | null = null;
  private readonly mathBridgeTimeouts: number[] = [];
  private lastKnownMasterVolume = 0.8;
  private pendingRound: PendingRoundResolution | null = null;

  constructor() {
    super();

    this.runtimeConfig = ResolvedRuntimeConfigStore.get();
    this.animationPolicy = new AnimationPolicyEngine(
      createAnimationPolicy({
        runtimeConfig: this.runtimeConfig,
        forcedSkipWinPresentation:
          this.runtimeConfig.capabilities.animationPolicy.forcedSkipWinPresentation,
        lowPerformanceMode: this.runtimeConfig.capabilities.animationPolicy.lowPerformanceMode,
      }),
    );

    const queryParams = new URLSearchParams(window.location.search);
    this.shellTheme = resolveCrazyRoosterBrandKit(
      queryParams.get("brandName") ?? queryParams.get("brand"),
      this.runtimeConfig,
      queryParams,
    );

    this.roundActionBuilder = new RoundActionBuilder(this.shellTheme.roundActions);
    this.featureModules = new FeatureModuleManager(this.runtimeConfig);
    this.audioCueRegistry = createAudioCueRegistry({
      overrides: {
        ...BETA5D_AUDIO_CUE_OVERRIDES,
        ...this.shellTheme.audio.cueOverrides,
      },
      themedOverrides: this.shellTheme.audio.themedCueOverrides,
      themeId: this.shellTheme.metadata.themeId,
      skinId: this.shellTheme.metadata.skinId,
    });
    this.wowVfx = new WowVfxOrchestrator(
      this.animationPolicy,
      {
        onAudioCue: (cue) => this.applySoundCue(cue),
        onAnimationCue: (cue) => this.applyAnimationCue(cue),
        showWinCounter: (amountMinor, title, tier) =>
          this.winCounter.showWin(amountMinor, title, this.resolveTierStyleHook(tier)),
        hideWinCounter: () => this.winCounter.hideNow(),
        showHeavyWinFx: (symbols, tier) =>
          this.winHighlight.showWin(
            symbols as Array<{ getGlobalPosition: () => { x: number; y: number } }>,
            this.resolveTierStyleHook(tier),
          ),
        clearHeavyWinFx: () => this.winHighlight.clear(),
        playCoinBurst: (origin) => this.particleBurst.play(origin.x, origin.y),
      },
      {
        tierLabels: this.shellTheme.winPresentation.tierLabels,
        tierStyleHooks: this.shellTheme.winPresentation.tierStyleHooks,
        intensity: this.shellTheme.vfx.intensity,
        heavyFxEnabled: this.shellTheme.vfx.heavyFxEnabled,
        coinBurstEnabled: this.shellTheme.vfx.coinBurstEnabled,
      },
    );
    this.baseHudVisibility = resolvePremiumHudVisibility(
      this.runtimeConfig,
      readHudFeatureFlagsFromQuery(),
    );

    this.turboSelected = this.animationPolicy.value.turbo.selected;
    this.lastKnownMasterVolume = Math.max(0.25, userSettings.getMasterVolume() || 0.8);
    this.soundEnabled = userSettings.getMasterVolume() > 0;

    this.backgroundSprite.anchor.set(0.5);
    this.backgroundSprite.alpha = 0.82;
    this.addChild(this.backgroundSprite);
    this.addChild(this.reelsLayer);
    this.addChild(this.fxLayer);
    this.addChild(this.uiLayer);
    this.addChild(this.debugOverlay);

    this.reelsLayer.addChild(this.visualChrome);
    this.reelsLayer.addChild(this.topperMascot);
    this.reelsLayer.addChild(this.slotMachine);
    this.fxLayer.addChild(this.layeredFx);
    this.fxLayer.addChild(this.paylineOverlay);
    this.fxLayer.addChild(this.paylineHighlight);
    this.fxLayer.addChild(this.winHighlight);
    this.fxLayer.addChild(this.particleBurst);

    this.statusText.anchor.set(0.5);
    this.statusText.visible = false;
    this.titleText.anchor.set(0.5);
    this.titleText.visible = this.showLegacyTopUi;
    this.footerText.anchor.set(0.5);
    this.visualChrome.onBuyFeatureRequest = () => {
      void this.handleBuyFeature();
    };

    this.hud.applyTheme(this.shellTheme.hud);
    this.hud.applyVisibility(this.baseHudVisibility);
    this.hud.setCallbacks({
      onControlPress: (controlId) => {
        void this.handleHudControl(controlId);
      },
    });

    this.uiLayer.addChild(this.winCounter);
    this.uiLayer.addChild(this.hudChrome);
    this.uiLayer.addChild(this.hud);
    this.uiLayer.addChild(this.statusText);
    this.uiLayer.addChild(this.titleText);
    this.uiLayer.addChild(this.footerText);

    this.slotMachine.onSpinComplete = () => this.handleSpinComplete();
    this.connectSpinHoldGesture();
    this.applyPreviewState();
    this.refreshHudState(0);
    this.showStatus(this.resolveBenchmarkReadyStatus());
  }

  public prepare(): void {}

  public update(): void {
    if (this.paused) return;
  }

  public async pause(): Promise<void> {
    this.uiLayer.interactiveChildren = false;
    this.paused = true;
  }

  public async resume(): Promise<void> {
    this.uiLayer.interactiveChildren = true;
    this.paused = false;
  }

  public reset(): void {
    this.clearAutoplayTimeout();
    this.clearWinPresentationTimeout();
    this.clearSpinHoldTimeout();
    this.clearMathBridgeTimeouts();
    this.layeredFx.clearPresentation();
    this.topperMascot.setState("idle");
    this.paylineOverlay.clear();
    this.paylineHighlight.clear();
    this.debugOverlay.setMathBridgeSummary(null);
    this.visualChrome.clearPresentationCue();
    this.activeMathBridgeHints = null;
  }

  public resize(width: number, height: number): void {
    const viewport = engine().layout.getViewport();
    const safe = viewport.safeArea;
    const hudSpace = viewport.orientation === "portrait" ? 300 : 210;
    const centerX = width * 0.5;
    const availableTop = safe.top + 110;
    const availableBottom = height - safe.bottom - hudSpace;
    const centerY = (availableTop + availableBottom) * 0.5;

    const machineWidth =
      CRAZY_ROOSTER_LAYOUT.reelCount * CRAZY_ROOSTER_LAYOUT.symbolWidth +
      (CRAZY_ROOSTER_LAYOUT.reelCount - 1) * CRAZY_ROOSTER_LAYOUT.reelSpacing;
    const machineHeight =
      CRAZY_ROOSTER_LAYOUT.rowCount * CRAZY_ROOSTER_LAYOUT.symbolHeight +
      (CRAZY_ROOSTER_LAYOUT.rowCount - 1) * CRAZY_ROOSTER_LAYOUT.rowSpacing;
    const visualPadding = Beta3VisualChrome.padding;
    const chromeWidth = machineWidth + visualPadding.left + visualPadding.right;
    const chromeHeight = machineHeight + visualPadding.top + visualPadding.bottom;
    const availableWidth = width - safe.left - safe.right - 48;
    const availableHeight = Math.max(320, availableBottom - availableTop);
    const scale = Math.min(availableWidth / chromeWidth, availableHeight / chromeHeight);
    const backgroundUrl = resolveProviderBackgroundUrl(width, height);

    if (backgroundUrl) {
      this.backgroundSprite.tint = 0xffffff;
      this.ensureBackgroundTexture(backgroundUrl);
    } else {
      this.backgroundAssetUrl = null;
      this.backgroundSprite.texture = Texture.WHITE;
      this.backgroundSprite.tint = 0x120c0c;
    }
    this.backgroundSprite.x = width * 0.5;
    this.backgroundSprite.y = height * 0.5;
    this.backgroundSprite.width = width;
    this.backgroundSprite.height = height;

    this.visualChrome.resize(machineWidth, machineHeight);
    this.topperMascot.resize(machineWidth);
    this.layeredFx.resize(machineWidth, machineHeight);
    const topperFocus = this.topperMascot.getFocusPoint();
    this.layeredFx.setTopperAnchor(topperFocus.x, topperFocus.y);
    this.reelsLayer.scale.set(scale);
    this.fxLayer.scale.set(scale);
    this.reelsLayer.x = centerX - (chromeWidth * scale) / 2 + visualPadding.left * scale;
    this.reelsLayer.y = centerY - (chromeHeight * scale) / 2 + visualPadding.top * scale;
    this.fxLayer.x = this.reelsLayer.x;
    this.fxLayer.y = this.reelsLayer.y;

    this.hud.resize({
      width,
      height,
      orientation: viewport.orientation,
      safeArea: safe,
    });
    this.syncHudChrome();

    this.titleText.x = width * 0.5;
    this.titleText.y = safe.top + 28;
    this.statusText.x = width * 0.5;
    this.statusText.y = safe.top + 68;
    this.winCounter.x = width * 0.5;
    this.winCounter.y = safe.top + 156;
    this.footerText.x = width * 0.5;
    this.footerText.y = height - Math.max(24, safe.bottom + 18);

    this.debugOverlay.resize(width, height);
  }

  private ensureBackgroundTexture(backgroundUrl: string): void {
    if (this.backgroundAssetUrl === backgroundUrl) {
      return;
    }

    this.backgroundAssetUrl = backgroundUrl;
    void Assets.load(backgroundUrl)
      .then(() => {
        if (this.backgroundAssetUrl !== backgroundUrl) {
          return;
        }
        this.backgroundSprite.texture = Texture.from(backgroundUrl);
      })
      .catch(() => {
        if (this.backgroundAssetUrl !== backgroundUrl) {
          return;
        }
        this.backgroundSprite.texture = Texture.WHITE;
        this.backgroundSprite.tint = 0x120c0c;
      });
  }

  public async show(): Promise<void> {
    try {
      await engine().audio.bgm.play(AppAssetKeys.BGM_MAIN, {
        volume: userSettings.getBgmVolume(),
      });
    } catch {
      // Placeholder audio is optional for this vertical slice.
    }
  }

  public async hide(): Promise<void> {
    this.clearAutoplayTimeout();
    this.clearWinPresentationTimeout();
    this.clearSpinHoldTimeout();
    this.clearMathBridgeTimeouts();
    this.layeredFx.clearPresentation();
    this.topperMascot.setState("idle");
    this.visualChrome.clearPresentationCue();
    this.activeMathBridgeHints = null;
  }

  public blur(): void {
    this.showStatus("SESSION PAUSED");
  }

  public installTestingHooks(): void {
    const target = window as Window & {
      __game7000?: Record<string, unknown>;
    };

    target.__game7000 = {
      spin: () => this.handleSpin(),
      buy: () => this.handleBuyFeature(),
      autoplay: () => this.toggleAutoplay(),
      turbo: () => this.toggleTurbo(),
      rules: () => engine().navigation.presentPopup(RulesHelpPopup),
      history: () => this.handleHistory(),
      math: {
        presets: [...SUPPORTED_MATH_PRESETS],
        modes: [...SUPPORTED_MATH_MODES],
        state: () => this.readMathControlState(),
        setSource: (source: unknown) => this.setMathSourceControl(source),
        setPreset: (preset: unknown) => this.setMathPresetControl(preset),
        setMode: (mode: unknown) => this.setMathModeControl(mode),
        clearPreset: () => this.setMathPresetControl(null),
        spinPreset: async (preset: unknown, mode?: unknown) =>
          this.spinWithMathControls(preset, mode),
        help: () => ({
          spin: "window.__game7000.math.spinPreset('collect')",
          mode: "window.__game7000.math.setMode('buy75')",
          source: "window.__game7000.math.setSource('provisional')",
          state: "window.__game7000.math.state()",
        }),
      },
      screen: this,
    };
  }

  private readMathControlState(): {
    source: string | null;
    preset: string | null;
    mode: string | null;
    allowDevFallback: string | null;
  } {
    const params = new URLSearchParams(window.location.search);
    return {
      source: params.get("mathSource"),
      preset: params.get("mathPreset"),
      mode: params.get("mathMode"),
      allowDevFallback: params.get("allowDevFallback"),
    };
  }

  private replaceSearchParams(mutator: (params: URLSearchParams) => void): void {
    const params = new URLSearchParams(window.location.search);
    mutator(params);
    const nextQuery = params.toString();
    const nextUrl = `${window.location.pathname}${nextQuery ? `?${nextQuery}` : ""}${window.location.hash}`;
    window.history.replaceState({}, "", nextUrl);
  }

  private ensureProvisionalMathSourceParams(): void {
    this.replaceSearchParams((params) => {
      params.set("allowDevFallback", "1");
      params.set("mathSource", "provisional");
    });
  }

  private setMathSourceControl(source: unknown): {
    ok: boolean;
    state: ReturnType<MainScreen["readMathControlState"]>;
    error?: string;
  } {
    if (source === null || source === undefined || source === "") {
      this.replaceSearchParams((params) => {
        params.delete("mathSource");
      });
      this.showStatus("MATH SOURCE CLEARED");
      return { ok: true, state: this.readMathControlState() };
    }

    if (source !== "provisional") {
      return {
        ok: false,
        state: this.readMathControlState(),
        error: `Unsupported math source: ${String(source)}`,
      };
    }

    this.ensureProvisionalMathSourceParams();
    this.showStatus("MATH SOURCE: PROVISIONAL");
    return { ok: true, state: this.readMathControlState() };
  }

  private setMathPresetControl(preset: unknown): {
    ok: boolean;
    state: ReturnType<MainScreen["readMathControlState"]>;
    error?: string;
  } {
    if (preset === null || preset === undefined || preset === "") {
      this.replaceSearchParams((params) => {
        params.delete("mathPreset");
      });
      this.showStatus("MATH PRESET CLEARED");
      return { ok: true, state: this.readMathControlState() };
    }

    if (typeof preset !== "string" || !isSupportedMathPreset(preset)) {
      return {
        ok: false,
        state: this.readMathControlState(),
        error: `Unsupported math preset: ${String(preset)}`,
      };
    }

    this.ensureProvisionalMathSourceParams();
    this.replaceSearchParams((params) => {
      params.set("mathPreset", preset);
    });
    this.showStatus(`MATH PRESET: ${preset.toUpperCase()}`);
    return { ok: true, state: this.readMathControlState() };
  }

  private setMathModeControl(mode: unknown): {
    ok: boolean;
    state: ReturnType<MainScreen["readMathControlState"]>;
    error?: string;
  } {
    if (mode === null || mode === undefined || mode === "") {
      this.replaceSearchParams((params) => {
        params.delete("mathMode");
      });
      this.showStatus("MATH MODE CLEARED");
      return { ok: true, state: this.readMathControlState() };
    }

    if (typeof mode !== "string" || !isSupportedMathMode(mode)) {
      return {
        ok: false,
        state: this.readMathControlState(),
        error: `Unsupported math mode: ${String(mode)}`,
      };
    }

    this.ensureProvisionalMathSourceParams();
    this.replaceSearchParams((params) => {
      params.set("mathMode", mode);
    });
    this.showStatus(`MATH MODE: ${mode.toUpperCase()}`);
    return { ok: true, state: this.readMathControlState() };
  }

  private async spinWithMathControls(
    preset: unknown,
    mode?: unknown,
  ): Promise<{
    ok: boolean;
    state: ReturnType<MainScreen["readMathControlState"]>;
    error?: string;
  }> {
    const presetResult = this.setMathPresetControl(preset);
    if (!presetResult.ok) {
      return presetResult;
    }

    if (mode !== undefined) {
      const modeResult = this.setMathModeControl(mode);
      if (!modeResult.ok) {
        return modeResult;
      }
    }

    await this.handleSpin();
    return { ok: true, state: this.readMathControlState() };
  }

  private connectSpinHoldGesture(): void {
    const spinButton = (this.hud as unknown as HudInternals).buttons.spin;
    spinButton.onDown.connect(() => {
      this.clearSpinHoldTimeout();
      this.holdTurboRequested = false;
      this.spinHoldTimeout = window.setTimeout(() => {
        this.holdTurboRequested = true;
        this.showStatus("HOLD FOR TURBO");
        this.refreshHudState(PresentationStateStore.get().lastWinAmount);
      }, 320);
      window.addEventListener("pointerup", this.releaseSpinHold, { once: true });
    });
  }

  private readonly releaseSpinHold = () => {
    this.clearSpinHoldTimeout();
  };

  private clearSpinHoldTimeout(): void {
    if (this.spinHoldTimeout !== null) {
      window.clearTimeout(this.spinHoldTimeout);
      this.spinHoldTimeout = null;
    }
  }

  private scheduleMathBridgeCue(cue: string, delayMs: number): void {
    const timeout = window.setTimeout(() => {
      this.applyAnimationCue(cue);
    }, Math.max(0, delayMs));
    this.mathBridgeTimeouts.push(timeout);
  }

  private clearMathBridgeTimeouts(): void {
    while (this.mathBridgeTimeouts.length > 0) {
      const timeout = this.mathBridgeTimeouts.pop();
      if (timeout !== undefined) {
        window.clearTimeout(timeout);
      }
    }
    this.layeredFx.clearPresentation();
    this.topperMascot.setState("idle");
    this.paylineOverlay.clear();
    this.paylineHighlight.clear();
  }

  private scheduleMathBridgeFeatureCues(
    mathBridgeHints: MathBridgePresentationHints | null,
    lineCount: number,
  ): void {
    if (!mathBridgeHints) {
      return;
    }

    const lineTiming = this.resolveLineSequenceTiming(mathBridgeHints, lineCount);
    const featureDelay = Math.max(
      Math.max(0, mathBridgeHints.timingHints.featureStartDelayMs),
      lineTiming.sequenceEndDelayMs + 80,
    );
    const summaryDelay =
      lineCount > 0
        ? Math.max(lineTiming.delayMs, lineTiming.sequenceEndDelayMs - Math.round(lineTiming.durationMs * 0.4))
        : Math.max(0, mathBridgeHints.timingHints.lineHighlightDelayMs);
    if (mathBridgeHints.mode !== "base") {
      this.scheduleMathBridgeCue("feature.buyBonus.enter", Math.max(0, featureDelay - 120));
    }

    for (const cue of mathBridgeHints.eventTriggers) {
      if (cue.startsWith("round.reel.stop")) {
        continue;
      }
      if (
        (mathBridgeHints.triggers.boost || mathBridgeHints.triggers.bonus) &&
        cue === "feature.collect.triggered"
      ) {
        continue;
      }
      if (
        (mathBridgeHints.triggers.bonus || mathBridgeHints.triggers.jackpot) &&
        cue === "feature.boost.triggered"
      ) {
        continue;
      }

      let delay = featureDelay;
      if (cue === "overlay.totalSummary.update") {
        delay = summaryDelay;
      } else if (cue === "feature.collect.triggered") {
        delay = featureDelay;
      } else if (cue === "feature.boost.triggered") {
        delay = featureDelay + 90;
      } else if (cue === "feature.bonus.enter") {
        delay = featureDelay + 170;
      } else if (cue === "feature.jackpot.attached") {
        delay = featureDelay + 250;
      } else if (cue === "overlay.winTier.enter") {
        delay = featureDelay + 330;
      }

      this.scheduleMathBridgeCue(cue, delay);
    }
  }

  private scheduleLineVisualization(
    mathBridgeHints: MathBridgePresentationHints | null,
    linePresentations: ResolvedLinePresentation[],
  ): void {
    if (!mathBridgeHints || linePresentations.length === 0) {
      return;
    }

    const timing = this.resolveLineSequenceTiming(mathBridgeHints, linePresentations.length);
    const tone = this.resolveLineTone(mathBridgeHints);

    linePresentations.forEach((linePresentation, index) => {
      const startDelay = timing.delayMs + index * (timing.durationMs + timing.gapMs);
      const showTimeout = window.setTimeout(() => {
        const styleHook = this.resolveLineStyleHook(mathBridgeHints, index, linePresentations.length);
        this.paylineHighlight.showWin(linePresentation.symbols, styleHook);
        this.paylineOverlay.showLine(linePresentation, {
          durationMs: timing.durationMs,
          sequenceCount: linePresentations.length,
          sequenceIndex: index,
          styleHook,
          tone,
        });
        this.playLinePresentationChoreography(
          linePresentation,
          mathBridgeHints,
          tone,
          index,
          linePresentations.length,
        );
      }, startDelay);
      const hideTimeout = window.setTimeout(() => {
        this.paylineOverlay.clear();
        this.paylineHighlight.clear();
      }, startDelay + timing.durationMs);
      this.mathBridgeTimeouts.push(showTimeout, hideTimeout);
    });
  }

  private resolveLineSequenceTiming(
    mathBridgeHints: MathBridgePresentationHints,
    lineCount: number,
  ): {
    delayMs: number;
    durationMs: number;
    gapMs: number;
    sequenceEndDelayMs: number;
  } {
    const delayMs = Math.max(0, mathBridgeHints.timingHints.lineHighlightDelayMs);
    const durationMs = Math.max(360, mathBridgeHints.timingHints.lineHighlightDurationMs);
    const gapMs = Math.min(180, Math.max(70, Math.round(durationMs * 0.18)));
    const sequenceEndDelayMs =
      lineCount > 0 ? delayMs + lineCount * durationMs + Math.max(0, lineCount - 1) * gapMs : delayMs;
    return {
      delayMs,
      durationMs,
      gapMs,
      sequenceEndDelayMs,
    };
  }

  private resolveLineTone(
    mathBridgeHints: MathBridgePresentationHints,
  ): PaylineOverlayTone {
    if (mathBridgeHints.triggers.jackpot) {
      return "jackpot";
    }
    if (mathBridgeHints.triggers.bonus) {
      return "bonus";
    }
    if (mathBridgeHints.triggers.boost) {
      return "boost";
    }
    if (mathBridgeHints.triggers.collect) {
      return "collect";
    }
    return "standard";
  }

  private resolveLineStyleHook(
    mathBridgeHints: MathBridgePresentationHints,
    lineIndex: number,
    lineCount: number,
  ): string {
    if (mathBridgeHints.triggers.jackpot) {
      return "intense";
    }
    if (mathBridgeHints.triggers.boost || mathBridgeHints.triggers.bonus) {
      return lineIndex === lineCount - 1 ? "intense" : "neon";
    }
    if (mathBridgeHints.triggers.collect) {
      return "neon";
    }
    if (lineCount > 1 && lineIndex === lineCount - 1) {
      return "neon";
    }
    return mathBridgeHints.winTier === "none" ? "subtle" : "neon";
  }

  private playLinePresentationChoreography(
    linePresentation: ResolvedLinePresentation,
    mathBridgeHints: MathBridgePresentationHints,
    tone: PaylineOverlayTone,
    lineIndex: number,
    lineCount: number,
  ): void {
    const origin =
      linePresentation.points[Math.floor(linePresentation.points.length * 0.5)] ??
      linePresentation.points[0] ?? {
        x:
          CRAZY_ROOSTER_LAYOUT.reelCount * CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5,
        y:
          CRAZY_ROOSTER_LAYOUT.rowCount * CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5,
      };
    const captionParts = [
      `L${linePresentation.lineId}`,
      `x${formatLineMultiplier(linePresentation.multiplier)}`,
      formatMinorCurrency(linePresentation.amountMinor),
    ];
    if (lineCount > 1) {
      captionParts.push(`${lineIndex + 1}/${lineCount}`);
    }

    this.visualChrome.triggerPresentationCue({
      tone,
      title: this.resolveLinePresentationTitle(tone),
      caption: captionParts.join("  •  "),
      holdMs: tone === "jackpot" ? 980 : 760,
      jackpotTier: mathBridgeHints.jackpotTier,
      plaqueIndexes: this.resolveLinePlaqueIndexes(linePresentation, tone, lineIndex, lineCount),
    });

    switch (tone) {
      case "collect":
        this.topperMascot.setState("react_collect");
        this.layeredFx.playCollectSweep(origin.x, origin.y);
        break;
      case "boost":
        this.topperMascot.setState(lineIndex === 0 ? "react_boost_start" : "react_boost_loop");
        void this.layeredFx.playBoostStart();
        this.layeredFx.playCoinFly(origin.x, origin.y, 6);
        break;
      case "bonus":
        this.topperMascot.setState("react_bigwin");
        this.layeredFx.playBoostLoop();
        this.layeredFx.playCoinFly(origin.x, origin.y, 7);
        break;
      case "jackpot":
        this.topperMascot.setState("react_jackpot");
        this.layeredFx.playJackpotHit(
          this.resolveJackpotReactionLevel(mathBridgeHints.jackpotTier),
        );
        break;
      case "standard":
      default:
        this.topperMascot.setState(
          mathBridgeHints.winTier === "none" ? "react_collect" : "react_bigwin",
        );
        this.layeredFx.playCoinFly(origin.x, origin.y, lineCount > 1 ? 5 : 4);
        if (lineIndex === lineCount - 1 && mathBridgeHints.winTier !== "none") {
          this.layeredFx.playWinPulse(mathBridgeHints.winTier);
        }
        break;
    }

    this.particleBurst.play(origin.x, Math.max(24, origin.y - 8));
    this.applySoundCue(this.resolveLineAudioCue(tone, lineIndex, lineCount));
  }

  private resolveLinePresentationTitle(tone: PaylineOverlayTone): string {
    switch (tone) {
      case "collect":
        return "COLLECT RUN";
      case "boost":
        return "BOOST STRIKE";
      case "bonus":
        return "BONUS ENTRY";
      case "jackpot":
        return "JACKPOT CALL";
      case "standard":
      default:
        return "ROOSTER WIN";
    }
  }

  private resolveLineAudioCue(
    tone: PaylineOverlayTone,
    lineIndex: number,
    lineCount: number,
  ): string {
    if (lineCount > 1 && lineIndex > 0) {
      return "line-sequence-advance";
    }

    switch (tone) {
      case "collect":
        return "line-win-collect";
      case "boost":
        return "line-win-boost";
      case "bonus":
        return "line-win-bonus";
      case "jackpot":
        return "line-win-jackpot";
      case "standard":
      default:
        return "line-win-standard";
    }
  }

  private resolveLinePlaqueIndexes(
    linePresentation: ResolvedLinePresentation,
    tone: PaylineOverlayTone,
    lineIndex: number,
    lineCount: number,
  ): number[] {
    if (tone === "jackpot") {
      return [this.resolveJackpotReactionLevel(this.activeMathBridgeHints?.jackpotTier ?? null) - 1];
    }
    if (tone === "bonus") {
      return [1, 2];
    }
    if (tone === "boost") {
      return linePresentation.multiplier >= 5 ? [2, 3] : [2];
    }
    if (tone === "collect") {
      return linePresentation.multiplier >= 3 ? [0, 1] : [0];
    }
    if (lineCount > 1) {
      return [lineIndex % 2 === 0 ? 1 : 2];
    }
    if (linePresentation.multiplier >= 8) {
      return [3];
    }
    if (linePresentation.multiplier >= 5) {
      return [2];
    }
    if (linePresentation.multiplier >= 3) {
      return [1];
    }
    return [0];
  }

  private resolveJackpotReactionLevel(jackpotTier: string | null): number {
    switch ((jackpotTier ?? "").toLowerCase()) {
      case "grand":
        return 4;
      case "major":
        return 3;
      case "minor":
        return 2;
      case "mini":
      default:
        return 1;
    }
  }

  private resolveBenchmarkReadyStatus(): string {
    const provider = getProviderPackStatus().effectiveProvider;
    if (provider === "donorlocal") {
      return "DONORLOCAL BENCHMARK READY";
    }
    if (provider === "openai") {
      return "OPENAI FALLBACK READY";
    }
    return `${provider.toUpperCase()} READY`;
  }

  private resolveTierStyleHook(tier: PresentationWinTier): string | undefined {
    return this.shellTheme.winPresentation.tierStyleHooks[tier];
  }

  private async handleHudControl(controlId: PremiumHudControlId): Promise<void> {
    switch (controlId) {
      case "spin":
        await this.handleSpin();
        break;
      case "turbo":
        this.toggleTurbo();
        break;
      case "autoplay":
        this.toggleAutoplay();
        break;
      case "buyFeature":
        await this.handleBuyFeature();
        break;
      case "sound":
        this.toggleSound();
        break;
      case "settings":
        engine().navigation.presentPopup(SettingsPopup);
        break;
      case "history":
        engine().navigation.presentPopup(RulesHelpPopup);
        break;
      default:
        break;
    }
  }

  private toggleTurbo(): void {
    if (!this.animationPolicy.value.turbo.allowed) {
      return;
    }

    this.turboSelected = !this.turboSelected;
    PresentationStateStore.patch({
      turboSelected: this.turboSelected,
      soundEnabled: this.soundEnabled,
    });
    this.refreshHudState(PresentationStateStore.get().lastWinAmount);
    this.showStatus(this.turboSelected ? "TURBO ENABLED" : "TURBO DISABLED");
  }

  private toggleAutoplay(): void {
    this.autoplayActive = !this.autoplayActive;
    this.showStatus(this.autoplayActive ? "AUTOPLAY ENABLED" : "AUTOPLAY STOPPED");
    this.refreshHudState(PresentationStateStore.get().lastWinAmount);

    if (this.autoplayActive && !this.isSpinning && !this.isPresentingWin) {
      this.scheduleAutoplaySpin(180);
      return;
    }

    if (!this.autoplayActive) {
      this.clearAutoplayTimeout();
    }
  }

  private toggleSound(): void {
    if (this.soundEnabled) {
      this.lastKnownMasterVolume = Math.max(0.25, userSettings.getMasterVolume() || 0.8);
      userSettings.setMasterVolume(0);
    } else {
      userSettings.setMasterVolume(this.lastKnownMasterVolume);
    }

    this.soundEnabled = !this.soundEnabled;
    PresentationStateStore.patch({ soundEnabled: this.soundEnabled });
    this.refreshHudState(PresentationStateStore.get().lastWinAmount);
    this.showStatus(this.soundEnabled ? "SOUND ENABLED" : "SOUND DISABLED");
  }

  private async handleHistory(): Promise<void> {
    try {
      const history = await gsRuntimeClient.gethistory({
        fromRoundId: null,
        limit: 20,
        includeFeatureDetails: true,
      });
      this.showStatus(`HISTORY ITEMS: ${history.length}`);
    } catch (error) {
      this.showStatus(`HISTORY FAILED: ${String(error)}`);
    }
  }

  private async handleBuyFeature(): Promise<void> {
    if (this.isSpinning || this.isPresentingWin) {
      return;
    }

    try {
      const explicitTier = new URLSearchParams(window.location.search).get("buyTier");
      const tier =
        CRAZY_ROOSTER_BUY_TIERS.find((entry) => entry.id === explicitTier) ??
        CRAZY_ROOSTER_BUY_TIERS[this.buyTierIndex % CRAZY_ROOSTER_BUY_TIERS.length];
      this.buyTierIndex += 1;

      const totalBetMinor = Math.max(1, Math.round(ResolvedRuntimeConfigStore.limits.defaultBet));
      const action = this.roundActionBuilder.buildBuyFeatureAction({
        totalBetMinor,
        explicitPriceMinor: Math.round(totalBetMinor * tier.priceMultiplier),
        payload: {
          source: "hud",
          tierId: tier.id,
        },
      });

      const response = await gsRuntimeClient.featureaction("buy-feature", {
        selectedBet: action.selectedBet,
        selectedFeatureChoice: {
          ...action.selectedFeatureChoice,
          payload: {
            ...action.selectedFeatureChoice.payload,
            tierId: tier.id,
          },
        },
        priceMinor: action.selectedFeatureChoice.priceMinor,
      });

      const timing = this.animationPolicy.resolveSpinTiming(false);
      const mathBridgeHints = readMathBridgeHints(response);
      this.queueRuntimeEnvelope(
        mapPlayRoundToPresentation(response),
        timing,
        mathBridgeHints,
      );
      this.showStatus(`${tier.label} TRIGGERED`);
    } catch (error) {
      this.showStatus(`BUY FEATURE FAILED: ${String(error)}`);
    }
  }

  private async handleSpin(): Promise<void> {
    if (this.isPresentingWin && this.animationPolicy.shouldAllowForcedSkip()) {
      this.skipWinPresentation();
      return;
    }

    if (this.isSpinning || this.isPresentingWin) {
      return;
    }

    this.isSpinning = true;
    this.pendingRound = null;
    this.winHighlight.clear();
    this.paylineHighlight.clear();
    this.paylineOverlay.clear();
    this.debugOverlay.setMathBridgeSummary(null);
    this.clearAutoplayTimeout();

    const requestTurbo = this.turboSelected || this.holdTurboRequested;
    this.holdTurboRequested = false;
    const timing = this.animationPolicy.resolveSpinTiming(requestTurbo);
    const totalBetMinor = Math.max(1, Math.round(ResolvedRuntimeConfigStore.limits.defaultBet));
    const selectedBet = this.roundActionBuilder.buildSpinBet(totalBetMinor);

    try {
      const round = await gsRuntimeClient.playround(selectedBet);
      const mathBridgeHints = readMathBridgeHints(round);
      this.queueRuntimeEnvelope(
        mapPlayRoundToPresentation(round),
        timing,
        mathBridgeHints,
      );
    } catch (error) {
      this.isSpinning = false;
      this.pendingRound = null;
      PresentationStateStore.patch({
        isSpinning: false,
        statusText: `ROUND_FAILED: ${String(error)}`,
      });
      this.showStatus("ROUND FAILED");
    }
  }

  private queueRuntimeEnvelope(
    presentation: RoundPresentationModel,
    timing: ReturnType<AnimationPolicyEngine["resolveSpinTiming"]>,
    mathBridgeHints: MathBridgePresentationHints | null,
  ): void {
    this.clearMathBridgeTimeouts();
    this.activeMathBridgeHints = mathBridgeHints;
    this.debugOverlay.setMathBridgeSummary(
      mathBridgeHints
        ? {
            lineIds: mathBridgeHints.lineWins.map((line) => line.lineId),
            lineMultipliers: mathBridgeHints.lineWins.map((line) => line.multiplier),
            totalWinMultiplier: mathBridgeHints.totalWinMultiplier,
          }
        : null,
    );
    const featureFrame = this.featureModules.resolve(presentation);
    this.pendingRound = {
      presentation,
      featureFrame,
      mathBridgeHints,
    };

    if (mathBridgeHints) {
      const reelCueBase = Math.max(0, timing.minSpinMs);
      const reelCueMap = [
        "round.reel.stop.1",
        "round.reel.stop.2",
        mathBridgeHints.timingHints.reelStopDelaysMs[2] >= 3000
          ? "round.reel.stop.3.bonusHold"
          : "round.reel.stop.3",
      ] as const;
      reelCueMap.forEach((cue, index) => {
        const offset =
          mathBridgeHints.timingHints.reelStopDelaysMs[index] ??
          (index + 1) * timing.spinStaggerMs;
        this.scheduleMathBridgeCue(cue, reelCueBase + Math.max(0, offset));
      });
    }

    this.slotMachine.spin({
      minSpinDurationMs: timing.minSpinMs,
      spinStaggerMs: timing.spinStaggerMs,
      speedMultiplier: timing.speedMultiplier,
      reelStopColumns: presentation.reels.stopColumns,
    });
    this.layeredFx.beginSpinCycle();
    this.topperMascot.setState("idle");
    this.visualChrome.beginSpinCycle();

    PresentationStateStore.patch({
      isSpinning: true,
      statusText: "ROUND_REQUESTED",
    });
    this.showStatus("ROUND REQUESTED");
  }

  private handleSpinComplete(): void {
    const resolution = this.pendingRound;
    if (!resolution) {
      this.isSpinning = false;
      this.showStatus("MISSING ROUND PRESENTATION PAYLOAD");
      PresentationStateStore.patch({
        isSpinning: false,
        statusText: "ROUND_FAILED: missing presentation payload",
      });
      return;
    }

    this.pendingRound = null;

    const { presentation, featureFrame, mathBridgeHints } = resolution;
    this.applyDynamicControlVisibility(featureFrame);

    const mergedMessages = [
      ...presentation.messages,
      ...featureFrame.messages,
      ...featureFrame.overlays.map((overlay) => overlay.label),
    ];
    this.showStatus(formatMessages(mergedMessages));

    const winSymbols = this.resolveWinningSymbols(presentation, mathBridgeHints);
    const linePresentations = this.resolveLinePresentations(mathBridgeHints);
    const winAmount = presentation.winAmount;
    const defaultBet = ResolvedRuntimeConfigStore.limits.defaultBet;

    this.scheduleMathBridgeFeatureCues(mathBridgeHints, linePresentations.length);
    this.scheduleLineVisualization(mathBridgeHints, linePresentations);

    const vfxState = this.wowVfx.startWinPresentation({
      winAmountMinor: winAmount,
      defaultBetMinor: defaultBet,
      winSymbols: winSymbols as never[],
      soundCues: [...presentation.soundCues, ...featureFrame.soundCues],
      animationCues: [...presentation.animationCues, ...featureFrame.animationCues],
      burstOrigin: { x: 0, y: -100 },
    });

    this.isSpinning = false;
    this.isPresentingWin = vfxState.hasWinPresentation;

    PresentationStateStore.patch({
      isSpinning: false,
      isPresentingWin: vfxState.hasWinPresentation,
      turboSelected: this.turboSelected,
      soundEnabled: this.soundEnabled,
      lastWinAmount: winAmount,
      lastMessages: mergedMessages,
      activeFeatureModules: featureFrame.activeModuleIds,
      statusText: `ROUND_SETTLED:${presentation.roundId}`,
    });

    this.refreshHudState(winAmount);

    if (!vfxState.hasWinPresentation) {
      this.scheduleAutoplaySpin(this.animationPolicy.getAutoplayDelayMs("none", true));
      return;
    }

    this.clearWinPresentationTimeout();
    if (vfxState.durationMs <= 0) {
      this.finishWinPresentation(vfxState.policyTier, vfxState.forcedSkip);
      return;
    }

    this.currentWinPresentationTimeout = window.setTimeout(() => {
      this.finishWinPresentation(vfxState.policyTier, vfxState.forcedSkip);
    }, vfxState.durationMs);
  }

  private resolveWinningSymbols(
    presentation: RoundPresentationModel,
    mathBridgeHints: MathBridgePresentationHints | null,
  ) {
    const reels = this.slotMachine.getReels();

    const hintedLine = mathBridgeHints?.lineWins[0];
    if (hintedLine?.positions?.length) {
      const hintedSymbols = hintedLine.positions
        .map((position) =>
          reels[position.reelIndex]?.getVisibleSymbols()[position.rowIndex],
        )
        .filter(Boolean);
      if (hintedSymbols.length > 0) {
        return hintedSymbols;
      }
    }

    for (const payline of CRAZY_ROOSTER_PAYLINES) {
      const symbols = payline.map(
        (rowIndex, reelIndex) => presentation.symbolGrid[rowIndex]?.[reelIndex],
      );
      const first = symbols[0];
      if (first === undefined || first === 9) {
        continue;
      }
      if (symbols.every((symbol) => symbol === first)) {
        return payline
          .map((rowIndex, reelIndex) => reels[reelIndex]?.getVisibleSymbols()[rowIndex])
          .filter(Boolean);
      }
    }

    return reels
      .map((reel) => reel.getVisibleSymbols()[Math.min(1, CRAZY_ROOSTER_LAYOUT.rowCount - 1)])
      .filter(Boolean);
  }

  private resolveLinePresentations(
    mathBridgeHints: MathBridgePresentationHints | null,
  ): ResolvedLinePresentation[] {
    if (!mathBridgeHints || mathBridgeHints.lineWins.length === 0) {
      return [];
    }

    const reels = this.slotMachine.getReels();
    return mathBridgeHints.lineWins
      .map((lineWin) => {
        const symbols = lineWin.positions
          .map((position) => reels[position.reelIndex]?.getVisibleSymbols()[position.rowIndex])
          .filter(Boolean) as HighlightSymbolLike[];

        if (symbols.length === 0) {
          return null;
        }

        return {
          lineId: lineWin.lineId,
          symbolId: lineWin.symbolId,
          multiplier: lineWin.multiplier,
          amountMinor: lineWin.amountMinor,
          points: symbols.map((symbol) => this.resolveFxLayerCenter(symbol)),
          symbols,
        };
      })
      .filter((entry): entry is ResolvedLinePresentation => Boolean(entry));
  }

  private resolveFxLayerCenter(symbol: HighlightSymbolLike): { x: number; y: number } {
    const globalPoint = symbol.getGlobalPosition();
    const localPoint = this.fxLayer.toLocal(globalPoint);
    return {
      x: localPoint.x + CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5,
      y: localPoint.y + CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5,
    };
  }

  private applyDynamicControlVisibility(featureFrame: FeatureFrame): void {
    const visibility = mergePremiumHudVisibility(
      this.baseHudVisibility,
      featureFrame.controlVisibility,
    );
    this.hud.applyVisibility({ controls: visibility.controls });
    this.syncHudChrome();
  }

  private applySoundCue(cue: string): void {
    applyAudioCue(
      cue,
      {
        playSfx: (assetKey, options) => {
          try {
            engine().audio.sfx.play(assetKey, options);
          } catch {
            // Missing QA slice cues should not break presentation.
          }
        },
        setBgmVolume: (volume) => {
          userSettings.setBgmVolume(volume);
        },
        isSoundEnabled: () => this.soundEnabled,
      },
      this.audioCueRegistry,
    );
  }

  private applyAnimationCue(cue: string): void {
    const machineWidth =
      CRAZY_ROOSTER_LAYOUT.reelCount * CRAZY_ROOSTER_LAYOUT.symbolWidth +
      (CRAZY_ROOSTER_LAYOUT.reelCount - 1) * CRAZY_ROOSTER_LAYOUT.reelSpacing;
    const machineHeight =
      CRAZY_ROOSTER_LAYOUT.rowCount * CRAZY_ROOSTER_LAYOUT.symbolHeight +
      (CRAZY_ROOSTER_LAYOUT.rowCount - 1) * CRAZY_ROOSTER_LAYOUT.rowSpacing;
    const reelStopMatch = /^round\.reel\.stop\.(\d)(?:\.bonusHold)?$/.exec(cue);

    if (reelStopMatch) {
      const reelIndex = Number(reelStopMatch[1]) - 1;
      const plaqueIndex = Math.max(0, Math.min(3, reelIndex + (cue.endsWith("bonusHold") ? 1 : 0)));
      this.topperMascot.pulseForReelStop(reelIndex);
      this.visualChrome.triggerPresentationCue({
        tone: cue.endsWith("bonusHold") ? "bonus" : "standard",
        title: cue.endsWith("bonusHold") ? "BONUS HOLD" : `REEL ${reelIndex + 1} LOCK`,
        caption: cue.endsWith("bonusHold") ? "FINAL REEL HELD" : "STOP CASCADE",
        holdMs: cue.endsWith("bonusHold") ? 760 : 340,
        plaqueIndexes: [plaqueIndex],
      });
      if (!cue.endsWith("bonusHold")) {
        this.particleBurst.play(
          machineWidth * (0.22 + reelIndex * 0.28),
          machineHeight * 0.58,
        );
      }
      return;
    }

    if (cue === "focus-status-banner") {
      this.showStatus("LIGHTNING BOOST");
      this.topperMascot.setState("react_boost_start");
      this.visualChrome.triggerPresentationCue({
        tone: "boost",
        title: "LIGHTNING BOOST",
        caption: "STAGE IGNITE",
        holdMs: 980,
      });
      void this.layeredFx.playBoostStart();
      this.particleBurst.play(machineWidth * 0.5, machineHeight * 0.2);
      this.applySoundCue("feature-boost-enter");
      return;
    }

    if (cue === "feature.buyBonus.enter") {
      const buyMode = this.activeMathBridgeHints?.mode ?? "base";
      if (buyMode === "base") {
        return;
      }
      const buyLabel = this.resolveBuyBonusModeLabel(buyMode);
      this.showStatus(`${buyLabel} ENTRY`);
      this.topperMascot.setState("react_boost_start");
      this.visualChrome.triggerPresentationCue({
        tone: "bonus",
        title: "BUY BONUS ENTRY",
        caption: `${buyLabel} ROUTED`,
        holdMs: 960,
      });
      void this.layeredFx.playBoostStart();
      this.layeredFx.playCoinFly(machineWidth * 0.5, machineHeight * 0.32, 6);
      this.particleBurst.play(machineWidth * 0.5, machineHeight * 0.24);
      this.applySoundCue("feature-bonus-enter");
      return;
    }

    if (cue === "feature.collect.triggered") {
      this.showStatus("COLLECT RUN");
      this.topperMascot.setState("react_collect");
      this.visualChrome.triggerPresentationCue({
        tone: "collect",
        title: "COLLECT RUN",
        caption: "COIN SWEEP",
        holdMs: 940,
      });
      this.layeredFx.playCollectSweep(machineWidth * 0.5, machineHeight * 0.44);
      this.particleBurst.play(machineWidth * 0.5, machineHeight * 0.44);
      this.applySoundCue("feature-collect-enter");
      return;
    }

    if (cue === "collect-sweep") {
      this.layeredFx.playCoinFly(machineWidth * 0.5, machineHeight * 0.32, 5);
      this.particleBurst.play(machineWidth * 0.5, machineHeight * 0.44);
      return;
    }

    if (cue === "feature.boost.triggered") {
      this.showStatus("BOOST CHARGE");
      this.topperMascot.setState("react_boost_loop");
      this.visualChrome.triggerPresentationCue({
        tone: "boost",
        title: "BOOST CHARGE",
        caption: "LIGHTNING SURGE",
        holdMs: 1020,
      });
      void this.layeredFx.playBoostStart();
      this.particleBurst.play(machineWidth * 0.5, machineHeight * 0.22);
      this.applySoundCue("feature-boost-enter");
      return;
    }

    if (cue === "feature.bonus.enter") {
      this.showStatus("HOLD & WIN");
      this.topperMascot.setState("react_bigwin");
      this.visualChrome.triggerPresentationCue({
        tone: "bonus",
        title: "HOLD & WIN",
        caption: "LOCKED ENTRY",
        holdMs: 1020,
      });
      this.layeredFx.playBoostLoop();
      this.layeredFx.playCoinFly(machineWidth * 0.5, machineHeight * 0.2, 7);
      this.particleBurst.play(machineWidth * 0.5, machineHeight * 0.18);
      this.applySoundCue("feature-bonus-enter");
      return;
    }

    if (cue === "feature.jackpot.attached" || cue === "jackpot-overlay") {
      this.showStatus("JACKPOT CALL");
      this.topperMascot.setState("react_jackpot");
      this.visualChrome.triggerPresentationCue({
        tone: "jackpot",
        title: "JACKPOT CALL",
        caption:
          this.activeMathBridgeHints?.jackpotTier?.toUpperCase() ?? "PLAQUE STRIKE",
        holdMs: 1220,
        jackpotTier: this.activeMathBridgeHints?.jackpotTier ?? null,
      });
      this.layeredFx.playJackpotHit(
        this.resolveJackpotReactionLevel(this.activeMathBridgeHints?.jackpotTier ?? null),
      );
      this.particleBurst.play(machineWidth * 0.5, machineHeight * 0.1);
      this.applySoundCue("feature-jackpot-enter");
      return;
    }

    if (cue === "coin-fly") {
      this.layeredFx.playCoinFly(machineWidth * 0.5, machineHeight * 0.32, 6);
      this.particleBurst.play(machineWidth * 0.5, machineHeight * 0.32);
      return;
    }

    if (cue === "overlay.winTier.enter") {
      const tier = this.activeMathBridgeHints?.winTier ?? "none";
      if (tier !== "none") {
        this.topperMascot.setState("react_bigwin");
      }
      this.visualChrome.triggerPresentationCue({
        tone: tier === "mega" ? "jackpot" : "standard",
        title: tier === "none" ? "TOTAL WIN" : `${tier.toUpperCase()} WIN`,
        caption: "COUNTER CHARGE",
        holdMs: 1080,
        jackpotTier: this.activeMathBridgeHints?.jackpotTier ?? null,
      });
      this.layeredFx.playWinPulse(tier);
      this.applySoundCue("feature-win-tier");
      return;
    }

    if (cue === "round.reel.stop.3.bonusHold") {
      this.showStatus("BONUS HOLD");
      this.visualChrome.triggerPresentationCue({
        tone: "bonus",
        title: "BONUS HOLD",
        caption: "FINAL REEL HELD",
        holdMs: 760,
      });
      return;
    }
  }

  private resolveBuyBonusModeLabel(mode: MathBridgePresentationHints["mode"]): string {
    switch (mode) {
      case "buy75":
        return "BUY 75x";
      case "buy200":
        return "BUY 200x";
      case "buy300":
        return "BUY 300x";
      case "base":
      default:
        return "BASE";
    }
  }

  private refreshHudState(winAmount: number): void {
    const session = SessionRuntimeStore.getSnapshot();
    this.hud.setState({
      balance: session?.balance ?? 0,
      bet: ResolvedRuntimeConfigStore.limits.defaultBet,
      win: winAmount,
      turboSelected: this.turboSelected || this.holdTurboRequested,
      soundEnabled: this.soundEnabled,
    });

    const buttons = (this.hud as unknown as HudInternals).buttons;
    buttons.autoplay.text = this.autoplayActive ? "STOP AUTO" : "AUTO";
    const buyLabel =
      CRAZY_ROOSTER_BUY_TIERS[this.buyTierIndex % CRAZY_ROOSTER_BUY_TIERS.length]?.label ?? "BUY";
    buttons.buyFeature.text = buyLabel;
    this.visualChrome.setModeState({
      buyLabel,
      autoplayActive: this.autoplayActive,
      turboSelected: this.turboSelected || this.holdTurboRequested,
      soundEnabled: this.soundEnabled,
      statusText: this.statusText.text || "BETONLINE READY",
    });
    this.hudChrome.setState({
      buyLabel,
      autoplayActive: this.autoplayActive,
      turboSelected: this.turboSelected || this.holdTurboRequested,
      soundEnabled: this.soundEnabled,
      holdTurboRequested: this.holdTurboRequested,
    });
  }

  private showStatus(text: string): void {
    this.statusText.text = text;
    this.statusText.visible = this.showLegacyTopUi && text.length > 0;
    this.visualChrome.setModeState({
      statusText: text || "BETONLINE READY",
    });
  }

  private applyPreviewState(): void {
    const preview = buildPreviewPresentation();
    this.slotMachine.setPresentationColumns(preview.reels.stopColumns);
    this.paylineOverlay.clear();
    this.paylineHighlight.clear();
    this.debugOverlay.setMathBridgeSummary(null);
    this.layeredFx.clearPresentation();
    this.topperMascot.setState("idle");
    this.visualChrome.clearPresentationCue();
    const featureFrame = this.featureModules.resolve(preview);
    this.applyDynamicControlVisibility(featureFrame);
    PresentationStateStore.patch({
      isSpinning: false,
      isPresentingWin: false,
      turboSelected: this.turboSelected,
      soundEnabled: this.soundEnabled,
      lastWinAmount: preview.winAmount,
      lastMessages: preview.messages,
      activeFeatureModules: featureFrame.activeModuleIds,
      statusText: preview.labels.state ?? "idle",
    });
    this.showStatus(formatMessages([...preview.messages, ...featureFrame.messages]));
  }

  private syncHudChrome(): void {
    const buttons = (this.hud as unknown as HudInternals).buttons;
    this.hudChrome.attachButtons(buttons);
    for (const button of Object.values(buttons)) {
      button.alpha = 0.001;
    }
  }

  private skipWinPresentation(): void {
    if (!this.isPresentingWin) {
      return;
    }
    this.clearWinPresentationTimeout();
    this.finishWinPresentation("none", true);
  }

  private finishWinPresentation(
    policyTier: "none" | "big" | "huge" | "mega",
    skipped = false,
  ): void {
    this.wowVfx.finishWinPresentation();
    this.layeredFx.clearPresentation();
    this.topperMascot.setState("idle");
    this.paylineOverlay.clear();
    this.paylineHighlight.clear();
    this.isPresentingWin = false;
    this.activeMathBridgeHints = null;
    this.visualChrome.clearPresentationCue();
    PresentationStateStore.patch({
      isPresentingWin: false,
      statusText: skipped ? "ROUND_PRESENTATION_SKIPPED" : "ROUND_PRESENTATION_FINISHED",
    });
    this.scheduleAutoplaySpin(this.animationPolicy.getAutoplayDelayMs(policyTier, skipped));
  }

  private scheduleAutoplaySpin(delayMs: number): void {
    if (!this.autoplayActive) {
      return;
    }

    this.clearAutoplayTimeout();
    this.autoplayTimeout = window.setTimeout(() => {
      this.autoplayTimeout = null;
      if (!this.autoplayActive || this.isSpinning || this.isPresentingWin) {
        return;
      }
      void this.handleSpin();
    }, Math.max(120, delayMs));
  }

  private clearAutoplayTimeout(): void {
    if (this.autoplayTimeout !== null) {
      window.clearTimeout(this.autoplayTimeout);
      this.autoplayTimeout = null;
    }
  }

  private clearWinPresentationTimeout(): void {
    if (this.currentWinPresentationTimeout !== null) {
      window.clearTimeout(this.currentWinPresentationTimeout);
      this.currentWinPresentationTimeout = null;
    }
  }
}
