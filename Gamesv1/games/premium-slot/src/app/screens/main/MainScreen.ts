import { Container, Text } from "pixi.js";

import {
  AnimationPolicyEngine,
  createAnimationPolicy,
  type ResolvedConfig,
} from "@gamesv1/core-compliance";
import { engine } from "@gamesv1/pixi-engine";
import {
  createAudioCueRegistry,
  FeatureModuleManager,
  type FeatureFrame,
  GameConfig,
  PremiumTemplateHud,
  type AudioCueRegistry,
  type PremiumHudControlId,
  type PremiumHudFeatureFlags,
  type PremiumHudVisibility,
  resolvePremiumHudVisibility,
  SettingsPopup,
  SlotMachine,
  resolveAudioCueActions,
  WowVfxOrchestrator,
} from "@gamesv1/ui-kit";
import {
  gsRuntimeClient,
  mapPlayRoundToPresentation,
  type RoundPresentationModel,
} from "../../runtime";
import {
  PresentationStateStore,
  ResolvedRuntimeConfigStore,
  SessionRuntimeStore,
} from "../../stores";

import { AppAssetKeys } from "../../assets/assetKeys";
import { userSettings } from "../../utils/userSettings";
import { ParticleBurst } from "../../../game/fx/ParticleBurst";
import { WinHighlight } from "../../../game/fx/WinHighlight";
import { WinCounter } from "../../../game/ui/WinCounter";
import { DebugOverlay } from "./DebugOverlay";

type PendingRoundResolution = {
  presentation: RoundPresentationModel;
  featureFrame: FeatureFrame;
  balance: number;
};

const readHudFeatureFlagsFromQuery = (): PremiumHudFeatureFlags => {
  const params = new URLSearchParams(window.location.search);
  const controls: PremiumHudFeatureFlags["controls"] = {};

  const maybeOverride = (key: PremiumHudControlId, queryKey?: string) => {
    const resolvedQueryKey = queryKey ?? key;
    const value = params.get(resolvedQueryKey);
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

  return {
    controls,
  };
};

const formatMessages = (messages: string[]): string =>
  messages.filter(Boolean).slice(0, 3).join(" | ");

export class MainScreen extends Container {
  public static assetBundles = ["main"];

  private readonly runtimeConfig: ResolvedConfig;

  private reelsLayer: Container;
  private fxLayer: Container;
  private uiLayer: Container;

  private slotMachine: SlotMachine;
  private winHighlight: WinHighlight;
  private particleBurst: ParticleBurst;
  private winCounter: WinCounter;
  private debugOverlay: DebugOverlay;
  private hud: PremiumTemplateHud;
  private statusText: Text;

  private paused = false;
  private isSpinning = false;
  private isPresentingWin = false;
  private turboSelected = false;
  private soundEnabled = true;
  private currentWinPresentationTimeout: ReturnType<typeof setTimeout> | null = null;

  private readonly animationPolicy: AnimationPolicyEngine;
  private readonly audioCueRegistry: AudioCueRegistry;
  private readonly wowVfx: WowVfxOrchestrator;
  private readonly featureModules: FeatureModuleManager;
  private readonly baseHudVisibility: PremiumHudVisibility;

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

    this.featureModules = new FeatureModuleManager(this.runtimeConfig);
    this.audioCueRegistry = createAudioCueRegistry();
    this.wowVfx = new WowVfxOrchestrator(this.animationPolicy, {
      onAudioCue: (cue) => this.applySoundCue(cue),
      onAnimationCue: (cue) => this.applyAnimationCue(cue),
      showWinCounter: (amountMinor, title) => this.winCounter.showWin(amountMinor, title),
      hideWinCounter: () => this.winCounter.hideNow(),
      showHeavyWinFx: (symbols) => this.winHighlight.showWin(symbols),
      clearHeavyWinFx: () => this.winHighlight.clear(),
      playCoinBurst: (origin) => this.particleBurst.play(origin.x, origin.y),
    });
    this.baseHudVisibility = resolvePremiumHudVisibility(
      this.runtimeConfig,
      readHudFeatureFlagsFromQuery(),
    );

    this.turboSelected = this.animationPolicy.value.turbo.selected;
    this.soundEnabled = this.runtimeConfig.soundDefaults.modeByDefault !== "off";

    this.reelsLayer = new Container();
    this.fxLayer = new Container();
    this.uiLayer = new Container();

    this.addChild(this.reelsLayer);
    this.addChild(this.fxLayer);
    this.addChild(this.uiLayer);

    this.slotMachine = new SlotMachine();
    this.reelsLayer.addChild(this.slotMachine);

    this.winHighlight = new WinHighlight();
    this.fxLayer.addChild(this.winHighlight);

    this.particleBurst = new ParticleBurst();
    this.fxLayer.addChild(this.particleBurst);

    this.winCounter = new WinCounter();
    this.uiLayer.addChild(this.winCounter);

    this.hud = new PremiumTemplateHud();
    this.hud.applyVisibility(this.baseHudVisibility);
    this.hud.setCallbacks({
      onControlPress: (controlId) => {
        void this.handleHudControl(controlId);
      },
    });
    this.uiLayer.addChild(this.hud);

    this.statusText = new Text({
      text: "",
      style: {
        fontFamily: "Arial",
        fontSize: 24,
        fill: 0xffffff,
        stroke: { color: 0x111111, width: 3 },
        fontWeight: "700",
        align: "center",
      },
    });
    this.statusText.anchor.set(0.5);
    this.statusText.visible = false;
    this.uiLayer.addChild(this.statusText);

    this.debugOverlay = new DebugOverlay();
    this.addChild(this.debugOverlay);

    this.slotMachine.onSpinComplete = () => this.handleSpinComplete();

    this.refreshHudState(0);
    PresentationStateStore.patch({
      turboSelected: this.turboSelected,
      soundEnabled: this.soundEnabled,
      lastMessages: [],
      activeFeatureModules: [],
    });
    this.showStatus(`FEATURE MODULES: ${this.featureModules.listEnabledModules().join(", ")}`);
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
        this.showStatus("AUTOPLAY CONFIGURED");
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
    if (!this.animationPolicy.value.turbo.allowed) return;
    this.turboSelected = !this.turboSelected;
    PresentationStateStore.patch({
      turboSelected: this.turboSelected,
      soundEnabled: this.soundEnabled,
    });
    this.refreshHudState(PresentationStateStore.get().lastWinAmount);
  }

  private toggleSound(): void {
    this.soundEnabled = !this.soundEnabled;
    if (this.soundEnabled) {
      userSettings.setMasterVolume(this.runtimeConfig.soundDefaults.masterVolume);
      this.showStatus("SOUND ENABLED");
    } else {
      userSettings.setMasterVolume(0);
      this.showStatus("SOUND DISABLED");
    }
    PresentationStateStore.patch({ soundEnabled: this.soundEnabled });
    this.refreshHudState(PresentationStateStore.get().lastWinAmount);
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
    try {
      const totalBetMinor = Math.max(1, Math.round(ResolvedRuntimeConfigStore.limits.defaultBet));
      const lines = 20;
      const multiplier = 1;
      const coinValueMinor = Math.max(1, Math.floor(totalBetMinor / (lines * multiplier)));

      await gsRuntimeClient.featureaction("buy-feature", {
        selectedBet: {
          coinValueMinor,
          lines,
          multiplier,
          totalBetMinor,
        },
        featureType: "BUY_FEATURE",
        priceMinor: totalBetMinor,
        payload: { source: "hud" },
      });
      this.showStatus("BUY FEATURE ACTION SENT");
    } catch (error) {
      this.showStatus(`BUY FEATURE FAILED: ${String(error)}`);
    }
  }

  private async handleSpin() {
    if (this.isPresentingWin && this.animationPolicy.shouldAllowForcedSkip()) {
      this.skipWinPresentation();
      return;
    }

    if (this.isSpinning || this.isPresentingWin) return;

    this.isSpinning = true;
    this.pendingRound = null;

    this.winHighlight.clear();
    this.showStatus("ROUND REQUESTED");
    PresentationStateStore.patch({ isSpinning: true, statusText: "ROUND_REQUESTED" });

    const timing = this.animationPolicy.resolveSpinTiming(this.turboSelected);
    const totalBetMinor = Math.max(1, Math.round(ResolvedRuntimeConfigStore.limits.defaultBet));
    const lines = 20;
    const multiplier = 1;
    const selectedBet = {
      coinValueMinor: Math.max(1, Math.floor(totalBetMinor / (lines * multiplier))),
      lines,
      multiplier,
      totalBetMinor,
    };

    try {
      const round = await gsRuntimeClient.playround(selectedBet);
      const presentation = mapPlayRoundToPresentation(round);
      const featureFrame = this.featureModules.resolve(presentation);

      this.pendingRound = {
        presentation,
        featureFrame,
        balance: SessionRuntimeStore.get().balance,
      };

      this.slotMachine.spin({
        minSpinDurationMs: timing.minSpinMs,
        spinStaggerMs: timing.spinStaggerMs,
        speedMultiplier: timing.speedMultiplier,
        reelStopColumns: presentation.reels.stopColumns,
      });
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

  private handleSpinComplete() {
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

    const { presentation, featureFrame } = resolution;
    this.applyDynamicControlVisibility(featureFrame);

    const mergedMessages = [
      ...presentation.messages,
      ...featureFrame.messages,
      ...featureFrame.overlays.map((overlay) => overlay.label),
    ];
    this.showStatus(formatMessages(mergedMessages));

    const reels = this.slotMachine.getReels();
    const winSymbols = [
      reels[0].getVisibleSymbols()[1],
      reels[1].getVisibleSymbols()[1],
      reels[2].getVisibleSymbols()[1],
    ];

    const winAmount = presentation.winAmount;
    const defaultBet = ResolvedRuntimeConfigStore.limits.defaultBet;
    const vfxState = this.wowVfx.startWinPresentation({
      winAmountMinor: winAmount,
      defaultBetMinor: defaultBet,
      winSymbols,
      soundCues: [...presentation.soundCues, ...featureFrame.soundCues],
      animationCues: [...presentation.animationCues, ...featureFrame.animationCues],
      burstOrigin: { x: 0, y: -100 },
    });

    this.isSpinning = false;
    this.isPresentingWin = vfxState.hasWinPresentation;
    PresentationStateStore.patch({
      isSpinning: false,
      isPresentingWin: vfxState.hasWinPresentation,
      lastWinAmount: winAmount,
      soundEnabled: this.soundEnabled,
      lastMessages: mergedMessages,
      activeFeatureModules: featureFrame.activeModuleIds,
      statusText: `ROUND_SETTLED:${presentation.roundId}`,
    });

    this.refreshHudState(winAmount);

    if (!vfxState.hasWinPresentation) {
      return;
    }

    const presentationMs = vfxState.durationMs;
    this.clearWinPresentationTimeout();
    if (presentationMs <= 0) {
      this.finishWinPresentation(vfxState.forcedSkip);
      return;
    }

    this.currentWinPresentationTimeout = setTimeout(() => {
      this.finishWinPresentation(vfxState.forcedSkip);
    }, presentationMs);
  }

  private applyDynamicControlVisibility(featureFrame: FeatureFrame): void {
    if (featureFrame.controlVisibility.buyFeature !== undefined) {
      this.hud.applyVisibility({
        controls: {
          buyFeature: featureFrame.controlVisibility.buyFeature,
        },
      });
    }
  }

  private applySoundCue(cue: string): void {
    for (const action of resolveAudioCueActions(cue, this.audioCueRegistry)) {
      if (action.type === "sfx") {
        if (action.respectSoundEnabled && !this.soundEnabled) {
          continue;
        }
        engine().audio.sfx.play(action.assetKey, {
          volume: action.volume ?? 1,
        });
        continue;
      }

      if (action.type === "bgmVolume") {
        userSettings.setBgmVolume(action.volume);
      }
    }
  }

  private applyAnimationCue(cue: string): void {
    if (cue === "focus-status-banner") {
      this.showStatus("FEATURE EVENT");
    }
  }

  private refreshHudState(winAmount: number): void {
    const session = SessionRuntimeStore.getSnapshot();
    this.hud.setState({
      balance: session?.balance ?? 0,
      bet: ResolvedRuntimeConfigStore.limits.defaultBet,
      win: winAmount,
      turboSelected: this.turboSelected,
      soundEnabled: this.soundEnabled,
    });
  }

  private showStatus(text: string): void {
    this.statusText.text = text;
    this.statusText.visible = text.length > 0;
  }

  private skipWinPresentation() {
    if (!this.isPresentingWin) return;
    this.clearWinPresentationTimeout();
    this.finishWinPresentation(true);
  }

  private finishWinPresentation(skipped = false) {
    this.wowVfx.finishWinPresentation();
    this.isPresentingWin = false;
    PresentationStateStore.patch({
      isPresentingWin: false,
      statusText: skipped ? "ROUND_PRESENTATION_SKIPPED" : "ROUND_PRESENTATION_FINISHED",
    });

    if (skipped) {
      const delay = this.animationPolicy.getAutoplayDelayMs("none", true);
      if (delay > 0) {
        this.isSpinning = true;
        setTimeout(() => {
          this.isSpinning = false;
        }, delay);
      }
    }
  }

  private clearWinPresentationTimeout() {
    if (this.currentWinPresentationTimeout) {
      clearTimeout(this.currentWinPresentationTimeout);
      this.currentWinPresentationTimeout = null;
    }
  }

  public prepare() {}

  public update() {
    if (this.paused) return;
  }

  public async pause() {
    this.uiLayer.interactiveChildren = false;
    this.paused = true;
  }

  public async resume() {
    this.uiLayer.interactiveChildren = true;
    this.paused = false;
  }

  public reset() {}

  public resize(width: number, height: number) {
    const viewport = engine().layout.getViewport();
    const safe = viewport.safeArea;
    const hudSpace = viewport.orientation === "portrait" ? 290 : 210;

    const centerX = width * 0.5;
    const availableTop = safe.top + 90;
    const availableBottom = height - safe.bottom - hudSpace;
    const centerY = (availableTop + availableBottom) * 0.5;

    const machineWidth =
      GameConfig.numReels * GameConfig.symbolWidth +
      (GameConfig.numReels - 1) * GameConfig.reelSpacing;
    const machineHeight =
      GameConfig.numRows * GameConfig.symbolHeight +
      (GameConfig.numRows - 1) * GameConfig.rowSpacing;

    const availableWidth = width - safe.left - safe.right - 40;
    const availableHeight = availableBottom - availableTop;

    const scale = Math.min(availableWidth / machineWidth, availableHeight / machineHeight);

    this.reelsLayer.scale.set(scale);
    this.fxLayer.scale.set(scale);

    this.reelsLayer.x = centerX - (machineWidth * scale) / 2;
    this.reelsLayer.y = centerY - (machineHeight * scale) / 2;

    this.fxLayer.x = this.reelsLayer.x;
    this.fxLayer.y = this.reelsLayer.y;

    this.uiLayer.x = 0;
    this.uiLayer.y = 0;

    this.hud.resize({
      width,
      height,
      orientation: viewport.orientation,
      safeArea: safe,
    });

    this.statusText.x = width * 0.5;
    this.statusText.y = safe.top + 86;

    this.winCounter.x = width * 0.5;
    this.winCounter.y = safe.top + 180;

    this.debugOverlay.resize(width, height);
  }

  public async show(): Promise<void> {
    engine().audio.bgm.play(AppAssetKeys.BGM_MAIN, {
      volume: this.runtimeConfig.soundDefaults.bgmVolume,
    });
  }

  public async hide() {
    this.clearWinPresentationTimeout();
  }

  public blur() {
    this.showStatus("SESSION PAUSED");
  }
}
