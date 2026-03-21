import { Assets, Container, Graphics, Point, Sprite, Text, Texture } from "pixi.js";

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
import {
  CrazyRoosterSlotMachine,
  type PresentationVariantMap,
} from "../../../game/slots/CrazyRoosterSlotMachine.ts";
import type { DonorMultiplierVariantKey } from "../../../game/slots/CrazyRoosterSymbol";
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
import { WinHighlight, type WinHighlightMotionProfile } from "../../../game/fx/WinHighlight";
import {
  DonorBuyBonusModal,
  type DonorBuyBonusVariant,
} from "../../../game/presentation/DonorBuyBonusModal";
import {
  DonorFeatureIntroOverlay,
  type DonorFeatureIntroVariant,
} from "../../../game/presentation/DonorFeatureIntroOverlay";
import { JackpotPlaqueController } from "../../../game/presentation/JackpotPlaqueController";
import { LayeredFxController } from "../../../game/presentation/LayeredFxController";
import { TopperMascotController } from "../../../game/presentation/TopperMascotController";
import { WinCounter } from "../../../game/ui/WinCounter";
import { Beta3VisualChrome } from "./Beta3VisualChrome";
import { DebugOverlay } from "./DebugOverlay";
import { HeroHudChrome } from "./HeroHudChrome";
import roosterGameLogoUrl from "../../../../raw-assets/preload{m}/rooster-logo.png?url";

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

type FxCuePoint = {
  x: number;
  y: number;
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

const DONOR_STABLE_JACKPOT_COLUMNS = [
  [3, 8, 7, 0],
  [1, 4, 6, 5],
  [8, 2, 9, 7],
] as const;

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

const DONOR_VIDEO_TIMING = {
  lineMinStandardMs: 1900,
  lineMinFeatureMs: 1600,
  boostChargeWindowMs: 1700,
  jackpotImpactWindowMs: 2100,
} as const;

const PREVIEW_PROOF_COLUMNS: Partial<Record<string, number[][]>> = {
  symbols: [
    [3, 1, 2, 0],
    [1, 4, 6, 5],
    [2, 3, 5, 4],
  ],
  collect: [
    [8, 1, 2, 8],
    [2, 8, 4, 6],
    [8, 2, 7, 8],
  ],
  boost: [
    [7, 9, 2, 0],
    [8, 8, 9, 1],
    [7, 9, 8, 2],
  ],
  bonus: [
    [8, 8, 1, 2],
    [9, 8, 9, 8],
    [8, 8, 2, 3],
  ],
  mega: [
    [9, 7, 8, 7],
    [9, 8, 7, 8],
    [9, 7, 8, 9],
  ],
};

const resolvePreviewProofState = (): string | null => {
  const params = new URLSearchParams(window.location.search);
  const requested = params.get("proofState")?.trim().toLowerCase();
  if (!requested) {
    return null;
  }
  if (requested === "collector") return "collect";
  if (requested === "lightning") return "boost";
  if (requested === "special") return "bonus";
  return requested;
};

const buildPreviewPresentation = (): RoundPresentationModel => {
  const proofState = resolvePreviewProofState();
  const stopColumns = PREVIEW_PROOF_COLUMNS[proofState ?? ""] ?? CRAZY_ROOSTER_IDLE_COLUMNS;
  const stateLabel = proofState ?? "idle";
  const messages =
    proofState === "symbols"
      ? ["SYMBOL SHOWCASE", "0..9 RENDER CHECK"]
      : ["BETONLINE READY", "8 FIXED LINES LIVE"];

  return {
    roundId: `preview-${stateLabel}`,
    winAmount: 0,
    slotIndex: 0,
    reels: {
      stopColumns,
    },
    symbolGrid: buildGridFromColumns(stopColumns),
    counters: {
      buyFeatureAvailable: true,
    },
    messages,
    soundCues: [],
    animationCues: [],
    labels: {
      state: stateLabel,
    },
  };
};

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
  private readonly fxOverlayLayer = new Container();
  private readonly uiLayer = new Container();
  private readonly visualChrome = new Beta3VisualChrome();
  private readonly topperMascot = new TopperMascotController();
  private readonly jackpotPlaques = new JackpotPlaqueController();
  private readonly slotMachine = new CrazyRoosterSlotMachine(resolveProviderSymbolRoot());
  private readonly layeredFx = new LayeredFxController();
  private readonly startupPaylineMap = new Graphics();
  private readonly startupPaylineLabels = new Container();
  private readonly runtimePaylineMap = new Graphics();
  private readonly runtimePaylineCallout = new Container();
  private readonly runtimePaylineCalloutShadow = new Graphics();
  private readonly runtimePaylineCalloutBase = new Graphics();
  private readonly runtimePaylineCalloutAccent = new Graphics();
  private readonly runtimePaylineBadge = new Graphics();
  private readonly runtimePaylineSequenceChip = new Graphics();
  private readonly runtimePaylineTitle = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 16,
      fontWeight: "900",
      fill: 0xfff1d1,
      stroke: { color: 0x220508, width: 4 },
      align: "center",
      letterSpacing: 0.6,
    },
  });
  private readonly runtimePaylineBadgeText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 24,
      fontWeight: "900",
      fill: 0xfff4d5,
      stroke: { color: 0x2a080b, width: 5 },
      align: "center",
    },
  });
  private readonly runtimePaylineMainText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 24,
      fontWeight: "900",
      fill: 0xffffff,
      stroke: { color: 0x1d0608, width: 5 },
      align: "center",
      letterSpacing: 0.4,
    },
  });
  private readonly runtimePaylineSequenceText = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 18,
      fontWeight: "900",
      fill: 0xfff0cf,
      stroke: { color: 0x250709, width: 4 },
      align: "center",
    },
  });
  private readonly paylineOverlay = new PaylineOverlay();
  private readonly paylineHighlight = new WinHighlight();
  private readonly winHighlight = new WinHighlight();
  private readonly particleBurst = new ParticleBurst();
  private readonly donorBuyBonusModal = new DonorBuyBonusModal();
  private readonly featureIntroOverlay = new DonorFeatureIntroOverlay();
  private readonly donorCountdownOverlay = new Container();
  private readonly donorCountdownBackdrop = new Graphics();
  private readonly donorCountdownGuide = new Graphics();
  private readonly donorCountdownCaption = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 28,
      fontWeight: "800",
      fill: 0xfff1be,
      stroke: { color: 0x2a0811, width: 5 },
      align: "center",
      letterSpacing: 1,
    },
  });
  private readonly donorCountdownValue = new Text({
    text: "",
    style: {
      fontFamily: "Trebuchet MS, Arial, sans-serif",
      fontSize: 116,
      fontWeight: "900",
      fill: 0xffdc78,
      stroke: { color: 0x2a0811, width: 8 },
      align: "center",
    },
  });
  private readonly winCounter = new WinCounter();
  private readonly hud = new PremiumTemplateHud();
  private readonly hudChrome = new HeroHudChrome();
  private readonly topLeftLogo = new Sprite(Texture.EMPTY);
  private readonly topRightMenu = new Graphics();
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
  private activeIntroPreviewGrid: number[][] | null = null;
  private currentWinPresentationTimeout: number | null = null;
  private autoplayTimeout: number | null = null;
  private spinHoldTimeout: number | null = null;
  private readonly mathBridgeTimeouts: number[] = [];
  private lastKnownMasterVolume = 0.8;
  private pendingRound: PendingRoundResolution | null = null;
  private jackpotFeatureCueConsumed = false;
  private startupPaylineGuideDismissed = false;
  private startupPaylineGlowElapsedMs = 0;
  private runtimePaylineGlowElapsedMs = 0;

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
        showWinCounter: (amountMinor, title, tier) => {
          this.winCounter.reportWin(amountMinor);
          if (!this.shouldShowWinCounter(amountMinor, tier)) {
            return;
          }
          this.winCounter.showWin(amountMinor, title, this.resolveTierStyleHook(tier));
        },
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
    this.reelsLayer.addChild(this.slotMachine);
    this.reelsLayer.addChild(this.topperMascot);
    this.reelsLayer.addChild(this.jackpotPlaques);
    this.fxLayer.addChild(this.layeredFx);
    this.startupPaylineMap.visible = false;
    this.startupPaylineLabels.visible = false;
    this.startupPaylineLabels.eventMode = "none";
    this.runtimePaylineMap.visible = false;
    this.runtimePaylineMap.eventMode = "none";
    this.runtimePaylineMap.blendMode = "add";
    this.runtimePaylineCallout.visible = false;
    this.runtimePaylineCallout.eventMode = "none";
    this.runtimePaylineTitle.anchor.set(0.5);
    this.runtimePaylineBadgeText.anchor.set(0.5);
    this.runtimePaylineMainText.anchor.set(0.5);
    this.runtimePaylineSequenceText.anchor.set(0.5);
    this.runtimePaylineCallout.addChild(
      this.runtimePaylineCalloutShadow,
      this.runtimePaylineCalloutBase,
      this.runtimePaylineCalloutAccent,
      this.runtimePaylineBadge,
      this.runtimePaylineSequenceChip,
      this.runtimePaylineTitle,
      this.runtimePaylineBadgeText,
      this.runtimePaylineMainText,
      this.runtimePaylineSequenceText,
    );
    this.fxLayer.addChild(this.startupPaylineMap);
    this.fxLayer.addChild(this.startupPaylineLabels);
    this.fxLayer.addChild(this.runtimePaylineMap);
    this.fxLayer.addChild(this.paylineHighlight);
    this.fxLayer.addChild(this.winHighlight);
    this.fxLayer.addChild(this.paylineOverlay);
    this.fxLayer.addChild(this.runtimePaylineCallout);
    this.fxLayer.addChild(this.particleBurst);
    this.donorCountdownValue.anchor.set(0.5);
    this.donorCountdownCaption.anchor.set(0.5);
    this.donorCountdownOverlay.visible = false;
    this.donorCountdownOverlay.eventMode = "none";
    this.donorCountdownOverlay.addChild(
      this.donorCountdownBackdrop,
      this.donorCountdownGuide,
      this.donorCountdownCaption,
      this.donorCountdownValue,
    );
    this.fxLayer.addChild(this.donorCountdownOverlay);
    this.layeredFx.setOverlayLayer(this.fxOverlayLayer);

    this.statusText.anchor.set(0.5);
    this.statusText.visible = false;
    this.titleText.anchor.set(0.5);
    this.titleText.visible = this.showLegacyTopUi;
    this.footerText.anchor.set(0.5);
    this.topLeftLogo.anchor.set(0, 0);
    this.topLeftLogo.texture = Texture.from(roosterGameLogoUrl);
    this.topLeftLogo.alpha = 0.95;
    this.topLeftLogo.visible = false;
    this.topRightMenu.visible = false;
    void Assets.load(roosterGameLogoUrl)
      .then(() => {
        this.topLeftLogo.texture = Texture.from(roosterGameLogoUrl);
        const viewport = engine().layout.getViewport();
        this.resize(viewport.width, viewport.height);
      })
      .catch(() => {
        // Keep the existing placeholder texture if the donor logo asset is unavailable.
      });
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
    this.uiLayer.addChild(this.topLeftLogo);
    this.uiLayer.addChild(this.topRightMenu);
    this.uiLayer.addChild(this.fxOverlayLayer);
    this.uiLayer.addChild(this.hudChrome);
    this.uiLayer.addChild(this.hud);
    this.uiLayer.addChild(this.statusText);
    this.uiLayer.addChild(this.titleText);
    this.uiLayer.addChild(this.footerText);
    this.uiLayer.addChild(this.featureIntroOverlay);
    this.uiLayer.addChild(this.donorBuyBonusModal);

    this.slotMachine.onSpinComplete = () => this.handleSpinComplete();
    this.slotMachine.onReelStop = (reelIndex) => this.handleRuntimeReelStopCue(reelIndex);
    this.connectSpinHoldGesture();
    this.applyPreviewState();
    this.refreshHudState(0);
    this.showStatus(this.resolveBenchmarkReadyStatus());
  }

  public prepare(): void {}

  public update(ticker: { deltaMS: number }): void {
    if (this.paused) return;
    if (
      !this.startupPaylineGuideDismissed &&
      !this.isSpinning &&
      !this.isPresentingWin &&
      !this.startupPaylineMap.visible
    ) {
      this.showStartupPaylineGuideOnLoad();
    }
    const deltaMs = Math.max(0, ticker.deltaMS);
    this.startupPaylineGlowElapsedMs += deltaMs;
    if (this.startupPaylineMap.visible) {
      const pulse = 0.82 + Math.sin(this.startupPaylineGlowElapsedMs * 0.012) * 0.08;
      this.startupPaylineMap.alpha = pulse;
      this.startupPaylineLabels.alpha = 0.92 + Math.sin(this.startupPaylineGlowElapsedMs * 0.01) * 0.03;
    }
    this.runtimePaylineGlowElapsedMs += deltaMs;
    if (this.runtimePaylineMap.visible) {
      const pulse = 0.9 + Math.sin(this.runtimePaylineGlowElapsedMs * 0.015) * 0.05;
      this.runtimePaylineMap.alpha = pulse;
      this.runtimePaylineCallout.alpha =
        0.96 + Math.sin(this.runtimePaylineGlowElapsedMs * 0.012) * 0.03;
    }
    this.layeredFx.update(deltaMs);
    this.featureIntroOverlay.update(deltaMs);
    this.donorBuyBonusModal.update(deltaMs);
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
    this.startupPaylineGuideDismissed = false;
    this.clearStartupPaylineGuide();
    this.clearRuntimePaylinePresentation();
    this.clearMathBridgeTimeouts();
    this.layeredFx.clearPresentation();
    this.topperMascot.setState("idle");
    this.jackpotPlaques.clear();
    this.paylineOverlay.clear();
    this.paylineHighlight.clear();
    this.debugOverlay.setMathBridgeSummary(null);
    this.visualChrome.clearPresentationCue();
    this.activeMathBridgeHints = null;
    this.activeIntroPreviewGrid = null;
    this.featureIntroOverlay.clear();
    this.donorBuyBonusModal.clear();
    this.showStartupPaylineGuideOnLoad();
  }

  public resize(width: number, height: number): void {
    const viewport = engine().layout.getViewport();
    const safe = viewport.safeArea;
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const isPortrait = viewport.orientation === "portrait";
    const hudSpace = isPortrait ? 190 : 128;
    const centerX = width * 0.5 + (isDonorlocal && !isPortrait ? 3 : 0);
    const availableTop = safe.top + (isPortrait ? 66 : 94);
    const availableBottom = height - safe.bottom - hudSpace;
    const centerY =
      (availableTop + availableBottom) * 0.5 +
      (isPortrait ? 38 : 18 + (isDonorlocal ? 10 : 0));

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
    const providerScaleMultiplier = isDonorlocal ? (isPortrait ? 1.04 : 0.954) : 1.06;
    const scale =
      Math.min(availableWidth / chromeWidth, availableHeight / chromeHeight) *
      providerScaleMultiplier;
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
    this.visualChrome.setViewportOrientation(viewport.orientation);
    this.topperMascot.resize(machineWidth, machineHeight);
    this.jackpotPlaques.resize(machineWidth);
    this.layeredFx.resize(machineWidth, machineHeight);
    const showDonorPortraitChrome = isDonorlocal && isPortrait;
    this.topperMascot.visible = true;
    this.jackpotPlaques.visible = isDonorlocal;
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
    this.hudChrome.setState({ orientation: viewport.orientation });

    this.titleText.x = width * 0.5;
    this.titleText.y = safe.top + 28;
    this.statusText.x = width * 0.5;
    this.statusText.y = safe.top + 68;
    this.winCounter.x = width * 0.5;
    this.winCounter.y = safe.top + 156;
    this.footerText.x = width * 0.5;
    this.footerText.y = height - Math.max(24, safe.bottom + 18);

    this.topLeftLogo.visible = showDonorPortraitChrome;
    this.topRightMenu.visible = showDonorPortraitChrome;
    if (showDonorPortraitChrome) {
      const logoTexture = this.topLeftLogo.texture;
      const desiredLogoWidth = 180;
      const logoRatio =
        logoTexture.width > 0 && logoTexture.height > 0
          ? logoTexture.height / logoTexture.width
          : 0.68;
      this.topLeftLogo.width = desiredLogoWidth;
      this.topLeftLogo.height = desiredLogoWidth * logoRatio;
      this.topLeftLogo.x = safe.left + 18;
      this.topLeftLogo.y = safe.top + 10;

      const menuSize = 54;
      const menuX = width - safe.right - menuSize;
      const menuY = safe.top + menuSize;
      this.topRightMenu.clear();
      this.topRightMenu.circle(menuX, menuY, menuSize * 0.5);
      this.topRightMenu.fill({ color: 0x1d0709, alpha: 0.84 });
      this.topRightMenu.stroke({ color: 0xf2d392, width: 2, alpha: 0.75 });
      for (let index = 0; index < 3; index += 1) {
        const lineY = menuY - 10 + index * 10;
        this.topRightMenu.moveTo(menuX - 12, lineY);
        this.topRightMenu.lineTo(menuX + 12, lineY);
      }
      this.topRightMenu.stroke({
        color: 0xf9f1e1,
        width: 3,
        alpha: 0.9,
        cap: "round",
      });
    } else {
      this.topRightMenu.clear();
    }

    this.debugOverlay.resize(width, height);
    this.featureIntroOverlay.resize(width, height);
    this.donorBuyBonusModal.resize(width, height);
    this.layoutDonorCountdownOverlay(machineWidth, machineHeight);
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
    this.showStartupPaylineGuideOnLoad();
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
    this.clearStartupPaylineGuide();
    this.clearRuntimePaylinePresentation();
    this.clearMathBridgeTimeouts();
    this.layeredFx.clearPresentation();
    this.topperMascot.setState("idle");
    this.jackpotPlaques.clear();
    this.visualChrome.clearPresentationCue();
    this.activeMathBridgeHints = null;
    this.activeIntroPreviewGrid = null;
    this.featureIntroOverlay.clear();
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

  private isDonorFeatureScenario(
    mathBridgeHints: MathBridgePresentationHints | null = this.activeMathBridgeHints,
  ): boolean {
    return (
      getProviderPackStatus().effectiveProvider === "donorlocal" &&
      Boolean(
        mathBridgeHints?.triggers.boost ||
          mathBridgeHints?.triggers.bonus ||
          mathBridgeHints?.triggers.jackpot,
      )
    );
  }

  private shouldSuppressFeatureCueForScenario(
    cue: string,
    mathBridgeHints: MathBridgePresentationHints | null = this.activeMathBridgeHints,
  ): boolean {
    if (!mathBridgeHints || !this.isDonorFeatureScenario(mathBridgeHints)) {
      return false;
    }

    const dominantFeatureCue = this.resolveDominantFeatureCue(mathBridgeHints);
    if (cue.startsWith("feature.") && dominantFeatureCue && cue !== dominantFeatureCue) {
      const allowBoostBeforeJackpot =
        dominantFeatureCue === "feature.jackpot.attached" &&
        cue === "feature.boost.triggered";
      if (!allowBoostBeforeJackpot) {
        return true;
      }
    }

    return (
      cue === "collect-sweep" ||
      cue === "focus-status-banner" ||
      cue === "coin-fly" ||
      cue === "jackpot-overlay" ||
      cue === "overlay.winTier.enter"
    );
  }

  private resolveDominantFeatureCue(
    mathBridgeHints: MathBridgePresentationHints | null = this.activeMathBridgeHints,
  ): string | null {
    if (!mathBridgeHints) {
      return null;
    }

    if (mathBridgeHints.triggers.jackpot) {
      return "feature.jackpot.attached";
    }
    if (mathBridgeHints.triggers.bonus) {
      return "feature.bonus.enter";
    }
    if (mathBridgeHints.triggers.boost) {
      return "feature.boost.triggered";
    }
    if (mathBridgeHints.triggers.collect) {
      return "feature.collect.triggered";
    }
    return null;
  }

  private shouldSuppressFeatureCueInSchedule(
    cue: string,
    mathBridgeHints: MathBridgePresentationHints,
  ): boolean {
    if (!this.isDonorFeatureScenario(mathBridgeHints)) {
      return false;
    }

    const dominantFeatureCue = this.resolveDominantFeatureCue(mathBridgeHints);
    if (cue.startsWith("feature.")) {
      const allowBoostBeforeJackpot =
        dominantFeatureCue === "feature.jackpot.attached" &&
        cue === "feature.boost.triggered";
      if (allowBoostBeforeJackpot) {
        return false;
      }
      return dominantFeatureCue !== null && cue !== dominantFeatureCue;
    }

    if (cue.startsWith("round.reel.stop")) {
      return false;
    }

    if (cue === "overlay.totalSummary.update") {
      return false;
    }

    if (
      cue === "collect-sweep" ||
      cue === "coin-fly" ||
      cue === "focus-status-banner" ||
      cue === "jackpot-overlay" ||
      cue === "overlay.winTier.enter"
    ) {
      return true;
    }

    if (cue.startsWith("feature.")) {
      return (
        dominantFeatureCue !== null &&
        cue !== dominantFeatureCue
      );
    }
    if (cue.startsWith("overlay.")) {
      return true;
    }
    return false;
  }

  private isDonorFeatureIntroActive(): boolean {
    const introVariant = (
      this.featureIntroOverlay as unknown as {
        activeVariant?: string | null;
      }
    ).activeVariant;
    return (
      getProviderPackStatus().effectiveProvider === "donorlocal" &&
      Boolean(this.featureIntroOverlay.visible || introVariant)
    );
  }

  private scheduleAfterDonorFeatureIntro(
    action: () => void,
    initialDelayMs = 0,
    maxWaitMs = 6400,
  ): void {
    const beginWait = (startedAt: number) => {
      if (!this.isDonorFeatureIntroActive()) {
        action();
        return;
      }
      if (performance.now() - startedAt >= maxWaitMs) {
        action();
        return;
      }
      const pollTimeout = window.setTimeout(() => beginWait(startedAt), 90);
      this.mathBridgeTimeouts.push(pollTimeout);
    };

    const kickoff = () => beginWait(performance.now());
    if (initialDelayMs > 0) {
      const initialTimeout = window.setTimeout(kickoff, Math.max(0, initialDelayMs));
      this.mathBridgeTimeouts.push(initialTimeout);
    } else {
      kickoff();
    }
  }

  private resolveDonorBuyFeatureIntroVariant(): DonorFeatureIntroVariant {
    switch (this.activeMathBridgeHints?.mode) {
      case "buy75":
        return "blitz";
      case "buy200":
        return "power";
      case "buy300":
        return "ultimate";
      default:
        return this.activeMathBridgeHints?.triggers.boost ? "power" : "ultimate";
    }
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
    this.clearRuntimePaylinePresentation();
    this.paylineOverlay.clear();
    this.paylineHighlight.clear();
    this.activeIntroPreviewGrid = null;
    this.featureIntroOverlay.clear();
    this.clearDonorCountdownPhase();
  }

  private resolveReelStopCue(
    reelIndex: number,
    mathBridgeHints: MathBridgePresentationHints,
  ): "round.reel.stop.1" | "round.reel.stop.2" | "round.reel.stop.3" | "round.reel.stop.3.bonusHold" | null {
    if (reelIndex === 0) {
      return "round.reel.stop.1";
    }
    if (reelIndex === 1) {
      return "round.reel.stop.2";
    }
    if (reelIndex !== 2) {
      return null;
    }
    return mathBridgeHints.timingHints.reelStopDelaysMs[2] >= 3000
      ? "round.reel.stop.3.bonusHold"
      : "round.reel.stop.3";
  }

  private handleRuntimeReelStopCue(reelIndex: number): void {
    const mathBridgeHints = this.activeMathBridgeHints;
    if (!mathBridgeHints) {
      return;
    }
    const cue = this.resolveReelStopCue(reelIndex, mathBridgeHints);
    if (!cue) {
      return;
    }
    this.applyAnimationCue(cue);
  }

  private clearFeatureScenePresentation(): void {
    this.layeredFx.clearPresentation();
    this.particleBurst.clear();
    this.winHighlight.clear();
    this.clearRuntimePaylinePresentation();
    this.paylineOverlay.clear();
    this.paylineHighlight.clear();
    this.clearDonorCountdownPhase();
  }

  private layoutDonorCountdownOverlay(machineWidth: number, machineHeight: number): void {
    this.donorCountdownBackdrop.clear();
    this.donorCountdownBackdrop.roundRect(0, 0, machineWidth, machineHeight, 18);
    this.donorCountdownBackdrop.fill({ color: 0x0d0718, alpha: 0.78 });
    this.donorCountdownBackdrop.stroke({
      color: 0xd8b15a,
      width: 3,
      alpha: 0.62,
    });
    this.donorCountdownGuide.clear();
    this.donorCountdownCaption.x = machineWidth * 0.5;
    this.donorCountdownCaption.y = machineHeight * 0.28;
    this.donorCountdownValue.x = machineWidth * 0.5;
    this.donorCountdownValue.y = machineHeight * 0.5;
  }

  private playDonorCountdownPhase(
    values: number[] = [0, 1, 2, 3],
    stepMs: number | number[] = 250,
    tailMs = 2200,
    options: {
      mode?: "countdown" | "lineCount";
      caption?: string;
      guideYRatio?: number;
    } = {},
  ): number {
    if (getProviderPackStatus().effectiveProvider !== "donorlocal") {
      return 0;
    }

    const mode = options.mode ?? "countdown";
    const boardWidth =
      CRAZY_ROOSTER_LAYOUT.reelCount * CRAZY_ROOSTER_LAYOUT.symbolWidth +
      (CRAZY_ROOSTER_LAYOUT.reelCount - 1) * CRAZY_ROOSTER_LAYOUT.reelSpacing;
    const boardHeight =
      CRAZY_ROOSTER_LAYOUT.rowCount * CRAZY_ROOSTER_LAYOUT.symbolHeight +
      (CRAZY_ROOSTER_LAYOUT.rowCount - 1) * CRAZY_ROOSTER_LAYOUT.rowSpacing;
    this.clearDonorCountdownPhase();
    this.donorCountdownOverlay.visible = true;
    this.donorCountdownOverlay.alpha = 1;
    this.donorCountdownCaption.text =
      options.caption ?? (mode === "lineCount" ? "TOP LINE COUNT" : "");
    this.donorCountdownCaption.visible = this.donorCountdownCaption.text.length > 0;
    this.donorCountdownValue.scale.set(mode === "lineCount" ? 0.86 : 1);
    this.donorCountdownValue.y = mode === "lineCount" ? boardHeight * 0.46 : boardHeight * 0.5;
    this.donorCountdownGuide.clear();
    if (mode === "lineCount") {
      const guideY = boardHeight * Math.max(0.08, Math.min(0.92, options.guideYRatio ?? 0.22));
      this.donorCountdownGuide.moveTo(24, guideY);
      this.donorCountdownGuide.lineTo(boardWidth - 24, guideY);
      this.donorCountdownGuide.stroke({
        color: 0xffca54,
        width: 5,
        alpha: 0.92,
        cap: "round",
      });
      this.donorCountdownGuide.moveTo(24, guideY);
      this.donorCountdownGuide.lineTo(boardWidth - 24, guideY);
      this.donorCountdownGuide.stroke({
        color: 0xfff6d5,
        width: 2.4,
        alpha: 0.86,
        cap: "round",
      });
    }

    let elapsedMs = 0;
    values.forEach((value, index) => {
      const stepValue = Array.isArray(stepMs)
        ? stepMs[Math.min(index, stepMs.length - 1)] ?? 250
        : stepMs;
      const timeout = window.setTimeout(() => {
        this.donorCountdownValue.text = `${value}`;
        const flashAlpha =
          mode === "lineCount"
            ? value >= values[values.length - 1]
              ? 0.74
              : 0.64
            : value >= values[values.length - 1]
              ? 0.88
              : 0.78;
        this.donorCountdownBackdrop.clear();
        this.donorCountdownBackdrop.roundRect(0, 0, boardWidth, boardHeight, 18);
        this.donorCountdownBackdrop.fill({
          color: mode === "lineCount" ? 0x12071f : 0x0b0615,
          alpha: flashAlpha,
        });
        this.donorCountdownBackdrop.stroke({
          color: mode === "lineCount" ? 0xffb036 : value >= 2 ? 0xeb4b2b : 0xd8b15a,
          width: mode === "lineCount" ? 3 : 4,
          alpha: mode === "lineCount" ? 0.58 : 0.68,
        });
      }, elapsedMs);
      this.mathBridgeTimeouts.push(timeout);
      elapsedMs += Math.max(80, Math.floor(stepValue));
    });

    const totalMs = elapsedMs + tailMs;
    const hideTimeout = window.setTimeout(() => {
      this.clearDonorCountdownPhase();
    }, totalMs);
    this.mathBridgeTimeouts.push(hideTimeout);

    return totalMs;
  }

  private clearDonorCountdownPhase(): void {
    this.donorCountdownOverlay.visible = false;
    this.donorCountdownGuide.clear();
    this.donorCountdownCaption.text = "";
    this.donorCountdownCaption.visible = false;
    this.donorCountdownValue.scale.set(1);
    this.donorCountdownValue.text = "";
  }

  private shouldUseDonorCountdownPrelude(
    mathBridgeHints: MathBridgePresentationHints | null = this.activeMathBridgeHints,
  ): boolean {
    if (!this.isDonorFeatureScenario(mathBridgeHints)) {
      return false;
    }
    if ((mathBridgeHints?.mode ?? "base") !== "base") {
      return false;
    }
    const countdownParam = new URLSearchParams(window.location.search).get("donorCountdownPrelude");
    // Keep countdown prelude explicit to avoid unintended full-screen counting on normal rounds.
    return countdownParam === "1";
  }

  private shouldUseDonorLineCountPrelude(
    mathBridgeHints: MathBridgePresentationHints | null = this.activeMathBridgeHints,
  ): boolean {
    if (!this.isDonorFeatureScenario(mathBridgeHints)) {
      return false;
    }
    if ((mathBridgeHints?.mode ?? "base") !== "base") {
      return false;
    }
    // Keep line-count prelude explicit to avoid broad full-screen counting in normal donorlocal flow.
    const lineCountParam = new URLSearchParams(window.location.search).get("donorLineCountPrelude");
    return lineCountParam === "1";
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
    const boostCueDelay = featureDelay + 90;
    const summaryDelay =
      lineCount > 0
        ? Math.max(lineTiming.delayMs, lineTiming.sequenceEndDelayMs - Math.round(lineTiming.durationMs * 0.4))
        : Math.max(0, mathBridgeHints.timingHints.lineHighlightDelayMs);

    for (const cue of mathBridgeHints.eventTriggers) {
      if (this.shouldSuppressFeatureCueInSchedule(cue, mathBridgeHints)) {
        continue;
      }
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
        mathBridgeHints.triggers.bonus &&
        !mathBridgeHints.triggers.jackpot &&
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
        delay = boostCueDelay;
      } else if (cue === "feature.bonus.enter") {
        delay = featureDelay + 170;
      } else if (cue === "feature.jackpot.attached") {
        delay =
          getProviderPackStatus().effectiveProvider === "donorlocal" &&
          mathBridgeHints.triggers.boost
            ? boostCueDelay + DONOR_VIDEO_TIMING.boostChargeWindowMs
            : featureDelay + 250;
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
    if (linePresentations.length === 0) {
      return;
    }

    if (!mathBridgeHints) {
      const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
      const durationMs = isDonorlocal ? 1600 : 760;
      const gapMs = isDonorlocal ? 160 : 100;
      const hideTailMs = isDonorlocal ? 140 : 80;
      const flashWindowMs = Math.max(
        980,
        linePresentations.length * (durationMs + gapMs) + 320,
      );
      this.slotMachine.flashPaylines(flashWindowMs);
      linePresentations.forEach((linePresentation, index) => {
        const startDelay = index * (durationMs + gapMs);
        const showTimeout = window.setTimeout(() => {
          const styleHook = isDonorlocal ? "neon" : "subtle";
          this.paylineHighlight.showWin(linePresentation.symbols, styleHook, null);
          if (isDonorlocal) {
            this.showRuntimePaylinePresentation(
              linePresentation,
              "standard",
              index,
              linePresentations.length,
            );
          } else {
            this.fxLayer.addChild(this.paylineOverlay);
            this.paylineOverlay.showLine(linePresentation, {
              durationMs,
              sequenceCount: linePresentations.length,
              sequenceIndex: index,
              styleHook,
              tone: "standard",
            });
          }
        }, startDelay);
        const hideTimeout = window.setTimeout(() => {
          this.clearRuntimePaylinePresentation();
          this.paylineOverlay.clear();
          this.paylineHighlight.clear();
        }, startDelay + durationMs + hideTailMs);
        this.mathBridgeTimeouts.push(showTimeout, hideTimeout);
      });
      return;
    }

    const timing = this.resolveLineSequenceTiming(mathBridgeHints, linePresentations.length);
    const hideTailMs =
      getProviderPackStatus().effectiveProvider === "donorlocal" ? 140 : 80;
    const tone = this.resolveLineTone(mathBridgeHints);
    const flashWindowMs = Math.max(
      980,
      timing.sequenceEndDelayMs + Math.round(timing.durationMs * 0.75),
    );
    this.slotMachine.flashPaylines(flashWindowMs);

    linePresentations.forEach((linePresentation, index) => {
      const startDelay = timing.delayMs + index * (timing.durationMs + timing.gapMs);
      const showTimeout = window.setTimeout(() => {
        const styleHook = this.resolveLineStyleHook(mathBridgeHints, index, linePresentations.length);
        this.paylineHighlight.showWin(
          linePresentation.symbols,
          styleHook,
          this.resolveLineMotionProfile(
            linePresentation,
            tone,
            mathBridgeHints,
            index,
            linePresentations.length,
          ),
        );
        if (getProviderPackStatus().effectiveProvider === "donorlocal") {
          this.showRuntimePaylinePresentation(
            linePresentation,
            tone,
            index,
            linePresentations.length,
          );
        } else {
          this.fxLayer.addChild(this.paylineOverlay);
          this.paylineOverlay.showLine(linePresentation, {
            durationMs: timing.durationMs,
            sequenceCount: linePresentations.length,
            sequenceIndex: index,
            styleHook,
            tone,
          });
        }
        this.playLinePresentationChoreography(
          linePresentation,
          mathBridgeHints,
          tone,
          index,
          linePresentations.length,
        );
      }, startDelay);
      const hideTimeout = window.setTimeout(() => {
        this.clearRuntimePaylinePresentation();
        this.paylineOverlay.clear();
        this.paylineHighlight.clear();
      }, startDelay + timing.durationMs + hideTailMs);
      this.mathBridgeTimeouts.push(showTimeout, hideTimeout);
    });
  }

  private resolveWinPresentationAnimationCues(
    mathBridgeHints: MathBridgePresentationHints | null,
    presentationCues: string[],
    featureCues: string[],
  ): string[] {
    const scheduledMathBridgeCues = new Set(mathBridgeHints?.eventTriggers ?? []);
    const donorFeatureSceneActive =
      getProviderPackStatus().effectiveProvider === "donorlocal" &&
      Boolean(
        mathBridgeHints?.triggers.boost ||
          mathBridgeHints?.triggers.bonus ||
          mathBridgeHints?.triggers.jackpot,
      );
    const donorSuppressedCues = donorFeatureSceneActive
      ? new Set([
          "focus-status-banner",
          "collect-sweep",
          "coin-fly",
          "hold-and-win-frame",
          "jackpot-overlay",
          "overlay.winTier.enter",
        ])
      : null;
    const merged = [...presentationCues, ...featureCues];
    return merged.filter((cue, index) => {
      if (!cue) {
        return false;
      }
      if (scheduledMathBridgeCues.has(cue)) {
        return false;
      }
      if (donorSuppressedCues?.has(cue)) {
        return false;
      }
      return merged.indexOf(cue) === index;
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
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const delayMs = Math.max(
      isDonorlocal ? 80 : 0,
      mathBridgeHints.timingHints.lineHighlightDelayMs,
    );
    const hasFeatureTone =
      mathBridgeHints.triggers.collect ||
      mathBridgeHints.triggers.boost ||
      mathBridgeHints.triggers.bonus ||
      mathBridgeHints.triggers.jackpot;
    const minDurationMs = isDonorlocal
      ? hasFeatureTone
        ? DONOR_VIDEO_TIMING.lineMinFeatureMs
        : DONOR_VIDEO_TIMING.lineMinStandardMs
      : 360;
    const durationMs = Math.max(minDurationMs, mathBridgeHints.timingHints.lineHighlightDurationMs);
    const gapMs = isDonorlocal
      ? Math.min(280, Math.max(140, Math.round(durationMs * 0.18)))
      : Math.min(180, Math.max(70, Math.round(durationMs * 0.18)));
    const sequenceEndDelayMs =
      lineCount > 0
        ? delayMs +
          lineCount * durationMs +
          Math.max(0, lineCount - 1) * gapMs +
          (isDonorlocal ? 180 : 0)
        : delayMs;
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
    return "neon";
  }

  private resolveLineMotionProfile(
    linePresentation: ResolvedLinePresentation,
    tone: PaylineOverlayTone,
    mathBridgeHints: MathBridgePresentationHints,
    lineIndex: number,
    lineCount: number,
  ): WinHighlightMotionProfile | null {
    if (getProviderPackStatus().effectiveProvider !== "donorlocal") {
      return null;
    }

    const isCherryLine = linePresentation.symbolId === 1;
    const lineScale = lineCount > 1 ? (lineIndex === lineCount - 1 ? 0.92 : 0.8) : 1;
    const toneScale =
      tone === "jackpot" ? 0.66 : tone === "boost" || tone === "bonus" ? 0.78 : 1;
    const escalatedScale = this.isEscalatedWinTier(mathBridgeHints.winTier) ? 1.08 : 1;
    const cherryScale = isCherryLine ? 1.25 : 1;
    const donorMotionGain = tone === "standard" ? 1.18 : 1.08;
    const intensity = lineScale * toneScale * escalatedScale * cherryScale * donorMotionGain;

    return {
      amplitudePx: (isCherryLine ? 2 : 1.35) * intensity,
      liftPx: (isCherryLine ? 2.4 : 1.8) * intensity,
      scalePulse: (isCherryLine ? 0.026 : 0.02) * intensity,
      frequencyHz: isCherryLine ? 2.58 : 2.16,
      phaseStep: isCherryLine ? 0.62 : 0.84,
    };
  }

  private isEscalatedWinTier(
    tier: MathBridgePresentationHints["winTier"] | PresentationWinTier | null | undefined,
  ): boolean {
    return tier === "big" || tier === "huge" || tier === "mega";
  }

  private shouldUseBigWinMascot(
    mathBridgeHints: MathBridgePresentationHints | null | undefined,
  ): boolean {
    if (!mathBridgeHints) {
      return false;
    }
    const donorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    if (donorlocal) {
      return mathBridgeHints.triggers.jackpot || mathBridgeHints.winTier === "mega";
    }
    if (mathBridgeHints.triggers.jackpot || mathBridgeHints.winTier === "mega") {
      return true;
    }
    if (mathBridgeHints.winTier === "huge") {
      return true;
    }
    // Keep donorlocal eyes stable for normal/small wins. "big" can happen at low bet
    // via provisional multipliers, so require a stronger threshold before big-win eyes.
    if (mathBridgeHints.winTier === "big") {
      return mathBridgeHints.totalWinMultiplier >= 15;
    }
    return false;
  }

  private resolveLineMascotState(
    tone: PaylineOverlayTone,
    mathBridgeHints: MathBridgePresentationHints,
    deferFeatureFxToScenario: boolean,
    lineIndex: number,
  ): "react_collect" | "react_boost_start" | "react_boost_loop" | "react_jackpot" | "react_bigwin" | null {
    if (deferFeatureFxToScenario) {
      return null;
    }

    switch (tone) {
      case "collect":
        return "react_collect";
      case "boost":
        return lineIndex === 0 ? "react_boost_start" : "react_boost_loop";
      case "bonus":
        return "react_collect";
      case "jackpot":
        return "react_jackpot";
      case "standard":
      default:
        return this.shouldUseBigWinMascot(mathBridgeHints) ? "react_bigwin" : null;
    }
  }

  private shouldPlayLineParticleBurst(
    tone: PaylineOverlayTone,
    mathBridgeHints: MathBridgePresentationHints,
    deferFeatureFxToScenario: boolean,
  ): boolean {
    if (deferFeatureFxToScenario) {
      return false;
    }
    if (tone === "standard" && !this.isEscalatedWinTier(mathBridgeHints.winTier)) {
      return false;
    }
    return true;
  }

  private playLinePresentationChoreography(
    linePresentation: ResolvedLinePresentation,
    mathBridgeHints: MathBridgePresentationHints,
    tone: PaylineOverlayTone,
    lineIndex: number,
    lineCount: number,
  ): void {
    const origins = this.resolveLineCueOrigins(linePresentation);
    const focusPoint = this.resolveCueFocusPoint(origins);
    const jackpotTarget = this.resolveJackpotPlaqueTargetPoint(mathBridgeHints.jackpotTier);
    const topperTarget = this.topperMascot.getFocusPoint();
    const deferFeatureFxToScenario =
      getProviderPackStatus().effectiveProvider === "donorlocal" &&
      (tone === "boost" || tone === "bonus" || tone === "jackpot");
    const mascotState = this.resolveLineMascotState(
      tone,
      mathBridgeHints,
      deferFeatureFxToScenario,
      lineIndex,
    );
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
        if (mascotState) {
          this.topperMascot.setState(mascotState);
        }
        this.jackpotPlaques.clear();
        this.layeredFx.playCollectSweepBurst(
          origins,
          Math.max(1240, mathBridgeHints.timingHints.coinFlyDurationMs + 280),
        );
        break;
      case "boost":
        if (mascotState) {
          this.topperMascot.setState(mascotState);
        }
        this.jackpotPlaques.clear();
        if (deferFeatureFxToScenario) {
          break;
        }
        this.layeredFx.playBoostStrike(origins, {
          targetX: topperTarget.x,
          targetY: topperTarget.y - 20,
          durationMs: Math.max(920, mathBridgeHints.timingHints.coinFlyDurationMs + 80),
        });
        break;
      case "bonus":
        if (mascotState) {
          this.topperMascot.setState(mascotState);
        }
        this.jackpotPlaques.clear();
        if (deferFeatureFxToScenario) {
          break;
        }
        this.layeredFx.playBoostLoop();
        this.layeredFx.playCoinFlyBurst(origins, {
          durationMs: Math.max(1320, mathBridgeHints.timingHints.coinFlyDurationMs + 220),
          countPerOrigin: 2,
          waveCount: 2,
          waveGapMs: 160,
          startSpreadX: 20,
          startSpreadY: 12,
          endSpreadX: 18,
          endSpreadY: 10,
          controlLift: 104,
        });
        break;
      case "jackpot":
        if (mascotState) {
          this.topperMascot.setState(mascotState);
        }
        if (deferFeatureFxToScenario) {
          break;
        }
        this.jackpotPlaques.celebrate(this.resolveJackpotPlaqueLevel(mathBridgeHints.jackpotTier));
        this.layeredFx.playJackpotStrike(
          this.resolveJackpotReactionLevel(mathBridgeHints.jackpotTier),
          origins,
          jackpotTarget,
          Math.max(1420, mathBridgeHints.timingHints.coinFlyDurationMs + 320),
        );
        break;
      case "standard":
      default:
        if (mascotState) {
          this.topperMascot.setState(mascotState);
        }
        this.jackpotPlaques.clear();
        this.layeredFx.playCoinFlyBurst(origins, {
          durationMs: Math.max(920, mathBridgeHints.timingHints.coinFlyDurationMs + 40),
          countPerOrigin: this.isEscalatedWinTier(mathBridgeHints.winTier)
            ? lineCount > 1
              ? 2
              : 3
            : 1,
          waveCount: this.isEscalatedWinTier(mathBridgeHints.winTier) ? 2 : 1,
          waveGapMs: this.isEscalatedWinTier(mathBridgeHints.winTier) ? 120 : 0,
          startSpreadX: 14,
          startSpreadY: 10,
          endSpreadX: 18,
          endSpreadY: 10,
          controlLift: 86,
        });
        if (lineIndex === lineCount - 1 && this.isEscalatedWinTier(mathBridgeHints.winTier)) {
          this.layeredFx.playWinPulse(mathBridgeHints.winTier);
        }
        break;
    }

    if (this.shouldPlayLineParticleBurst(tone, mathBridgeHints, deferFeatureFxToScenario)) {
      const burstPoint = tone === "jackpot" && jackpotTarget ? jackpotTarget : focusPoint;
      this.particleBurst.play(burstPoint.x, Math.max(24, burstPoint.y - 8));
    }
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

  private resolveJackpotPlaqueLevel(
    jackpotTier: string | null,
  ): "mini" | "minor" | "major" | "grand" | null {
    switch ((jackpotTier ?? "").toLowerCase()) {
      case "grand":
        return "grand";
      case "major":
        return "major";
      case "minor":
        return "minor";
      case "mini":
        return "mini";
      default:
        return null;
    }
  }

  private resolveBenchmarkReadyStatus(): string {
    return "DONORLOCAL BENCHMARK READY";
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
        await this.handleHistory();
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

  private async handleBuyFeature(forcedTierId?: string): Promise<void> {
    if (this.isSpinning || this.isPresentingWin) {
      return;
    }

    try {
      const totalBetMinor = Math.max(
        1,
        Math.round(ResolvedRuntimeConfigStore.limits.defaultBet),
      );
      const explicitTier =
        forcedTierId ?? new URLSearchParams(window.location.search).get("buyTier");
      if (
        !explicitTier &&
        getProviderPackStatus().effectiveProvider === "donorlocal"
      ) {
        const selectedTierId = await this.donorBuyBonusModal.open(
          CRAZY_ROOSTER_BUY_TIERS.map((tier) => ({
            id: tier.id,
            priceMinor: Math.round(totalBetMinor * tier.priceMultiplier),
            priceMultiplier: tier.priceMultiplier,
            variant: this.resolveBuyBonusVariant(tier.priceMultiplier),
          })),
        );
        if (!selectedTierId) {
          this.showStatus("BUY FEATURE CANCELLED");
          return;
        }
        await this.handleBuyFeature(selectedTierId);
        return;
      }

      const tier =
        CRAZY_ROOSTER_BUY_TIERS.find((entry) => entry.id === explicitTier) ??
        CRAZY_ROOSTER_BUY_TIERS[this.buyTierIndex % CRAZY_ROOSTER_BUY_TIERS.length];
      this.buyTierIndex += explicitTier ? 0 : 1;

      this.isSpinning = true;
      this.pendingRound = null;
      this.clearRuntimePaylinePresentation();
      this.winHighlight.clear();
      this.paylineHighlight.clear();
      this.paylineOverlay.clear();
      this.debugOverlay.setMathBridgeSummary(null);
      this.clearAutoplayTimeout();
      this.clearMathBridgeTimeouts();

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
      this.isSpinning = false;
      this.pendingRound = null;
      this.showStatus(`BUY FEATURE FAILED: ${String(error)}`);
    }
  }

  private resolveBuyBonusVariant(priceMultiplier: number): DonorBuyBonusVariant {
    if (priceMultiplier >= 300) {
      return "ultimate";
    }
    if (priceMultiplier >= 200) {
      return "power";
    }
    return "blitz";
  }

  private async handleSpin(): Promise<void> {
    if (this.donorBuyBonusModal.visible) {
      return;
    }

    if (this.isPresentingWin && this.animationPolicy.shouldAllowForcedSkip()) {
      this.skipWinPresentation();
      return;
    }

    if (this.isSpinning || this.isPresentingWin) {
      return;
    }

    this.isSpinning = true;
    this.pendingRound = null;
    this.startupPaylineGuideDismissed = true;
    this.clearStartupPaylineGuide();
    this.clearRuntimePaylinePresentation();
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
    const spinTiming = this.resolveDonorSpinTiming(timing, mathBridgeHints);
    this.clearMathBridgeTimeouts();
    this.activeMathBridgeHints = mathBridgeHints;
    this.jackpotFeatureCueConsumed = false;
    const resolvedStopColumns = this.resolvePresentationColumns(
      presentation.reels.stopColumns,
      mathBridgeHints,
    );
    const resolvedPresentation =
      resolvedStopColumns === presentation.reels.stopColumns
        ? presentation
        : {
            ...presentation,
            reels: {
              ...presentation.reels,
              stopColumns: resolvedStopColumns,
            },
            symbolGrid: buildGridFromColumns(resolvedStopColumns),
          };
    const presentationVariants = this.resolvePresentationVariants(
      resolvedPresentation.symbolGrid,
      mathBridgeHints,
    );
    const reelStopVariants = Array.from(
      { length: CRAZY_ROOSTER_LAYOUT.reelCount },
      (_, reelIndex) =>
        Array.from({ length: CRAZY_ROOSTER_LAYOUT.rowCount }, (_, rowIndex) =>
          presentationVariants[`${reelIndex}-${rowIndex}`] ?? null,
        ),
    );

    this.activeIntroPreviewGrid = resolvedPresentation.symbolGrid.map((row) => [...row]);
    this.debugOverlay.setMathBridgeSummary(
      mathBridgeHints
        ? {
            lineIds: mathBridgeHints.lineWins.map((line) => line.lineId),
            lineMultipliers: mathBridgeHints.lineWins.map((line) => line.multiplier),
            totalWinMultiplier: mathBridgeHints.totalWinMultiplier,
          }
        : null,
    );
    const featureFrame = this.featureModules.resolve(resolvedPresentation);
    this.pendingRound = {
      presentation: resolvedPresentation,
      featureFrame,
      mathBridgeHints,
    };

    this.slotMachine.spin({
      minSpinDurationMs: spinTiming.minSpinMs,
      spinStaggerMs: spinTiming.spinStaggerMs,
      speedMultiplier: spinTiming.speedMultiplier,
      finalReelHoldMs: spinTiming.finalReelHoldMs,
      reelStopColumns: resolvedPresentation.reels.stopColumns,
      reelStopVariants,
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

  private resolveDonorSpinTiming(
    timing: ReturnType<AnimationPolicyEngine["resolveSpinTiming"]>,
    mathBridgeHints: MathBridgePresentationHints | null,
  ): {
    minSpinMs: number;
    spinStaggerMs: number;
    speedMultiplier: number;
    finalReelHoldMs: number;
  } {
    const baseTiming = {
      minSpinMs: timing.minSpinMs,
      spinStaggerMs: timing.spinStaggerMs,
      speedMultiplier: timing.speedMultiplier,
      finalReelHoldMs: 0,
    };
    if (getProviderPackStatus().effectiveProvider !== "donorlocal") {
      return baseTiming;
    }

    const hasHeavyFeatureTrigger = Boolean(
      mathBridgeHints?.triggers.bonus || mathBridgeHints?.triggers.jackpot,
    );
    const hasBoostTrigger = Boolean(mathBridgeHints?.triggers.boost);

    return {
      minSpinMs: Math.max(1360, timing.minSpinMs),
      spinStaggerMs: 66,
      speedMultiplier: Math.max(0.9, timing.speedMultiplier),
      finalReelHoldMs: hasHeavyFeatureTrigger ? 240 : hasBoostTrigger ? 120 : 0,
    };
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
    const settledPresentation = presentation;
    const presentationVariants = this.resolvePresentationVariants(
      settledPresentation.symbolGrid,
      mathBridgeHints,
    );
    const hasPresentationVariants = Object.keys(presentationVariants).length > 0;
    const shouldReapplyPresentationVariants =
      hasPresentationVariants && getProviderPackStatus().effectiveProvider !== "donorlocal";
    if (shouldReapplyPresentationVariants) {
      this.slotMachine.applyPresentationVariants(presentationVariants);
    }
    this.applyDynamicControlVisibility(featureFrame);

    const mergedMessages = [
      ...settledPresentation.messages,
      ...featureFrame.messages,
      ...featureFrame.overlays.map((overlay) => overlay.label),
    ];
    const donorFeatureStatusPending =
      getProviderPackStatus().effectiveProvider === "donorlocal" &&
      Boolean(
        mathBridgeHints?.triggers.collect ||
          mathBridgeHints?.triggers.boost ||
          mathBridgeHints?.triggers.bonus ||
          mathBridgeHints?.triggers.jackpot,
      );
    if (!donorFeatureStatusPending) {
      this.showStatus(formatMessages(mergedMessages));
    }

    const winSymbols = this.resolveWinningSymbols(settledPresentation, mathBridgeHints);
    const linePresentations = this.resolveLinePresentations(
      settledPresentation,
      mathBridgeHints,
    );
    const hintedLineWinAmountMinor = Math.max(
      0,
      Math.round(
        (mathBridgeHints?.lineWins ?? []).reduce(
          (sum, lineWin) => sum + Math.max(0, lineWin.amountMinor),
          0,
        ),
      ),
    );
    const winAmount = Math.max(settledPresentation.winAmount, hintedLineWinAmountMinor);
    this.winCounter.reportWin(winAmount);
    const defaultBet = ResolvedRuntimeConfigStore.limits.defaultBet;
    const animationCues = this.resolveWinPresentationAnimationCues(
      mathBridgeHints,
      presentation.animationCues,
      featureFrame.animationCues,
    );

    this.scheduleMathBridgeFeatureCues(mathBridgeHints, linePresentations.length);
    this.scheduleLineVisualization(mathBridgeHints, linePresentations);

    const vfxState = this.wowVfx.startWinPresentation({
      winAmountMinor: winAmount,
      defaultBetMinor: defaultBet,
      winSymbols: winSymbols as never[],
      soundCues: [...settledPresentation.soundCues, ...featureFrame.soundCues],
      animationCues,
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
      statusText: `ROUND_SETTLED:${settledPresentation.roundId}`,
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

  private resolvePresentationVariants(
    symbolGrid: number[][],
    mathBridgeHints: MathBridgePresentationHints | null,
  ): PresentationVariantMap {
    if (getProviderPackStatus().effectiveProvider !== "donorlocal" || !mathBridgeHints) {
      return {};
    }

    const variants: PresentationVariantMap = {};

    if (this.shouldUseDonorStableJackpotLayout(mathBridgeHints)) {
      variants["0-1"] = "x10";
      variants["0-2"] = "x7";
      variants["2-0"] = "x15";
      variants["2-3"] = "x5";
    }

    if (!mathBridgeHints.jackpotTier) {
      return variants;
    }

    const variant = mathBridgeHints.jackpotTier as DonorMultiplierVariantKey;

    symbolGrid.forEach((row, rowIndex) => {
      row.forEach((symbolId, reelIndex) => {
        if (symbolId === 9) {
          variants[`${reelIndex}-${rowIndex}`] = variant;
        }
      });
    });

    return variants;
  }

  private resolvePresentationColumns(
    stopColumns: number[][],
    mathBridgeHints: MathBridgePresentationHints | null,
  ): number[][] {
    if (!this.shouldUseDonorStableJackpotLayout(mathBridgeHints)) {
      return stopColumns;
    }

    return DONOR_STABLE_JACKPOT_COLUMNS.map((column) => [...column]);
  }

  private shouldUseDonorStableJackpotLayout(
    mathBridgeHints: MathBridgePresentationHints | null,
  ): boolean {
    const params = new URLSearchParams(window.location.search);
    if (params.get("donorStableJackpotLayout") !== "1") {
      return false;
    }
    if (
      getProviderPackStatus().effectiveProvider !== "donorlocal" ||
      !mathBridgeHints?.jackpotTier
    ) {
      return false;
    }

    return (
      mathBridgeHints.preset === "jackpot" ||
      (mathBridgeHints.preset === "bonus" && mathBridgeHints.mode === "buy200")
    );
  }

  private shouldShowWinCounter(
    amountMinor: number,
    tier: PresentationWinTier,
  ): boolean {
    if (amountMinor <= 0) {
      return false;
    }
    if (tier === "none") {
      return false;
    }
    if (getProviderPackStatus().effectiveProvider === "donorlocal") {
      // Donor normal/small wins should not trigger the full-screen counter.
      return tier === "mega" || tier === "huge";
    }
    // Keep large center counter for material wins only.
    return tier === "big" || tier === "huge" || tier === "mega" || amountMinor >= 100;
  }

  private resolveLinePresentations(
    presentation: RoundPresentationModel,
    mathBridgeHints: MathBridgePresentationHints | null,
  ): ResolvedLinePresentation[] {
    const reels = this.slotMachine.getReels();
    const fromMathBridge = (mathBridgeHints?.lineWins ?? [])
      .map((lineWin) => {
        let symbols = lineWin.positions
          .map((position) => reels[position.reelIndex]?.getVisibleSymbols()[position.rowIndex])
          .filter(Boolean) as HighlightSymbolLike[];

        if (symbols.length === 0) {
          const payline =
            CRAZY_ROOSTER_PAYLINES[Math.max(0, (lineWin.lineId ?? 1) - 1)] ?? null;
          if (payline) {
            symbols = payline
              .map((rowIndex, reelIndex) => reels[reelIndex]?.getVisibleSymbols()[rowIndex])
              .filter(Boolean) as HighlightSymbolLike[];
          }
        }

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

    if (fromMathBridge.length > 0) {
      return fromMathBridge;
    }

    if (presentation.winAmount <= 0) {
      return [];
    }

    return this.resolveFallbackLinePresentations(presentation, reels);
  }

  private resolveFallbackLinePresentations(
    presentation: RoundPresentationModel,
    reels: ReturnType<CrazyRoosterSlotMachine["getReels"]>,
  ): ResolvedLinePresentation[] {
    const baseLineWins = CRAZY_ROOSTER_PAYLINES.map((payline, index) => {
      const symbolsOnLine = payline.map(
        (rowIndex, reelIndex) => presentation.symbolGrid[rowIndex]?.[reelIndex],
      );
      const first = symbolsOnLine[0];
      if (first === undefined || first === 7 || first === 8 || first === 9) {
        return null;
      }
      if (!symbolsOnLine.every((symbol) => symbol === first)) {
        return null;
      }
      return {
        lineId: index + 1,
        symbolId: first,
        payline,
      };
    }).filter(
      (entry): entry is { lineId: number; symbolId: number; payline: number[] } =>
        Boolean(entry),
    );

    if (baseLineWins.length === 0) {
      const fallbackPayline = CRAZY_ROOSTER_PAYLINES[1] ?? [1, 1, 1];
      const symbols = fallbackPayline
        .map((rowIndex, reelIndex) => reels[reelIndex]?.getVisibleSymbols()[rowIndex])
        .filter(Boolean) as HighlightSymbolLike[];
      if (symbols.length === 0) {
        return [];
      }
      const symbolId =
        presentation.symbolGrid[fallbackPayline[0] ?? 1]?.[0] ??
        presentation.symbolGrid[1]?.[0] ??
        presentation.symbolGrid[0]?.[0] ??
        0;
      const amountMinor = Math.max(1, Math.round(presentation.winAmount));
      const defaultBet = Math.max(0.1, ResolvedRuntimeConfigStore.limits.defaultBet);
      const multiplier = Math.max(
        1,
        amountMinor / Math.max(1, Math.round(defaultBet * 100)),
      );
      return [
        {
          lineId: 2,
          symbolId,
          multiplier,
          amountMinor,
          points: symbols.map((symbol) => this.resolveFxLayerCenter(symbol)),
          symbols,
        },
      ];
    }

    const perLineAmountMinor = Math.max(
      1,
      Math.round(presentation.winAmount / baseLineWins.length),
    );
    const defaultBet = Math.max(0.1, ResolvedRuntimeConfigStore.limits.defaultBet);
    const perLineMultiplier = Math.max(
      1,
      perLineAmountMinor / Math.max(1, Math.round(defaultBet * 100)),
    );

    return baseLineWins
      .map((lineWin) => {
        const symbols = lineWin.payline
          .map((rowIndex, reelIndex) => reels[reelIndex]?.getVisibleSymbols()[rowIndex])
          .filter(Boolean) as HighlightSymbolLike[];
        if (symbols.length === 0) {
          return null;
        }
        return {
          lineId: lineWin.lineId,
          symbolId: lineWin.symbolId,
          multiplier: perLineMultiplier,
          amountMinor: perLineAmountMinor,
          points: symbols.map((symbol) => this.resolveFxLayerCenter(symbol)),
          symbols,
        };
      })
      .filter((entry): entry is ResolvedLinePresentation => Boolean(entry));
  }

  private showStartupPaylineGuideOnLoad(): void {
    if (this.startupPaylineGuideDismissed) {
      return;
    }
    if (this.startupPaylineMap.visible || this.startupPaylineLabels.visible) {
      return;
    }
    const params = new URLSearchParams(window.location.search);
    if (params.get("startupPaylines") === "0") {
      return;
    }
    this.drawStartupPaylineGuide();
  }

  private clearStartupPaylineGuide(): void {
    this.startupPaylineMap.clear();
    this.startupPaylineMap.visible = false;
    this.startupPaylineLabels.removeChildren();
    this.startupPaylineLabels.visible = false;
  }

  private clearRuntimePaylinePresentation(): void {
    this.runtimePaylineMap.clear();
    this.runtimePaylineMap.visible = false;
    this.runtimePaylineCallout.visible = false;
    this.runtimePaylineTitle.text = "";
    this.runtimePaylineBadgeText.text = "";
    this.runtimePaylineMainText.text = "";
    this.runtimePaylineSequenceText.text = "";
    this.runtimePaylineGlowElapsedMs = 0;
  }

  private showRuntimePaylinePresentation(
    linePresentation: ResolvedLinePresentation,
    tone: "standard" | "collect" | "boost" | "bonus" | "jackpot",
    sequenceIndex: number,
    sequenceCount: number,
  ): void {
    this.clearRuntimePaylinePresentation();
    const points = linePresentation.points;
    if (points.length < 2) {
      return;
    }

    const style = this.resolveRuntimePaylineStyle(tone);
    const first = points[0];
    const second = points[Math.min(1, points.length - 1)];
    const last = points[points.length - 1];
    const lineExtensionPx = 16;
    const startDirection = this.normalizeVector(first.x - second.x, first.y - second.y);
    const endDirection = this.normalizeVector(last.x - second.x, last.y - second.y);
    const pathPoints = [
      {
        x: first.x + startDirection.x * lineExtensionPx,
        y: first.y + startDirection.y * lineExtensionPx,
      },
      ...points.slice(1, -1),
      {
        x: last.x + endDirection.x * lineExtensionPx,
        y: last.y + endDirection.y * lineExtensionPx,
      },
    ];

    this.drawRuntimePaylineStroke(pathPoints, 11, style.outerGlow, 0.14);
    this.drawRuntimePaylineStroke(pathPoints, 7.2, style.midGlow, 0.34);
    this.drawRuntimePaylineStroke(pathPoints, 4.4, style.innerGlow, 0.72);
    this.drawRuntimePaylineStroke(pathPoints, 2.2, 0xfffff0, 0.84);

    points.forEach((point) => {
      this.runtimePaylineMap.circle(point.x, point.y, 11);
      this.runtimePaylineMap.fill({ color: style.midGlow, alpha: 0.26 });
      this.runtimePaylineMap.circle(point.x, point.y, 5.2);
      this.runtimePaylineMap.fill({ color: 0xfffff2, alpha: 0.9 });
    });

    const reelAreaWidth =
      CRAZY_ROOSTER_LAYOUT.reelCount * CRAZY_ROOSTER_LAYOUT.symbolWidth +
      (CRAZY_ROOSTER_LAYOUT.reelCount - 1) * CRAZY_ROOSTER_LAYOUT.reelSpacing;
    const minY = Math.min(...points.map((point) => point.y));
    const minX = Math.min(...points.map((point) => point.x));
    const maxX = Math.max(...points.map((point) => point.x));
    const calloutWidth = 268;
    const calloutHeight = 66;
    const halfWidth = calloutWidth * 0.5;
    const calloutX = Math.max(
      halfWidth + 8,
      Math.min(reelAreaWidth - halfWidth - 8, (minX + maxX) * 0.5),
    );
    const calloutY = Math.max(84, minY - 30);

    this.runtimePaylineCalloutShadow.clear();
    this.runtimePaylineCalloutShadow.roundRect(
      -halfWidth,
      -calloutHeight * 0.5 + 5,
      calloutWidth,
      calloutHeight,
      18,
    );
    this.runtimePaylineCalloutShadow.fill({ color: 0x050102, alpha: 0.46 });

    this.runtimePaylineCalloutBase.clear();
    this.runtimePaylineCalloutBase.roundRect(
      -halfWidth,
      -calloutHeight * 0.5,
      calloutWidth,
      calloutHeight,
      18,
    );
    this.runtimePaylineCalloutBase.fill({ color: style.panelFill, alpha: 0.95 });
    this.runtimePaylineCalloutBase.stroke({ color: style.panelStroke, width: 4, alpha: 0.98 });

    this.runtimePaylineCalloutAccent.clear();
    this.runtimePaylineCalloutAccent.roundRect(
      -halfWidth + 10,
      -calloutHeight * 0.5 + 8,
      calloutWidth - 20,
      14,
      8,
    );
    this.runtimePaylineCalloutAccent.fill({ color: style.accentFill, alpha: 0.94 });

    this.runtimePaylineBadge.clear();
    this.runtimePaylineBadge.circle(-halfWidth + 36, 4, 20);
    this.runtimePaylineBadge.fill({ color: style.badgeFill, alpha: 0.98 });
    this.runtimePaylineBadge.stroke({ color: style.badgeStroke, width: 3, alpha: 0.98 });

    this.runtimePaylineSequenceChip.clear();
    if (sequenceCount > 1) {
      this.runtimePaylineSequenceChip.roundRect(
        halfWidth - 70,
        -calloutHeight * 0.5 + 10,
        56,
        24,
        12,
      );
      this.runtimePaylineSequenceChip.fill({ color: style.badgeFill, alpha: 0.95 });
      this.runtimePaylineSequenceChip.stroke({ color: style.badgeStroke, width: 2, alpha: 0.96 });
    }

    this.runtimePaylineTitle.text =
      tone === "jackpot"
        ? "JACKPOT LINE"
        : tone === "boost"
          ? "BOOST LINE"
          : tone === "collect"
            ? "COLLECT LINE"
            : "WINNING LINE";
    this.runtimePaylineTitle.x = 0;
    this.runtimePaylineTitle.y = -18;

    this.runtimePaylineBadgeText.text = `L${linePresentation.lineId}`;
    this.runtimePaylineBadgeText.x = -halfWidth + 36;
    this.runtimePaylineBadgeText.y = 4;

    this.runtimePaylineMainText.text = `${formatLineMultiplier(linePresentation.multiplier)} PAY ${formatMinorCurrency(linePresentation.amountMinor)}`;
    this.runtimePaylineMainText.x = 22;
    this.runtimePaylineMainText.y = 7;

    this.runtimePaylineSequenceText.text =
      sequenceCount > 1 ? `${sequenceIndex + 1}/${sequenceCount}` : "";
    this.runtimePaylineSequenceText.visible = sequenceCount > 1;
    this.runtimePaylineSequenceText.x = halfWidth - 42;
    this.runtimePaylineSequenceText.y = -13;

    this.runtimePaylineCallout.x = calloutX;
    this.runtimePaylineCallout.y = calloutY;
    this.runtimePaylineCallout.alpha = 0.98;
    this.runtimePaylineCallout.visible = true;
    this.runtimePaylineMap.alpha = 0.94;
    this.runtimePaylineMap.visible = true;
  }

  private drawRuntimePaylineStroke(
    points: Array<{ x: number; y: number }>,
    width: number,
    color: number,
    alpha: number,
  ): void {
    if (points.length < 2) {
      return;
    }
    this.runtimePaylineMap.moveTo(points[0].x, points[0].y);
    for (let index = 1; index < points.length; index += 1) {
      this.runtimePaylineMap.lineTo(points[index].x, points[index].y);
    }
    this.runtimePaylineMap.stroke({
      color,
      width,
      alpha,
      cap: "round",
      join: "round",
    });
  }

  private resolveRuntimePaylineStyle(
    tone: "standard" | "collect" | "boost" | "bonus" | "jackpot",
  ): {
    outerGlow: number;
    midGlow: number;
    innerGlow: number;
    panelFill: number;
    panelStroke: number;
    accentFill: number;
    badgeFill: number;
    badgeStroke: number;
  } {
    switch (tone) {
      case "collect":
        return {
          outerGlow: 0xff860d,
          midGlow: 0xffb93d,
          innerGlow: 0xffef9f,
          panelFill: 0x5b1a10,
          panelStroke: 0xffd497,
          accentFill: 0x8b3313,
          badgeFill: 0x6b240d,
          badgeStroke: 0xffd59c,
        };
      case "boost":
        return {
          outerGlow: 0xff9d32,
          midGlow: 0xffd25a,
          innerGlow: 0xffffd8,
          panelFill: 0x5d200a,
          panelStroke: 0xffe0a8,
          accentFill: 0x9d4c15,
          badgeFill: 0x7a340b,
          badgeStroke: 0xffe0ac,
        };
      case "bonus":
        return {
          outerGlow: 0xffa03a,
          midGlow: 0xffd667,
          innerGlow: 0xffffde,
          panelFill: 0x4f180a,
          panelStroke: 0xffd79f,
          accentFill: 0x8f3913,
          badgeFill: 0x72270d,
          badgeStroke: 0xffddaa,
        };
      case "jackpot":
        return {
          outerGlow: 0xffd168,
          midGlow: 0xffec9a,
          innerGlow: 0xfffff3,
          panelFill: 0x4c1a06,
          panelStroke: 0xffefbc,
          accentFill: 0xa06f17,
          badgeFill: 0x70440a,
          badgeStroke: 0xffefbd,
        };
      case "standard":
      default:
        return {
          outerGlow: 0xff7a00,
          midGlow: 0xffb11f,
          innerGlow: 0xffdf73,
          panelFill: 0x5b141b,
          panelStroke: 0xf6dd9d,
          accentFill: 0x9c2127,
          badgeFill: 0x63151b,
          badgeStroke: 0xf5d99b,
        };
    }
  }

  private drawStartupPaylineGuide(): void {
    this.clearStartupPaylineGuide();
    const reelStep = CRAZY_ROOSTER_LAYOUT.symbolWidth + CRAZY_ROOSTER_LAYOUT.reelSpacing;
    const rowStep = CRAZY_ROOSTER_LAYOUT.symbolHeight + CRAZY_ROOSTER_LAYOUT.rowSpacing;
    const lineExtensionPx = 84;

    this.startupPaylineMap.clear();
    this.startupPaylineMap.blendMode = "add";
    this.startupPaylineGlowElapsedMs = 0;
    CRAZY_ROOSTER_PAYLINES.forEach((payline, lineIndex) => {
      const points = payline.map((rowIndex, reelIndex) => ({
        x: reelIndex * reelStep + CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5,
        y: rowIndex * rowStep + CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5,
      }));
      const [first, second, third] = points;
      const startDirection = this.normalizeVector(first.x - second.x, first.y - second.y);
      const endDirection = this.normalizeVector(third.x - second.x, third.y - second.y);
      const extendedFirst = {
        x: first.x + startDirection.x * lineExtensionPx,
        y: first.y + startDirection.y * lineExtensionPx,
      };
      const extendedThird = {
        x: third.x + endDirection.x * lineExtensionPx,
        y: third.y + endDirection.y * lineExtensionPx,
      };
      const labelPlacement = this.resolveStartupPaylineLabelPlacement(first, third, lineIndex);

      this.startupPaylineMap.moveTo(extendedFirst.x, extendedFirst.y);
      this.startupPaylineMap.lineTo(second.x, second.y);
      this.startupPaylineMap.lineTo(extendedThird.x, extendedThird.y);
      this.startupPaylineMap.stroke({
        color: 0xff7a00,
        width: 12,
        alpha: 0.12,
      });

      this.startupPaylineMap.moveTo(extendedFirst.x, extendedFirst.y);
      this.startupPaylineMap.lineTo(second.x, second.y);
      this.startupPaylineMap.lineTo(extendedThird.x, extendedThird.y);
      this.startupPaylineMap.stroke({
        color: 0xffa116,
        width: 7.4,
        alpha: 0.32,
      });

      this.startupPaylineMap.moveTo(extendedFirst.x, extendedFirst.y);
      this.startupPaylineMap.lineTo(second.x, second.y);
      this.startupPaylineMap.lineTo(extendedThird.x, extendedThird.y);
      this.startupPaylineMap.stroke({
        color: 0xffd247,
        width: 4.8,
        alpha: 0.65,
      });

      this.startupPaylineMap.moveTo(extendedFirst.x, extendedFirst.y);
      this.startupPaylineMap.lineTo(second.x, second.y);
      this.startupPaylineMap.lineTo(extendedThird.x, extendedThird.y);
      this.startupPaylineMap.stroke({
        color: 0xffffd2,
        width: 2.7,
        alpha: 0.78,
      });

      this.startupPaylineMap.moveTo(extendedFirst.x, extendedFirst.y);
      this.startupPaylineMap.lineTo(second.x, second.y);
      this.startupPaylineMap.lineTo(extendedThird.x, extendedThird.y);
      this.startupPaylineMap.stroke({
        color: 0xffffff,
        width: 1.25,
        alpha: 0.78,
      });

      const label = new Text({
        text: `${lineIndex + 1}`,
        style: {
          fontFamily: "Trebuchet MS, Arial, sans-serif",
          fontSize: 52,
          fontWeight: "900",
          fill: 0xfff2b8,
          stroke: { color: 0x311103, width: 10 },
          dropShadow: {
            color: 0x000000,
            blur: 3,
            distance: 2,
          },
          align: "center",
        },
      });
      label.anchor.set(0.5);
      label.x = labelPlacement.label.x;
      label.y = labelPlacement.label.y;
      this.startupPaylineLabels.addChild(label);
    });

    this.startupPaylineMap.alpha = 0.92;
    this.startupPaylineMap.visible = true;
    this.startupPaylineLabels.alpha = 0.98;
    this.startupPaylineLabels.visible = true;
  }

  private resolveStartupPaylineLabelPlacement(
    first: { x: number; y: number },
    third: { x: number; y: number },
    lineIndex: number,
  ): { label: { x: number; y: number }; anchor: { x: number; y: number } } {
    const reelAreaWidth =
      CRAZY_ROOSTER_LAYOUT.reelCount * CRAZY_ROOSTER_LAYOUT.symbolWidth +
      (CRAZY_ROOSTER_LAYOUT.reelCount - 1) * CRAZY_ROOSTER_LAYOUT.reelSpacing;
    const isHorizontalBand = lineIndex < 4;
    const label = {
      x: isHorizontalBand ? -48 : reelAreaWidth + 48,
      y: isHorizontalBand ? first.y : third.y,
    };
    if (!isHorizontalBand) {
      const topRowY = 0.5 * CRAZY_ROOSTER_LAYOUT.symbolHeight;
      const secondRowY =
        CRAZY_ROOSTER_LAYOUT.symbolHeight +
        CRAZY_ROOSTER_LAYOUT.rowSpacing +
        0.5 * CRAZY_ROOSTER_LAYOUT.symbolHeight;
      const thirdRowY =
        CRAZY_ROOSTER_LAYOUT.symbolHeight * 2 +
        CRAZY_ROOSTER_LAYOUT.rowSpacing * 2 +
        0.5 * CRAZY_ROOSTER_LAYOUT.symbolHeight;
      const fourthRowY =
        CRAZY_ROOSTER_LAYOUT.symbolHeight * 3 +
        CRAZY_ROOSTER_LAYOUT.rowSpacing * 3 +
        0.5 * CRAZY_ROOSTER_LAYOUT.symbolHeight;
      // Right-side sequence tuned to match the user-marked arrow spacing:
      // 7 (highest), 8 (upper-mid), 5 (lower-mid), 6 (lowest).
      if (lineIndex === 6) {
        label.y = topRowY - 34;
      } else if (lineIndex === 7) {
        label.y = secondRowY - 18;
      } else if (lineIndex === 4) {
        label.y = thirdRowY + 18;
      } else if (lineIndex === 5) {
        label.y = fourthRowY + 34;
      }
    }
    return { label, anchor: isHorizontalBand ? first : third };
  }

  private normalizeVector(x: number, y: number): { x: number; y: number } {
    const length = Math.hypot(x, y);
    if (length <= 0.0001) {
      return { x: 0, y: 0 };
    }
    return { x: x / length, y: y / length };
  }

  private resolveFxLayerCenter(symbol: HighlightSymbolLike): { x: number; y: number } {
    const symbolLike = symbol as unknown as {
      x?: number;
      y?: number;
      parent?: {
        toGlobal?: (point: { x: number; y: number }) => { x: number; y: number };
      };
    };
    const fallbackPoint = symbol.getGlobalPosition();
    const globalPoint =
      symbolLike.parent?.toGlobal && typeof symbolLike.x === "number" && typeof symbolLike.y === "number"
        ? symbolLike.parent.toGlobal({
            x: symbolLike.x + CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5,
            y: symbolLike.y + CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.5,
          })
        : fallbackPoint;
    const localPoint = this.fxLayer.toLocal(globalPoint);
    return {
      x: localPoint.x,
      y: localPoint.y,
    };
  }

  private resolveDefaultFxOrigin(): FxCuePoint {
    return {
      x:
        CRAZY_ROOSTER_LAYOUT.reelCount * CRAZY_ROOSTER_LAYOUT.symbolWidth * 0.5,
      y:
        CRAZY_ROOSTER_LAYOUT.rowCount * CRAZY_ROOSTER_LAYOUT.symbolHeight * 0.44,
    };
  }

  private resolveLineCueOrigins(linePresentation: ResolvedLinePresentation): FxCuePoint[] {
    return this.dedupeFxPoints(
      linePresentation.points.length > 0
        ? linePresentation.points
        : [this.resolveDefaultFxOrigin()],
    );
  }

  private resolveFxPointFromBridgePosition(position: {
    reelIndex: number;
    rowIndex: number;
  }): FxCuePoint | null {
    const symbol =
      this.slotMachine.getReels()[position.reelIndex]?.getVisibleSymbols()[position.rowIndex];
    return symbol ? this.resolveFxLayerCenter(symbol) : null;
  }

  private resolveMathBridgeCueOrigins(
    mathBridgeHints: MathBridgePresentationHints | null = this.activeMathBridgeHints,
  ): FxCuePoint[] {
    if (!mathBridgeHints) {
      return [this.resolveDefaultFxOrigin()];
    }

    const points = mathBridgeHints.lineWins
      .flatMap((lineWin) => lineWin.positions)
      .map((position) => this.resolveFxPointFromBridgePosition(position))
      .filter((point): point is FxCuePoint => Boolean(point));

    return this.dedupeFxPoints(
      points.length > 0 ? points : [this.resolveDefaultFxOrigin()],
    );
  }

  private dedupeFxPoints(points: FxCuePoint[]): FxCuePoint[] {
    const seen = new Set<string>();
    return points.filter((point) => {
      const key = `${Math.round(point.x)}:${Math.round(point.y)}`;
      if (seen.has(key)) {
        return false;
      }
      seen.add(key);
      return true;
    });
  }

  private resolveCueFocusPoint(points: FxCuePoint[]): FxCuePoint {
    return points[Math.floor(points.length / 2)] ?? this.resolveDefaultFxOrigin();
  }

  private resolveJackpotPlaqueTargetPoint(
    jackpotTier: string | null,
  ): FxCuePoint | null {
    const level = this.resolveJackpotPlaqueLevel(jackpotTier);
    if (!level) {
      return null;
    }
    const plaqueCenter = this.jackpotPlaques.getPlaqueCenter(level);
    const globalPoint = this.jackpotPlaques.toGlobal(
      new Point(plaqueCenter.x, plaqueCenter.y),
    );
    const localPoint = this.fxLayer.toLocal(globalPoint);
    return { x: localPoint.x, y: localPoint.y };
  }

  private scheduleBoostFinish(
    mathBridgeHints: MathBridgePresentationHints | null,
    focusPoint: FxCuePoint,
    startDelayMs = 0,
  ): void {
    if (
      !mathBridgeHints ||
      !mathBridgeHints.triggers.boost ||
      mathBridgeHints.triggers.bonus ||
      mathBridgeHints.triggers.jackpot
    ) {
      return;
    }

    const finishDelayMs =
      Math.max(
        getProviderPackStatus().effectiveProvider === "donorlocal"
          ? DONOR_VIDEO_TIMING.boostChargeWindowMs
          : 280,
        mathBridgeHints.timingHints.featureLoopDurationMs,
      ) +
      Math.max(0, startDelayMs);
    const settleDelayMs =
      finishDelayMs +
      Math.max(
        getProviderPackStatus().effectiveProvider === "donorlocal" ? 220 : 140,
        mathBridgeHints.timingHints.featureFinishDelayMs,
      );

    const finishTimeout = window.setTimeout(() => {
      this.topperMascot.setState("react_boost_finish");
      this.layeredFx.playBoostFinish();
      this.particleBurst.play(focusPoint.x, Math.max(24, focusPoint.y - 10));
    }, finishDelayMs);
    const settleTimeout = window.setTimeout(() => {
      this.layeredFx.playCoinFlyBurst([focusPoint], {
        durationMs: Math.max(
          getProviderPackStatus().effectiveProvider === "donorlocal"
            ? DONOR_VIDEO_TIMING.jackpotImpactWindowMs
            : 420,
          mathBridgeHints.timingHints.coinFlyDurationMs - 160,
        ),
        countPerOrigin: getProviderPackStatus().effectiveProvider === "donorlocal" ? 1 : 2,
        startSpreadX: 16,
        startSpreadY: 12,
        endSpreadX: 18,
        endSpreadY: 10,
        controlLift: 72,
      });
    }, settleDelayMs);
    this.mathBridgeTimeouts.push(finishTimeout, settleTimeout);
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
    const featureOrigins = this.resolveMathBridgeCueOrigins();
    const featureFocus = this.resolveCueFocusPoint(featureOrigins);
    const jackpotTarget = this.resolveJackpotPlaqueTargetPoint(
      this.activeMathBridgeHints?.jackpotTier ?? null,
    );
    const topperTarget = this.topperMascot.getFocusPoint();

    if (this.shouldSuppressFeatureCueForScenario(cue)) {
      return;
    }

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
      const donorFeatureIntroPending =
        getProviderPackStatus().effectiveProvider === "donorlocal" &&
        Boolean(
          this.activeMathBridgeHints?.triggers.collect ||
            this.activeMathBridgeHints?.triggers.boost ||
            this.activeMathBridgeHints?.triggers.bonus ||
            this.activeMathBridgeHints?.triggers.jackpot,
        );
      if (donorFeatureIntroPending) {
        return;
      }
      this.showStatus("LIGHTNING BOOST");
      this.topperMascot.setState("react_boost_start");
      this.jackpotPlaques.clear();
      this.visualChrome.triggerPresentationCue({
        tone: "boost",
        title: "LIGHTNING BOOST",
        caption: "STAGE IGNITE",
        holdMs: 980,
      });
      this.layeredFx.playBoostStrike(featureOrigins, {
        targetX: topperTarget.x,
        targetY: topperTarget.y - 20,
        durationMs: Math.max(
          920,
          (this.activeMathBridgeHints?.timingHints.coinFlyDurationMs ?? 880) + 40,
        ),
      });
      this.particleBurst.play(featureFocus.x, Math.max(24, featureFocus.y - 14));
      this.applySoundCue("feature-boost-enter");
      return;
    }

    if (cue === "feature.collect.triggered") {
      this.showStatus("COLLECT RUN");
      this.topperMascot.setState("react_collect");
      this.jackpotPlaques.clear();
      this.visualChrome.triggerPresentationCue({
        tone: "collect",
        title: "COLLECT RUN",
        caption: "COIN SWEEP",
        holdMs: 940,
      });
      this.layeredFx.playCollectSweepBurst(
        featureOrigins,
        Math.max(1240, (this.activeMathBridgeHints?.timingHints.coinFlyDurationMs ?? 880) + 280),
      );
      this.particleBurst.play(featureFocus.x, Math.max(24, featureFocus.y - 10));
      this.applySoundCue("feature-collect-enter");
      return;
    }

    if (cue === "collect-sweep") {
      this.layeredFx.playCoinFlyBurst(featureOrigins, {
        durationMs: Math.max(1180, (this.activeMathBridgeHints?.timingHints.coinFlyDurationMs ?? 880) + 200),
        countPerOrigin: 2,
        waveCount: 2,
        waveGapMs: 160,
        startSpreadX: 14,
        startSpreadY: 10,
        endSpreadX: 18,
        endSpreadY: 10,
        controlLift: 88,
      });
      this.particleBurst.play(featureFocus.x, Math.max(24, featureFocus.y - 10));
      return;
    }

    if (cue === "feature.boost.triggered") {
      const introHoldMs = this.isDonorFeatureScenario() ? 2920 : 1620;
      this.clearFeatureScenePresentation();
      const useCountdownPrelude = this.shouldUseDonorCountdownPrelude(this.activeMathBridgeHints);
      const countdownDelayMs = useCountdownPrelude
        ? this.playDonorCountdownPhase([0, 1, 2, 3], [200, 300, 350, 350], 4250)
        : 0;
      const boostHandoffDelayMs = this.isDonorFeatureScenario()
        ? Math.max(140, countdownDelayMs + (useCountdownPrelude ? 260 : 0))
        : 0;
      this.showStatus("BOOST CHARGE");
      this.topperMascot.setState("react_boost_loop");
      this.jackpotPlaques.clear();
      if (!useCountdownPrelude) {
        void this.featureIntroOverlay.play("power", {
          holdMs: introHoldMs,
          symbolGrid: this.activeIntroPreviewGrid,
        });
      } else {
        this.featureIntroOverlay.clear();
      }
      this.visualChrome.triggerPresentationCue({
        tone: "boost",
        title: "BOOST CHARGE",
        caption: "LIGHTNING SURGE",
        holdMs: 1020,
      });
      const boardStrikeTarget = {
        x: featureFocus.x,
        y: Math.max(42, featureFocus.y - 18),
      };
      const startBoostHandoff = () => {
        this.layeredFx.playBoostStrike(featureOrigins, {
          targetX: boardStrikeTarget.x,
          targetY: boardStrikeTarget.y,
          durationMs: Math.max(
            980,
            (this.activeMathBridgeHints?.timingHints.coinFlyDurationMs ?? 880) + 120,
          ),
        });
        this.layeredFx.playBoostLoop();
        if (!this.isDonorFeatureScenario()) {
          this.particleBurst.play(featureFocus.x, Math.max(24, featureFocus.y - 14));
        }
        this.scheduleBoostFinish(this.activeMathBridgeHints, featureFocus);
      };
      if (boostHandoffDelayMs > 0) {
        this.scheduleAfterDonorFeatureIntro(startBoostHandoff, boostHandoffDelayMs);
      } else {
        startBoostHandoff();
      }
      this.applySoundCue("feature-boost-enter");
      return;
    }

    if (cue === "feature.bonus.enter") {
      const introVariant = this.resolveDonorBuyFeatureIntroVariant();
      const introHoldMs =
        introVariant === "blitz" ? 1320 : introVariant === "power" ? 1760 : 2820;
      this.clearFeatureScenePresentation();
      const useCountdownPrelude =
        this.shouldUseDonorCountdownPrelude(this.activeMathBridgeHints) &&
        introVariant !== "blitz";
      const countdownDelayMs = useCountdownPrelude
        ? this.playDonorCountdownPhase([0, 1, 2, 3], [200, 300, 350, 350], 3850)
        : 0;
      const lineCountPreludeMs =
        this.shouldUseDonorLineCountPrelude(this.activeMathBridgeHints) &&
        !useCountdownPrelude
          ? this.playDonorCountdownPhase(
              [0, 2, 5, 7, 10, 12, 14, 16, 19, 21, 24],
              [140, 140, 140, 140, 140, 140, 140, 140, 140, 140, 180],
              3280,
              {
                mode: "lineCount",
                caption: "LINE COUNT",
                guideYRatio: 0.22,
              },
            )
          : 0;
      const bonusHandoffDelayMs = this.isDonorFeatureScenario()
        ? Math.max(
            140,
            Math.max(countdownDelayMs, lineCountPreludeMs) + (useCountdownPrelude ? 280 : 240),
          )
        : 0;
      this.showStatus("HOLD & WIN");
      this.topperMascot.setState("react_collect");
      this.jackpotPlaques.clear();
      if (!useCountdownPrelude) {
        void this.featureIntroOverlay.play(introVariant, {
          holdMs: introHoldMs,
          symbolGrid: this.activeIntroPreviewGrid,
        });
      } else {
        this.featureIntroOverlay.clear();
      }
      this.visualChrome.triggerPresentationCue({
        tone: "bonus",
        title: "HOLD & WIN",
        caption: "LOCKED ENTRY",
        holdMs: 1020,
      });
      const boardStrikeTarget = {
        x: featureFocus.x,
        y: Math.max(42, featureFocus.y - 18),
      };
      const startBonusHandoff = () => {
        if (introVariant === "power" && this.isDonorFeatureScenario()) {
          if (this.activeMathBridgeHints?.triggers.jackpot) {
            return;
          }
          this.clearFeatureScenePresentation();
          this.layeredFx.playBoostStrike(featureOrigins, {
            targetX: boardStrikeTarget.x,
            targetY: boardStrikeTarget.y,
            durationMs: Math.max(
              980,
              (this.activeMathBridgeHints?.timingHints.coinFlyDurationMs ?? 880) + 80,
            ),
            spawnCoinBurst: false,
          });
          this.layeredFx.playBoostLoop();
          this.layeredFx.playCoinFlyBurst([featureFocus], {
            durationMs: Math.max(760, (this.activeMathBridgeHints?.timingHints.coinFlyDurationMs ?? 880) - 120),
            countPerOrigin: 1,
            waveCount: 1,
            waveGapMs: 0,
            startSpreadX: 6,
            startSpreadY: 6,
            endSpreadX: 8,
            endSpreadY: 6,
            controlLift: 56,
          });
          return;
        }
        if (this.isDonorFeatureScenario()) {
          this.layeredFx.playCoinFlyBurst(featureOrigins, {
            durationMs: Math.max(
              1160,
              (this.activeMathBridgeHints?.timingHints.coinFlyDurationMs ?? 880) + 120,
            ),
            countPerOrigin: 1,
            waveCount: 2,
            waveGapMs: 180,
            startSpreadX: 14,
            startSpreadY: 10,
            endSpreadX: 14,
            endSpreadY: 8,
            controlLift: 72,
          });
          return;
        }
        this.layeredFx.playBoostLoop();
        this.layeredFx.playCoinFlyBurst(featureOrigins, {
          durationMs: Math.max(
            1320,
            (this.activeMathBridgeHints?.timingHints.coinFlyDurationMs ?? 880) + 220,
          ),
          countPerOrigin: 2,
          waveCount: 2,
          waveGapMs: 160,
          startSpreadX: 18,
          startSpreadY: 12,
          endSpreadX: 18,
          endSpreadY: 10,
          controlLift: 100,
        });
        if (!this.isDonorFeatureScenario()) {
          this.particleBurst.play(featureFocus.x, Math.max(24, featureFocus.y - 16));
        }
      };
      if (bonusHandoffDelayMs > 0) {
        this.scheduleAfterDonorFeatureIntro(startBonusHandoff, bonusHandoffDelayMs);
      } else {
        startBonusHandoff();
      }
      this.applySoundCue("feature-bonus-enter");
      return;
    }

    if (cue === "feature.jackpot.attached") {
      if (this.jackpotFeatureCueConsumed) {
        return;
      }
      this.jackpotFeatureCueConsumed = true;
      const preserveBoostPhase =
        this.isDonorFeatureScenario() &&
        Boolean(this.activeMathBridgeHints?.triggers.boost);
      if (preserveBoostPhase) {
        this.clearDonorCountdownPhase();
        this.featureIntroOverlay.clear();
      } else {
        this.clearFeatureScenePresentation();
      }
      const useCountdownPrelude = this.shouldUseDonorCountdownPrelude(this.activeMathBridgeHints);
      const countdownDelayMs = useCountdownPrelude
        ? this.playDonorCountdownPhase([0, 1, 2, 3], [200, 300, 350, 350], 3850)
        : 0;
      const lineCountPreludeMs =
        this.shouldUseDonorLineCountPrelude(this.activeMathBridgeHints) &&
        !useCountdownPrelude
          ? this.playDonorCountdownPhase(
              [0, 1, 2, 3],
              [220, 260, 320, 360],
              3380,
              {
                mode: "lineCount",
                caption: "JACKPOT CHARGE",
                guideYRatio: 0.52,
              },
            )
          : 0;
      const jackpotHandoffDelayMs = this.isDonorFeatureScenario()
        ? Math.max(
            200,
            Math.max(countdownDelayMs, lineCountPreludeMs) + (useCountdownPrelude ? 320 : 240),
          )
        : 0;
      const startJackpotHandoff = () => {
        this.showStatus("JACKPOT CALL");
        this.topperMascot.setState("react_jackpot");
        this.jackpotPlaques.celebrate(
          this.resolveJackpotPlaqueLevel(this.activeMathBridgeHints?.jackpotTier ?? null),
        );
        this.visualChrome.triggerPresentationCue({
          tone: "jackpot",
          title: "JACKPOT CALL",
          caption:
            this.activeMathBridgeHints?.jackpotTier?.toUpperCase() ?? "PLAQUE STRIKE",
          holdMs: 1220,
          jackpotTier: this.activeMathBridgeHints?.jackpotTier ?? null,
        });
        const playStrike = () => {
          this.layeredFx.playJackpotStrike(
            this.resolveJackpotReactionLevel(this.activeMathBridgeHints?.jackpotTier ?? null),
            featureOrigins,
            jackpotTarget,
            Math.max(
              1420,
              (this.activeMathBridgeHints?.timingHints.coinFlyDurationMs ?? 880) + 320,
            ),
            this.activeMathBridgeHints?.jackpotTier ?? null,
          );
          const strikePoint = jackpotTarget ?? featureFocus;
          if (!this.isDonorFeatureScenario()) {
            this.particleBurst.play(strikePoint.x, Math.max(24, strikePoint.y - 12));
          }
          this.applySoundCue("feature-jackpot-enter");
        };
        const strikeDelayMs =
          getProviderPackStatus().effectiveProvider === "donorlocal" ? 110 : 0;
        if (strikeDelayMs > 0) {
          const strikeTimeout = window.setTimeout(playStrike, strikeDelayMs);
          this.mathBridgeTimeouts.push(strikeTimeout);
        } else {
          playStrike();
        }
      };
      if (jackpotHandoffDelayMs > 0) {
        this.scheduleAfterDonorFeatureIntro(startJackpotHandoff, jackpotHandoffDelayMs);
      } else {
        startJackpotHandoff();
      }
      return;
    }

    if (cue === "jackpot-overlay") {
      if (this.isDonorFeatureScenario()) {
        return;
      }
      const jackpotOverlayDelayMs = this.isDonorFeatureScenario() ? 180 : 0;
      const startJackpotOverlay = () => {
        const reactionLevel = this.resolveJackpotReactionLevel(
          this.activeMathBridgeHints?.jackpotTier ?? null,
        );
        const overlayPoint = jackpotTarget ?? featureFocus;
        this.jackpotPlaques.celebrate(
          this.resolveJackpotPlaqueLevel(this.activeMathBridgeHints?.jackpotTier ?? null),
        );
        this.layeredFx.playJackpotStrike(
          reactionLevel,
          featureOrigins,
          jackpotTarget,
          Math.max(
            1520,
            (this.activeMathBridgeHints?.timingHints.coinFlyDurationMs ?? 880) + 420,
          ),
          this.activeMathBridgeHints?.jackpotTier ?? null,
        );
        this.layeredFx.playCoinFlyBurst([overlayPoint], {
          durationMs: Math.max(
            1360,
            (this.activeMathBridgeHints?.timingHints.coinFlyDurationMs ?? 880) + 320,
          ),
          countPerOrigin: 3 + Math.min(1, reactionLevel),
          waveCount: 2,
          waveGapMs: 140,
          startSpreadX: 18,
          startSpreadY: 12,
          endSpreadX: 16,
          endSpreadY: 10,
          controlLift: 88,
        });
        if (!this.isDonorFeatureScenario()) {
          this.particleBurst.play(overlayPoint.x, Math.max(24, overlayPoint.y - 12));
        }
      };
      if (jackpotOverlayDelayMs > 0) {
        this.scheduleAfterDonorFeatureIntro(startJackpotOverlay, jackpotOverlayDelayMs);
      } else {
        startJackpotOverlay();
      }
      return;
    }

    if (cue === "coin-fly") {
      if (this.isDonorFeatureScenario()) {
        return;
      }
      this.layeredFx.playCoinFlyBurst(featureOrigins, {
        durationMs: Math.max(1100, (this.activeMathBridgeHints?.timingHints.coinFlyDurationMs ?? 880) + 140),
        countPerOrigin: 2,
        waveCount: 2,
        waveGapMs: 150,
        startSpreadX: 14,
        startSpreadY: 10,
        endSpreadX: 18,
        endSpreadY: 10,
        controlLift: 84,
      });
      this.particleBurst.play(featureFocus.x, Math.max(24, featureFocus.y - 8));
      return;
    }

    if (cue === "overlay.winTier.enter") {
      if (this.isDonorFeatureScenario()) {
        return;
      }
      if (this.isDonorFeatureScenario() && this.isDonorFeatureIntroActive()) {
        this.scheduleAfterDonorFeatureIntro(() => {
          this.applyAnimationCue("overlay.winTier.enter");
        }, 120);
        return;
      }
      const donorFeatureIntroVariant = this.isDonorFeatureScenario()
        ? this.resolveDonorBuyFeatureIntroVariant()
        : null;
      if (donorFeatureIntroVariant === "power") {
        this.visualChrome.triggerPresentationCue({
          tone: "boost",
          title: "POWER STRIKE",
          caption: "BOARD CHARGE",
          holdMs: 820,
          jackpotTier: null,
        });
        return;
      }
      const tier = this.activeMathBridgeHints?.winTier ?? "none";
      if (this.shouldUseBigWinMascot(this.activeMathBridgeHints)) {
        this.topperMascot.setState("react_bigwin");
      }
      this.visualChrome.triggerPresentationCue({
        tone: tier === "mega" ? "jackpot" : "standard",
        title: tier === "none" ? "TOTAL WIN" : `${tier.toUpperCase()} WIN`,
        caption: "COUNTER CHARGE",
        holdMs: 1080,
        jackpotTier: this.activeMathBridgeHints?.jackpotTier ?? null,
      });
      if (this.isEscalatedWinTier(tier)) {
        // Keep line visibility through win-tier pulse on donorlocal so
        // paylines remain readable instead of dropping out mid-sequence.
        const pulseTimeout = window.setTimeout(() => {
          this.layeredFx.playWinPulse(tier);
        }, 80);
        this.mathBridgeTimeouts.push(pulseTimeout);
      }
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
      betLabel: String(ResolvedRuntimeConfigStore.limits.defaultBet),
      balanceLabel: Math.max(0, session?.balance ?? 0).toLocaleString("en-US"),
      winLabel: Math.max(0, winAmount).toLocaleString("en-US"),
      autoplayActive: this.autoplayActive,
      turboSelected: this.turboSelected || this.holdTurboRequested,
      soundEnabled: this.soundEnabled,
      holdTurboRequested: this.holdTurboRequested,
      orientation: engine().layout.getViewport().orientation,
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
    this.clearRuntimePaylinePresentation();
    this.paylineOverlay.clear();
    this.paylineHighlight.clear();
    this.debugOverlay.setMathBridgeSummary(null);
    this.layeredFx.clearPresentation();
    this.topperMascot.setState("idle");
    this.jackpotPlaques.clear();
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
    this.showStartupPaylineGuideOnLoad();
  }

  private syncHudChrome(): void {
    const buttons = (this.hud as unknown as HudInternals).buttons;
    this.applyDonorHudLayout(buttons);
    this.hud.alpha = 0.001;
    this.hudChrome.attachButtons(buttons);
    for (const button of Object.values(buttons)) {
      button.alpha = 0.001;
      button.text = "";
    }
  }

  private applyDonorHudLayout(buttons: Record<PremiumHudControlId, HudButtonHandle>): void {
    const viewport = engine().layout.getViewport();
    const safe = viewport.safeArea;
    const isDonorlocal = getProviderPackStatus().effectiveProvider === "donorlocal";
    const isPortrait = viewport.orientation === "portrait";
    const isNarrow = viewport.width < 900;
    const bottomY = viewport.height - Math.max(58, safe.bottom + 58);
    const rightEdge = viewport.width - safe.right - 74;
    const leftEdge = safe.left + 54;

    buttons.settings.x = leftEdge;
    buttons.settings.y = bottomY;
    buttons.settings.width = isDonorlocal ? 58 : 64;
    buttons.settings.height = isDonorlocal ? 58 : 64;
    buttons.settings.visible = true;

    buttons.sound.x = leftEdge + 74;
    buttons.sound.y = bottomY;
    buttons.sound.width = 58;
    buttons.sound.height = 58;
    buttons.sound.visible = isDonorlocal;

    buttons.history.x = leftEdge + 148;
    buttons.history.y = bottomY;
    buttons.history.width = 58;
    buttons.history.height = 58;
    buttons.history.visible = isDonorlocal;

    const reelScale = this.reelsLayer.scale.x;
    const machineWidth =
      CRAZY_ROOSTER_LAYOUT.reelCount * CRAZY_ROOSTER_LAYOUT.symbolWidth +
      (CRAZY_ROOSTER_LAYOUT.reelCount - 1) * CRAZY_ROOSTER_LAYOUT.reelSpacing;
    const machineHeight =
      CRAZY_ROOSTER_LAYOUT.rowCount * CRAZY_ROOSTER_LAYOUT.symbolHeight +
      (CRAZY_ROOSTER_LAYOUT.rowCount - 1) * CRAZY_ROOSTER_LAYOUT.rowSpacing;
    const reelMidY =
      this.reelsLayer.y +
      reelScale * (CRAZY_ROOSTER_LAYOUT.symbolHeight * CRAZY_ROOSTER_LAYOUT.rowCount * 0.5);
    const reelBottomY = this.reelsLayer.y + reelScale * machineHeight;

    if (isDonorlocal && isPortrait) {
      buttons.buyFeature.x = this.reelsLayer.x + reelScale * machineWidth * 0.5;
      buttons.buyFeature.y = reelBottomY + reelScale * 63;
      buttons.buyFeature.width = isNarrow ? 176 : 188;
      buttons.buyFeature.height = isNarrow ? 70 : 76;
    } else {
      buttons.buyFeature.x = Math.max(safe.left + 108, this.reelsLayer.x - (isDonorlocal ? 28 : 66));
      buttons.buyFeature.y = reelMidY + (isDonorlocal ? -38 : -4);
      buttons.buyFeature.width = isDonorlocal ? (isNarrow ? 124 : 136) : 128;
      buttons.buyFeature.height = isDonorlocal ? (isNarrow ? 74 : 84) : 128;
    }

    buttons.spin.x = rightEdge;
    buttons.spin.y = bottomY + (isDonorlocal ? 4 : 2);
    buttons.spin.width = isDonorlocal ? 82 : 78;
    buttons.spin.height = isDonorlocal ? 82 : 78;

    buttons.autoplay.x = rightEdge + (isDonorlocal ? 6 : 0);
    buttons.autoplay.y = bottomY - (isDonorlocal ? 74 : 88);
    buttons.autoplay.width = 58;
    buttons.autoplay.height = 58;
    buttons.autoplay.visible = true;

    buttons.turbo.x = rightEdge - 82;
    buttons.turbo.y = bottomY - 30;
    buttons.turbo.width = 136;
    buttons.turbo.height = 136;
    buttons.turbo.visible = true;

    if (isDonorlocal && isPortrait) {
      const footerY = viewport.height - Math.max(82, safe.bottom + 82);
      const rightClusterX = viewport.width - safe.right - 56;

      buttons.settings.x = safe.left + 28;
      buttons.settings.y = footerY + 4;
      buttons.settings.width = 56;
      buttons.settings.height = 56;
      buttons.settings.visible = true;

      buttons.sound.x = safe.left + 92;
      buttons.sound.y = footerY + 4;
      buttons.sound.width = 58;
      buttons.sound.height = 58;
      buttons.sound.visible = false;

      buttons.history.x = safe.left + 156;
      buttons.history.y = footerY + 4;
      buttons.history.width = 58;
      buttons.history.height = 58;
      buttons.history.visible = false;

      buttons.turbo.x = viewport.width * (isNarrow ? 0.64 : 0.655);
      buttons.turbo.y = footerY - 8;
      buttons.turbo.width = isNarrow ? 144 : 152;
      buttons.turbo.height = isNarrow ? 144 : 152;
      buttons.turbo.visible = true;

      buttons.spin.x = rightClusterX;
      buttons.spin.y = footerY + 2;
      buttons.spin.width = 68;
      buttons.spin.height = 68;
      buttons.spin.visible = true;

      buttons.autoplay.x = rightClusterX;
      buttons.autoplay.y = footerY - 62;
      buttons.autoplay.width = 54;
      buttons.autoplay.height = 54;
      buttons.autoplay.visible = true;
    } else if (isDonorlocal) {
      const betPanelY = bottomY + (isNarrow ? -24 : -22);
      const donorBottomY = betPanelY - (isNarrow ? 48 : 54);
      const rightActionX = viewport.width - safe.right - (isNarrow ? 232 : 340);

      buttons.settings.x = safe.left + (isNarrow ? 34 : 52);
      buttons.settings.y = donorBottomY;
      buttons.settings.width = isNarrow ? 52 : 58;
      buttons.settings.height = isNarrow ? 52 : 58;

      buttons.sound.x = viewport.width * (isNarrow ? 0.56 : 0.57);
      buttons.sound.y = donorBottomY;
      buttons.sound.width = 56;
      buttons.sound.height = 56;
      buttons.sound.visible = false;

      buttons.history.x = viewport.width * (isNarrow ? 0.68 : 0.67);
      buttons.history.y = donorBottomY;
      buttons.history.width = 56;
      buttons.history.height = 56;
      buttons.history.visible = false;

      buttons.spin.x = rightActionX;
      buttons.spin.y = donorBottomY + 20;
      buttons.spin.width = 62;
      buttons.spin.height = 62;
      buttons.autoplay.x = rightActionX + 4;
      buttons.autoplay.y = donorBottomY - (isNarrow ? 42 : 48);
      buttons.autoplay.width = 48;
      buttons.autoplay.height = 48;
      buttons.turbo.x = rightActionX - (isNarrow ? 84 : 94);
      buttons.turbo.y = donorBottomY + 12;
      buttons.turbo.width = isNarrow ? 144 : 156;
      buttons.turbo.height = isNarrow ? 144 : 156;
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
    this.jackpotPlaques.clear();
    this.clearRuntimePaylinePresentation();
    this.paylineOverlay.clear();
    this.paylineHighlight.clear();
    this.isPresentingWin = false;
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
