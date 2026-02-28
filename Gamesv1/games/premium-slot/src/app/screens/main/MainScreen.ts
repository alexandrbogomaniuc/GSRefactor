import { FancyButton } from "@pixi/ui";
import { Container, Text } from "pixi.js";

import {
  AnimationPolicyEngine,
  createAnimationPolicy,
  type WinTier,
} from "@gamesv1/core-compliance";
import { engine } from "@gamesv1/pixi-engine";
import {
  Button,
  GameConfig,
  PausePopup,
  ResponsiveHudLayoutController,
  SettingsPopup,
  SlotMachine,
  UiAssetKeys,
} from "@gamesv1/ui-kit";

import { AppAssetKeys } from "../../assets/assetKeys";
import { ParticleBurst } from "../../../game/fx/ParticleBurst";
import { WinHighlight } from "../../../game/fx/WinHighlight";
import { WinCounter } from "../../../game/ui/WinCounter";
import { RuntimeStore } from "../../utils/RuntimeStore";
import { DebugOverlay } from "./DebugOverlay";

export class MainScreen extends Container {
  public static assetBundles = ["main"];

  private reelsLayer: Container;
  private fxLayer: Container;
  private uiLayer: Container;

  private slotMachine: SlotMachine;
  private winHighlight: WinHighlight;
  private particleBurst: ParticleBurst;
  private winCounter: WinCounter;
  private debugOverlay: DebugOverlay;

  private spinButton: Button;
  private turboToggleButton: Button;
  private autoplayButton: Button;
  private buyBonusButton: Button;
  private pauseButton: FancyButton;
  private settingsButton: FancyButton;
  private statusText: Text;
  private turboStateText: Text;

  private paused = false;
  private isSpinning = false;
  private isPresentingWin = false;
  private turboSelected = false;
  private currentWinPresentationTimeout: ReturnType<typeof setTimeout> | null = null;

  private readonly animationPolicy: AnimationPolicyEngine;
  private readonly hudLayout: ResponsiveHudLayoutController;

  constructor() {
    super();

    const runtimeConfig = RuntimeStore.get();
    this.animationPolicy = new AnimationPolicyEngine(
      createAnimationPolicy({
        runtimeConfig,
        forcedSkipWinPresentation: true,
        lowPerformanceMode: import.meta.env.DEV ? false : false,
      }),
    );
    this.turboSelected = this.animationPolicy.value.turbo.selected;

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

    this.debugOverlay = new DebugOverlay();
    this.addChild(this.debugOverlay);

    this.spinButton = new Button({ text: "SPIN", width: 210, height: 100 });
    this.spinButton.onPress.connect(() => this.handleSpin());
    this.uiLayer.addChild(this.spinButton);

    this.turboToggleButton = new Button({ text: "TURBO", width: 190, height: 84, fontSize: 24 });
    this.turboToggleButton.onPress.connect(() => this.toggleTurbo());
    this.uiLayer.addChild(this.turboToggleButton);

    this.autoplayButton = new Button({ text: "AUTO", width: 190, height: 84, fontSize: 24 });
    this.autoplayButton.onPress.connect(() => {
      this.statusText.text = "AUTOPLAY DEMO";
      this.statusText.visible = true;
      setTimeout(() => (this.statusText.visible = false), 700);
    });
    this.uiLayer.addChild(this.autoplayButton);

    this.buyBonusButton = new Button({ text: "BUY", width: 190, height: 84, fontSize: 24 });
    this.buyBonusButton.onPress.connect(() => {
      this.statusText.text = "BUY BONUS DEMO";
      this.statusText.visible = true;
      setTimeout(() => (this.statusText.visible = false), 700);
    });
    this.uiLayer.addChild(this.buyBonusButton);

    const buttonAnimation = {
      hover: { props: { scale: { x: 1.1, y: 1.1 } }, duration: 100 },
      pressed: { props: { scale: { x: 0.9, y: 0.9 } }, duration: 100 },
    };

    this.pauseButton = new FancyButton({
      defaultView: UiAssetKeys.ICON_PAUSE,
      anchor: 0.5,
      animations: buttonAnimation,
    });
    this.pauseButton.onPress.connect(() =>
      engine().navigation.presentPopup(PausePopup),
    );
    this.uiLayer.addChild(this.pauseButton);

    this.settingsButton = new FancyButton({
      defaultView: UiAssetKeys.ICON_SETTINGS,
      anchor: 0.5,
      animations: buttonAnimation,
    });
    this.settingsButton.onPress.connect(() =>
      engine().navigation.presentPopup(SettingsPopup),
    );
    this.uiLayer.addChild(this.settingsButton);

    this.turboStateText = new Text({
      text: "Turbo: OFF",
      style: {
        fontFamily: "Arial",
        fontSize: 22,
        fill: 0xffef9f,
        stroke: { color: 0x000000, width: 3 },
        fontWeight: "700",
      },
    });
    this.turboStateText.anchor.set(0.5);
    this.uiLayer.addChild(this.turboStateText);

    this.statusText = new Text({
      text: "",
      style: {
        fontFamily: "Arial",
        fontSize: 28,
        fill: 0xffffff,
        stroke: { color: 0x000000, width: 4 },
        fontWeight: "700",
        align: "center",
      },
    });
    this.statusText.anchor.set(0.5);
    this.statusText.visible = false;
    this.uiLayer.addChild(this.statusText);

    const params = new URLSearchParams(window.location.search);
    const capabilityMatrix = runtimeConfig.capabilities;
    const capabilityFeatures = capabilityMatrix.features;

    const featureVisibility = {
      spin: params.get("spin") !== "0",
      turbo:
        capabilityMatrix.turbo.allowed &&
        params.get("turbo") !== "0",
      autoplay:
        capabilityFeatures.autoplay &&
        params.get("autoplay") !== "0",
      buybonus:
        (capabilityFeatures.buyFeature ||
          capabilityFeatures.buyFeatureForCashBonus) &&
        params.get("buybonus") !== "0",
      pause: params.get("pause") !== "0",
      settings:
        capabilityMatrix.sound.showToggle &&
        params.get("settings") !== "0",
    };

    this.hudLayout = new ResponsiveHudLayoutController([
      {
        id: "spin",
        object: this.spinButton,
        width: 210,
        height: 100,
        visible: featureVisibility.spin,
      },
      {
        id: "turbo",
        object: this.turboToggleButton,
        width: 190,
        height: 84,
        visible: featureVisibility.turbo,
      },
      {
        id: "autoplay",
        object: this.autoplayButton,
        width: 190,
        height: 84,
        visible: featureVisibility.autoplay,
      },
      {
        id: "buybonus",
        object: this.buyBonusButton,
        width: 190,
        height: 84,
        visible: featureVisibility.buybonus,
      },
      {
        id: "pause",
        object: this.pauseButton,
        width: 68,
        height: 68,
        visible: featureVisibility.pause,
      },
      {
        id: "settings",
        object: this.settingsButton,
        width: 68,
        height: 68,
        visible: featureVisibility.settings,
      },
    ]);

    this.slotMachine.onSpinComplete = () => this.handleSpinComplete();
    this.refreshTurboButtonLabel();
  }

  private toggleTurbo() {
    if (!this.animationPolicy.value.turbo.allowed) return;
    this.turboSelected = !this.turboSelected;
    this.refreshTurboButtonLabel();
  }

  private refreshTurboButtonLabel() {
    this.turboStateText.text = this.turboSelected ? "Turbo: ON" : "Turbo: OFF";
  }

  private handleSpin() {
    if (this.isPresentingWin && this.animationPolicy.shouldAllowForcedSkip()) {
      this.skipWinPresentation();
      return;
    }

    if (this.isSpinning || this.isPresentingWin) return;

    this.isSpinning = true;
    this.winHighlight.clear();
    this.statusText.visible = false;

    const timing = this.animationPolicy.resolveSpinTiming(this.turboSelected);

    this.slotMachine.spin({
      minSpinDurationMs: timing.minSpinMs,
      spinStaggerMs: timing.spinStaggerMs,
      speedMultiplier: timing.speedMultiplier,
    });
  }

  private handleSpinComplete() {
    const reels = this.slotMachine.getReels();
    const winSymbols = [
      reels[0].getVisibleSymbols()[1],
      reels[1].getVisibleSymbols()[1],
      reels[2].getVisibleSymbols()[1],
    ];

    const winAmount = this.generateMockWinAmount();
    const defaultBet = RuntimeStore.limits.defaultBet;
    const multiplier = defaultBet > 0 ? winAmount / defaultBet : 0;
    const tier = this.animationPolicy.classifyWinByMultiplier(multiplier);

    if (this.animationPolicy.shouldPlayHeavyWinFx(tier)) {
      this.winHighlight.showWin(winSymbols);
      this.particleBurst.play(0, -100);
    } else {
      this.winHighlight.clear();
    }

    this.showTierOverlay(tier, multiplier, winAmount);
    this.winCounter.showWin(winAmount, this.getTierLabel(tier));

    this.isSpinning = false;
    this.isPresentingWin = true;

    const presentationMs = this.animationPolicy.getWinPresentationDurationMs(tier, false);

    this.clearWinPresentationTimeout();
    this.currentWinPresentationTimeout = setTimeout(() => {
      this.finishWinPresentation();
    }, presentationMs);
  }

  private generateMockWinAmount(): number {
    const bet = Math.max(1, RuntimeStore.limits.defaultBet);
    const randomMultiplier = Math.random() * 70;
    return Math.round(bet * randomMultiplier);
  }

  private getTierLabel(tier: WinTier): string {
    if (tier === "mega") return "MEGA WIN";
    if (tier === "huge") return "HUGE WIN";
    if (tier === "big") return "BIG WIN";
    return "WIN";
  }

  private showTierOverlay(tier: WinTier, multiplier: number, winAmount: number) {
    const tierLabel = this.getTierLabel(tier);
    this.statusText.text = `${tierLabel}\n${multiplier.toFixed(1)}x (${winAmount})`;
    this.statusText.visible = true;
  }

  private skipWinPresentation() {
    if (!this.isPresentingWin) return;
    this.clearWinPresentationTimeout();
    this.finishWinPresentation(true);
  }

  private finishWinPresentation(skipped = false) {
    this.winHighlight.clear();
    this.statusText.visible = false;
    this.winCounter.hideNow();
    this.isPresentingWin = false;

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
    const hudSpace = viewport.orientation === "portrait" ? 270 : 170;

    const centerX = width * 0.5;
    const availableTop = safe.top + 20;
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

    const scale = Math.min(
      availableWidth / machineWidth,
      availableHeight / machineHeight,
    );

    this.reelsLayer.scale.set(scale);
    this.fxLayer.scale.set(scale);

    this.reelsLayer.x = centerX - (machineWidth * scale) / 2;
    this.reelsLayer.y = centerY - (machineHeight * scale) / 2;

    this.fxLayer.x = this.reelsLayer.x;
    this.fxLayer.y = this.reelsLayer.y;

    this.uiLayer.x = 0;
    this.uiLayer.y = 0;

    this.hudLayout.apply({
      width,
      height,
      orientation: viewport.orientation,
      safeArea: safe,
    });

    if (this.hudLayout.isFeatureVisible("turbo")) {
      this.turboStateText.visible = true;
      this.turboStateText.x = this.turboToggleButton.x;
      this.turboStateText.y = this.turboToggleButton.y - 56;
    } else {
      this.turboStateText.visible = false;
    }

    this.statusText.x = width * 0.5;
    this.statusText.y = safe.top + 100;

    this.winCounter.x = width * 0.5;
    this.winCounter.y = safe.top + 200;

    this.debugOverlay.resize(width, height);
  }

  public async show(): Promise<void> {
    engine().audio.bgm.play(AppAssetKeys.BGM_MAIN, { volume: 0.5 });
  }

  public async hide() {
    this.clearWinPresentationTimeout();
  }

  public blur() {
    if (!engine().navigation.currentPopup) {
      engine().navigation.presentPopup(PausePopup);
    }
  }
}
