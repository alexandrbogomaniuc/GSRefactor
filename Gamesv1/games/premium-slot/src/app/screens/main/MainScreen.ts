import { FancyButton } from "@pixi/ui";
import { Container } from "pixi.js";

import { engine } from "@gamesv1/pixi-engine";
import {
  Button,
  GameConfig,
  PausePopup,
  SettingsPopup,
  SlotMachine,
  UiAssetKeys,
} from "@gamesv1/ui-kit";

import { AppAssetKeys } from "../../assets/assetKeys";
import { ParticleBurst } from "../../../game/fx/ParticleBurst";
import { WinHighlight } from "../../../game/fx/WinHighlight";
import { WinCounter } from "../../../game/ui/WinCounter";
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
  private pauseButton: FancyButton;
  private settingsButton: FancyButton;

  private paused = false;
  private isSpinning = false;

  constructor() {
    super();

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

    this.spinButton = new Button({ text: "SPIN", width: 200, height: 100 });
    this.spinButton.onPress.connect(() => this.handleSpin());
    this.uiLayer.addChild(this.spinButton);

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

    this.slotMachine.onSpinComplete = () => this.handleSpinComplete();
  }

  private handleSpin() {
    if (this.isSpinning) return;
    this.isSpinning = true;
    this.winHighlight.clear();
    this.slotMachine.spin();
  }

  private handleSpinComplete() {
    const reels = this.slotMachine.getReels();
    const winSymbols = [
      reels[0].getVisibleSymbols()[1],
      reels[1].getVisibleSymbols()[1],
      reels[2].getVisibleSymbols()[1],
    ];

    this.winHighlight.showWin(winSymbols);
    this.particleBurst.play(0, -100);
    this.winCounter.y = -200;
    this.winCounter.showWin(5000);

    setTimeout(() => {
      this.isSpinning = false;
    }, 2000);
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
    const centerX = width * 0.5;
    const centerY = height * 0.5;

    const machineWidth =
      GameConfig.numReels * GameConfig.symbolWidth +
      (GameConfig.numReels - 1) * GameConfig.reelSpacing;
    const machineHeight =
      GameConfig.numRows * GameConfig.symbolHeight +
      (GameConfig.numRows - 1) * GameConfig.rowSpacing;

    const scale = Math.min(
      width / (machineWidth + 100),
      height / (machineHeight + 300),
    );

    this.reelsLayer.scale.set(scale);
    this.fxLayer.scale.set(scale);

    this.reelsLayer.x = centerX - (machineWidth * scale) / 2;
    this.reelsLayer.y = centerY - (machineHeight * scale) / 2;

    this.fxLayer.x = this.reelsLayer.x;
    this.fxLayer.y = this.reelsLayer.y;

    this.uiLayer.x = centerX;
    this.uiLayer.y = centerY;

    this.spinButton.x = 0;
    this.spinButton.y = height / 2 - 100;

    this.pauseButton.x = -centerX + 50;
    this.pauseButton.y = -centerY + 50;
    this.settingsButton.x = centerX - 50;
    this.settingsButton.y = -centerY + 50;

    this.debugOverlay.resize(width, height);
  }

  public async show(): Promise<void> {
    engine().audio.bgm.play(AppAssetKeys.BGM_MAIN, { volume: 0.5 });
  }

  public async hide() {}

  public blur() {
    if (!engine().navigation.currentPopup) {
      engine().navigation.presentPopup(PausePopup);
    }
  }
}
