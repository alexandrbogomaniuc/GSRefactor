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
  private readonly paylineOverlay = new PaylineOverlay();
  private readonly paylineHighlight = new WinHighlight();
  private readonly winHighlight = new WinHighlight();
  private readonly particleBurst = new ParticleBurst();
  private readonly donorBuyBonusModal = new DonorBuyBonusModal();
  private readonly featureIntroOverlay = new DonorFeatureIntroOverlay();
  private readonly donorCountdownOverlay = new Container();
  private readonly donorCountdownBackdrop = new Graphics();
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
    this.reelsLayer.addChild(this.slotMachine);
    this.reelsLayer.addChild(this.topperMascot);
    this.reelsLayer.addChild(this.jackpotPlaques);
    this.fxLayer.addChild(this.layeredFx);
    this.fxLayer.addChild(this.paylineOverlay);
    this.fxLayer.addChild(this.paylineHighlight);
    this.fxLayer.addChild(this.winHighlight);
    this.fxLayer.addChild(this.particleBurst);
    this.donorCountdownValue.anchor.set(0.5);
    this.donorCountdownOverlay.visible = false;
    this.donorCountdownOverlay.eventMode = "none";
    this.donorCountdownOverlay.addChild(this.donorCountdownBackdrop, this.donorCountdownValue);
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
    this.connectSpinHoldGesture();
    this.applyPreviewState();
    this.refreshHudState(0);
    this.showStatus(this.resolveBenchmarkReadyStatus());
  }

  public prepare(): void {}

  public update(ticker: { deltaMS: number }): void {
    if (this.paused) return;
    const deltaMs = Math.max(0, ticker.deltaMS);
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
      return true;
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
    this.paylineOverlay.clear();
    this.paylineHighlight.clear();
    this.activeIntroPreviewGrid = null;
    this.featureIntroOverlay.clear();
    this.clearDonorCountdownPhase();
  }

  private clearFeatureScenePresentation(): void {
    this.layeredFx.clearPresentation();
    this.particleBurst.clear();
    this.winHighlight.clear();
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
    this.donorCountdownValue.x = machineWidth * 0.5;
    this.donorCountdownValue.y = machineHeight * 0.5;
  }

  private playDonorCountdownPhase(
    values: number[] = [0, 1, 2, 3],
    stepMs = 420,
    tailMs = 220,
  ): number {
    if (getProviderPackStatus().effectiveProvider !== "donorlocal") {
      return 0;
    }

    this.clearDonorCountdownPhase();
    this.donorCountdownOverlay.visible = true;
    this.donorCountdownOverlay.alpha = 1;

    values.forEach((value, index) => {
      const timeout = window.setTimeout(() => {
        this.donorCountdownValue.text = `${value}`;
        const flashAlpha = value >= values[values.length - 1] ? 0.88 : 0.78;
        this.donorCountdownBackdrop.clear();
        this.donorCountdownBackdrop.roundRect(
          0,
          0,
          CRAZY_ROOSTER_LAYOUT.reelCount * CRAZY_ROOSTER_LAYOUT.symbolWidth +
            (CRAZY_ROOSTER_LAYOUT.reelCount - 1) * CRAZY_ROOSTER_LAYOUT.reelSpacing,
          CRAZY_ROOSTER_LAYOUT.rowCount * CRAZY_ROOSTER_LAYOUT.symbolHeight +
            (CRAZY_ROOSTER_LAYOUT.rowCount - 1) * CRAZY_ROOSTER_LAYOUT.rowSpacing,
          18,
        );
        this.donorCountdownBackdrop.fill({ color: 0x0b0615, alpha: flashAlpha });
        this.donorCountdownBackdrop.stroke({
          color: value >= 2 ? 0xeb4b2b : 0xd8b15a,
          width: 4,
          alpha: 0.68,
        });
      }, index * stepMs);
      this.mathBridgeTimeouts.push(timeout);
    });

    const totalMs = values.length * stepMs + tailMs;
    const hideTimeout = window.setTimeout(() => {
      this.clearDonorCountdownPhase();
    }, totalMs);
    this.mathBridgeTimeouts.push(hideTimeout);

    return totalMs;
  }

  private clearDonorCountdownPhase(): void {
    this.donorCountdownOverlay.visible = false;
    this.donorCountdownValue.text = "";
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

    if (this.isDonorFeatureScenario(mathBridgeHints)) {
      this.paylineOverlay.clear();
      this.paylineHighlight.clear();
      return;
    }

    const timing = this.resolveLineSequenceTiming(mathBridgeHints, linePresentations.length);
    const tone = this.resolveLineTone(mathBridgeHints);

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
    const intensity = lineScale * toneScale * escalatedScale * cherryScale;

    return {
      amplitudePx: 1.2 * intensity,
      liftPx: 1.5 * intensity,
      scalePulse: 0.018 * intensity,
      frequencyHz: isCherryLine ? 2.45 : 2.1,
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
    if (!mathBridgeHints || !this.isEscalatedWinTier(mathBridgeHints.winTier)) {
      return false;
    }
    if (mathBridgeHints.winTier === "mega" || mathBridgeHints.triggers.jackpot) {
      return true;
    }
    return mathBridgeHints.totalWinMultiplier >= 10;
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
        return this.shouldUseBigWinMascot(mathBridgeHints) ? "react_bigwin" : "react_collect";
      case "jackpot":
        return "react_jackpot";
      case "standard":
      default:
        return this.shouldUseBigWinMascot(mathBridgeHints) ? "react_bigwin" : "react_collect";
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
    this.jackpotFeatureCueConsumed = false;
    this.activeIntroPreviewGrid = presentation.symbolGrid.map((row) => [...row]);
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
    const settledColumns = this.resolvePresentationColumns(
      presentation.reels.stopColumns,
      mathBridgeHints,
    );
    const settledPresentation =
      settledColumns === presentation.reels.stopColumns
        ? presentation
        : {
            ...presentation,
            reels: {
              ...presentation.reels,
              stopColumns: settledColumns,
            },
            symbolGrid: buildGridFromColumns(settledColumns),
          };
    this.slotMachine.setPresentationColumns(
      settledPresentation.reels.stopColumns,
      this.resolvePresentationVariants(settledPresentation.symbolGrid, mathBridgeHints),
    );
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
    const linePresentations = this.resolveLinePresentations(mathBridgeHints);
    const winAmount = settledPresentation.winAmount;
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
      Math.max(280, mathBridgeHints.timingHints.featureLoopDurationMs) +
      Math.max(0, startDelayMs);
    const settleDelayMs =
      finishDelayMs + Math.max(140, mathBridgeHints.timingHints.featureFinishDelayMs);

    const finishTimeout = window.setTimeout(() => {
      this.topperMascot.setState("react_boost_finish");
      this.layeredFx.playBoostFinish();
      this.particleBurst.play(focusPoint.x, Math.max(24, focusPoint.y - 10));
    }, finishDelayMs);
    const settleTimeout = window.setTimeout(() => {
      this.layeredFx.playCoinFlyBurst([focusPoint], {
        durationMs: Math.max(420, mathBridgeHints.timingHints.coinFlyDurationMs - 160),
        countPerOrigin: 2,
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
      const introHoldMs = 1620;
      this.clearFeatureScenePresentation();
      const useCountdownPrelude =
        this.isDonorFeatureScenario() && (this.activeMathBridgeHints?.mode ?? "base") === "base";
      const countdownDelayMs = useCountdownPrelude
        ? this.playDonorCountdownPhase([0, 1, 2, 3], 420, 160)
        : 0;
      const boostHandoffDelayMs = this.isDonorFeatureScenario()
        ? Math.max(120, countdownDelayMs + (useCountdownPrelude ? 80 : 0))
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
      };
      if (boostHandoffDelayMs > 0) {
        this.scheduleAfterDonorFeatureIntro(startBoostHandoff, boostHandoffDelayMs);
      } else {
        startBoostHandoff();
      }
      this.scheduleBoostFinish(this.activeMathBridgeHints, featureFocus, boostHandoffDelayMs);
      this.applySoundCue("feature-boost-enter");
      return;
    }

    if (cue === "feature.bonus.enter") {
      const introVariant = this.resolveDonorBuyFeatureIntroVariant();
      const introHoldMs =
        introVariant === "blitz" ? 1320 : introVariant === "power" ? 1760 : 2820;
      this.clearFeatureScenePresentation();
      const useCountdownPrelude =
        this.isDonorFeatureScenario() &&
        (this.activeMathBridgeHints?.mode ?? "base") === "base" &&
        introVariant !== "blitz";
      const countdownDelayMs = useCountdownPrelude
        ? this.playDonorCountdownPhase([0, 1, 2, 3], 420, 220)
        : 0;
      const bonusHandoffDelayMs = this.isDonorFeatureScenario()
        ? Math.max(120, countdownDelayMs + (useCountdownPrelude ? 100 : 0))
        : 0;
      this.showStatus("HOLD & WIN");
      this.topperMascot.setState(
        this.shouldUseBigWinMascot(this.activeMathBridgeHints)
          ? "react_bigwin"
          : "react_collect",
      );
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
      this.clearFeatureScenePresentation();
      const useCountdownPrelude =
        this.isDonorFeatureScenario() && (this.activeMathBridgeHints?.mode ?? "base") === "base";
      const countdownDelayMs = useCountdownPrelude
        ? this.playDonorCountdownPhase([0, 1, 2, 3], 420, 220)
        : 0;
      const jackpotHandoffDelayMs = this.isDonorFeatureScenario()
        ? Math.max(180, countdownDelayMs + (useCountdownPrelude ? 120 : 0))
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
        this.layeredFx.playWinPulse(tier);
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
